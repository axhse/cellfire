import React, { Component } from "react";

import { toLonLat } from "ol/proj";
import { Fill, Stroke, Style } from "ol/style";

import { MapModel } from "./MapModel";
import { LayerGradients, START_RECTANGLE_STROKE_COLOR } from "./MapTheme";
import { MapToolbar } from "./MapToolbar";
import { Layer, PointerMode } from "../models/Enumerations";
import { SIGNIFICANT_OVERHEAT } from "../models/Simulation";
import { SIMULATOR } from "../services/Registry";

export default class MapComponent extends Component {
  constructor() {
    super();
    this.mapContainerRef = React.createRef();
    this.model = MapModel(this.createTools());
    this.toolbar = new MapToolbar(this.model.runtimeControls);
    this.simulation = undefined;
  }

  componentDidMount() {
    this.model.olMap.setTarget(this.mapContainerRef.current);
    this.toolbar.initialize();
  }

  render() {
    return <div id="map-container" ref={this.mapContainerRef}></div>;
  }

  createTools() {
    return {
      handleMapClick: (event) => this.handleMapClick(event),
      openInstruction: () => this.openInstruction(),
      switchLayer: (layer) => this.switchLayer(layer),
      switchLighter: () => this.switchLighter(),
      navigateTimeline: (layer) => this.navigateTimeline(layer),
    };
  }

  switchLighter() {
    const isRegular = this.toolbar.pointerMode === PointerMode.Regular;
    const newMode = isRegular ? PointerMode.Lighter : PointerMode.Regular;
    this.toolbar.setPointerMode(newMode);
  }

  openInstruction() {
    document.getElementById("control-container-instruction").hidden = false;
  }

  async handleMapClick(event) {
    if (this.toolbar.pointerMode !== PointerMode.Lighter) {
      return;
    }
    if (this.simulation !== undefined) {
      SIMULATOR.removeSimulation(this.simulation);
    }
    this.toolbar.setPointerMode(PointerMode.Regular);
    this.simulation = await SIMULATOR.createSimulation(
      toLonLat(event.coordinate),
    );
    if (this.simulation === undefined) {
      this.model.vectorSource.clear();
      this.toolbar.exitSimulation();
    } else {
      this.updateMap();
      this.toolbar.enterSimulation(this.simulation);
    }
  }

  async navigateTimeline(steps) {
    const newStep = this.simulation.timeline.navigate(steps);
    if (await SIMULATOR.progressSimulation(this.simulation, newStep)) {
      this.updateMap();
    } else {
      this.simulation = undefined;
      this.model.vectorSource.clear();
      this.toolbar.exitSimulation();
    }
  }

  updateMap() {
    this.toolbar.updateControls(this.simulation);
    this.updateVectorLayer();
  }

  updateVectorLayer() {
    this.model.vectorSource.clear();

    for (const cell of this.simulation.getSimulatedCells()) {
      const significantHeat =
        cell.factors.airTemperature + SIGNIFICANT_OVERHEAT;
      if (!cell.state.damaged && cell.state.heat < significantHeat) {
        continue;
      }
      const rectangle = this.simulation.grid.createRectangle(cell.coordinates);
      const style = this.getRectangleStyle(cell);
      rectangle.setStyle(style);
      this.model.vectorSource.addFeature(rectangle);
    }

    const startRectangle = this.simulation.grid.createStartRectangle();
    startRectangle.setStyle(getStartRectangleStyle());
    this.model.vectorSource.addFeature(startRectangle);
  }

  switchLayer(layer) {
    this.toolbar.selectLayer(layer);
    this.updateMap();
  }

  getRectangleStyle(cell) {
    const gradients = new LayerGradients();
    let color;
    if (this.toolbar.layer === Layer.Fire) {
      const heat = cell.state.heat;
      let gradient = gradients.burning;
      if (!cell.burning) {
        gradient = cell.state.damaged ? gradients.burned : gradients.intact;
      }
      color = gradient.forValue(heat);
    }
    if (this.toolbar.layer === Layer.Fuel) {
      color = gradients.fuel.forValue(cell.state.fuel);
    }
    if (this.toolbar.layer === Layer.Elevation) {
      color = gradients.elevation.forValue(cell.factors.elevation);
    }

    return new Style({ fill: new Fill({ color: color.css() }) });
  }
}

function getStartRectangleStyle() {
  const color = START_RECTANGLE_STROKE_COLOR.rgba();
  return new Style({ stroke: new Stroke({ color, width: 3 }) });
}
