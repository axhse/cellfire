import { Grid } from '../../models/Grid';
import { Simulation } from '../../models/Simulation';
import { Timeline } from '../../models/Timeline';

export class Simulator {
  async createSimulation(startLonLat, algorithm) {
    const response = await fetch('/simulation/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startLonLat, algorithm }),
    });

    if (response.ok) {
      const body = await response.json();
      const params = body.simulation;

      const grid = new Grid(params.grid.scale, params.grid.startCoordinates);
      const timeline = new Timeline(
        new Date(params.timeline.startDateMs),
        params.timeline.stepDurationMs,
        params.timeline.limitTicks
      );
      const simulation = new Simulation(
        params.id,
        grid,
        timeline,
        params.conditions,
        params.algorithm
      );
      simulation.appendSteps(params.steps, 0);
      return simulation;
    }
  }

  async removeSimulation(simulation) {
    await fetch('/simulation/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ simulationId: simulation.id }),
    });
  }

  async progressSimulation(simulation, endTick) {
    if (endTick < simulation.steps.length) {
      return true;
    }
    const startTick = simulation.steps.length;

    const response = await fetch('/simulation/progress', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ simulationId: simulation.id, startTick, endTick }),
    });

    if (response.ok) {
      const body = await response.json();
      if (body.hasResult) {
        simulation.appendSteps(body.steps, startTick);
        return true;
      }
    }

    return false;
  }
}
