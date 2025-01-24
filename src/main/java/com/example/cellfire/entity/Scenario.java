package com.example.cellfire.entity;

import com.google.maps.model.LatLng;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Scenario {
    private final Instant creationDate = Instant.now();
    private final String id = UUID.randomUUID().toString();
    private final Forecast forecast = new Forecast();
    private final LatLng startPoint;
    private final Instant startDate;

    public Scenario(LatLng startPoint, Instant startDate, Cell initialCell) {
        this.startPoint = startPoint;
        this.startDate = startDate;
        InstantForecast initialInstantForecast = new InstantForecast();
        initialInstantForecast.getCells().add(initialCell);
        forecast.getInstantForecasts().add(initialInstantForecast);
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public String getId() {
        return id;
    }

    public Forecast getForecast() {
        return forecast;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public boolean hasInstantForecast(Instant date)
    {
        return getInstantForecast(date) != null;
    }

    public InstantForecast getInstantForecast(Instant date)
    {
        Duration forecastPeriod = Duration.between(startDate, date);
        int instantForecastIndex = (int)forecastPeriod.dividedBy(Domain.FORECAST_STEP);
        if (instantForecastIndex < forecast.getInstantForecasts().size()) {
            return forecast.getInstantForecasts().get(instantForecastIndex);
        }
        return null;
    }
}
