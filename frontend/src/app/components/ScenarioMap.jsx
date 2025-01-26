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
  heat: {
    minValue: 0,
    maxValue: 200,
    minColor: [255, 255, 0],
    maxColor: [255, 0, 0],
  },
  resource: {
    minValue: 0,
    maxValue: 1,
    minColor: [127, 127, 127],
    maxColor: [255, 0, 255],
  },
};

export class ScenarioMap extends Component {
  constructor(props) {
    super(props);

    this.scenario = undefined;
    this.forecast = undefined;
    this.layerName = 'heat';
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

    const heatLayerSwitch = document.createElement('button');
    heatLayerSwitch.innerHTML = '🔥';
    heatLayerSwitch.addEventListener('click', () => {
      this.switchLayer('heat');
    });
    container.appendChild(heatLayerSwitch);

    const resourceLayerSwitch = document.createElement('button');
    resourceLayerSwitch.innerHTML = '⛽';
    resourceLayerSwitch.addEventListener('click', () => {
      this.switchLayer('resource');
    });
    container.appendChild(resourceLayerSwitch);

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
      let value = 0;
      if (this.layerName === 'heat') {
        value = cell.fire.heat;
      }
      if (this.layerName === 'resource') {
        value = cell.fire.resource;
      }
      const feature = new Feature({
        geometry: createCellFigure(cell.coordinates),
      });
      const style = new Style({ fill: createCellFill(value, this.layerName) });
      feature.setStyle(style);

      this.scenarioLayerSource.addFeature(feature);
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

function createCellFill(value, layerName) {
  const params = LAYER_PARAMS[layerName];
  const gradient = calculateLinearGradient(
    value,
    params.minValue,
    params.maxValue
  );
  const color = [0, 1, 2].map(
    (index) =>
      params.minColor[index] +
      Math.round((params.maxColor[index] - params.minColor[index]) * gradient)
  );

  return new Fill({ color: `rgba(${color},${LAYER_OPACITY})` });
}

function calculateLinearGradient(value, minValue, maxValue) {
  let gradient = (value - minValue) / (maxValue - minValue);
  if (gradient < 0) {
    gradient = 0;
  }
  if (gradient > 1) {
    gradient = 1;
  }
  return gradient;
}
