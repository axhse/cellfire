package ru.cellularwildfire.tuner.experiment;

import java.util.ArrayList;
import java.util.List;

public final class ModelParameter {
  public static final String COMBUSTION_INTENSITY = "combustionIntensity";
  public static final String ENERGY_EMISSION = "energyEmission";
  public static final String PROPAGATION_INTENSITY = "propagationIntensity";
  public static final String CONVECTION_INTENSITY = "convectionIntensity";
  public static final String RADIATION_INTENSITY = "radiationIntensity";
  public static final String HUMIDITY_EFFECT = "humidityEffect";
  public static final String SLOPE_EFFECT = "slopeEffect";
  public static final String WIND_EFFECT = "windEffect";

  private final String name;
  private final List<Double> variations;

  public ModelParameter(String name, double fixedValue) {
    this.name = name;
    this.variations = List.of(fixedValue);
  }

  public ModelParameter(String name, List<Double> variations) {
    this.name = name;
    this.variations = variations;
  }

  public static List<Double> linRange(double min, double max, int steps) {
    return createRange(min, max, steps, false);
  }

  public static List<Double> linRange(double unit, double min, double max, int steps) {
    return linRange(unit * min, unit * max, steps);
  }

  public static List<Double> logRange(double min, double max, int steps) {
    return createRange(min, max, steps, true);
  }

  public static List<Double> logUnitRange(double unit, double min, double max, int steps) {
    return logRange(unit * min, unit * max, steps);
  }

  public static List<Double> logTargetRange(double target, double variation, int steps) {
    return logRange(target / variation, target * variation, steps);
  }

  private static List<Double> createRange(double min, double max, int steps, boolean logarithmic) {
    if (max < min) {
      max += min;
      min = max - min;
      max -= min;
    }
    if (min == max || steps < 2) {
      return List.of(min);
    }
    if (min < 0) {
      logarithmic = false;
    }

    List<Double> values = new ArrayList<>();
    double value;
    steps -= 1;
    for (int i = 0; i <= steps; i++) {
      if (logarithmic) {
        value = min * Math.pow(max / min, 1.0 * i / steps);
      } else {
        value = min + (max - min) * i / steps;
      }
      values.add(value);
    }
    return values;
  }

  public String getName() {
    return name;
  }

  public boolean isFixed() {
    return variations.size() == 1;
  }

  public List<Double> getVariations() {
    return variations;
  }
}
