package ru.cellularwildfire.tuner.cases.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ru.cellularwildfire.data.ForestTypeFactors.ForestType;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;

public final class HeatRegulation extends TuneCase {
  private static Cell createCell(double heat) {
    return new Cell(
        new Coordinates(0, 0),
        new Cell.State(heat, 0, true),
        new Cell.Factors(new Weather(30, 0, 0, 0), 0, ForestType.MIXED));
  }

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    try {
      Method heatRegulator =
          AutomatonAlgorithm.class.getDeclaredMethod("regulateHeat", Cell.class, Simulation.class);
      heatRegulator.setAccessible(true);

      Simulation simulation = createSimulation();
      Simulation roughSimulation =
          createSimulation(Simulator.DEFAULT_STEP_DURATION.multipliedBy(2));
      double heat;

      Cell overheatedCell = createCell(5000);
      heatRegulator.invoke(algorithm, overheatedCell, simulation);
      heat = overheatedCell.getState().getHeat();
      assessment.requireInRange(heat, 750, 1300, "Overheated cell heat");

      overheatedCell = createCell(5000);
      heatRegulator.invoke(algorithm, overheatedCell, roughSimulation);
      heat = overheatedCell.getState().getHeat();
      assessment.requireMoreThan(heat, 700, "Overheated cell heat in rough simulation");

      Cell hotCell = createCell(1300);
      heatRegulator.invoke(algorithm, hotCell, simulation);
      heat = hotCell.getState().getHeat();
      assessment.requireInRange(heat, 650, 850, "Hot cell heat");

      Cell warmCell = createCell(700);
      heatRegulator.invoke(algorithm, warmCell, simulation);
      heat = warmCell.getState().getHeat();
      assessment.requireInRange(heat, 450, 600, "Warm cell heat");

      Cell coldCell = createCell(200);
      heatRegulator.invoke(algorithm, coldCell, simulation);
      heat = coldCell.getState().getHeat();
      assessment.requireInRange(heat, 100, 150, "Cold cell heat");
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
      assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
  }
}
