package com.example.cellfire.api.params;

import java.time.Instant;

public final class ScenarioForecastParams extends ScenarioIdParams {
    private final long actualTs;

    public ScenarioForecastParams(String scenarioId, long actualTs) {
        super(scenarioId);
        this.actualTs = actualTs;
    }

    public Instant getActualDate() {
        return Instant.ofEpochMilli(actualTs);
    }
}
