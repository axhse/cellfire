import {
  TICK_DELTAS,
  getCellCounterId,
  getLayerToggleId,
  getTickShifterId,
} from "./MapControl";
import { INDICATOR_GRADIENTS } from "./MapTheme";
import { Indicator, Layer, PointerMode } from "../models/Enumerations";
import {
  capitalizeText,
  describeTimePeriod,
  formatDate,
} from "../models/Presentation";

export class MapToolbar {
  constructor(runtimeControls) {
    this.runtimeControls = runtimeControls;
    this.pointerMode = PointerMode.Regular;
    this.layer = Layer.Fire;
  }

  initialize() {
    this.selectLayer(this.layer);
  }

  enterSimulation(simulation) {
    this.configureTimelineControl(simulation.timeline);
    this.runtimeControls.forEach((control) => control.show());
  }

  exitSimulation() {
    this.runtimeControls.forEach((control) => control.hide());
  }

  configureTimelineControl(timeline) {
    for (const tickDelta of TICK_DELTAS) {
      const period = tickDelta * timeline.stepDurationMs;
      const tickShifterId = getTickShifterId(tickDelta);
      const tickShifter = document.getElementById(tickShifterId);
      tickShifter.innerHTML = `${describeTimePeriod(period, false)}`;
      tickShifter.title = `${tickDelta < 0 ? "Rewind" : "Advance"} simulation by ${describeTimePeriod(period).slice(2)}`;
      if (tickDelta < 0) {
        setElementAvailability(getTickShifterId(tickDelta), false);
      }
    }
    setTimelineDate("label-start-date", "Start", timeline.startDate);
  }

  updateControls(simulation) {
    this.updateInfoControl(simulation);
    this.updateTimelineControl(simulation.timeline);
  }

  updateInfoControl(simulation) {
    setDamagedArea(simulation.estimateDamagedArea());
    setCellCounters(
      simulation.getBurningCells().length,
      simulation.getIgnitingCells().length,
      simulation.getBurnedCells().length,
    );
    setAirTemperature(simulation.calculateAverageAirTemperature());
    setAirHumidity(simulation.calculateAverageAirHumidity());
    const windSpeed = roundWindSpeed(simulation.calculateAverageWindSpeed());
    setWindSpeed(windSpeed);
    setWindDirection(simulation.calculateAverageWindAngle(), windSpeed);
    setFuelDensity(simulation.calculateAverageFuelDensity());
  }

  updateTimelineControl(timeline) {
    for (const tickDelta of TICK_DELTAS) {
      const potentialTick = timeline.simulatedTick + (tickDelta < 0 ? -1 : 1);
      const isAvailable =
        0 <= potentialTick && potentialTick <= timeline.limitTicks;
      setElementAvailability(getTickShifterId(tickDelta), isAvailable);
    }
    const simulatedPeriod = timeline.getSimulatedPeriod();
    setTimelinePeriod(simulatedPeriod);
    const startTs = timeline.startDate.valueOf();
    const simulatedDate = new Date(startTs + simulatedPeriod);
    setTimelineDate("label-simulated-date", "In-simulation", simulatedDate);
  }

  setPointerMode(pointerMode) {
    this.pointerMode = pointerMode;
    const isLighter = pointerMode === PointerMode.Lighter;
    switchElementClass("map-container", "lighter-pointer", isLighter);
  }

  selectLayer(selectedLayer) {
    this.layer = selectedLayer;
    for (const layer of Object.values(Layer)) {
      const toggleId = getLayerToggleId(layer);
      switchElementClass(toggleId, "selected", layer === selectedLayer);
    }
  }
}

function nameRangeSection(value, moderateThreshold, highThreshold) {
  if (highThreshold <= value) {
    return "high";
  }
  if (moderateThreshold <= value) {
    return "moderate";
  }
  return "low";
}

function setDamagedArea(damagedArea) {
  const content = `Damaged area: ${Math.round(damagedArea / 10000)} ha`;
  setLabelContent("label-damaged-area", content);
}

function setCellCounters(burningCount, ignitingCount, burnedCount) {
  const cellStateCounts = [
    ["burning", burningCount],
    ["igniting", ignitingCount],
    ["burned", burnedCount],
  ];
  for (const [name, value] of cellStateCounts) {
    setLabelContent(
      getCellCounterId(name),
      `- ${capitalizeText(name)}: ${value}`,
    );
  }
}

function setAirTemperature(airTemperature) {
  if (Math.abs(Math.round(airTemperature)) < 10) {
    airTemperature = Math.round(airTemperature * 10) / 10;
  } else {
    airTemperature = Math.round(airTemperature);
  }

  const content = `${airTemperature} °C`;
  const title = `Air temperature: ${nameRangeSection(airTemperature, 10, 30)}`;
  setIndicator(Indicator.AirTemperature, airTemperature, content, title);
}

function setAirHumidity(airHumidity) {
  airHumidity = Math.round(airHumidity * 100);

  const content = `${airHumidity} %`;
  const title = `Air humidity: ${nameRangeSection(airHumidity, 30, 70)}`;
  setIndicator(Indicator.AirHumidity, airHumidity, content, title);
}

function setWindSpeed(roundedWindSpeed) {
  const content = `${roundedWindSpeed} m/s`;
  const title = `Wind speed: ${nameRangeSection(roundedWindSpeed, 3, 7)}`;
  setIndicator(Indicator.WindSpeed, roundedWindSpeed, content, title);
}

function setWindDirection(windAngle, roundedWindSpeed) {
  const icon = document.getElementById("wind-direction-icon");
  const gradient = INDICATOR_GRADIENTS[Indicator.WindSpeed];
  icon.style.color = gradient.textFor(roundedWindSpeed).css();
  icon.style.transform = `rotate(${-windAngle.toFixed(3)}rad)`;
}

function roundWindSpeed(windSpeed) {
  if (Math.abs(Math.round(windSpeed)) < 10) {
    return Math.round(windSpeed * 10) / 10;
  } else {
    return Math.round(windSpeed);
  }
}

function setFuelDensity(fuelDensity) {
  fuelDensity = Math.round(fuelDensity * 100) / 100;

  const content = `${fuelDensity}`;
  const title = `Fuel density: ${nameRangeSection(fuelDensity, 0.3, 0.7)}`;
  setIndicator(Indicator.FuelDensity, fuelDensity, content, title);
}

function setIndicator(indicator, value, content, title) {
  const containerId = `indicator-container-${indicator}`;
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
  setLabelContent("label-period", describeTimePeriod(period));
}

function setTimelineDate(dateLabelId, dateTitle, date) {
  setLabelContent(dateLabelId, `${dateTitle}: ${formatDate(date)}`);
}

function setLabelContent(elementId, content) {
  document.getElementById(elementId).innerHTML = content;
}

function setElementAvailability(elementId, isAvailable) {
  document.getElementById(elementId).disabled = !isAvailable;
}

function switchElementClass(elementId, className, isWanted) {
  const element = document.getElementById(elementId);
  if (isWanted) {
    element.classList.add(className);
  } else {
    element.classList.remove(className);
  }
}
