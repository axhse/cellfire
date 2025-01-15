const Distance = {
  meters: (n) => n,
};

const DateInterval = {
  minutes: (n) => n * 60 * 1000,
  hours: (n) => n * 60 * 60 * 1000,
  days: (n) => n * 24 * 60 * 60 * 1000,
};

export const CELL_WIDTH = Distance.meters(1000);
export const DATE_SHIFT_STEP = DateInterval.minutes(30);
export const DATE_SHIFT_INTERVAL_LIMIT = DateInterval.days(3);

export function createScenario(startPoint, startDate) {
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
