import { DATE_SHIFT_STEP, roundGeoCoordinates } from './domain';

export function createScenario(startPoint, startDate) {
  startPoint = roundGeoCoordinates(startPoint);
  return {
    id: 'TODO generate id',
    startPoint: startPoint,
    startDate: startDate,
    currentDate: startDate,
  };
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
