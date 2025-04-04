package ru.cellularwildfire.tuner.cases.accuracy.simulation;

import java.util.List;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.data.ForestTypeConditions;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.experiment.Assessment;
import ru.cellularwildfire.tuner.experiment.TuneCase;
import ru.cellularwildfire.tuner.services.UniformTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public final class ResilientForestModerateFactors extends TuneCase {
  private static final int FOREST_TYPE = ForestTypeConditions.ForestType.DECIDUOUS_BROADLEAF;
  private static final double FUEL = 0.5;
  private static final double AIR_TEMPERATURE = 30;
  private static final double AIR_HUMIDITY = 0.3;
  private static final double WIND_X = 4;
  private static final double WIND_Y = 2;

  @Override
  public void assess(ThermalAlgorithm algorithm, Assessment assessment)
      throws TuneCaseFailedException {
    Simulator simulator =
        new Simulator(
            new UniformTerrainService(FOREST_TYPE, FUEL, 0),
            new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
            algorithm);
    Simulation simulation = startDefaultSimulation(simulator);

    int limitTicks = 10;
    for (int endTick = 2; endTick <= limitTicks; endTick++) {
      simulator.tryProgressSimulation(simulation, endTick);
      List<Cell> cells = simulation.getSteps().getLast().getCells();
      long damagedCellCount = cells.stream().filter(cell -> cell.getState().isDamaged()).count();
      if (9 <= damagedCellCount) {
        assessment.victory();
        return;
      }
    }
    assessment.failure("Resilient forest never burns.");
  }
}
