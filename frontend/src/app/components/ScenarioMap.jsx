import React, { Component } from 'react';
import { Map, View } from 'ol';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Style, Fill, Stroke } from 'ol/style';
import Polygon from 'ol/geom/Polygon';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import Feature from 'ol/Feature';
import Control from 'ol/control/Control';

import {
  CELL_WIDTH,
  DATE_SHIFT_STEP,
  DATE_SHIFT_INTERVAL_LIMIT,
} from '../services/domain';
import {
  createScenario,
  removeScenario,
  predictScenario,
} from '../services/scenario';

const INITIAL_MAP_CENTER = [37.6173, 55.7558];
const INITIAL_MAP_ZOOM = 12;

export class ScenarioMap extends Component {
  constructor(props) {
    super(props);

    this.scenario = undefined;
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

    map.setView(
      new View({
        center: fromLonLat(INITIAL_MAP_CENTER),
        zoom: INITIAL_MAP_ZOOM,
      })
    );

    map.on('singleclick', (event) => {
      this.handleMapClick(event);
    });

    return map;
  }

  createScenarioControl() {
    const container = document.createElement('div');
    container.id = 'scenario-control-container';
    container.className = 'ol-unselectable ol-control';

    const scenarioPicker = document.createElement('button');
    scenarioPicker.className = 'scenario-control';
    scenarioPicker.innerHTML = '🔥';
    scenarioPicker.addEventListener('click', () => {
      this.setScenarioPickingMode(true);
    });
    container.appendChild(scenarioPicker);

    const dateBackShifter = document.createElement('button');
    dateBackShifter.className = 'scenario-control';
    dateBackShifter.innerHTML = '<';
    dateBackShifter.addEventListener('click', () => {
      this.shiftDate(-1);
    });
    container.appendChild(dateBackShifter);

    const dateForwardShifter = document.createElement('button');
    dateForwardShifter.className = 'scenario-control';
    dateForwardShifter.innerHTML = '>';
    dateForwardShifter.addEventListener('click', () => {
      this.shiftDate(1);
    });
    container.appendChild(dateForwardShifter);

    return new Control({ element: container });
  }

  handleMapClick(event) {
    if (!this.isScenarioPickingMode) {
      return;
    }
    if (this.scenario !== undefined) {
      removeScenario(this.scenario);
    }
    this.setScenarioPickingMode(false);
    this.scenario = createScenario(toLonLat(event.coordinate), Date.now());
    this.shiftDate(0);
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

  shiftDate(steps) {
    const desiredDate = this.scenario.currentDate + steps * DATE_SHIFT_STEP;
    if (
      desiredDate < this.scenario.startDate ||
      this.scenario.startDate + DATE_SHIFT_INTERVAL_LIMIT < desiredDate
    ) {
      return;
    }
    this.scenario.currentDate = desiredDate;
    this.displayPrediction(predictScenario(this.scenario));
  }

  createCell(coordinates) {
    const x = coordinates[0],
      y = coordinates[1];
    const center = this.scenario.startPoint;

    const leftEdgeLon = center[0] + (x - 0.5) * CELL_WIDTH;
    const rightEdgeLon = center[0] + (x + 0.5) * CELL_WIDTH;
    const topEdgeLat = center[1] + (y - 0.5) * CELL_WIDTH;
    const bottomEdgeLat = center[1] + (y + 0.5) * CELL_WIDTH;

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

  displayPrediction(prediction) {
    this.scenarioLayerSource.clear();

    const startCellStyle = new Style({
      fill: new Fill({ color: 'rgba(255, 0, 0, 0)' }),
      stroke: new Stroke({ color: 'red', width: 2 }),
    });
    const startCellFeature = new Feature({
      geometry: this.createCell([0, 0]),
    });
    startCellFeature.setStyle(startCellStyle);

    this.scenarioLayerSource.addFeature(startCellFeature);

    for (const cell of prediction.fireCells) {
      const style = new Style({
        fill: new Fill({ color: 'rgba(255, 0, 0, 0.5)' }),
      });
      const feature = new Feature({
        geometry: this.createCell(cell.coordinates),
      });
      feature.setStyle(style);

      this.scenarioLayerSource.addFeature(feature);
    }
  }
}
