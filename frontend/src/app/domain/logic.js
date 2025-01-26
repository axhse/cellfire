import { SCALE_FACTOR } from './definitions';

export function fromCellCoordinates(coordinates) {
  return [
    (coordinates.x + 0.5) / SCALE_FACTOR,
    (coordinates.y + 0.5) / SCALE_FACTOR,
  ];
}

export function toCellCoordinates(geoPoint) {
  const point = {
    x: Math.round(geoPoint[0] * SCALE_FACTOR - 0.5),
    y: Math.max(
      -90 * SCALE_FACTOR,
      Math.round(geoPoint[1] * SCALE_FACTOR - 0.5)
    ),
  };
  if (point.y === -180 * SCALE_FACTOR) {
    point.y = 180 * SCALE_FACTOR;
  }
  return point;
}
