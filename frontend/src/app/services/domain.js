const TimePeriod = {
  minutes: (n) => n * 60 * 1000,
  hours: (n) => n * 60 * 60 * 1000,
  days: (n) => n * 24 * 60 * 60 * 1000,
};

// 1/100° for both latitude and longitude. ≈1.1 km near the Equator
// Earth Equatorial circumference: 40 075 km.
// Earth Polar circumference: 39 930  km.
export const CELL_WIDTH = 10;

export const DATE_SHIFT_STEP = TimePeriod.minutes(30);
export const DATE_SHIFT_INTERVAL_LIMIT = TimePeriod.days(3);

export function roundGeoCoordinate(degrees) {
  return Math.round(degrees / CELL_WIDTH) * CELL_WIDTH;
}

export function roundGeoCoordinates(lonLat) {
  return lonLat.map(roundGeoCoordinate);
}
