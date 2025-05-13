package ru.cellularwildfire.api.params;

import java.util.Optional;

public final class SimulationProgressParams extends SimulationIdParams {
  private final Integer startTick;
  private final Integer endTick;

  public SimulationProgressParams(String simulationId, Integer startTick, Integer endTick) {
    super(simulationId);
    this.startTick = startTick;
    this.endTick = endTick;
  }

  public Optional<Integer> getStartTick() {
    return startTick == null ? Optional.empty() : Optional.of(startTick);
  }

  public Optional<Integer> getEndTick() {
    return endTick == null ? Optional.empty() : Optional.of(endTick);
  }
}
