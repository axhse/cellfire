import React, { Component } from 'react';
import { Map, View } from 'ol';
import Polygon from 'ol/geom/Polygon';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Fill, Style } from 'ol/style';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Control from 'ol/control/Control';

import {
  SCALE_FACTOR,
  FORECAST_STEP,
  MAX_FORECAST_PERIOD,
} from '../domain/definitions';

import { toCellCoordinates } from '../domain/logic';
import { scenarioService } from '../services/registry';

// const INITIAL_MAP_CENTER = [37.6173, 55.7558];
const INITIAL_MAP_CENTER = [49, 37.5];
const INITIAL_MAP_ZOOM = 12;

const LAYER_OPACITY = 0.5;
const LAYER_PARAMS = {
  fire: {
    zeroTemperature: 0,
    plantColor: [0, 255, 0, LAYER_OPACITY],
    coalColor: [50, 50, 50, LAYER_OPACITY],
    ignitionColor: [255, 255, 0, LAYER_OPACITY],
    flameTemperature: 1000,
    flameColor: [255, 0, 0, LAYER_OPACITY],
  },
  fuel: {
    minAmount: 0,
    maxAmount: 1,
    minColor: [127, 127, 127, LAYER_OPACITY],
    maxColor: [255, 0, 255, LAYER_OPACITY],
  },
};

export class ScenarioMap extends Component {
  constructor(props) {
    super(props);

    this.scenario = undefined;
    this.forecast = undefined;
    this.layerName = 'fire';
    this.isScenarioPickingMode = false;

    this.scenarioLayerSource = new VectorSource();
    this.map = this.createMap();

    this.mapContainerRef = React.createRef();
  }

  componentDidMount() {
    this.map.setTarget(this.mapContainerRef.current);
  }

  render() {
    return <div id='map-container' ref={this.mapContainerRef}></div>;
  }

  createMap() {
    const map = new Map();

    map.addLayer(new TileLayer({ source: new OSM() }));
    map.addLayer(new VectorLayer({ source: this.scenarioLayerSource }));
    map.addControl(this.createScenarioControl());
    map.addControl(this.createLayerControl());

    map.setView(
      new View({
        center: fromLonLat(INITIAL_MAP_CENTER),
        zoom: INITIAL_MAP_ZOOM,
      })
    );

    map.on('singleclick', (event) => this.handleMapClick(event));

    return map;
  }

  createScenarioControl() {
    const container = document.createElement('div');
    container.id = 'scenario-control-container';
    container.className = 'ol-unselectable ol-control';

    const scenarioPicker = document.createElement('button');
    scenarioPicker.className = 'control-inline-button';
    scenarioPicker.innerHTML = '🔥';
    scenarioPicker.addEventListener('click', () => {
      this.setScenarioPickingMode(true);
    });
    container.appendChild(scenarioPicker);

    const dateBackFastShifter = document.createElement('button');
    dateBackFastShifter.className = 'control-inline-button';
    dateBackFastShifter.innerHTML = '<<';
    dateBackFastShifter.addEventListener('click', () => this.shiftDate(-10));
    container.appendChild(dateBackFastShifter);

    const dateBackShifter = document.createElement('button');
    dateBackShifter.className = 'control-inline-button';
    dateBackShifter.innerHTML = '<';
    dateBackShifter.addEventListener('click', () => this.shiftDate(-1));
    container.appendChild(dateBackShifter);

    const dateForwardShifter = document.createElement('button');
    dateForwardShifter.className = 'control-inline-button';
    dateForwardShifter.innerHTML = '>';
    dateForwardShifter.addEventListener('click', () => this.shiftDate(1));
    container.appendChild(dateForwardShifter);

    const dateForwardFastShifter = document.createElement('button');
    dateForwardFastShifter.className = 'control-inline-button';
    dateForwardFastShifter.innerHTML = '>>';
    dateForwardFastShifter.addEventListener('click', () => this.shiftDate(10));
    container.appendChild(dateForwardFastShifter);

    return new Control({ element: container });
  }

