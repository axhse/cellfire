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

public final class HumidFlammableForest extends TuneCase {
  private static final int FOREST_TYPE = ForestTypeFactors.ForestType.EVERGREEN_NEEDLE_LEAF;
  private static final double FUEL = 0.5;
  private static final double AIR_TEMPERATURE = 20;
  private static final double AIR_HUMIDITY = 0.9;
  private static final double WIND_X = 1;
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
        assessment.failure("Humid flammable forest burns.");
      }
    }
    assessment.victory();
  }
}
