package ru.cellularwildfire.tuner.cases.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ru.cellularwildfire.data.ForestTypeFactors.ForestType;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;

public final class CombustionRate extends TuneCase {
  private static Cell createCell(double heat, double airHumidity) {
    return new Cell(
        new Coordinates(0, 0),
        new Cell.State(1, heat),
        new Cell.Factors(new Weather(0, airHumidity, 0, 0), 0, ForestType.MIXED));
  }

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    try {
      Method rateCalculator =
          AutomatonAlgorithm.class.getDeclaredMethod("calculateCombustionRate", Cell.class);
      rateCalculator.setAccessible(true);
      double rate;

      Cell initiallCell = createCell(Simulator.INITIAL_HEAT, 0.5f);
      rate = (double) rateCalculator.invoke(algorithm, initiallCell);
      assessment.requireMoreThan(rate, 0.3, "Initial rate");

      Cell burningCell = createCell(800, 0.2f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell);
      assessment.requireLessThan(rate, 0.8, "Intensive rate");

      burningCell = createCell(750, 0.4f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell);
      assessment.requireInRange(rate, 0.1, 0.5, "Moderate rate");

      burningCell = createCell(700, 0.2f);
      rate = (double) rateCalculator.invoke(algorithm, burningCell);
      assessment.requireInRange(rate, 0.1, 0.5, "Moderate rate");

      Cell smolderingCell = createCell(600, 0.3f);
      rate = (double) rateCalculator.invoke(algorithm, smolderingCell);
      assessment.requireLessThan(rate, 0.1, "Smoldering rate");

      Cell boilingCell = createCell(800, 0.8f);
      rate = (double) rateCalculator.invoke(algorithm, boilingCell);
      assessment.requireLessThan(rate, 0.1, "Boiling rate");
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
      assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }
  }
}
