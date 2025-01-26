package com.example.cellfire.models;

import java.util.ArrayList;
import java.util.List;

public final class Forecast {
    private final List<InstantForecast> instantForecasts = new ArrayList<>();

    public List<InstantForecast> getInstantForecasts() {
        return instantForecasts;
    }
}
