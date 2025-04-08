package ru.cellularwildfire.api.params;

public class SimulationIdParams {
  private final String simulationId;

  public SimulationIdParams(String simulationId) {
    this.simulationId = simulationId;
  }

  public String getSimulationId() {
    return simulationId;
  }
}
