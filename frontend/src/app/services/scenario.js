import { FORECAST_STEP, roundPoint } from './domain';

const IS_DEMO_MODE = true;

export async function createScenario(startPoint, startDate) {
  startPoint = roundPoint(startPoint);
  if (IS_DEMO_MODE) {
    return {
      id: 'demo id',
      startPoint,
      startDate,
      actualDate: startDate,
    };
  }

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
  if (IS_DEMO_MODE) {
    return;
  }
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
  if (IS_DEMO_MODE) {
    return produceDemoForecast(scenario);
  }
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
function produceDemoCell(x, y) {
  return {
    x,
    y,
    fireCell: { heat: 100 + (((x + 3) * 5) % 77) + (((y + 7) * 9) % 100) },
    fuelCell: {
      capacity: 0 + (((x + 4) * 5) % 77) / 77 + (((y + 3) * 9) % 100) / 44,
    },
  };
}

// TODO: remove
function produceDemoForecast(scenario) {
  const demoCells = [];
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
      demoCells.push(produceDemoCell(x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(-x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(-x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(x, -y));
      }
    }
  }
  return { cells: demoCells };
}
