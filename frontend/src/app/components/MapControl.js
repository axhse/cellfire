import { Indicator, Layer } from '../models/Enumerations';
import { capitalizeText } from '../models/Presentation';

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
  [Layer.Fire]: 'fire status',
  [Layer.Fuel]: 'fuel density',
  [Layer.Elevation]: 'elevation',
  [Layer.Wind]: 'wind speed',
};

const LAYER_ICONS = {
  [Layer.Fire]: '🔥',
  [Layer.Fuel]: '🌳',
  [Layer.Elevation]: '⛰️',
  [Layer.Wind]: '🌀',
};

const INDICATORS_WITH_ICONS = [
  [Indicator.AirTemperature, '🌡️'],
  [Indicator.AirHumidity, '💧'],
  [Indicator.WindSpeed, '🌀'],
  [Indicator.FuelDensity, '🌳'],
];

export function InfoControl() {
  const control = new MapControl('control-container-info', 'Information');

  control.append(createLabel('label-active-algorithm'));
  control.append(createLabel('label-damaged-area'));

  for (const [indicator, icon] of INDICATORS_WITH_ICONS) {
    const containerId = `container-indicator-${indicator}`;
    const container = createContainer(containerId, 'container-indicator');
    container.appendChild(createLabel('', '', icon));
    container.appendChild(createLabel(`label-indicator-${indicator}`));
    control.append(container);
  }

  return control;
}

export function LayerControl(tools) {
  const control = new MapControl('control-container-layer', 'Layers');

  for (const layer of Object.values(Layer)) {
    const toggle = createButton(
      () => tools.switchLayer(layer),
      getLayerToggleId(layer),
      'control-inline-button layer-toggle off',
      `${LAYER_ICONS[layer]} ${capitalizeText(LAYER_NAMES[layer])}`,
      `Display ${LAYER_NAMES[layer]} layer`
    );
    control.append(toggle);
  }

  return control;
}

export function SimulationControl(tools) {
  const control = new MapControl(
    'control-container-simulation',
    'Simulation',
    true
  );

  const algorithmSwitch = createButton(
    tools.switchAlgorithm,
    'algorithm-switch',
    'control-inline-button',
    '',
    'Switch algorithm'
  );
  control.append(algorithmSwitch);

  const lighter = createButton(
    tools.switchLighter,
    '',
    'control-inline-button lighter',
    '🔥',
    'Set ignition point'
  );
  control.append(lighter);

  return control;
}

export function TimelineControl(tools) {
  const spacing = '&nbsp;'.repeat(5);
  const title = `<<${spacing}Timeline${spacing}>>`;
  const control = new MapControl('control-container-timeline', title);

  for (const deltaTicks of [-10, -1, 1, 10]) {
    const tickShifter = createButton(
      () => tools.navigateTimeline(deltaTicks),
      getTickShifterId(deltaTicks),
      'control-inline-button timeline'
    );
    control.append(tickShifter);
  }

  control.append(createLabel('label-timeline-start-date'));
  control.append(createLabel('label-timeline-period'));
  control.append(createLabel('label-timeline-current-date'));

  return control;
}

export function getLayerToggleId(layer) {
  return `layer-toggle-${layer}`;
}

export function getTickShifterId(deltaTicks) {
  const direction = deltaTicks < 0 ? 'backward' : 'forward';
  return `timeline-navigator-${direction}-${Math.abs(deltaTicks)}`;
}

function createButton(
  handler,
  id = '',
  className = '',
  content = '',
  title = ''
) {
  const button = createElement('button', id, className, content, title);
  button.addEventListener('click', handler);
  return button;
}

function createContainer(id = '', className = '', title = '') {
  return createElement('div', id, className, '', title);
}

function createLabel(id = '', className = '', content = '', title = '') {
  return createElement('label', id, className, content, title);
}

function createElement(
  elementType,
  id = '',
  className = '',
  content = '',
  title = ''
) {
  const element = document.createElement(elementType);
  if (id !== '') {
    element.id = id;
  }
  if (className !== '') {
    element.className = className;
  }
  if (content !== '') {
    element.innerHTML = content;
  }
  if (title !== '') {
    element.title = title;
  }
  return element;
}

function createControlContainer(containerId, isCoreControl) {
  const containerClassName = 'ol-unselectable ol-control map-control-container';
  const container = createContainer(containerId, containerClassName);
  if (!isCoreControl) {
    container.hidden = true;
  }
  return container;
}

function createControlHeader(title) {
  return createLabel('', 'map-control-header', title);
}

function createControlBody() {
  return createContainer('', 'map-control-body');
}
