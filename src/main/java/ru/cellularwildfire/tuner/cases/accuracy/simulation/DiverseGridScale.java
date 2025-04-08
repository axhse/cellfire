package ru.cellularwildfire.tuner.cases.accuracy.simulation;

import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.data.ForestTypeConditions.ForestType;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;
import ru.cellularwildfire.tuner.services.UniformTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public final class DiverseGridScale extends TuneCase {
  private static final int FOREST_TYPE = ForestType.MIXED;
  private static final double FUEL = 0.5;
  private static final double AIR_TEMPERATURE = 25;
  private static final double AIR_HUMIDITY = 0.3;
  private static final double WIND_X = 3;
  private static final double WIND_Y = 1;

  @Override
  public void assess(ThermalAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    Simulator simulator =
        new Simulator(
            new UniformTerrainService(FOREST_TYPE, FUEL, 0),
            new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
            algorithm);
    int factor = 2;
    Simulation standartSimulation = createSimulation();
    Simulation roughSimulation = createSimulation(DEFAULT_GRID_SCALE / factor);
    simulator.tryStartSimulation(standartSimulation);
    simulator.tryStartSimulation(roughSimulation);

    while (hasBurningCells(roughSimulation)) {
      simulator.tryProgressSimulation(standartSimulation, standartSimulation.getSteps().size());
      simulator.tryProgressSimulation(roughSimulation, roughSimulation.getSteps().size());
      if (countDamagedCells(roughSimulation) > 9) {
        double ratio =
            countBurnedFuel(roughSimulation, FUEL) / countBurnedFuel(standartSimulation, FUEL);
        assessment.scoreLogAccuracy(
            ratio, 1, factor * factor, "Burned fuel [rough simulation]/[standard simulation]");
        return;
      }
    }
    assessment.failure("Too few burned cells.");
  }
}
