package com.example.cellfire.entity;

import java.util.ArrayList;
import java.util.List;

public final class FireForecast {
    private final List<InstantFireForecast> instantForecasts = new ArrayList<>();

    public List<InstantFireForecast> getInstantForecasts() {
        return instantForecasts;
    }

    public void addInstantForecast(InstantFireForecast forecast) {
        instantForecasts.add(forecast);
    }
}
