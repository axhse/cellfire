package com.example.cellfire.models;

import java.time.Duration;

public final class ModelSettings {
    public static int DEFAULT_GRID_SCALE = 200;
    public static Duration DEFAULT_STEP_DURATION = Duration.ofMinutes(30);
    public static Duration DEFAULT_LIMIT_DURATION = Duration.ofDays(7);
    public static final float INITIAL_HEAT = 1000;
    public static final float SIGNIFICANT_FUEL = 0.01F;
}
