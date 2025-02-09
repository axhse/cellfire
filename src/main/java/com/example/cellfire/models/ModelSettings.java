package com.example.cellfire.models;

import java.time.Duration;

public final class ModelSettings {
    // -- Grid --
    /**
     * Cell size of 1/SCALE_FACTOR° for both latitude and longitude
     * corresponds with height ≈110/SCALE_FACTOR km and width ≈110/SCALE_FACTOR km near the equator.
     */
    public static final int GRID_SCALE = 200;

    // -- Forecast --
    public static final Duration FORECAST_STEP = Duration.ofMinutes(30);
    public static final Duration MAX_FORECAST_PERIOD = Duration.ofDays(7);
    public static final float INITIAL_HEAT = 1000;
    public static final float SIGNIFICANT_FUEL = 0.01F;

    // -- Derived --
    /**
     * The number of grid vertical sections.
     */
    public static final int GRID_X_TICKS = 360 * GRID_SCALE;
    /**
     * Cell both vertical and horizontal size in degrees.
     */
    public static final double GRID_CELL_SIZE = 1.0 / GRID_SCALE;
    /**
     * Cell height in meters.
     */
    public static final double CELL_HEIGHT = Domain.EARTH_CIRCUMFERENCE / GRID_X_TICKS;
}
