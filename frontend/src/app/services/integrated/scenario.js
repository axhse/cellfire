export class ScenarioService {
  async createScenario(startCoordinates, startDate) {
    const response = await fetch('/scenario/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        startCoordinates,
        startTs: startDate.valueOf(),
      }),
    });

    if (response.ok) {
      const body = await response.json();
      return {
        id: body.scenarioId,
        startCoordinates,
        startDate,
        actualDate: startDate,
      };
      // TODO: What if response is not ok?
    }
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

  async forecastScenario(scenario) {
    if (scenario.actualDate === scenario.startDate) {
      // TODO: optimization
    }

    const response = await fetch('/scenario/forecast', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        scenarioId: scenario.id,
        actualTs: scenario.actualDate.valueOf(),
      }),
    });

    if (response.ok) {
      const body = await response.json();
      return body.forecast;
    }
    // TODO: What if response is not ok?
  }
}
