import { DATE_SHIFT_STEP, roundPoint } from './domain';

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
    const id = await response.json()['id'];
    return {
      id,
      startPoint,
      startDate,
      currentDate: startDate,
    };
  }
}

export function removeScenario() {}

export function predictScenario(scenario) {
  if (scenario.currentDate === scenario.startDate) {
    // TODO: optimization
  }

  const demoFireCells = [];
  for (
    let x = 0;
    x <= (scenario.currentDate - scenario.startDate) / DATE_SHIFT_STEP;
    x++
  ) {
    for (
      let y = 0;
      x + y <= (scenario.currentDate - scenario.startDate) / DATE_SHIFT_STEP;
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
