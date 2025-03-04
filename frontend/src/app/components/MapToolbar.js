import { getLayerToggleId, getTickShifterId } from './MapControl';
import { INDICATOR_GRADIENTS } from './MapTheme';
import {
  Algorithm,
  Indicator,
  Layer,
  PointerMode,
} from '../models/Enumerations';
import {
  capitalizeText,
  describeTimePeriod,
  formatDate,
} from '../models/Presentation';

export class MapToolbar {
  constructor(runtimeControls) {
    this.runtimeControls = runtimeControls;
    this.pointerMode = PointerMode.Regular;
    this.algorithm = Algorithm.Thermal;
    this.layer = Layer.Fire;
  }

  initialize() {
    this.selectLayer(this.layer);
    this.selectAlgorithm(this.algorithm);
  }

  enterSimulation(simulation) {
    this.configureInfoControl(simulation);
    this.configureTimelineControl(simulation.timeline);
    this.runtimeControls.forEach((control) => control.show());
  }

  exitSimulation() {
    this.runtimeControls.forEach((control) => control.hide());
  }

  configureInfoControl(simulation) {
    setLabelContent(
      'label-active-algorithm',
      `Active algorithm: ${capitalizeText(simulation.algorithm)}`
    );
  }

  configureTimelineControl(timeline) {
    for (const deltaTicks of [-10, -1, 1, 10]) {
      const period = deltaTicks * timeline.stepDurationMs;
      const tickShifterId = getTickShifterId(deltaTicks);
      const tickShifter = document.getElementById(tickShifterId);
      tickShifter.innerHTML = `${describeTimePeriod(period, false)}`;
      tickShifter.title = `${deltaTicks < 0 ? 'Rewind' : 'Advance'} simulation by ${describeTimePeriod(period).slice(2)}`;
    }
    setTimelineDate('label-timeline-start-date', 'Start', timeline.startDate);
  }

  updateControls(simulation) {
    this.updateInfoControl(simulation);
    this.updateTimelineControl(simulation.timeline);
  }

  updateInfoControl(simulation) {
    setDamagedArea(simulation.estimateDamagedArea());
    setAverageAirTemperature(simulation.calculateAverageAirTemperature());
    setAverageAirHumidity(simulation.calculateAverageAirHumidity());
    setAverageWindSpeed(simulation.calculateAverageWindSpeed());
    setAverageFuelDensity(simulation.calculateAverageFuelDensity());
  }

  updateTimelineControl(timeline) {
    const currentPeriod = timeline.getCurrentPeriod();
    setTimelinePeriod(currentPeriod);
    const startTs = timeline.startDate.valueOf();
    const currentDate = new Date(startTs + currentPeriod);
    setTimelineDate('label-timeline-current-date', 'Current', currentDate);
  }

  setPointerMode(pointerMode) {
    this.pointerMode = pointerMode;
    const isLighter = pointerMode === PointerMode.Lighter;
    switchElementClass('map-container', 'lighter-pointer', isLighter);
  }

  switchAlgorithm() {
    this.selectAlgorithm(
      this.algorithm === Algorithm.Thermal
        ? Algorithm.Probabilistic
        : Algorithm.Thermal
    );
  }

  selectAlgorithm(selectedAlgorithm) {
    this.algorithm = selectedAlgorithm;
    setLabelContent('algorithm-switch', capitalizeText(selectedAlgorithm));
  }

  selectLayer(selectedLayer) {
    this.layer = selectedLayer;
    for (const layer of Object.values(Layer)) {
      const toggleId = getLayerToggleId(layer);
      switchElementClass(toggleId, 'selected', layer === selectedLayer);
    }
  }
}

function nameRangeSection(value, moderateThreshold, highThreshold) {
  if (highThreshold <= value) {
    return 'high';
  }
  if (moderateThreshold <= value) {
    return 'moderate';
  }
  return 'low';
}

function setDamagedArea(damagedArea) {
  const content = `Damaged area: ${Math.round(damagedArea / 10000)} ha`;
  setLabelContent('label-damaged-area', content);
}

function setAverageAirTemperature(airTemperature) {
  if (Math.abs(Math.round(airTemperature)) < 10) {
    airTemperature = Math.round(airTemperature * 10) / 10;
  } else {
    airTemperature = Math.round(airTemperature);
  }

  const content = `${airTemperature} °C`;
  const title = `Air temperature: ${nameRangeSection(airTemperature, 10, 30)}`;
  setIndicator(Indicator.AirTemperature, airTemperature, content, title);
}

function setAverageAirHumidity(airHumidity) {
  airHumidity = Math.round(airHumidity * 100);

  const content = `${airHumidity} %`;
  const title = `Air humidity: ${nameRangeSection(airHumidity, 30, 70)}`;
  setIndicator(Indicator.AirHumidity, airHumidity, content, title);
}

function setAverageWindSpeed(windSpeed) {
  if (Math.abs(Math.round(windSpeed)) < 10) {
    windSpeed = Math.round(windSpeed * 10) / 10;
  } else {
    windSpeed = Math.round(windSpeed);
  }

  const content = `${windSpeed} m/s`;
  const title = `Wind speed: ${nameRangeSection(windSpeed, 3, 7)}`;
  setIndicator(Indicator.WindSpeed, windSpeed, content, title);
}

function setAverageFuelDensity(fuelDensity) {
  fuelDensity = Math.round(fuelDensity * 100) / 100;

  const content = `${fuelDensity}`;
  const title = `Fuel density: ${nameRangeSection(fuelDensity, 0.3, 0.7)}`;
  setIndicator(Indicator.FuelDensity, fuelDensity, content, title);
}

function setIndicator(indicator, value, content, title) {
  const containerId = `container-indicator-${indicator}`;
  const container = document.getElementById(containerId);
  const backgroundColor = INDICATOR_GRADIENTS[indicator]
    .backgroundFor(value)
    .css();
  container.style.background = backgroundColor;
  container.title = title;

  const label = document.getElementById(`label-indicator-${indicator}`);
  label.innerHTML = content;
  label.style.color = INDICATOR_GRADIENTS[indicator].textFor(value).css();
}

function setTimelinePeriod(period) {
  setLabelContent('label-timeline-period', describeTimePeriod(period));
}

function setTimelineDate(dateLabelId, dateTitle, date) {
  setLabelContent(dateLabelId, `${dateTitle}: ${formatDate(date)}`);
}

function setLabelContent(elementId, content) {
  document.getElementById(elementId).innerHTML = content;
}

function switchElementClass(elementId, className, isWanted) {
  const element = document.getElementById(elementId);
  if (isWanted) {
    element.classList.add(className);
  } else {
    element.classList.remove(className);
  }
}
