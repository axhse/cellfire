import { Layer } from './mapState';

export function fillLayerToggle(layer) {
  switch (layer) {
    case Layer.Fire:
      return '🔥 Fire';
    case Layer.Fuel:
      return '🌳 Fuel';
    case Layer.Elevation:
      return '⛰️ Elevation';
    case Layer.Wind:
      return '🌀 Wind';
  }
}

export function titleAlgorithm(algorithm) {
  return algorithm.charAt(0).toUpperCase() + algorithm.slice(1);
}

export function titleLayerToggle(layer) {
  return `Display ${layer} layer`;
}

export function titleTimelineButton(steps, stepDurationMs) {
  return `${steps < 0 ? 'Rewind' : 'Advance'} simulation by ${describeTimePeriod(steps, stepDurationMs).slice(2)}`;
}

export function identifyRangeSection(value, moderateThreshold, highThreshold) {
  if (highThreshold <= value) {
    return 'high';
  }
  if (moderateThreshold <= value) {
    return 'moderate';
  }
  return 'low';
}

export function describeTimePeriod(steps, stepDurationMs, verbose = true) {
  const sign = steps < 0 ? -1 : 1;
  steps *= sign;
  let duration = steps * stepDurationMs;
  let description = '';
  let sections = 0;
  const periods = [
    [verbose ? 'day' : 'd', 24 * 60],
    [verbose ? 'hour' : 'h', 60],
    [verbose ? 'minute' : 'm', 1],
  ];
  for (const [periodName, periodMinutes] of periods) {
    const period = periodMinutes * 60 * 1000;
    if (period <= duration || (sections === 0 && periodName[0] === 'm')) {
      const amount = Math.floor(duration / period);
      duration -= amount * period;
      sections += 1;
      description += ` ${amount}${verbose ? ' ' : ''}${periodName}${verbose && amount !== 1 ? 's' : ''}`;
    }
  }
  return `${sign < 0 ? '-' : '+'}${verbose || sections > 1 ? ' ' : ''}${description.slice(1)}`;
}

export function formatDate(date) {
  return `${date.getFullYear()}-${make2digit(date.getMonth() + 1)}-${make2digit(date.getDate())} ${make2digit(date.getHours())}:${make2digit(date.getMinutes())}`;
}

function make2digit(n) {
  return String(n).padStart(2, '0');
}
