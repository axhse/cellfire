export const TimePeriod = {
  days: (n) => n * 24 * 60 * 60 * 1000,
  hours: (n) => n * 60 * 60 * 1000,
  minutes: (n) => n * 60 * 1000,
};

export const GRID_SCALE = 200;
export const STEP_DURATION = TimePeriod.minutes(30);
const LIMIT_DURATION = TimePeriod.days(7);
export const LIMIT_STEPS = LIMIT_DURATION / STEP_DURATION;
export const SIGNIFICANT_OVERHEAT = 30;
