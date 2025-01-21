package com.example.cellfire.entity;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Forecast {
    private final List<InstantForecast> instantForecasts = new ArrayList<>();

    public List<InstantForecast> getInstantForecasts() {
        return instantForecasts;
    }
}
