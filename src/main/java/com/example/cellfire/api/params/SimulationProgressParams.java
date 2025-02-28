package com.example.cellfire.api.params;

public final class SimulationProgressParams extends SimulationIdParams {
    private final int startStep;
    private final int endStep;

    public SimulationProgressParams(String simulationId, int startStep, int endStep) {
        super(simulationId);
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
