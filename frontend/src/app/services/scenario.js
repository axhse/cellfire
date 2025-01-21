import { FORECAST_STEP, roundPoint } from './domain';

export async function createScenario(startPoint, startDate) {
  startPoint = roundPoint(startPoint);

  const response = await fetch('/scenario/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
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

export async function removeScenario(scenario) {
  await fetch('/scenario/remove', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      scenarioId: scenario.id,
    }),
  });
}

export async function forecastScenario(scenario) {
  if (scenario.actualDate === scenario.startDate) {
    // TODO: optimization
  }

  const response = await fetch('/scenario/forecast', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
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

// TODO: remove
export function produceDemoForecast(scenario) {
  const demoFireCells = [];
  for (
    let x = 0;
    x <= (scenario.actualDate - scenario.startDate) / FORECAST_STEP;
    x++
  ) {
    for (
      let y = 0;
      x + y <= (scenario.actualDate - scenario.startDate) / FORECAST_STEP;
      y++
    ) {
      demoFireCells.push({ coordinates: [x, y] });
      if (x > 0) {
        demoFireCells.push({ coordinates: [-x, y] });
        if (y > 0) {
          demoFireCells.push({ coordinates: [-x, -y] });
        }
      }
      if (y > 0) {
        demoFireCells.push({ coordinates: [x, -y] });
      }
    }
  }
  return { fireCells: demoFireCells };
}
