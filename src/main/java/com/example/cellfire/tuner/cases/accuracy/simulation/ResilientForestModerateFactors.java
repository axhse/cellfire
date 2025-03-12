package com.example.cellfire.tuner.cases.accuracy.simulation;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.experiment.TuneCase;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

import java.util.List;

public final class ResilientForestModerateFactors extends TuneCase {
    private static final int FOREST_TYPE = ForestTypeConditions.ForestType.DECIDUOUS_BROADLEAF;
    private static final double FUEL = 0.5;
    private static final double AIR_TEMPERATURE = 30;
    private static final double AIR_HUMIDITY = 0.3;
    private static final double WIND_X = 4;
    private static final double WIND_Y = 2;

    public ResilientForestModerateFactors(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public ResilientForestModerateFactors(double weight) {
        super(weight);
    }

    public ResilientForestModerateFactors(boolean isObligatory) {
        super(isObligatory);
    }

    public ResilientForestModerateFactors() {
        super();
    }

    @Override
    protected ModelScore score(ThermalAlgorithm algorithm) {
        Simulator simulator = new Simulator(
                new UniformTerrainService(FOREST_TYPE, FUEL, 0),
                new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
                algorithm
        );
        Simulation simulation = startDefaultSimulation(simulator);

        int limitTicks = 10;
        for (int endTick = 2; endTick <= limitTicks; endTick++) {
            simulator.tryProgressSimulation(simulation, endTick);
            List<Cell> cells = simulation.getSteps().getLast().getCells();
            long damagedCellCount = cells.stream().filter(cell -> cell.getState().isDamaged()).count();
            if (9 <= damagedCellCount) {
                return ModelScore.victory();
            }
        }
        return ModelScore.failure("Resilient forest never burns.");
    }
}
