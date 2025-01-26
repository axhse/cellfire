package com.example.cellfire.models;

import com.example.cellfire.DomainSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Scenario {
    private final Instant creationDate = Instant.now();
    private final String id = UUID.randomUUID().toString();
    private final ForecastLog forecastLog = new ForecastLog();
    private final Instant startDate;

    public Scenario(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public String getId() {
        return id;
    }

    public ForecastLog getForecastLog() {
        return forecastLog;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public boolean hasForecast(Instant date)
    {
        return getForecast(date) != null;
    }

    public Forecast getForecast(Instant date)
    {
        Duration forecastPeriod = Duration.between(startDate, date);
        int forecastIndex = (int)forecastPeriod.dividedBy(DomainSettings.FORECAST_STEP);
        if (forecastIndex < forecastLog.getForecasts().size()) {
            return forecastLog.getForecasts().get(forecastIndex);
        }
        return null;
    }
}
