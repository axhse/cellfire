export class Simulator {
  async createSimulation(startLonLat, algorithm) {
    const stepDurationMs = 30 * 60 * 1000;
    const limitDurationSteps = (7 * 24 * 60 * 60 * 1000) / stepDurationMs;
    const startDate = new Date(
      ((new Date().valueOf() / stepDurationMs) >> 0) * stepDurationMs
    );
    const grid = { scale: 200 };
    const startCoordinates = {
      x: Math.round(startLonLat[0] * grid.scale),
      y: Math.round(startLonLat[1] * grid.scale),
    };
    const simulation = {
      id: 'DEMO-ID',
      grid,
      startCoordinates,
      startDate,
      stepDurationMs,
      limitDurationSteps,
      algorithm,
      conditions: { ignitionTemperature: 280 },
      steps: [],
    };
    simulation.steps.push(produceDemoSimulationStep(simulation, 0));
    return simulation;
  }

  async removeSimulation() {
    return;
  }

  async progressSimulation(simulation, endStep) {
    while (simulation.steps.length <= endStep) {
      simulation.steps.push(
        produceDemoSimulationStep(simulation, simulation.steps.length)
      );
    }
  }
}

function produceDemoSimulationStep(simulation, step) {
  const demoCells = [];
  for (let x = 0; x <= step; x++) {
    for (let y = 0; x + y <= step; y++) {
      demoCells.push(produceDemoCell(simulation.startCoordinates, x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(simulation.startCoordinates, -x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(simulation.startCoordinates, -x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(simulation.startCoordinates, x, -y));
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
  const windX = (Math.round(fuel * 100 + 1002) % 15) - 7;
  const windY = (Math.round(fuel * 123 + 12340) % 8) - 3;
  const damaged = (offsetX * 2 + offsetY * 5 + 7) % 20 < 10;

  return {
    coordinates: {
      x: startCoordinates.x + offsetX,
      y: startCoordinates.y + offsetY,
    },
    state: { heat, fuel, damaged },
    weather: { airTemperature, airHumidity, elevation, windX, windY },
  };
}
