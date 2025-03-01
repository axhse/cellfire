export class Simulator {
  async createSimulation(startLonLat, algorithm) {
    const response = await fetch('/simulation/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        startLonLat,
        startDateMs: new Date().valueOf(),
        algorithm,
      }),
    });

    if (response.ok) {
      const body = await response.json();
      return {
        ...body.simulation,
        startDate: new Date(body.simulation.startDateMs),
      };
    }
    // TODO: Handle errors?
  }

  async removeSimulation(simulation) {
    await fetch('/simulation/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ simulationId: simulation.id }),
    });
  }

  async progressSimulation(simulation, step) {
    if (step < simulation.steps.length) {
      return;
    }

    const response = await fetch('/simulation/progress', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        simulationId: simulation.id,
        startStep: simulation.steps.length,
        endStep: step,
      }),
    });

    if (response.ok) {
      const body = await response.json();
      simulation.steps.push(...body.steps);
    }
  }
}
