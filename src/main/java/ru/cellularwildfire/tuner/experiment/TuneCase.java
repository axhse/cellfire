package ru.cellularwildfire.tuner.experiment;

import java.time.Duration;
import java.time.Instant;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;

public abstract class TuneCase {
  protected static Simulation createSimulation(Duration stepDuration) {
    return createSimulation(stepDuration, Simulator.DEFAULT_GRID_SCALE);
  }

  protected static Simulation createSimulation(int gridScale) {
    return createSimulation(Simulator.DEFAULT_STEP_DURATION, gridScale);
  }

  protected static Simulation createSimulation() {
    return createSimulation(Simulator.DEFAULT_STEP_DURATION, Simulator.DEFAULT_GRID_SCALE);
  }

  private static Simulation createSimulation(Duration stepDuration, int gridScale) {
    return new Simulation(
        new Simulation.MarkedGrid(gridScale, getDefaultStartPoint()),
        new Simulation.Timeline(Instant.now(), stepDuration, Duration.ofDays(7)));
  }

  protected static Simulation startDefaultSimulation(Simulator simulator) {
    Simulation simulation = simulator.createSimulation(getDefaultStartPoint());
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

  protected static long countBurningCells(Simulation simulation) {
    return simulation.getSteps().getLast().getCells().stream().filter(Cell::isBurning).count();
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
    return simulation.getSteps().getLast().getCells().stream().anyMatch(Cell::isBurning);
  }

  private static LatLng getDefaultStartPoint() {
    return new LatLng(0.000001, 0.000001);
  }

  public abstract void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException;

  public static final class TuneCaseFailedException extends Exception {
    public TuneCaseFailedException(String message) {
      super(message);
    }
  }
}
