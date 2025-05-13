package ru.cellularwildfire.tuner.cases.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ru.cellularwildfire.data.ForestTypeFactors.ForestType;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;

public final class HeatRegulation extends TuneCase {
  private static Cell createCell(double heat) {
    return new Cell(
        new Coordinates(0, 0),
        new Cell.State(1, heat),
        new Cell.Factors(new Weather(30, 0, 0, 0), 0, ForestType.MIXED));
  }

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    try {
      Method heatRegulator = AutomatonAlgorithm.class.getDeclaredMethod("regulateHeat", Cell.class);
      heatRegulator.setAccessible(true);
      double heat;

      Cell overheatedCell = createCell(5000);
      heatRegulator.invoke(algorithm, overheatedCell);
      heat = overheatedCell.getState().getHeat();
      assessment.requireInRange(heat, 750, 1300, "Overheated cell heat");

      Cell hotCell = createCell(1300);
      heatRegulator.invoke(algorithm, hotCell);
      heat = hotCell.getState().getHeat();
      assessment.requireInRange(heat, 650, 850, "Hot cell heat");

      Cell warmCell = createCell(700);
      heatRegulator.invoke(algorithm, warmCell);
      heat = warmCell.getState().getHeat();
      assessment.requireInRange(heat, 450, 600, "Warm cell heat");

      Cell coldCell = createCell(200);
      heatRegulator.invoke(algorithm, coldCell);
      heat = coldCell.getState().getHeat();
      assessment.requireInRange(heat, 100, 150, "Cold cell heat");
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
      assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
  }
}
