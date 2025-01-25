import { fromLonLat } from 'ol/proj';
import { Fill } from 'ol/style';
import Polygon from 'ol/geom/Polygon';

import { CELL_SIZE } from '../services/domain';

const LAYER_OPACITY = 0.5;
const LAYER_PARAMS = {
  heat: {
    minValue: 0,
    maxValue: 200,
    minColor: [255, 255, 0],
    maxColor: [255, 0, 0],
  },
  resource: {
    minValue: 0,
    maxValue: 1,
    minColor: [127, 127, 127],
    maxColor: [255, 0, 255],
  },
};

export function createCellFigure(center, x, y) {
  const leftEdgeLon = center[0] + (x - 0.5) * CELL_SIZE;
  const rightEdgeLon = center[0] + (x + 0.5) * CELL_SIZE;
  const topEdgeLat = center[1] + (y - 0.5) * CELL_SIZE;
  const bottomEdgeLat = center[1] + (y + 0.5) * CELL_SIZE;

  return new Polygon([
    [
      fromLonLat([leftEdgeLon, topEdgeLat]),
      fromLonLat([rightEdgeLon, topEdgeLat]),
      fromLonLat([rightEdgeLon, bottomEdgeLat]),
      fromLonLat([leftEdgeLon, bottomEdgeLat]),
      fromLonLat([leftEdgeLon, topEdgeLat]),
    ],
  ]);
}

export function createCellFill(value, layerName) {
  const params = LAYER_PARAMS[layerName];
  const gradient = calculateLinearGradient(
    value,
    params.minValue,
    params.maxValue
  );
  const color = [0, 1, 2].map(
    (index) =>
      params.minColor[index] +
      Math.round((params.maxColor[index] - params.minColor[index]) * gradient)
  );

  return new Fill({ color: `rgba(${color},${LAYER_OPACITY})` });
}

function calculateLinearGradient(value, minValue, maxValue) {
  let gradient = (value - minValue) / (maxValue - minValue);
  if (gradient < 0) {
    gradient = 0;
  }
  if (gradient > 1) {
    gradient = 1;
  }
  return gradient;
}
