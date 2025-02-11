import React, { Component } from 'react';
import { Map as OLMap, View } from 'ol';
import Polygon from 'ol/geom/Polygon';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Fill, Stroke, Style } from 'ol/style';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Control from 'ol/control/Control';

import {
  TimePeriod,
  SCALE_FACTOR,
  STEP_DURATION,
  LIMIT_STEPS,
  SIGNIFICANT_OVERHEAT,
} from '../domain/definitions';

import { toCellCoordinates } from '../domain/logic';
import { scenarioService } from '../services/registry';

// const INITIAL_MAP_CENTER = [37.6173, 55.7558];
const INITIAL_MAP_CENTER = [49, 37.5];
const INITIAL_MAP_ZOOM = 12;

const LAYER_OPACITY = 0.6;
const LAYER_PARAMS = {
  fire: {
    zeroTemperature: 0,
    flameTemperature: 1300,
    vegetationColor: [0, 255, 0, LAYER_OPACITY],
    coalColor: [0, 0, 0, LAYER_OPACITY / 1.6],
    flameMinColor: [255, 160, 0, LAYER_OPACITY],
    flameMaxColor: [255, 0, 0, LAYER_OPACITY],
  },
  fuel: {
    minAmount: 0,
    maxAmount: 1,
    minColor: [0, 0, 0, 0.1],
    maxColor: [0, 180, 30, LAYER_OPACITY],
  },
};

export class SimulationMap extends Component {
  constructor() {
    super();
    this.controls = new SimulationMapControls();
    this.scenario = undefined;
    this.initializeMap();
  }

  componentDidMount() {
    this.controls.map.setTarget(this.controls.mapContainerRef.current);
    this.controls.switchLayer(Layer.Fire);
  }

  render() {
    return <div id='map-container' ref={this.controls.mapContainerRef}></div>;
  }

  initializeMap() {
    const map = this.controls.map;

    map.addLayer(new TileLayer({ source: new OSM() }));
    map.addLayer(new VectorLayer({ source: this.controls.layerSource }));
    map.addControl(this.createScenarioControl());
    map.addControl(this.createTimelineControl());
    map.addControl(this.createInfoControl());
    map.addControl(this.createLayerControl());

    map.setView(
      new View({
        center: fromLonLat(INITIAL_MAP_CENTER),
        zoom: INITIAL_MAP_ZOOM,
      })
    );

    map.on('singleclick', (event) => this.handleMapClick(event));
  }

  createScenarioControl() {
    const container = document.createElement('div');
    container.id = 'control-container-scenario';
    container.className = 'ol-unselectable ol-control';

    const header = document.createElement('label');
    header.innerHTML = 'Algorithm';
    header.className = 'header';
    container.appendChild(header);

    addControlButton(
      container,
      () => {
        this.controls.switchAlgorithm();
      },
      this.controls.algorithmName,
      'Switch algorithm',
      'control-inline-button auto-width',
      'algorithm-switcher'
    );

    addControlButton(
      container,
      () => {
        this.controls.setPointerMode(
          this.controls.pointerMode === PointerMode.Regular
            ? PointerMode.Lighter
            : PointerMode.Regular
        );
      },
      '🔥',
      'Set ignition point',
      'control-inline-button lighter'
    );

    return new Control({ element: container });
  }

  createTimelineControl() {
    const container = document.createElement('div');
    container.id = 'control-container-timeline';
    container.className = 'ol-unselectable ol-control hidden';

    const header = document.createElement('label');
    header.innerHTML =
      '<<&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Timeline&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;>>';
    header.className = 'header';
    container.appendChild(header);

    for (const stepShift of [-10, -1, 1, 10]) {
      addControlButton(
        container,
        () => this.navigateTimeline(stepShift),
        `${describeTimePeriod(stepShift, false)}`,
        titleTimelineButton(stepShift),
        'control-inline-button auto-width'
      );
    }

    return new Control({ element: container });
  }

  createInfoControl() {
    const container = document.createElement('div');
    container.id = 'control-container-info';
    container.className = 'ol-unselectable ol-control hidden';

    const header = document.createElement('label');
    header.innerHTML = 'Information';
    header.className = 'header';
    container.appendChild(header);

    return new Control({ element: container });
  }

