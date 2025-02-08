const TimePeriod = {
  minutes: (n) => n * 60 * 1000,
  hours: (n) => n * 60 * 60 * 1000,
  days: (n) => n * 24 * 60 * 60 * 1000,
};

export const SCALE_FACTOR = 200;
export const FORECAST_STEP = TimePeriod.minutes(30);
export const MAX_FORECAST_PERIOD = TimePeriod.days(3);
export const SIGNIFICANT_OVERHEAT = 30;
