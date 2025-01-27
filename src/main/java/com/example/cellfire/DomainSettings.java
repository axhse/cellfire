package com.example.cellfire;

import java.time.Duration;

public final class DomainSettings {
    /**
     * Cell size is 1/100° for both latitude and longitude.
     * <br/>
     * Height ≈1.1 km.
     * <br/>
     * Width  ≈1.1 km near the Equator.
     * <br/>
     * Earth Equatorial circumference: 40 075 km.
     * <br/>
     * Earth Polar circumference: 39 930  km.
     */
    public static final int SCALE_FACTOR = 100;
    public static final int AXES_SCALE = 360 * SCALE_FACTOR;
    
    public static final Duration FORECAST_STEP = Duration.ofMinutes(30);
    public static final Duration MAX_FORECAST_PERIOD = Duration.ofDays(3);

    public static final float INITIAL_FIRE_HEAT = 1000;
    public static final float SIGNIFICANT_OVERHEAT = 20;
    public static final float SIGNIFICANT_RESOURCE = 0.01F;
}
