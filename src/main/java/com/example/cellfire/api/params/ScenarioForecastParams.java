package com.example.cellfire.api.params;

public final class ScenarioForecastParams extends ScenarioIdParams {
    private final int startStep;
    private final int endStep;

    public ScenarioForecastParams(String scenarioId, int startStep, int toTimePoint) {
        super(scenarioId);
        this.startStep = startStep;
        this.endStep = toTimePoint;
    }

    public int getStartStep() {
        return startStep;
    }

    public int getEndStep() {
        return endStep;
    }
}
