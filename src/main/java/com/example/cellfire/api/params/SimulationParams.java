package com.example.cellfire.api.params;

public final class SimulationParams extends ScenarioIdParams {
    private final int startStep;
    private final int endStep;

    public SimulationParams(String scenarioId, int startStep, int endStep) {
        super(scenarioId);
        this.startStep = startStep;
        this.endStep = endStep;
    }

    public int getStartStep() {
        return startStep;
    }

    public int getEndStep() {
        return endStep;
    }
}
