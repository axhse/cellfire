package com.example.cellfire.models;

import java.time.Duration;

public final class Domain {
    /**
     * Earth radius: 6 371  km.
     */
    public static final double EARTH_RADIUS = 6_371_300;
    /**
     * Earth equatorial circumference: 40 075 km.
     */
    public static final double EARTH_EQUATORIAL_CIRCUMFERENCE = 40_075_000;
    /**
     * Earth polar circumference: 39 930  km.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 39_930_000;

    public final static class Settings {
        /**
         * Cell size of 1/SCALE_FACTOR° for both latitude and longitude
         * corresponds with height ≈110/SCALE_FACTOR km and width ≈110/SCALE_FACTOR km near the equator.
         */
        public static final int GRID_SCALE_FACTOR = 200;
        /**
         * The number of sections per Earth circumference.
         */
        public static final int GRID_SCALE = 360 * GRID_SCALE_FACTOR;
        public static final Duration FORECAST_STEP = Duration.ofMinutes(30);
        public static final Duration MAX_FORECAST_PERIOD = Duration.ofDays(3);

        public static final float INITIAL_FIRE_HEAT = 1000;
        public static final float SIGNIFICANT_OVERHEAT = 20;
        public static final float SIGNIFICANT_FUEL = 0.01F;
    }
}
