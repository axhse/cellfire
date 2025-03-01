const EARTH_CIRCUMFERENCE = 40000000;

export function getWindSpeed(weather) {
  return Math.sqrt(Math.pow(weather.windX, 2) + Math.pow(weather.windY, 2));
}

export function calculateAverageAirTemperature(simulation) {
  let cells = getUnburnedCells(simulation);
  if (cells.length === 0) {
    cells = getCells(simulation);
  }
  return (
    cells
      .map((cell) => cell.weather.airTemperature)
      .reduce((a, t) => a + t, 0) / cells.length
  );
}

export function calculateAverageAirHumidity(simulation) {
  let cells = getUnburnedCells(simulation);
  if (cells.length === 0) {
    cells = getCells(simulation);
  }
  return (
    cells.map((cell) => cell.weather.airHumidity).reduce((a, h) => a + h, 0) /
    cells.length
  );
}

export function calculateAverageWindSpeed(simulation) {
  let cells = getUnburnedCells(simulation);
  if (cells.length === 0) {
    cells = getCells(simulation);
  }
  return (
    cells.map((cell) => getWindSpeed(cell.weather)).reduce((a, w) => a + w, 0) /
    cells.length
  );
}

export function calculateAverageFuelDensity(simulation) {
  let cells = getUnburnedCells(simulation);
  if (cells.length === 0) {
    cells = getCells(simulation);
  }
  return (
    cells.map((cell) => cell.state.fuel).reduce((a, f) => a + f, 0) /
    cells.length
  );
}

export function estimateDamagedArea(simulation) {
  return getDamagedCells(simulation)
    .map((cell) => estimateCellArea(simulation.grid.scale, cell.coordinates))
    .reduce((a, ai) => a + ai, 0);
}

function getCells(simulation) {
  return simulation.steps[simulation.step].cells;
}

function getDamagedCells(simulation) {
  return getCells(simulation).filter((cell) => cell.state.damaged);
}

function getUnburnedCells(simulation) {
  return getCells(simulation).filter(
    (cell) =>
      !cell.state.damaged ||
      simulation.conditions.ignitionTemperature <= cell.state.heat
  );
}

function toLonLat(gridScale, coordinates) {
  return [(coordinates.x + 0.5) / gridScale, (coordinates.y + 0.5) / gridScale];
}

function estimateCellArea(gridScale, coordinates) {
  const cellHeight = EARTH_CIRCUMFERENCE / 360 / gridScale;
  const lat = toLonLat(gridScale, coordinates)[1];
  return cellHeight * cellHeight * Math.cos(lat * (Math.PI / 180));
}
