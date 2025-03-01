class Color {
  constructor(r, g, b, a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  css() {
    return `rgba(${[this.r, this.g, this.b, this.a]})`;
  }
}

const INDICATOR_TEXT_OPACITY = 0.8;
const INDICATOR_BACKGROUND_OPACITY = 0.3;

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
  bg: {
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
    min: new LayerColor(0, 0, 0, LAYER_OPACITY / 1.6),
    max: WEAK_FLAME_COLOR,
  },
  fuel: {
    min: new LayerColor(0, 0, 0, 0.1),
    max: new LayerColor(0, 180, 30),
  },
  elevation: {
    min: new LayerColor(0, 0, 0, 0.1),
    max: new LayerColor(50, 30, 0),
  },
  wind: {
    min: new LayerColor(200, 200, 200, LAYER_OPACITY / 2),
    max: new LayerColor(200, 0, 50),
  },
};

class Gradient {
  constructor(minValue, maxValue, colors) {
    this.baseValue = minValue;
    this.valueRange = maxValue - minValue;
    this.baseColor = colors.min;
    this.colorRange = new Color(
      ...'rgba'.split('').map((p) => colors.max[p] - colors.min[p])
    );
  }

  for(value) {
    let gradient = (value - this.baseValue) / this.valueRange;
    gradient = Math.max(0, Math.min(1, gradient));
    let rgba = 'rgba'
      .split('')
      .map((p) => this.baseColor[p] + this.colorRange[p] * gradient);
    rgba = [...rgba.slice(0, 3).map(Math.round), rgba[3]];

    return new Color(...rgba);
  }
}

export class IndicatorGradient {
  constructor() {
    this.airTemperatureText = new Gradient(0, 40, INDICATOR_COLORS.text);
    this.airTemperatureBackground = new Gradient(0, 40, INDICATOR_COLORS.bg);
    this.airHumidityText = new Gradient(100, 0, INDICATOR_COLORS.text);
    this.airHumidityBackground = new Gradient(100, 0, INDICATOR_COLORS.bg);
    this.windSpeedText = new Gradient(0, 10, INDICATOR_COLORS.text);
    this.windSpeedBackground = new Gradient(0, 10, INDICATOR_COLORS.bg);
    this.fuelDensityText = new Gradient(0, 1, INDICATOR_COLORS.text);
    this.fuelDensityBackground = new Gradient(0, 1, INDICATOR_COLORS.bg);
  }
}

export class LayerGradient {
  constructor(ignitionTemperature) {
    this.intact = new Gradient(0, ignitionTemperature, LAYER_COLORS.intact);
    this.burning = new Gradient(ignitionTemperature, 900, LAYER_COLORS.burning);
    this.burned = new Gradient(0, ignitionTemperature, LAYER_COLORS.burned);
    this.fuel = new Gradient(0, 1, LAYER_COLORS.fuel);
    this.elevation = new Gradient(0, 6400, LAYER_COLORS.elevation);
    this.wind = new Gradient(0, 10, LAYER_COLORS.wind);
  }
}
