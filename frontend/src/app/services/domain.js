const TimePeriod = {
  minutes: (n) => n * 60 * 1000,
  hours: (n) => n * 60 * 60 * 1000,
  days: (n) => n * 24 * 60 * 60 * 1000,
};

export const CELL_SIZE = 0.01;

export const DATE_SHIFT_STEP = TimePeriod.minutes(30);
export const MAX_PREDICTION_PERIOD = TimePeriod.days(3);

export function roundGeoCoordinate(degrees) {
  return Math.round(degrees / CELL_SIZE) * CELL_SIZE;
}

export function roundPoint(lonLat) {
  return lonLat.map(roundGeoCoordinate);
}
