import React, { Component } from 'react';
import { Map, View } from 'ol';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Style, Stroke } from 'ol/style';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Control from 'ol/control/Control';

import { FORECAST_STEP, MAX_FORECAST_PERIOD } from '../services/domain';
import { createCellFigure, createCellFill } from '../services/layer';
import {
  createScenario,
  removeScenario,
  forecastScenario,
} from '../services/scenario';

const INITIAL_MAP_CENTER = [37.6173, 55.7558];
const INITIAL_MAP_ZOOM = 12;

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

  async handleMapClick(event) {
    if (!this.isScenarioPickingMode) {
      return;
    }
    if (this.scenario !== undefined) {
      removeScenario(this.scenario);
    }
    this.setScenarioPickingMode(false);
    this.scenario = await createScenario(
      toLonLat(event.coordinate),
      Date.now()
    );
    await this.shiftDate(0);
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

  async shiftDate(steps) {
    const desiredDate = this.scenario.actualDate + steps * FORECAST_STEP;
    if (
      desiredDate < this.scenario.startDate ||
      this.scenario.startDate + MAX_FORECAST_PERIOD < desiredDate
    ) {
      return;
    }
    this.scenario.actualDate = desiredDate;
    this.forecast = await forecastScenario(this.scenario);
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

    const startCellFeature = new Feature({
      geometry: createCellFigure(this.scenario.startPoint, 0, 0),
    });
    const startCellStyle = new Style({
      stroke: new Stroke({ color: 'red', width: 2 }),
    });
    startCellFeature.setStyle(startCellStyle);
    this.scenarioLayerSource.addFeature(startCellFeature);

    for (const cell of this.forecast.cells) {
      let value = 0;
      if (this.layerName === 'fire') {
        value = cell.fireCell.heat;
      }
      if (this.layerName === 'fuel') {
        value = cell.fuelCell.capacity;
      }
      const feature = new Feature({
        geometry: createCellFigure(this.scenario.startPoint, cell.x, cell.y),
      });
      const style = new Style({
        fill: createCellFill(value, this.layerName),
      });
      feature.setStyle(style);

      this.scenarioLayerSource.addFeature(feature);
    }
  }
}
