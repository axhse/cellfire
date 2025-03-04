export class Timeline {
  constructor(startDate, stepDurationMs, limitTicks) {
    this.startDate = startDate;
    this.stepDurationMs = stepDurationMs;
    this.limitTicks = limitTicks;
    this.simulatedTick = 0;
  }

  getSimulatedPeriod() {
    return this.simulatedTick * this.stepDurationMs;
  }

  navigate(deltaTicks) {
    const wantedTick = this.simulatedTick + deltaTicks;
    const newTick = Math.min(this.limitTicks, Math.max(0, wantedTick));
    this.simulatedTick = newTick;
    return newTick;
  }
}
