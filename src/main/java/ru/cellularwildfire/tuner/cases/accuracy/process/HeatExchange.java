package ru.cellularwildfire.tuner.cases.accuracy.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;

public final class HeatExchange extends TuneCase {
  private static Cell createCell(double heat) {
    return new Cell(
        new Coordinates(0, 0),
        new Cell.State(heat, 0, true),
        new Cell.Factors(0, new Weather(30, 0, 0, 0)));
  }

  @Override
  public void assess(ThermalAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    try {
      Method heatRegulator =
          ThermalAlgorithm.class.getDeclaredMethod("regulateHeat", Cell.class, Simulation.class);
      heatRegulator.setAccessible(true);

      Simulation simulation = createDefaultSimulation(Duration.ofMinutes(30));

      Cell overheatedCell = createCell(5000);
      heatRegulator.invoke(algorithm, overheatedCell, simulation);
      double heat = overheatedCell.getState().getHeat();
      assessment.requireInRange(heat, 700, 900, "Overheated cell heat");

      Cell hotCell = createCell(1300);
      heatRegulator.invoke(algorithm, hotCell, simulation);
      heat = hotCell.getState().getHeat();
      assessment.requireInRange(heat, 600, 850, "Hot cell heat");

      Cell warmCell = createCell(700);
      heatRegulator.invoke(algorithm, warmCell, simulation);
      heat = warmCell.getState().getHeat();
      assessment.requireInRange(heat, 400, 550, "Warm cell heat");

      Cell coldCell = createCell(200);
      heatRegulator.invoke(algorithm, coldCell, simulation);
      heat = coldCell.getState().getHeat();
      assessment.requireInRange(heat, 100, 150, "Cold cell heat");
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
      assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
  }
}