  createLayerControl() {
    const container = document.createElement('div');
    container.id = 'layer-control-container';
    container.className = 'ol-unselectable ol-control';

    const fireLayerSwitch = document.createElement('button');
    fireLayerSwitch.innerHTML = '🔥';
    fireLayerSwitch.addEventListener('click', () => {
      this.switchLayer('fire');
    });
    container.appendChild(fireLayerSwitch);

    const fuelLayerSwitch = document.createElement('button');
    fuelLayerSwitch.innerHTML = '⛽';
    fuelLayerSwitch.addEventListener('click', () => {
      this.switchLayer('fuel');
    });
    container.appendChild(fuelLayerSwitch);

    return new Control({ element: container });
  }

  setScenarioPickingMode(isPickingModeDesired) {
    if (isPickingModeDesired === this.isScenarioPickingMode) {
      return;
    }
    this.isScenarioPickingMode = isPickingModeDesired;
    if (isPickingModeDesired) {
      this.mapContainerRef.current.classList.add('scenario-picking-mode');
    } else {
      this.mapContainerRef.current.classList.remove('scenario-picking-mode');
    }
  }

  async handleMapClick(event) {
    if (!this.isScenarioPickingMode) {
      return;
    }
    if (this.scenario !== undefined) {
      scenarioService.removeScenario(this.scenario);
    }
    this.setScenarioPickingMode(false);
    const startCoordinates = toCellCoordinates(toLonLat(event.coordinate));
    this.scenario = await scenarioService.createScenario(
      startCoordinates,
      Date.now()
    );
    await this.shiftDate(0);
  }

  async shiftDate(steps) {
    if (this.scenario === undefined) {
      return;
    }
    let desiredDate = this.scenario.actualDate + steps * FORECAST_STEP;
    if (desiredDate < this.scenario.startDate) {
      desiredDate = this.scenario.startDate;
    } else if (this.scenario.startDate + MAX_FORECAST_PERIOD < desiredDate) {
      desiredDate = this.scenario.startDate + MAX_FORECAST_PERIOD;
    }
    this.scenario.actualDate = desiredDate;
    this.forecast = await scenarioService.forecastScenario(this.scenario);
    this.displayForecast();
  }

  switchLayer(layerName) {
    if (this.scenario === undefined) {
      return;
    }
    this.layerName = layerName;
    this.displayForecast();
  }

  displayForecast() {
    this.scenarioLayerSource.clear();

    //     const startCellFeature = new Feature({
    //       geometry: createCellFigure(this.scenario.startCellCoordinates, 0, 0),
    //     });
    //     const startCellStyle = new Style({
    //       stroke: new Stroke({ color: 'red', width: 2 }),
    //     });
    //     startCellFeature.setStyle(startCellStyle);
    //     this.scenarioLayerSource.addFeature(startCellFeature);

    for (const cell of this.forecast.cells) {
      const feature = new Feature({
        geometry: createCellFigure(cell.coordinates),
      });
      const style = new Style({ fill: this.createCellFill(cell) });
      feature.setStyle(style);

      this.scenarioLayerSource.addFeature(feature);
    }
  }

  createCellFill(cell) {
    let value;
    let bottomBoundary;
    let bottomColor;
    let topBoundary;
    let topColor;
    if (this.layerName === 'fire') {
      value = cell.fire.heat;
      if (value > cell.environment.ignitionTemperature) {
        bottomBoundary = cell.environment.ignitionTemperature;
        bottomColor = LAYER_PARAMS.fire.ignitionColor;
        topBoundary = LAYER_PARAMS.fire.flameTemperature;
        topColor = LAYER_PARAMS.fire.flameColor;
      } else {
        bottomBoundary = LAYER_PARAMS.fire.zeroTemperature;
        topBoundary = cell.environment.ignitionTemperature;
        topColor = LAYER_PARAMS.fire.ignitionColor;

        if (cell.fire.resource < cell.fire.initialResource) {
          bottomColor = LAYER_PARAMS.fire.coalColor;
        } else {
          bottomColor = LAYER_PARAMS.fire.plantColor;
        }
      }
    }
    if (this.layerName === 'fuel') {
      value = cell.fire.resource;
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
