import { Indicator } from '../models/Enumerations';

class Color {
  constructor(r, g, b, a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  rgba() {
    return [this.r, this.g, this.b, this.a];
  }

  css() {
    return `rgba(${this.rgba()})`;
  }
}

const INDICATOR_TEXT_OPACITY = 1;
const INDICATOR_BACKGROUND_OPACITY = 0.5;

class IndicatorTextColor extends Color {
  constructor(r, g, b) {
    super(r, g, b, INDICATOR_TEXT_OPACITY);
  }
}

class IndicatorBackgroundColor extends Color {
  constructor(r, g, b) {
    super(r, g, b, INDICATOR_BACKGROUND_OPACITY);
  }
}

const INDICATOR_COLORS = {
  text: {
    min: new IndicatorTextColor(0, 100, 0),
    max: new IndicatorTextColor(100, 0, 0),
  },
  background: {
    min: new IndicatorBackgroundColor(0, 255, 0),
    max: new IndicatorBackgroundColor(255, 0, 0),
  },
};

const LAYER_OPACITY = 0.6;

class LayerColor extends Color {
  constructor(r, g, b, a = LAYER_OPACITY) {
    super(r, g, b, a);
  }
}

export const START_RECTANGLE_STROKE_COLOR = new Color(255, 0, 0, 0.3);

const WEAK_FLAME_COLOR = new LayerColor(255, 180, 0);
const LAYER_COLORS = {
  intact: {
    min: new LayerColor(0, 255, 0),
    max: WEAK_FLAME_COLOR,
  },
  burning: {
    min: WEAK_FLAME_COLOR,
    max: new LayerColor(255, 0, 0),
  },
  burned: {
    min: new LayerColor(0, 0, 0, LAYER_OPACITY / 1.5),
    max: WEAK_FLAME_COLOR,
  },
  fuel: {
    min: new LayerColor(200, 0, 255, 0.05),
    max: new LayerColor(150, 0, 120),
  },
  elevation: {
    min: new LayerColor(0, 255, 200),
    max: new LayerColor(255, 0, 0),
  },
  wind: {
    min: new LayerColor(0, 200, 255, 0.05),
    max: new LayerColor(255, 0, 0),
  },
};

class Gradient {
  constructor(minValue, maxValue, colors) {
    this.baseValue = minValue;
    this.valueRange = maxValue - minValue;
    this.baseColor = colors.min;
    this.colorRange = new Color(
      ...[0, 1, 2, 3].map((i) => colors.max.rgba()[i] - colors.min.rgba()[i])
    );
  }

  forValue(value) {
    let gradient = (value - this.baseValue) / this.valueRange;
    gradient = Math.max(0, Math.min(1, gradient));
    let rgba = [0, 1, 2, 3].map(
      (i) => this.baseColor.rgba()[i] + this.colorRange.rgba()[i] * gradient
    );
    rgba = [...rgba.slice(0, 3).map(Math.round), rgba[3]];
    return new Color(...rgba);
  }
}

export class IndicatorGradient {
  constructor(minValue, maxValue) {
    this.textGradient = new Gradient(minValue, maxValue, INDICATOR_COLORS.text);
    this.backgroundGradient = new Gradient(
      minValue,
      maxValue,
      INDICATOR_COLORS.background
    );
  }

  textFor(value) {
    return this.textGradient.forValue(value);
  }

  backgroundFor(value) {
    return this.backgroundGradient.forValue(value);
  }
}

export const INDICATOR_GRADIENTS = {
  [Indicator.AirTemperature]: new IndicatorGradient(0, 40),
  [Indicator.AirHumidity]: new IndicatorGradient(100, 0),
  [Indicator.WindSpeed]: new IndicatorGradient(0, 10),
  [Indicator.FuelDensity]: new IndicatorGradient(0, 1),
};

export class LayerGradients {
  constructor(ignitionTemperature) {
    this.intact = new Gradient(0, ignitionTemperature, LAYER_COLORS.intact);
    this.burning = new Gradient(ignitionTemperature, 900, LAYER_COLORS.burning);
    this.burned = new Gradient(0, ignitionTemperature, LAYER_COLORS.burned);
    this.fuel = new Gradient(0, 1, LAYER_COLORS.fuel);
    this.elevation = new Gradient(0, 4500, LAYER_COLORS.elevation);
    this.wind = new Gradient(0, 10, LAYER_COLORS.wind);
  }
}
