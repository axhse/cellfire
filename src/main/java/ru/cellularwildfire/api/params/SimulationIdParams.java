package ru.cellularwildfire.api.params;

import java.util.Optional;

public class SimulationIdParams {
  private final String simulationId;

  public SimulationIdParams(String simulationId) {
    this.simulationId = simulationId;
  }

  public Optional<String> getSimulationId() {
    return simulationId == null ? Optional.empty() : Optional.of(simulationId);
  }
}
