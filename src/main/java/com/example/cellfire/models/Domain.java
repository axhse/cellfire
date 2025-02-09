package com.example.cellfire.models;

import java.time.Duration;

public final class Domain {
    /**
     * Earth equatorial circumference: 40 075 km.
     * Earth polar circumference: 39 930  km.
     */
    public static final double EARTH_CIRCUMFERENCE = 40_000_000;

    public final static class Settings {
        // -- Grid --
        /**
         * Cell size of 1/SCALE_FACTOR° for both latitude and longitude
         * corresponds with height ≈110/SCALE_FACTOR km and width ≈110/SCALE_FACTOR km near the equator.
         */
        public static final int GRID_SCALE_FACTOR = 200;

        // -- Forecast --
        public static final Duration FORECAST_STEP = Duration.ofMinutes(30);
        public static final Duration MAX_FORECAST_PERIOD = Duration.ofDays(3);
        public static final float INITIAL_FIRE_HEAT = 1000;
        public static final float SIGNIFICANT_FUEL = 0.01F;

        // -- Derived --
        /**
         * The number of sections per Earth circumference.
         */
        public static final int GRID_SCALE = 360 * GRID_SCALE_FACTOR;
        /**
         * Cell size in degrees for both latitude and longitude in degrees.
         */
        public static final double GRID_SIZE = 1.0 / GRID_SCALE_FACTOR;
        /**
         * Cell height in meters.
         */
        public static final double CELL_HEIGHT = EARTH_CIRCUMFERENCE / GRID_SCALE;
    }
}
