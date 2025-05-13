import { Grid } from "../../models/Grid";
import { Simulation } from "../../models/Simulation";
import { Timeline } from "../../models/Timeline";

export class Simulator {
  async createSimulation(startLonLat) {
    const stepDurationMs = 30 * 60 * 1000;
    const limitTicks = (7 * 24 * 60 * 60 * 1000) / stepDurationMs;
    const startDate = new Date(
      ((new Date().valueOf() / stepDurationMs) >> 0) * stepDurationMs,
    );
    const gridScale = 200;
    const startCoordinates = {
      x: Math.round(startLonLat[0] * gridScale - 0.5),
      y: Math.round(startLonLat[1] * gridScale - 0.5),
    };
    if (startCoordinates.x % 5 === 0) {
      return undefined;
    }
    const simulation = new Simulation(
      "DEMO-SIMULATION",
      new Grid(gridScale, startCoordinates),
      new Timeline(startDate, stepDurationMs, limitTicks),
    );
    this.progressSimulation(simulation, 0);
    return simulation;
  }

  async removeSimulation() {
    return;
  }

  async progressSimulation(simulation, endTick) {
    // 1 day 7 hours.
    const finalTick = 24 * 2 + 7 * 2;
    // 1 day 30 minutes.
    if (endTick === 24 * 2 + 1) {
      return false;
    }
    while (simulation.steps.length <= Math.min(endTick, finalTick)) {
      const wantedTick = simulation.steps.length;
      const step = produceDemoSimulationStep(
        simulation.grid.startCoordinates,
        wantedTick,
        wantedTick === finalTick,
      );
      simulation.appendSteps([step], wantedTick);
    }
    return true;
  }
}

function produceDemoSimulationStep(startCoordinates, tick, final) {
  const demoCells = [];
  for (let x = 0; x <= tick; x++) {
    for (let y = 0; x + y <= tick; y++) {
      demoCells.push(produceDemoCell(startCoordinates, tick, x, y, final));
      if (x > 0) {
        demoCells.push(produceDemoCell(startCoordinates, tick, -x, y, final));
        if (y > 0) {
          demoCells.push(
            produceDemoCell(startCoordinates, tick, -x, -y, final),
          );
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(startCoordinates, tick, x, -y, final));
      }
    }
  }
  return { cells: demoCells, final };
}

function produceDemoCell(startCoordinates, tick, offsetX, offsetY, final) {
  let heat =
    100 + (((offsetX + 3) * 5) % 77) * 10 + (((offsetY + 7) * 9) % 100) * 4;
  if (final) {
    heat %= 200;
  }
  const fuel =
    0 + (((offsetX + 4) * 2) % 77) / 77 + (((offsetY + 3) * 3) % 100) / 44;
  const elevation =
    ((fuel * 2000 + heat + (offsetX + 100) * 10 + 124023) % 7000) - 500;
  const airTemperature = (fuel * 40) % 40;
  const airHumidity = (Math.round(fuel * 100 + 1000) % 101) / 100;
  let windX = 0;
  let windY = 0;
  if (tick > 0) {
    const windSpeed =
      ((Math.round(offsetX + 1002) % 24) + (Math.round(offsetY + 12340) % 79)) /
      10;
    const windAngle = ((tick - 1) * 15 * Math.PI) / 180;
    windX = windSpeed * Math.cos(windAngle);
    windY = windSpeed * Math.sin(windAngle);
  }
  const windSpeed = Math.sqrt(windX * windX + windY * windY);
  const damaged = (offsetX * 2 + offsetY * 5 + 7) % 20 < 10;

  const burning =
    fuel > 0 && 500 <= heat && airHumidity < 1 && airTemperature > 0;

  return {
    coordinates: {
      x: startCoordinates.x + offsetX,
      y: startCoordinates.y + offsetY,
    },
    state: { heat, fuel, damaged },
    factors: {
      elevation,
      airTemperature,
      airHumidity,
      windX,
      windY,
      windSpeed,
    },
    burning,
  };
}
