import { roundPoint } from '../../domain/logic';

export class ScenarioService {
  async createScenario(startPoint, startDate) {
    startPoint = roundPoint(startPoint);

    const response = await fetch('/scenario/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        startPoint,
        startTs: startDate.valueOf(),
      }),
    });

    if (response.ok) {
      const id = (await response.json()).scenarioId;
      return {
        id,
        startPoint,
        startDate,
        actualDate: startDate,
      };
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
    const body = await response.json();
    if (response.ok) {
      return body.forecast;
    }
  }
}
