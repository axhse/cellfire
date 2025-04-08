package ru.cellularwildfire.tuner.cases.accuracy.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;

public final class CombustionRate extends TuneCase {
  private static Cell createCell(double heat, double airHumidity) {
    return new Cell(
        new Coordinates(0, 0),
        new Cell.State(heat, 0, true),
        new Cell.Factors(0, new Weather(0, airHumidity, 0, 0)));
  }

  @Override
  public void assess(ThermalAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    try {
      Method rateCalculator =
          ThermalAlgorithm.class.getDeclaredMethod(
              "calculateBurnedFraction", Cell.class, Simulation.class);
      rateCalculator.setAccessible(true);

      Simulation simulation = createSimulation();

      Cell initiallCell = createCell(INITIAL_HEAT, 0.5f);
      double rate = (double) rateCalculator.invoke(algorithm, initiallCell, simulation);
      assessment.requireMoreThan(rate, 0.3, "Initial rate");

      Cell burningCell = createCell(800, 0.2f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell, simulation);
      assessment.requireLessThan(rate, 0.8, "Intensive rate");

      burningCell = createCell(750, 0.4f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell, simulation);
      assessment.requireInRange(rate, 0.1, 0.5, "Moderate rate");

      burningCell = createCell(700, 0.2f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell, simulation);
      assessment.requireInRange(rate, 0.1, 0.5, "Moderate rate");

      Cell smolderingCell = createCell(600, 0.3f);
      rate = (double) rateCalculator.invoke(algorithm, smolderingCell, simulation);
      assessment.requireLessThan(rate, 0.1, "Smoldering rate");

      Cell boilingCell = createCell(800, 0.8f);
      rate = (double) rateCalculator.invoke(algorithm, boilingCell, simulation);
      assessment.requireLessThan(rate, 0.1, "Boiling rate");
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
      assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
  }
}
