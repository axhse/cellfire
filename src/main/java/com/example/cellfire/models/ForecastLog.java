package com.example.cellfire.models;

import java.util.ArrayList;
import java.util.List;

public final class ForecastLog {
    private final List<Forecast> forecasts = new ArrayList<>();

    public List<Forecast> getForecasts() {
        return forecasts;
    }
}
