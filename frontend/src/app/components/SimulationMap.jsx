import React, { Component } from 'react';
import { View } from 'ol';
import Polygon from 'ol/geom/Polygon';
import TileLayer from 'ol/layer/Tile';
import OSM from 'ol/source/OSM';
import { fromLonLat, toLonLat } from 'ol/proj';
import { Fill, Stroke, Style } from 'ol/style';
import VectorLayer from 'ol/layer/Vector';
import Feature from 'ol/Feature';
import Control from 'ol/control/Control';

import { LayerGradient } from './MapColoring';
import { SimulationMapControls } from './SimulationMapControls';

import { getWindSpeed } from '../models/domainLogic';
import { Layer, PointerMode } from '../models/mapState';
import {
  titleAlgorithm,
  titleLayerToggle,
  fillLayerToggle,
} from '../models/presentation';

import { simulator } from '../services/registry';

const SIGNIFICANT_OVERHEAT = 30;

const INITIAL_MAP_CENTER = [49, 37.5];
const INITIAL_MAP_ZOOM = 12;

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
    header.className = 'header';
    header.innerHTML = 'Algorithm';
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
    header.className = 'header';
    header.innerHTML =
      '<<&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Timeline&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;>>';
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
    header.className = 'header';
    header.innerHTML = 'Information';
    container.appendChild(header);

    const activeAlgorithmLabel = document.createElement('label');
    activeAlgorithmLabel.id = 'label-active-algorithm';
    container.appendChild(activeAlgorithmLabel);

    const damagedAreaLabel = document.createElement('label');
    damagedAreaLabel.id = 'label-damaged-area';
    container.appendChild(damagedAreaLabel);

    const indicatorItems = [
      ['air-temperature', '🌡️'],
      ['air-humidity', '💧'],
      ['wind-speed', '🌀'],
      ['fuel-density', '🌳'],
    ];
    for (const [indicatorName, indicatorIcon] of indicatorItems) {
      const indicatorContainer = document.createElement('div');
      indicatorContainer.className = 'indicator-container';
      indicatorContainer.id = `indicator-container-${indicatorName}`;
      container.appendChild(indicatorContainer);

      const indicatorIconLabel = document.createElement('label');
      indicatorIconLabel.innerHTML = indicatorIcon;
      indicatorContainer.appendChild(indicatorIconLabel);

      const indicatorLabel = document.createElement('label');
      indicatorLabel.id = `label-indicator-${indicatorName}`;
      indicatorContainer.appendChild(indicatorLabel);
    }

    return new Control({ element: container });
  }

  createLayerControl() {
    const container = document.createElement('div');
    container.id = 'control-container-layer';
    container.className = 'ol-unselectable ol-control hidden';

    const header = document.createElement('label');
    header.className = 'header';
    header.innerHTML = 'Layers';
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
    const ignitionTemperature = this.simulation.conditions.ignitionTemperature;
    const gradient = new LayerGradient(ignitionTemperature);
    let color;
    if (this.controls.layer === Layer.Fire) {
      const heat = cell.state.heat;
      let fireGradient = gradient.burning;
      if (heat < ignitionTemperature) {
        fireGradient = cell.state.damaged ? gradient.burned : gradient.intact;
      }
      color = fireGradient.for(heat);
    }
    if (this.controls.layer === Layer.Fuel) {
      color = gradient.fuel.for(cell.state.fuel);
    }
    if (this.controls.layer === Layer.Elevation) {
      color = gradient.elevation.for(cell.weather.elevation);
    }
    if (this.controls.layer === Layer.Wind) {
      color = gradient.wind.for(getWindSpeed(cell.weather));
    }

    return new Fill({ color: color.css() });
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