  createLayerControl() {
    const container = document.createElement('div');
    container.id = 'control-container-layer';
    container.className = 'ol-unselectable ol-control hidden';

    const header = document.createElement('label');
    header.innerHTML = 'Layers';
    header.className = 'header';
    container.appendChild(header);

    for (const layer of [Layer.Fire, Layer.Fuel]) {
      addControlButton(
        container,
        () => {
          this.controls.switchLayer(layer);
          this.displayForecast();
        },
        fillLayerToggle(layer),
        titleLayerToggle(layer),
        'control-inline-button layer-toggle off',
        `layer-toggle-${layer}`
      );
    }

    return new Control({ element: container });
  }

  async handleMapClick(event) {
    if (this.controls.pointerMode !== PointerMode.Lighter) {
      return;
    }
    if (this.scenario !== undefined) {
      scenarioService.removeScenario(this.scenario);
    }
    this.controls.setPointerMode(PointerMode.Regular);
    const startCoordinates = toCellCoordinates(toLonLat(event.coordinate));
    this.scenario = await scenarioService.createScenario(
      startCoordinates,
      Date.now(),
      this.controls.algorithm
    );
    this.displayForecast();
    this.controls.enterSimulation();
  }

  async navigateTimeline(stepShift) {
    const desiredTimePoint = Math.min(
      LIMIT_STEPS,
      Math.max(0, this.scenario.step + stepShift)
    );
    this.scenario.step = desiredTimePoint;
    await scenarioService.forecastScenario(this.scenario, desiredTimePoint);
    this.displayForecast();
  }

  displayForecast() {
    this.updateForecastCells();
    this.updateForecastInfo();
  }

  updateForecastCells() {
    this.controls.layerSource.clear();

    const startCellFeature = new Feature({
      geometry: createCellFigure(this.scenario.startCoordinates),
    });
    const startCellStyle = new Style({
      stroke: new Stroke({ color: [255, 0, 0, 0.3], width: 3 }),
    });
    startCellFeature.setStyle(startCellStyle);
    this.controls.layerSource.addFeature(startCellFeature);

    const forecast = this.scenario.forecastLog.forecasts[this.scenario.step];
    for (const cell of forecast.cells) {
      if (
        !cell.fire.isDamaged &&
        cell.fire.heat < cell.factors.airTemperature + SIGNIFICANT_OVERHEAT
      ) {
        continue;
      }
      const feature = new Feature({
        geometry: createCellFigure(cell.coordinates),
      });
      const style = new Style({ fill: this.createCellFiller(cell) });
      feature.setStyle(style);

      this.controls.layerSource.addFeature(feature);
    }
  }

  createCellFiller(cell) {
    let value;
    let bottomBoundary;
    let bottomColor;
    let topBoundary;
    let topColor;
    if (this.controls.layer === Layer.Fire) {
      value = cell.fire.heat;
      let ignitionTemperature = this.scenario.conditions.ignitionTemperature;
      if (value > ignitionTemperature) {
        bottomBoundary = ignitionTemperature;
        bottomColor = LAYER_PARAMS.fire.flameMinColor;
        topBoundary = LAYER_PARAMS.fire.flameTemperature;
        topColor = LAYER_PARAMS.fire.flameMaxColor;
      } else {
        bottomBoundary = LAYER_PARAMS.fire.zeroTemperature;
        topBoundary = ignitionTemperature;
        topColor = LAYER_PARAMS.fire.flameMinColor;

        if (cell.fire.isDamaged) {
          bottomColor = LAYER_PARAMS.fire.coalColor;
        } else {
          bottomColor = LAYER_PARAMS.fire.vegetationColor;
        }
      }
    }
    if (this.controls.layer === Layer.Fuel) {
      value = cell.fire.fuel;
      bottomBoundary = LAYER_PARAMS.fuel.minAmount;
      bottomColor = LAYER_PARAMS.fuel.minColor;
      topBoundary = LAYER_PARAMS.fuel.maxAmount;
      topColor = LAYER_PARAMS.fuel.maxColor;
    }

    const gradient = calculateGradient(value, bottomBoundary, topBoundary);
    const colorGradient = [0, 1, 2, 3].map(
      (index) =>
        bottomColor[index] + (topColor[index] - bottomColor[index]) * gradient
    );
    const color = [
      ...colorGradient.slice(0, 3).map(Math.round),
      colorGradient[3],
    ];

    return new Fill({ color: `rgba(${color})` });
  }

