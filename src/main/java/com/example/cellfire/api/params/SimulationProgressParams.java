package com.example.cellfire.api.params;

public final class SimulationProgressParams extends SimulationIdParams {
    private final int startTick;
    private final int endTick;

    public SimulationProgressParams(String simulationId, int startTick, int endTick) {
        super(simulationId);
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public int getStartTick() {
        return startTick;
    }

    public int getEndTick() {
        return endTick;
    }
}
