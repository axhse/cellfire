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

import { simulator } from '../services/registry';

const EARTH_CIRCUMFERENCE = 40000000;
const SIGNIFICANT_OVERHEAT = 30;

// const INITIAL_MAP_CENTER = [37.6173, 55.7558];
const INITIAL_MAP_CENTER = [49, 37.5];
const INITIAL_MAP_ZOOM = 12;

const LAYER_OPACITY = 0.6;
const LAYER_STYLE = {
  boundaries: {
    sparseFuel: 0,
    denseFuel: 1,
    noElevation: 0,
    peakElevation: 2000,
    zeroTemperature: 0,
    peakFlameTemperature: 900,
    noWind: 0,
    intenseWind: 10,
  },
  colors: {
    vegetation: [0, 255, 0, LAYER_OPACITY],
    weakFlame: [255, 180, 0, LAYER_OPACITY],
    strongFlame: [255, 0, 0, LAYER_OPACITY],
    coal: [0, 0, 0, LAYER_OPACITY / 1.6],
    sparseFuel: [0, 0, 0, 0.1],
    denseFuel: [0, 180, 30, LAYER_OPACITY],
    ground: [0, 0, 0, 0.1],
    mountain: [50, 30, 0, LAYER_OPACITY],
    calm: [200, 200, 200, LAYER_OPACITY / 2],
    storm: [200, 0, 50, LAYER_OPACITY],
  },
};

export class SimulationMap extends Component {
  constructor() {
    super();
    this.controls = new SimulationMapControls();
    this.simulation = undefined;
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
    map.addControl(this.createSimulationControl());
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

  createSimulationControl() {
    const container = document.createElement('div');
    container.id = 'control-container-simulation';
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
      titleAlgorithm(this.controls.algorithm),
      'Switch algorithm',
      'control-inline-button',
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
        '',
        '',
        'control-inline-button timeline',
        `stepShifter${stepShift}`
      );
    }

    const startDateLabel = document.createElement('label');
    startDateLabel.id = 'label-start-date';
    container.appendChild(startDateLabel);

    const timePeriodLabel = document.createElement('label');
    timePeriodLabel.id = 'label-period';
    container.appendChild(timePeriodLabel);

    const currentDateLabel = document.createElement('label');
    currentDateLabel.id = 'label-current-date';
    container.appendChild(currentDateLabel);

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

    const activeAlgorithmLabel = document.createElement('label');
    activeAlgorithmLabel.id = 'label-active-algorithm';
    container.appendChild(activeAlgorithmLabel);

    const damagedAreaLabel = document.createElement('label');
    damagedAreaLabel.id = 'label-damaged-area';
    container.appendChild(damagedAreaLabel);

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

