package com.example.cellfire.models;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class Scenario {
    private final Instant creationDate = Instant.now();
    private final String id = UUID.randomUUID().toString();
    private final ForecastLog forecastLog = new ForecastLog();
    private final Instant startDate;
    private final ScenarioConditions conditions;

    public Scenario(Instant startDate, ScenarioConditions conditions) {
        this.startDate = startDate;
        this.conditions = conditions;
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

    public ScenarioConditions getConditions() {
        return conditions;
    }

    public boolean hasForecast(int step)
    {
        return step < forecastLog.getForecasts().size();
    }
}
