import { CELL_SIZE } from './definitions';

export function roundGeoCoordinate(degrees) {
  return Math.round(degrees / CELL_SIZE) * CELL_SIZE;
}

export function roundPoint(lonLat) {
  return lonLat.map(roundGeoCoordinate);
}
