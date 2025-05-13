import { createRoot } from "react-dom/client";

import Instruction from "./InstructionComponent";

import { Indicator, Layer } from "../models/Enumerations";
import { capitalizeText } from "../models/Presentation";

class MapControl {
  constructor(containerId, title, isCoreControl = false) {
    this.container = createControlContainer(containerId, isCoreControl);
    this.header = createControlHeader(title);
    this.body = createControlBody();
    this.container.append(this.header);
    this.container.append(this.body);
  }

  show() {
    this.container.hidden = false;
  }

  hide() {
    this.container.hidden = true;
  }

  append(element) {
    this.body.appendChild(element);
  }
}

const LAYER_NAMES = {
  [Layer.Fire]: "fire status",
  [Layer.Fuel]: "fuel density",
  [Layer.Elevation]: "elevation",
};

const LAYER_ICONS = {
  [Layer.Fire]: "🔥",
  [Layer.Fuel]: "🌳",
  [Layer.Elevation]: "⛰️",
};

const INDICATORS_WITH_ICONS = [
  [Indicator.AirTemperature, "🌡️"],
  [Indicator.AirHumidity, "💧"],
  [Indicator.WindSpeed, "🌀"],
  [Indicator.FuelDensity, "🌳"],
];

export const TICK_DELTAS = [-10, -1, 1, 10];

export function InfoControl() {
  const control = new MapControl("control-container-info", "Information");

  const textBlock = createContainer("", "text-block first");
  control.append(textBlock);

  textBlock.appendChild(createLabel("label-damaged-area", "first"));

  textBlock.appendChild(createLabel("", "", "Cell states:"));
  for (const stateName of ["burning", "igniting", "burned"]) {
    textBlock.appendChild(createLabel(getCellCounterId(stateName)));
  }

  const indicatorPanel = createContainer("indicator-panel");
  control.append(indicatorPanel);

  let isFirst = true;
  for (const [indicator, icon] of INDICATORS_WITH_ICONS) {
    const containerId = `indicator-container-${indicator}`;
    const containerClassName = "indicator inline" + getOrdinalClass(isFirst);
    const container = createContainer(containerId, containerClassName);
    container.appendChild(createLabel("", "", icon));
    container.appendChild(createLabel(`label-indicator-${indicator}`));
    if (indicator === Indicator.WindSpeed) {
      const icon = createContainer("wind-direction-icon", "", "➤");
      container.appendChild(icon);
    }
    indicatorPanel.appendChild(container);
    isFirst = false;
  }

  return control;
}

export function LayerControl(tools) {
  const control = new MapControl("control-container-layer", "Layers");

  let isFirst = true;
  for (const layer of Object.values(Layer)) {
    const toggle = createButton(
      () => tools.switchLayer(layer),
      getLayerToggleId(layer),
      "off" + getOrdinalClass(isFirst),
      `${LAYER_ICONS[layer]} ${capitalizeText(LAYER_NAMES[layer])}`,
      `Display ${LAYER_NAMES[layer]} layer`,
    );
    control.append(toggle);
    isFirst = false;
  }

  return control;
}

export function SimulationControl(tools) {
  const control = new MapControl(
    "control-container-simulation",
    "Simulation",
    true,
  );

  const instructionOpener = createButton(
    tools.openInstruction,
    "",
    "inline first",
    "ℹ️",
    "Open instruction",
  );
  control.append(instructionOpener);

  const lighter = createButton(
    tools.switchLighter,
    "lighter",
    "inline",
    "🔥",
    "Set ignition point",
  );
  control.append(lighter);

  return control;
}

export function TimelineControl(tools) {
  const spacing = "&nbsp;".repeat(5);
  const title = `<<${spacing}Timeline${spacing}>>`;
  const control = new MapControl("control-container-timeline", title);

  let isFirst = true;
  for (const tickDelta of TICK_DELTAS) {
    const tickShifter = createButton(
      () => tools.navigateTimeline(tickDelta),
      getTickShifterId(tickDelta),
      "inline" + getOrdinalClass(isFirst),
    );
    control.append(tickShifter);
    isFirst = false;
  }

  const textBlock = createContainer("", "text-block");
  control.append(textBlock);

  textBlock.appendChild(createLabel("label-start-date"));
  textBlock.appendChild(createLabel("label-period"));
  textBlock.appendChild(createLabel("label-simulated-date"));

  return control;
}

export function InstructionControl() {
  const control = createContainer("control-container-instruction");

  const container = createContainer("instruction-container");
  control.appendChild(container);
  const root = createRoot(container);
  root.render(Instruction());

  const instructionCloser = createButton(
    () => {
      document.getElementById("control-container-instruction").hidden = true;
    },
    "instruction-closer",
    "",
    "❌",
    "Close instruction",
  );
  control.append(instructionCloser);

  control.hidden = true;

  return control;
}

export function getCellCounterId(stateName) {
  return `cell-counter-${stateName}`;
}

export function getLayerToggleId(layer) {
  return `layer-toggle-${layer}`;
}

export function getTickShifterId(tickDelta) {
  const direction = tickDelta < 0 ? "backward" : "forward";
  return `timeline-navigator-${direction}-${Math.abs(tickDelta)}`;
}

function createButton(
  handler,
  id = "",
  className = "",
  content = "",
  title = "",
) {
  const button = createElement("button", id, className, content, title);
  button.addEventListener("click", handler);
  return button;
}

function createContainer(id = "", className = "", content = "", title = "") {
  return createElement("div", id, className, content, title);
}

function createLabel(id = "", className = "", content = "", title = "") {
  return createElement("label", id, className, content, title);
}

function createElement(
  elementType,
  id = "",
  className = "",
  content = "",
  title = "",
) {
  const element = document.createElement(elementType);
  if (id !== "") {
    element.id = id;
  }
  if (className !== "") {
    element.className = className;
  }
  if (content !== "") {
    element.innerHTML = content;
  }
  if (title !== "") {
    element.title = title;
  }
  return element;
}

function createControlContainer(containerId, isCoreControl) {
  const containerClassName = "ol-unselectable ol-control control-container";
  const container = createContainer(containerId, containerClassName);
  if (!isCoreControl) {
    container.hidden = true;
  }
  return container;
}

function createControlHeader(title) {
  return createLabel("", "control-header", title);
}

function createControlBody() {
  return createContainer("", "control-body");
}

function getOrdinalClass(isFirst) {
  return isFirst ? " first" : "";
}
