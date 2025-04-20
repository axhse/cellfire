import { Map as OLMap, View } from "ol";
import TileLayer from "ol/layer/Tile";
import OSM from "ol/source/OSM";
import { fromLonLat } from "ol/proj";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import Control from "ol/control/Control";

import {
  InfoControl,
  InstructionControl,
  LayerControl,
  SimulationControl,
  TimelineControl,
} from "./MapControl";

const INITIAL_MAP_CENTER = [20, 45];
const INITIAL_MAP_ZOOM = 5;

export function MapModel(tools) {
  const olMap = new OLMap();
  const vectorSource = new VectorSource();

  olMap.addLayer(new TileLayer({ source: new OSM() }));
  olMap.addLayer(new VectorLayer({ source: vectorSource }));

  const coreControls = [SimulationControl(tools)];
  const runtimeControls = [
    InfoControl(),
    LayerControl(tools),
    TimelineControl(tools),
  ];
  for (const control of [...coreControls, ...runtimeControls]) {
    olMap.addControl(new Control({ element: control.container }));
  }
  olMap.addControl(new Control({ element: InstructionControl() }));

  const center = fromLonLat(INITIAL_MAP_CENTER);
  olMap.setView(new View({ center, zoom: INITIAL_MAP_ZOOM }));

  olMap.on("singleclick", tools.handleMapClick);

  return { olMap, vectorSource, coreControls, runtimeControls };
}
