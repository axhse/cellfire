package ru.cellularwildfire.tuner.cases.simulation;

import java.time.Duration;
import ru.cellularwildfire.data.ForestTypeFactors.ForestType;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;
import ru.cellularwildfire.tuner.services.SlopedTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public class StColomaFire extends TuneCase {
  private static final int FOREST_TYPE = ForestType.MIXED;
  private static final double FUEL = 0.45;
  private static final double AIR_TEMPERATURE = 30;
  private static final double AIR_HUMIDITY = 0.25;
  private static final double WIND_X = -4;
  private static final double WIND_Y = -2;

  @Override
  public void assess(AutomatonAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    Simulator simulator =
        new Simulator(
            new SlopedTerrainService(FOREST_TYPE, FUEL, 10, 180),
            new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
            algorithm);
    Simulation simulation = startDefaultSimulation(simulator);

    int ticks = (int) Duration.ofHours(6).dividedBy(simulation.getTimeline().getStepDuration());
    simulator.progressSimulation(simulation, ticks);

    long damagedHectares = Math.round(estimateDamagedHectares(simulation));
    long targetMinHectares = 1700;
    long targetTopBoundaryHectares = 3000;
    long targetMaxHectares = 5000;
    if (damagedHectares <= targetMinHectares) {
      assessment.failure("Damaged %d ha <= %d ha".formatted(damagedHectares, targetMinHectares));
    }
    if (damagedHectares > targetMaxHectares) {
      assessment.failure("Damaged %d ha > %d ha".formatted(damagedHectares, targetMaxHectares));
    }
    assessment.score(
        1.0
            - (double) (damagedHectares - targetTopBoundaryHectares)
                / (targetMaxHectares - targetTopBoundaryHectares));
    assessment.message("Damaged %d ha > %d ha".formatted(damagedHectares, targetMinHectares));
  }
}
