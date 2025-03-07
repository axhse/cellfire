import { Grid } from '../../models/Grid';
import { Simulation } from '../../models/Simulation';
import { Timeline } from '../../models/Timeline';

export class Simulator {
  async createSimulation(startLonLat, algorithm) {
    const stepDurationMs = 30 * 60 * 1000;
    const limitTicks = (7 * 24 * 60 * 60 * 1000) / stepDurationMs;
    const startDate = new Date(
      ((new Date().valueOf() / stepDurationMs) >> 0) * stepDurationMs
    );
    const gridScale = 200;
    const startCoordinates = {
      x: Math.round(startLonLat[0] * gridScale),
      y: Math.round(startLonLat[1] * gridScale),
    };
    const simulation = new Simulation(
      'DEMO-SIMULATION',
      new Grid(gridScale, startCoordinates),
      new Timeline(startDate, stepDurationMs, limitTicks),
      { ignitionTemperature: 280 },
      algorithm
    );
    this.progressSimulation(simulation, 0);
    return simulation;
  }

  async removeSimulation() {
    return;
  }

  async progressSimulation(simulation, endTick) {
    // 1 day 30 minutes.
    if (endTick === 24 * 2 + 1) {
      return false;
    }
    while (simulation.steps.length <= endTick) {
      const step = produceDemoSimulationStep(
        simulation.grid.startCoordinates,
        simulation.steps.length
      );
      simulation.appendSteps([step], simulation.steps.length);
    }
    return true;
  }
}

function produceDemoSimulationStep(startCoordinates, tick) {
  const demoCells = [];
  for (let x = 0; x <= tick; x++) {
    for (let y = 0; x + y <= tick; y++) {
      demoCells.push(produceDemoCell(startCoordinates, x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(startCoordinates, -x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(startCoordinates, -x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(startCoordinates, x, -y));
      }
    }
  }
  return { cells: demoCells };
}

function produceDemoCell(startCoordinates, offsetX, offsetY) {
  const heat =
    100 + (((offsetX + 3) * 5) % 77) * 10 + (((offsetY + 7) * 9) % 100) * 4;
  const fuel =
    0 + (((offsetX + 4) * 2) % 77) / 77 + (((offsetY + 3) * 3) % 100) / 44;
  const elevation =
    ((fuel * 2000 + heat + (offsetX + 100) * 10 + 124023) % 7000) - 500;
  const airTemperature = (fuel * 40) % 40;
  const airHumidity = (Math.round(fuel * 100 + 1000) % 101) / 100;
  const windX = (Math.round(offsetX + 1002) % 14) - 8;
  const windY = (Math.round(offsetY + 12340) % 9) - 3;
  const damaged = (offsetX * 2 + offsetY * 5 + 7) % 20 < 10;

  return {
    coordinates: {
      x: startCoordinates.x + offsetX,
      y: startCoordinates.y + offsetY,
    },
    state: { heat, fuel, damaged },
    factors: { elevation, airTemperature, airHumidity, windX, windY },
  };
}
