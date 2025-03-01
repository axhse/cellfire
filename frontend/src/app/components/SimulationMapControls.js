import React from 'react';
import { Map as OLMap } from 'ol';
import VectorSource from 'ol/source/Vector';

import { IndicatorGradient } from './MapColoring';

import {
  estimateDamagedArea,
  calculateAverageAirTemperature,
  calculateAverageAirHumidity,
  calculateAverageWindSpeed,
  calculateAverageFuelDensity,
} from '../models/domainLogic';
import { Algorithm, Layer, PointerMode } from '../models/mapState';
import {
  titleAlgorithm,
  titleTimelineButton,
  formatDate,
  describeTimePeriod,
  identifyRangeSection,
} from '../models/presentation';

export class SimulationMapControls {
  constructor() {
    this.pointerMode = PointerMode.Regular;
    this.mapContainerRef = React.createRef();
    this.layerSource = new VectorSource();
    this.map = new OLMap();
    this.algorithm = Algorithm.Thermal;
    this.layer = Layer.Fire;
  }

  setPointerMode(pointerMode) {
    if (pointerMode === this.pointerMode) {
      return;
    }
    this.pointerMode = pointerMode;
    switchElementClass(
      this.mapContainerRef.current,
      'lighter-pointer',
      pointerMode === PointerMode.Lighter
    );
  }

  switchAlgorithm() {
    this.algorithm =
      this.algorithm === Algorithm.Thermal
        ? Algorithm.Probabilistic
        : Algorithm.Thermal;
    const switcher = document.getElementById('algorithm-switcher');
    switcher.innerHTML = titleAlgorithm(this.algorithm);
  }

  switchLayer(layer) {
    this.layer = layer;
    for (const otherLayerField in Layer) {
      const otherLayer = Layer[otherLayerField];
      switchElementClass(
        document.getElementById(`layer-toggle-${otherLayer}`),
        'off',
        otherLayer !== this.layer
      );
    }
  }

  setPeriod(steps, stepDurationMs) {
    document.getElementById('label-period').innerHTML = describeTimePeriod(
      steps,
      stepDurationMs
    );
  }

  setDate(dateLabelId, dateTitle, date) {
    document.getElementById(dateLabelId).innerHTML =
      `${dateTitle}: ${formatDate(date)}`;
  }

  setCurrentDate(date) {
    this.setDate('label-current-date', 'Current', date);
  }

  setDamagedArea(damagedArea) {
    document.getElementById('label-damaged-area').innerHTML =
      `Damaged area: ${Math.round(damagedArea / 10000)} ha`;
  }

  setAverageAirTemperature(airTemperature) {
    if (Math.abs(Math.round(airTemperature)) < 10) {
      airTemperature = Math.round(airTemperature * 10) / 10;
    } else {
      airTemperature = Math.round(airTemperature);
    }
    const container = document.getElementById(
      'indicator-container-air-temperature'
    );
    container.style.background =
      new IndicatorGradient().airTemperatureBackground
        .for(airTemperature)
        .css();
    container.title = `Air temperature: ${identifyRangeSection(airTemperature, 10, 30)}`;
    const label = document.getElementById('label-indicator-air-temperature');
    label.innerHTML = `${airTemperature} °C`;
    label.style.color = new IndicatorGradient().airTemperatureText
      .for(airTemperature)
      .css();
  }

  setAverageAirHumidity(airHumidity) {
    airHumidity = Math.round(airHumidity * 100);
    const container = document.getElementById(
      'indicator-container-air-humidity'
    );
    container.style.background = new IndicatorGradient().airHumidityBackground
      .for(airHumidity)
      .css();
    container.title = `Air humidity: ${identifyRangeSection(airHumidity, 30, 70)}`;
    const label = document.getElementById('label-indicator-air-humidity');
    label.innerHTML = `${airHumidity} %`;
    label.style.color = new IndicatorGradient().airHumidityText
      .for(airHumidity)
      .css();
  }

  setAverageWindSpeed(windSpeed) {
    if (Math.abs(Math.round(windSpeed)) < 10) {
      windSpeed = Math.round(windSpeed * 10) / 10;
    } else {
      windSpeed = Math.round(windSpeed);
    }
    const container = document.getElementById('indicator-container-wind-speed');
    container.style.background = new IndicatorGradient().windSpeedBackground
      .for(windSpeed)
      .css();
    container.title = `Wind speed: ${identifyRangeSection(windSpeed, 3, 7)}`;
    const label = document.getElementById('label-indicator-wind-speed');
    label.innerHTML = `${windSpeed} m/s`;
    label.style.color = new IndicatorGradient().windSpeedText
      .for(windSpeed)
      .css();
  }

  setAverageFuelDensity(fuelDensity) {
    fuelDensity = Math.round(fuelDensity * 100) / 100;
    const container = document.getElementById(
      'indicator-container-fuel-density'
    );
    container.style.background = new IndicatorGradient().fuelDensityBackground
      .for(fuelDensity)
      .css();
    container.title = `Fuel density: ${identifyRangeSection(fuelDensity, 0.3, 0.7)}`;
    const label = document.getElementById('label-indicator-fuel-density');
    label.innerHTML = `${fuelDensity}`;
    label.style.color = new IndicatorGradient().fuelDensityText
      .for(fuelDensity)
      .css();
  }

  updateTimeline(simulation) {
    this.setPeriod(simulation.step, simulation.stepDurationMs);
    this.setCurrentDate(
      new Date(
        simulation.startDate.valueOf() +
          simulation.step * simulation.stepDurationMs
      )
    );
  }

  updateInformation(simulation) {
    this.setDamagedArea(estimateDamagedArea(simulation));
    this.setAverageAirTemperature(calculateAverageAirTemperature(simulation));
    this.setAverageAirHumidity(calculateAverageAirHumidity(simulation));
    this.setAverageWindSpeed(calculateAverageWindSpeed(simulation));
    this.setAverageFuelDensity(calculateAverageFuelDensity(simulation));
  }

  enterSimulation(simulation) {
    document.getElementById('label-active-algorithm').innerHTML =
      `Active algorithm: ${titleAlgorithm(simulation.algorithm)}`;
    this.setDate('label-start-date', 'Start', simulation.startDate);
    this.updateTimeline(simulation);
    this.updateInformation(simulation);
    for (const controlName of ['timeline', 'info', 'layer']) {
      switchElementClass(
        document.getElementById('control-container-' + controlName),
        'hidden',
        false
      );
    }
    for (const stepShift of [-10, -1, 1, 10]) {
      const stepShifter = document.getElementById(`stepShifter${stepShift}`);
      stepShifter.innerHTML = `${describeTimePeriod(stepShift, simulation.stepDurationMs, false)}`;
      stepShifter.title = titleTimelineButton(
        stepShift,
        simulation.stepDurationMs
      );
    }
  }
}

function switchElementClass(element, className, isWanted) {
  if (isWanted) {
    element.classList.add(className);
  } else {
    element.classList.remove(className);
  }
}
