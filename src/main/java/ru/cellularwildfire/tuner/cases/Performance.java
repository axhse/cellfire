package ru.cellularwildfire.tuner.cases;

import java.time.Instant;
import ru.cellularwildfire.data.ForestTypeFactors.ForestType;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;
import ru.cellularwildfire.tuner.services.SlopedTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public class Performance extends TuneCase {
  private static final int FOREST_TYPE = ForestType.MIXED;
  private static final double FUEL = 0.7;
  private static final double AIR_TEMPERATURE = 30;
  private static final double AIR_HUMIDITY = 0.3;
  private static final double WIND_X = 0;
  private static final double WIND_Y = 0;

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    Simulator simulator =
        new Simulator(
            new SlopedTerrainService(FOREST_TYPE, FUEL, 10, 180),
            new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
            algorithm);
    Simulation simulation = startDefaultSimulation(simulator);
    while (simulation.getSteps().getLast().getCells().size() < 100000) {
      progressOntStep(simulator, simulation);
    }
    long minAffected = Long.MAX_VALUE, minBurning = Long.MAX_VALUE;
    Instant startTime = Instant.now();
    for (int i = 0; i < 10; i++) {
      minAffected = Math.min(minAffected, simulation.getSteps().getLast().getCells().size());
      minBurning = Math.min(minBurning, countBurningCells(simulation));
      progressOntStep(simulator, simulation);
    }
    long elapsedMilli = (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 10;
    String template =
        "One step transition: %d <= affected cells; %d <= burning cells; %d milliseconds elapsed";
    assessment.message(String.format(template, minAffected, minBurning, elapsedMilli));
  }

  private void progressOntStep(Simulator simulator, Simulation simulation) {
    simulator.progressSimulation(simulation, simulation.getSteps().size());
  }
}
