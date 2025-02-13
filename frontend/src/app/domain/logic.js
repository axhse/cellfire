import { GRID_SCALE } from './definitions';

const EARTH_CIRCUMFERENCE = 40000000;

export function getCurrentDate() {
  return new Date();
}

export function fromCellCoordinates(coordinates) {
  return [
    (coordinates.x + 0.5) / GRID_SCALE,
    (coordinates.y + 0.5) / GRID_SCALE,
  ];
}

export function toCellCoordinates(geoPoint) {
  const point = {
    x: Math.round(geoPoint[0] * GRID_SCALE - 0.5),
    y: Math.max(-90 * GRID_SCALE, Math.round(geoPoint[1] * GRID_SCALE - 0.5)),
  };
  if (point.y === -180 * GRID_SCALE) {
    point.y = 180 * GRID_SCALE;
  }
  return point;
}

export function calculateCellArea(coordinates) {
  const cellHeight = EARTH_CIRCUMFERENCE / 360 / GRID_SCALE;
  const lat = fromCellCoordinates(coordinates)[1];
  return cellHeight * cellHeight * Math.cos(lat * (Math.PI / 180));
}