  updateForecastInfo() {}
}

const Algorithm = {
  Thermal: 'thermal',
  Probabilistic: 'probabilistic',
};

const PointerMode = {
  Regular: 'regular',
  Lighter: 'lighter',
};

const Layer = {
  Fire: 'fire',
  Fuel: 'fuel',
};

class SimulationMapControls {
  constructor() {
    this.pointerMode = PointerMode.Regular;
    this.mapContainerRef = React.createRef();
    this.layerSource = new VectorSource();
    this.map = new OLMap();
    this.algorithm = Algorithm.Thermal;
    this.layer = Layer.Fire;
  }

  get algorithmName() {
    return this.algorithm.charAt(0).toUpperCase() + this.algorithm.slice(1);
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
    switcher.innerHTML = this.algorithmName;
  }

  switchLayer(layer) {
    this.layer = layer;
    for (layer of [Layer.Fire, Layer.Fuel]) {
      switchElementClass(
        document.getElementById(`layer-toggle-${layer}`),
        'off',
        this.layer !== layer
      );
    }
  }

  enterSimulation() {
    for (const controlName of ['timeline', 'info', 'layer']) {
      switchElementClass(
        document.getElementById('control-container-' + controlName),
        'hidden',
        false
      );
    }
  }
}

function createCellFigure(coordinates) {
  const leftEdgeLon = coordinates.x / SCALE_FACTOR;
  const rightEdgeLon = (coordinates.x + 1) / SCALE_FACTOR;
  const bottomEdgeLat = coordinates.y / SCALE_FACTOR;
  const topEdgeLat = (coordinates.y + 1) / SCALE_FACTOR;

  return new Polygon([
    [
      fromLonLat([leftEdgeLon, topEdgeLat]),
      fromLonLat([rightEdgeLon, topEdgeLat]),
      fromLonLat([rightEdgeLon, bottomEdgeLat]),
      fromLonLat([leftEdgeLon, bottomEdgeLat]),
      fromLonLat([leftEdgeLon, topEdgeLat]),
    ],
  ]);
}

function calculateGradient(value, bottomBoundary, topBoundary) {
  let gradient = (value - bottomBoundary) / (topBoundary - bottomBoundary);
  if (gradient < 0) {
    gradient = 0;
  }
  if (gradient > 1) {
    gradient = 1;
  }
  return gradient;
}

function fillLayerToggle(layer) {
  switch (layer) {
    case Layer.Fire:
      return '🔥Fire';
    case Layer.Fuel:
      return '🌳Fuel';
  }
}

function titleLayerToggle(layer) {
  return `Display ${layer} layer`;
}

function titleTimelineButton(steps) {
  return `${steps < 0 ? 'Rewind' : 'Advance'} simulation by ${describeTimePeriod(steps).slice(2)}`;
}

function describeTimePeriod(steps, verbose = true) {
  const sign = steps < 0 ? -1 : 1;
  steps *= sign;
  let duration = steps * STEP_DURATION;
  let description = '';
  let sections = 0;
  [
    [verbose ? 'day' : 'd', TimePeriod.days],
    [verbose ? 'hour' : 'h', TimePeriod.hours],
    [verbose ? 'minute' : 'm', TimePeriod.minutes],
  ].forEach((periodItem) => {
    const periodName = periodItem[0];
    const period = periodItem[1];
    if (period(1) <= duration) {
      const amount = Math.floor(duration / period(1));
      duration -= amount * period(1);
      sections += 1;
      description += ` ${amount}${verbose ? ' ' : ''}${periodName}${verbose && amount > 1 ? 's' : ''}`;
    }
  });
  return `${sign < 0 ? '-' : '+'}${verbose || sections > 1 ? ' ' : ''}${description.slice(1)}`;
}

function addControlButton(
  container,
  handler,
  content,
  title,
  className = '',
  id = ''
) {
  const button = document.createElement('button');
  button.innerHTML = content;
  button.title = title;
  button.className = className;
  button.id = id;
  button.addEventListener('click', handler);
  container.appendChild(button);
}

function switchElementClass(element, className, isWanted) {
  if (isWanted) {
    element.classList.add(className);
  } else {
    element.classList.remove(className);
  }
}
