import { Indicator } from "../models/Enumerations";

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
    min: new LayerColor(0, 200, 0),
    max: WEAK_FLAME_COLOR,
  },
  burning: {
    min: WEAK_FLAME_COLOR,
    max: new LayerColor(200, 0, 0),
  },
  burned: {
    min: new LayerColor(100, 100, 80, LAYER_OPACITY),
    max: WEAK_FLAME_COLOR,
  },
  fuel: {
    min: new LayerColor(200, 0, 255, 0.05),
    max: new LayerColor(200, 0, 150),
  },
  elevation: {
    seaLevel: new LayerColor(0, 100, 0),
    hill: new LayerColor(220, 180, 0),
    mountain: new LayerColor(150, 0, 0),
    peak: new LayerColor(50, 50, 50),
  },
};

class Gradient {
  constructor(minValue, maxValue, minColor, maxColor) {
    this.baseValue = minValue;
    this.valueRange = maxValue - minValue;
    this.maxValue = maxValue;
    this.baseColor = minColor;
    this.colorRange = new Color(
      ...[0, 1, 2, 3].map((i) => maxColor.rgba()[i] - minColor.rgba()[i]),
    );
  }

  forValue(value) {
    let gradient = (value - this.baseValue) / this.valueRange;
    gradient = Math.max(0, Math.min(1, gradient));
    let rgba = [0, 1, 2, 3].map(
      (i) => this.baseColor.rgba()[i] + this.colorRange.rgba()[i] * gradient,
    );
    rgba = [...rgba.slice(0, 3).map(Math.round), rgba[3]];
    return new Color(...rgba);
  }
}

function gradientOf(minValue, maxValue, colors) {
  return new Gradient(minValue, maxValue, colors.min, colors.max);
}

class ElevationGradient {
  constructor() {
    this.gradients = [
      new Gradient(
        0,
        2000,
        LAYER_COLORS.elevation.seaLevel,
        LAYER_COLORS.elevation.hill,
      ),
      new Gradient(
        2000,
        4000,
        LAYER_COLORS.elevation.hill,
        LAYER_COLORS.elevation.mountain,
      ),
      new Gradient(
        4000,
        6400,
        LAYER_COLORS.elevation.mountain,
        LAYER_COLORS.elevation.peak,
      ),
    ];
  }

  forValue(value) {
    if (value < this.gradients[0].baseValue) {
      return this.gradients[0].forValue(value);
    }
    if (value > this.gradients[this.gradients.length - 1].maxValue) {
      return this.gradients[this.gradients.length - 1].forValue(value);
    }
    for (const gradient of this.gradients) {
      if (gradient.baseValue <= value && value <= gradient.maxValue) {
        return gradient.forValue(value);
      }
    }
    return new Color(255, 0, 255, 1);
  }
}

export class IndicatorGradient {
  constructor(minValue, maxValue) {
    this.textGradient = gradientOf(minValue, maxValue, INDICATOR_COLORS.text);
    this.backgroundGradient = gradientOf(
      minValue,
      maxValue,
      INDICATOR_COLORS.background,
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

const IGNITION_TEMPERATURE = 500;
export class LayerGradients {
  constructor() {
    this.intact = gradientOf(0, IGNITION_TEMPERATURE, LAYER_COLORS.intact);
    this.burning = gradientOf(IGNITION_TEMPERATURE, 900, LAYER_COLORS.burning);
    this.burned = gradientOf(0, IGNITION_TEMPERATURE, LAYER_COLORS.burned);
    this.fuel = gradientOf(0, 1, LAYER_COLORS.fuel);
    this.elevation = new ElevationGradient();
  }
}
