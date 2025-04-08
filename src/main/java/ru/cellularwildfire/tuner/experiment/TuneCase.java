package ru.cellularwildfire.tuner.experiment;

import java.time.Duration;
import java.time.Instant;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.data.ForestTypeConditions;
import ru.cellularwildfire.data.ForestTypeConditions.ForestType;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.Simulator;

public abstract class TuneCase {
  protected static final Duration DEFAULT_STEP_DURATION = Duration.ofMinutes(30);
  protected static final int DEFAULT_GRID_SCALE = 200;
  protected static final double INITIAL_HEAT = 1000;
  private static final double IGNITION_TEMPERATURE = 500;

  protected static Simulation createSimulation(Duration stepDuration) {
    return createSimulation(stepDuration, DEFAULT_GRID_SCALE);
  }

  protected static Simulation createSimulation(int gridScale) {
    return createSimulation(DEFAULT_STEP_DURATION, gridScale);
  }

  protected static Simulation createSimulation() {
    return createSimulation(DEFAULT_STEP_DURATION, DEFAULT_GRID_SCALE);
  }

  private static Simulation createSimulation(Duration stepDuration, int gridScale) {
    return new Simulation(
        new Simulation.MarkedGrid(gridScale, getDefaultStartPoint()),
        new Simulation.Timeline(Instant.now(), stepDuration, Duration.ofDays(7)),
        new Simulation.Conditions(ForestTypeConditions.determineActivationEnergy(ForestType.MIXED)),
        Simulation.Algorithm.THERMAL);
  }

  protected static Simulation startDefaultSimulation(Simulator simulator) {
    Simulation simulation =
        simulator.createSimulation(getDefaultStartPoint(), Simulation.Algorithm.THERMAL);
    simulator.tryStartSimulation(simulation);
    return simulation;
  }

  protected static double estimateDamagedHectares(Simulation simulation) {
    return simulation.getSteps().getLast().getCells().stream()
            .filter(cell -> cell.getState().isDamaged())
            .map(cell -> simulation.getGrid().estimateCellArea(cell.getCoordinates()))
            .mapToDouble(Double::doubleValue)
            .sum()
        / 10000;
  }

  protected static long countDamagedCells(Simulation simulation) {
    return simulation.getSteps().getLast().getCells().stream()
        .filter(cell -> cell.getState().isDamaged())
        .count();
  }

  protected static double countBurnedFuel(Simulation simulation, double initialUniformFuel) {
    return simulation.getSteps().getLast().getCells().stream()
            .map(cell -> initialUniformFuel - cell.getState().getFuel())
            .mapToDouble(Double::doubleValue)
            .sum()
        / Math.pow(simulation.getGrid().getScale(), 2);
  }

  protected static boolean hasBurningCells(Simulation simulation) {
    return simulation.getSteps().getLast().getCells().stream()
        .anyMatch(cell -> IGNITION_TEMPERATURE <= cell.getState().getHeat());
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
