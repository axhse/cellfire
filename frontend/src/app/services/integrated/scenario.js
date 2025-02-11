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
        id: body.scenarioId,
        forecastLog: body.forecastLog,
        conditions: body.conditions,
        startCoordinates,
        startDate,
        algorithm,
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

  async forecastScenario(scenario, step) {
    if (step < scenario.forecastLog.forecasts.length) {
      return;
    }

    const response = await fetch('/scenario/forecast', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        scenarioId: scenario.id,
        startStep: scenario.forecastLog.forecasts.length,
        endStep: step,
      }),
    });

    if (response.ok) {
      const body = await response.json();
      return scenario.forecastLog.forecasts.push(
        ...body.partialForecastLog.forecasts
      );
    }
  }
}
