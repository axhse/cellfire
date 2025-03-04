export class Timeline {
  constructor(startDate, stepDurationMs, limitTicks) {
    this.startDate = startDate;
    this.stepDurationMs = stepDurationMs;
    this.limitTicks = limitTicks;
    this.currentTick = 0;
  }

  getCurrentPeriod() {
    return this.currentTick * this.stepDurationMs;
  }

  navigate(deltaTicks) {
    const wantedTick = this.currentTick + deltaTicks;
    const newTick = Math.min(this.limitTicks, Math.max(0, wantedTick));
    this.currentTick = newTick;
    return newTick;
  }
}
