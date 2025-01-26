package com.example.cellfire.models;

import com.example.cellfire.DomainSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Scenario {
    private final Instant creationDate = Instant.now();
    private final String id = UUID.randomUUID().toString();
    private final Forecast forecast = new Forecast();
    private final Instant startDate;

    public Scenario(Instant startDate, Cell initialCell) {
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
        int instantForecastIndex = (int)forecastPeriod.dividedBy(DomainSettings.FORECAST_STEP);
        if (instantForecastIndex < forecast.getInstantForecasts().size()) {
            return forecast.getInstantForecasts().get(instantForecastIndex);
        }
        return null;
    }
}
