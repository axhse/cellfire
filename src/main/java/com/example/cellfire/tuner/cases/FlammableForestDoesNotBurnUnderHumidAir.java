package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.data.ForestConditions;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.experiment.TuneCase;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

public final class FlammableForestDoesNotBurnUnderHumidAir extends TuneCase {
    private static final byte FOREST_TYPE = ForestConditions.ForestType.DECIDUOUS_BROADLEAF;
    private static final double FUEL = 0.5;
    private static final double AIR_TEMPERATURE = 20;
    private static final double AIR_HUMIDITY = 0.8;
    private static final double WIND_X = 1;
    private static final double WIND_Y = 1;

    public FlammableForestDoesNotBurnUnderHumidAir(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public FlammableForestDoesNotBurnUnderHumidAir(double weight) {
        super(weight);
    }

    public FlammableForestDoesNotBurnUnderHumidAir(boolean isObligatory) {
        super(isObligatory);
    }

    public FlammableForestDoesNotBurnUnderHumidAir() {
        super();
    }

    @Override
    protected ModelScore score(Algorithm algorithm) {
        Simulator simulator = new Simulator(
                new UniformTerrainService(FOREST_TYPE, FUEL, 0),
                new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
                algorithm
        );
        Simulation simulation = startDefaultSimulation(simulator, algorithm);

        int limitTicks = 10;
        for (int endTick = 2; endTick <= limitTicks; endTick++) {
            simulator.progressSimulation(simulation, endTick);
            int damagedCellCount = 0;
            for (Cell cell : simulation.getSteps().get(endTick).getCells()) {
                if (cell.getState().isDamaged()) {
                    damagedCellCount++;
                }
            }
            if (9 <= damagedCellCount) {
                return ModelScore.failure("Flammable forest always burns.");
            }
        }
        return ModelScore.victory();
    }
}
