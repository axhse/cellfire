package com.example.cellfire.tuner.experiment;

import java.util.ArrayList;
import java.util.List;

public final class ModelParameter {
    public static final String COMBUSTION_RATE = "combustionRate";
    public static final String ENERGY_EMISSION = "energyEmission";
    public static final String AIR_HUMIDITY_EFFECT = "airHumidityEffect";
    public static final String DEFAULT_SLOPE_EFFECT = "slopeEffect";
    public static final String DEFAULT_WIND_EFFECT = "windEffect";
    public static final String DEFAULT_CONVECTION_RATE = "convectionRate";
    public static final String DEFAULT_RADIATION_RATE = "radiationRate";
    public static final String DEFAULT_DISTANCE_EFFECT = "distanceEffect";

    private final String name;
    private final List<Double> variations;

    public ModelParameter(String name, double fixedValue) {
        this.name = name;
        this.variations = List.of(fixedValue);
    }

    public ModelParameter(String name, double minValue, double maxValue, int valueSteps) {
        this.name = name;
        this.variations = createRange(minValue, maxValue, valueSteps);
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

    private static List<Double> createRange(double min, double max, int steps) {
        if (max <= min || steps == 1) {
            return List.of(min);
        }
        List<Double> values = new ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            values.add(min + (max - min) / steps * i);
        }
        return values;
    }
}
