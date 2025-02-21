export class ScenarioService {
  async createScenario(startCoordinates, startDate, algorithm) {
    const scenario = {
      id: 'DEMO-ID',
      algorithm,
      startCoordinates,
      startDate,
      step: 0,
      simulation: { steps: [] },
      conditions: { ignitionTemperature: 280 },
    };
    scenario.simulation.steps.push(produceDemoSimulationStep(scenario, 0));
    return scenario;
  }

  async removeScenario() {
    return;
  }

  async simulateScenario(scenario, step) {
    while (scenario.simulation.steps.length <= step) {
      scenario.simulation.steps.push(
        produceDemoSimulationStep(scenario, scenario.simulation.steps.length)
      );
    }
  }
}

function produceDemoSimulationStep(scenario, step) {
  const demoCells = [];
  for (let x = 0; x <= step; x++) {
    for (let y = 0; x + y <= step; y++) {
      demoCells.push(produceDemoCell(scenario.startCoordinates, x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(scenario.startCoordinates, -x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(scenario.startCoordinates, -x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(scenario.startCoordinates, x, -y));
      }
    }
  }
  return { cells: demoCells };
}

function produceDemoCell(startCoordinates, offsetX, offsetY) {
  const heat =
    100 + (((offsetX + 3) * 5) % 77) * 10 + (((offsetY + 7) * 9) % 100) * 4;
  const fuel =
    0 + (((offsetX + 4) * 5) % 77) / 77 + (((offsetY + 3) * 9) % 100) / 44;
  const airTemperature = (fuel * 40) % 40;
  const airHumidity = (Math.round(fuel * 100 + 1000) % 101) / 100;
  const elevation = (fuel * 200 + heat / 5 + 12402394) % 500;
  const windX = Math.round(fuel * 100 + 1000) % 7;
  const windY = Math.round(fuel * 123 + 12340) % 4;
  const isDamaged = (offsetX * 2 + offsetY * 5 + 7) % 20 < 10;

  return {
    coordinates: {
      x: startCoordinates.x + offsetX,
      y: startCoordinates.y + offsetY,
    },
    fire: { heat, fuel, isDamaged },
    factors: { airTemperature, airHumidity, elevation, windX, windY },
  };
}
