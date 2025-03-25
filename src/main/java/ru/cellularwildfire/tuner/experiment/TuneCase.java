package ru.cellularwildfire.tuner.experiment;

import java.time.Duration;
import java.time.Instant;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.Simulator;

public abstract class TuneCase {
  protected static Simulation createDefaultSimulation(Duration stepDuration) {
    return new Simulation(
        new Simulation.MarkedGrid(1, getDefaultStartPoint()),
        new Simulation.Timeline(Instant.now(), stepDuration, Duration.ofDays(7)),
        new Simulation.Conditions(100000),
        Simulation.Algorithm.THERMAL);
  }

  protected static Simulation startDefaultSimulation(Simulator simulator) {
    Simulation simulation =
        simulator.createSimulation(getDefaultStartPoint(), Simulation.Algorithm.THERMAL);
    simulator.tryStartSimulation(simulation);
    return simulation;
  }

  private static LatLng getDefaultStartPoint() {
    return new LatLng(0.000001, 0.000001);
  }

  public abstract void assess(ThermalAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException;

  public static final class TuneCaseFailedException extends Exception {
    public TuneCaseFailedException(String message) {
      super(message);
    }
  }
}
