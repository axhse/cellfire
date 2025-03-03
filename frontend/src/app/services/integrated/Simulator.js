export class Simulator {
  async createSimulation(startLonLat, algorithm) {
    const response = await fetch('/simulation/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startLonLat, algorithm }),
    });

    if (response.ok) {
      const body = await response.json();
      return {
        ...body.simulation,
        startDate: new Date(body.simulation.startDateMs),
      };
    }
  }

  async removeSimulation(simulation) {
    await fetch('/simulation/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ simulationId: simulation.id }),
    });
  }

  async progressSimulation(simulation, endStep) {
    if (endStep < simulation.steps.length) {
      return true;
    }
    const startStep = simulation.steps.length;

    const response = await fetch('/simulation/progress', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ simulationId: simulation.id, startStep, endStep }),
    });

    if (response.ok) {
      const body = await response.json();
      if (body.hasResult) {
        simulation.steps.push(...body.steps);
        return true;
      }
    }

    return false;
  }
}
