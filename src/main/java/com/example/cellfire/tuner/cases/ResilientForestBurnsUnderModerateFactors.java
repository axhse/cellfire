package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.data.ForestConditions;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

public final class ResilientForestBurnsUnderModerateFactors extends TuneCase {
    private static final byte FOREST_TYPE = ForestConditions.ForestType.EVERGREEN_NEEDLE_LEAF;
    private static final double FUEL = 1;
    private static final double AIR_TEMPERATURE = 30;
    private static final double AIR_HUMIDITY = 0.1;
    private static final double WIND_X = 4;
    private static final double WIND_Y = 1;

    public ResilientForestBurnsUnderModerateFactors(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public ResilientForestBurnsUnderModerateFactors(double weight) {
        super(weight);
    }

    public ResilientForestBurnsUnderModerateFactors(boolean isObligatory) {
        super(isObligatory);
    }

    public ResilientForestBurnsUnderModerateFactors() {
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

        int limitSteps = 10;
        for (int step = 2; step <= limitSteps; step++) {
            simulator.progressSimulation(simulation, step);
            int damagedCellCount = 0;
            for (Cell cell : simulation.getSteps().get(step).getCells()) {
                if (cell.getState().isDamaged()) {
                    damagedCellCount++;
                }
            }
            if (9 <= damagedCellCount) {
                return ModelScore.victory();
            }
        }
        return ModelScore.failure("Resilient forest never burns.");
    }
}
