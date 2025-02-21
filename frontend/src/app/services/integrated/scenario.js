export class ScenarioService {
  async createScenario(startCoordinates, startDate, algorithm) {
    const response = await fetch('/scenario/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        algorithm,
        startCoordinates,
        startTs: startDate.valueOf(),
      }),
    });

    if (response.ok) {
      const body = await response.json();
      return {
        id: body.id,
        simulation: body.simulation,
        conditions: body.conditions,
        algorithm,
        startCoordinates,
        startDate,
        step: 0,
      };
    }
    // TODO: Handle errors?
  }

  async removeScenario(scenario) {
    await fetch('/scenario/remove', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        scenarioId: scenario.id,
      }),
    });
  }

  async simulateScenario(scenario, step) {
    if (step < scenario.simulation.steps.length) {
      return;
    }

    const response = await fetch('/scenario/simulate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        scenarioId: scenario.id,
        startStep: scenario.simulation.steps.length,
        endStep: step,
      }),
    });

    if (response.ok) {
      const body = await response.json();
      scenario.simulation.steps.push(...body.steps);
    }
  }
}
