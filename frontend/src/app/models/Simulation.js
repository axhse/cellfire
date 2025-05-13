export const SIGNIFICANT_OVERHEAT = 30;

const EARTH_CIRCUMFERENCE = 40000000;

export class Simulation {
  constructor(id, grid, timeline) {
    this.id = id;
    this.grid = grid;
    this.timeline = timeline;
    this.steps = [];
  }

  appendSteps(steps, startIndex) {
    if (
      this.steps.length < startIndex ||
      startIndex + steps.length <= this.steps.length
    ) {
      return;
    }
    if (startIndex < this.steps.length) {
      this.steps = this.steps.slice(0, startIndex);
    }
    this.steps.push(...steps);
    if (steps[steps.length - 1].final) {
      this.timeline.limitTicks = this.steps.length - 1;
      this.timeline.simulatedTick = this.steps.length - 1;
    }
  }

  getSimulatedCells() {
    return this.steps[this.timeline.simulatedTick].cells;
  }

  getSampleCells() {
    let unburnedCells = this.getUnburnedCells();
    if (unburnedCells.length === 0) {
      return this.getSimulatedCells();
    }
    return unburnedCells;
  }

  getDamagedCells() {
    return this.getSimulatedCells().filter((cell) => cell.state.damaged);
  }

  getBurningCells() {
    return this.getSimulatedCells().filter((cell) => cell.burning);
  }

  getBurnedCells() {
    return this.getSimulatedCells().filter(
      (cell) => cell.state.damaged && !cell.burning,
    );
  }

  getIgnitingCells() {
    return this.getSimulatedCells().filter(
      (cell) => !cell.state.damaged && !cell.burning,
    );
  }

  getUnburnedCells() {
    return this.getSimulatedCells().filter(
      (cell) => !cell.state.damaged || cell.burning,
    );
  }

  calculateAverageAirTemperature() {
    return this.calculateAverageFactor((cell) => cell.factors.airTemperature);
  }

  calculateAverageAirHumidity() {
    return this.calculateAverageFactor((cell) => cell.factors.airHumidity);
  }

  calculateAverageWindSpeed() {
    return this.calculateAverageFactor((cell) => cell.factors.windSpeed);
  }

  calculateAverageFuelDensity() {
    return this.calculateAverageFactor((cell) => cell.state.fuel);
  }

  calculateAverageFactor(propertyGetter) {
    const cells = this.getSampleCells();
    const count = cells.length;
    return cells.map(propertyGetter).reduce((a, v) => a + v, 0) / count;
  }

  calculateAverageWindAngle() {
    const cells = this.getSampleCells();
    const cellCount = cells.length;
    let vectorX = 0;
    let vectorY = 0;
    for (const cell of cells) {
      vectorX += cell.factors.windX;
      vectorY += cell.factors.windY;
    }
    vectorX /= cellCount;
    vectorY /= cellCount;
    if (vectorX === 0 && vectorY === 0) {
      return 0;
    }
    return Math.atan2(vectorY, vectorX);
  }

  estimateDamagedArea() {
    return this.getDamagedCells()
      .map((cell) => estimateCellArea(this.grid.scale, cell.coordinates))
      .reduce((a, ai) => a + ai, 0);
  }
}

function estimateCellArea(gridScale, coordinates) {
  const cellHeight = EARTH_CIRCUMFERENCE / 360 / gridScale;
  const lat = toLonLat(gridScale, coordinates)[1];
  return cellHeight * cellHeight * Math.cos(lat * (Math.PI / 180));
}

function toLonLat(gridScale, coordinates) {
  return [(coordinates.x + 0.5) / gridScale, (coordinates.y + 0.5) / gridScale];
}
