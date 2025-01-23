package com.example.cellfire.entity;

import java.time.Duration;

public final class Domain {
    /**
     * 1/100° for both latitude and longitude.
     * <br/>
     * Height ≈1.1 km.
     * <br/>
     * Width  ≈1.1 km near the Equator.
     * <br/>
     * Earth Equatorial circumference: 40 075 km.
     * <br/>
     * Earth Polar circumference: 39 930  km.
     */
    public static final double CELL_SIZE = 0.01;

    public static double INFINITE_FLAMMABILITY = 1_000_000;

    public static final Duration FORECAST_STEP = Duration.ofMinutes(30);

    public static final Duration MAX_FORECAST_PERIOD = Duration.ofDays(3);

    public static final double INITIAL_FIRE_HEAT = 400;
}