    for (const layerField in Layer) {
      const layer = Layer[layerField];
      addControlButton(
        container,
        () => {
          this.controls.switchLayer(layer);
          this.updateMap();
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
    if (this.simulation !== undefined) {
      simulator.removeSimulation(this.simulation);
    }
    this.controls.setPointerMode(PointerMode.Regular);
    this.simulation = await simulator.createSimulation(
      toLonLat(event.coordinate),
      this.controls.algorithm
    );
    this.simulation.step = 0;
    this.updateMap();
    this.controls.enterSimulation(this.simulation);
  }

  async navigateTimeline(stepShift) {
    const newStep = Math.min(
      this.simulation.limitDurationSteps,
      Math.max(0, this.simulation.step + stepShift)
    );
    this.simulation.step = newStep;
    await simulator.progressSimulation(this.simulation, newStep);
    this.updateMap();
  }

  updateMap() {
    this.updateSimulationCells();
    this.controls.updateTimeline(this.simulation);
    this.controls.updateInformation(this.simulation);
  }

  updateSimulationCells() {
    this.controls.layerSource.clear();

    const startCellFeature = new Feature({
      geometry: this.createRectangle(this.simulation.startCoordinates),
    });
    startCellFeature.setStyle(
      new Style({ stroke: new Stroke({ color: [255, 0, 0, 0.3], width: 3 }) })
    );
    this.controls.layerSource.addFeature(startCellFeature);

    for (const cell of this.simulation.steps[this.simulation.step].cells) {
      if (
        !cell.state.damaged &&
        cell.state.heat < cell.weather.airTemperature + SIGNIFICANT_OVERHEAT
      ) {
        continue;
      }
      const feature = new Feature({
        geometry: this.createRectangle(cell.coordinates),
      });
      feature.setStyle(new Style({ fill: this.createCellFiller(cell) }));
      this.controls.layerSource.addFeature(feature);
    }
  }

  createCellFiller(cell) {
    let value;
    let bottomBoundary;
    let topBoundary;
    let bottomColor;
    let topColor;
    if (this.controls.layer === Layer.Fire) {
      value = cell.state.heat;
      const ignitionTemperature =
        this.simulation.conditions.ignitionTemperature;
      if (value > ignitionTemperature) {
        bottomBoundary = ignitionTemperature;
        topBoundary = LAYER_STYLE.boundaries.peakFlameTemperature;
        bottomColor = LAYER_STYLE.colors.weakFlame;
        topColor = LAYER_STYLE.colors.strongFlame;
      } else {
        bottomBoundary = LAYER_STYLE.boundaries.zeroTemperature;
        topBoundary = ignitionTemperature;
        if (cell.state.damaged) {
          bottomColor = LAYER_STYLE.colors.coal;
        } else {
          bottomColor = LAYER_STYLE.colors.vegetation;
        }
        topColor = LAYER_STYLE.colors.weakFlame;
      }
    }
    if (this.controls.layer === Layer.Fuel) {
      value = cell.state.fuel;
      bottomBoundary = LAYER_STYLE.boundaries.sparseFuel;
      topBoundary = LAYER_STYLE.boundaries.denseFuel;
      bottomColor = LAYER_STYLE.colors.sparseFuel;
      topColor = LAYER_STYLE.colors.denseFuel;
    }
    if (this.controls.layer === Layer.Elevation) {
      value = cell.weather.elevation;
      bottomBoundary = LAYER_STYLE.boundaries.noElevation;
      topBoundary = LAYER_STYLE.boundaries.peakElevation;
      bottomColor = LAYER_STYLE.colors.ground;
      topColor = LAYER_STYLE.colors.mountain;
    }
    if (this.controls.layer === Layer.WindSpeed) {
      value = Math.sqrt(
        Math.pow(cell.weather.windX, 2) + Math.pow(cell.weather.windY, 2)
      );
      bottomBoundary = LAYER_STYLE.boundaries.noWind;
      topBoundary = LAYER_STYLE.boundaries.intenseWind;
      bottomColor = LAYER_STYLE.colors.calm;
      topColor = LAYER_STYLE.colors.storm;
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

  createRectangle(coordinates) {
    const gridScale = this.simulation.grid.scale;
    const leftEdgeLon = coordinates.x / gridScale;
    const rightEdgeLon = (coordinates.x + 1) / gridScale;
    const bottomEdgeLat = coordinates.y / gridScale;
    const topEdgeLat = (coordinates.y + 1) / gridScale;

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
}

const Algorithm = { Thermal: 'thermal', Probabilistic: 'probabilistic' };

const PointerMode = { Regular: 'regular', Lighter: 'lighter' };

const Layer = {
  Fire: 'fire',
  Fuel: 'fuel',
  Elevation: 'elevation',
  WindSpeed: 'windSpeed',
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

function estimateDamagedArea(simulation) {
  const cells = simulation.steps[simulation.step].cells;
  const damagedCells = cells.filter((cell) => cell.state.damaged);
  const damagedCellAreas = damagedCells.map((cell) =>
    estimateCellArea(simulation.grid.scale, cell.coordinates)
  );
  return damagedCellAreas.reduce((a1, a2) => a1 + a2, 0);
}

function estimateCellArea(gridScale, coordinates) {
  const cellHeight = EARTH_CIRCUMFERENCE / 360 / gridScale;
  const lat = fromCellCoordinates(gridScale, coordinates)[1];
  return cellHeight * cellHeight * Math.cos(lat * (Math.PI / 180));
}

function fromCellCoordinates(gridScale, coordinates) {
  return [(coordinates.x + 0.5) / gridScale, (coordinates.y + 0.5) / gridScale];
}

function calculateGradient(value, bottomBoundary, topBoundary) {
  let gradient = (value - bottomBoundary) / (topBoundary - bottomBoundary);
  return Math.max(0, Math.min(1, gradient));
}

function fillLayerToggle(layer) {
  switch (layer) {
    case Layer.Fire:
      return '🔥 Fire';
    case Layer.Fuel:
      return '🌳 Fuel';
    case Layer.Elevation:
      return '⛰️ Elevation';
    case Layer.WindSpeed:
      return '🌀 Wind speed';
  }
}

function titleAlgorithm(algorithm) {
  return algorithm.charAt(0).toUpperCase() + algorithm.slice(1);
}

function titleLayerToggle(layer) {
  return `Display ${layer} layer`;
}

function titleTimelineButton(steps, stepDurationMs) {
  return `${steps < 0 ? 'Rewind' : 'Advance'} simulation by ${describeTimePeriod(steps, stepDurationMs).slice(2)}`;
}

function describeTimePeriod(steps, stepDurationMs, verbose = true) {
  const sign = steps < 0 ? -1 : 1;
  steps *= sign;
  let duration = steps * stepDurationMs;
  let description = '';
  let sections = 0;
  [
    [verbose ? 'day' : 'd', 24 * 60],
    [verbose ? 'hour' : 'h', 60],
    [verbose ? 'minute' : 'm', 1],
  ].forEach((periodItem) => {
    const periodName = periodItem[0];
    const period = periodItem[1] * 60 * 1000;
    if (period <= duration || (sections === 0 && periodName[0] === 'm')) {
      const amount = Math.floor(duration / period);
      duration -= amount * period;
      sections += 1;
      description += ` ${amount}${verbose ? ' ' : ''}${periodName}${verbose && amount !== 1 ? 's' : ''}`;
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

function formatDate(date) {
  return `${date.getFullYear()}-${make2digit(date.getMonth() + 1)}-${make2digit(date.getDate())} ${make2digit(date.getHours())}:${make2digit(date.getMinutes())}`;
}

function make2digit(n) {
  return String(n).padStart(2, '0');
}
