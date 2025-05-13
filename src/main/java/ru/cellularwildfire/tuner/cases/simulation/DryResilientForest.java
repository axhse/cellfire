package ru.cellularwildfire.tuner.cases.simulation;

import java.time.Duration;
import ru.cellularwildfire.data.ForestTypeFactors;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;
import ru.cellularwildfire.tuner.services.UniformTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public final class DryResilientForest extends TuneCase {
  private static final int FOREST_TYPE = ForestTypeFactors.ForestType.DECIDUOUS_BROADLEAF;
  private static final double FUEL = 0.7;
  private static final double AIR_TEMPERATURE = 30;
  private static final double AIR_HUMIDITY = 0.25;
  private static final double WIND_X = 3;
  private static final double WIND_Y = 1;

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    Simulator simulator =
        new Simulator(
            new UniformTerrainService(FOREST_TYPE, FUEL, 0),
            new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
            algorithm);
    Simulation simulation = startDefaultSimulation(simulator);

    long limitTicks = Duration.ofHours(12).dividedBy(Simulator.DEFAULT_STEP_DURATION);
    while (hasBurningCells(simulation) && simulation.getSteps().size() <= limitTicks) {
      simulator.progressSimulation(simulation, simulation.getSteps().size());
      if (25 < countDamagedCells(simulation)) {
        assessment.victory();
        return;
      }
    }
    assessment.failure("Dry resilient forest does not burn.");
  }
}
