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

public final class FlammableForestHumidAir extends TuneCase {
    private static final int FOREST_TYPE = ForestTypeConditions.ForestType.EVERGREEN_NEEDLE_LEAF;
    private static final double FUEL = 0.25;
    private static final double AIR_TEMPERATURE = 20;
    private static final double AIR_HUMIDITY = 0.8;
    private static final double WIND_X = 1;
    private static final double WIND_Y = 1;

    public FlammableForestHumidAir(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public FlammableForestHumidAir(double weight) {
        super(weight);
    }

    public FlammableForestHumidAir(boolean isObligatory) {
        super(isObligatory);
    }

    public FlammableForestHumidAir() {
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
            simulator.progressSimulation(simulation, endTick);
            List<Cell> cells = simulation.getSteps().get(endTick).getCells();
            long damagedCellCount = cells.stream().filter(cell -> cell.getState().isDamaged()).count();
            if (9 <= damagedCellCount) {
                return ModelScore.failure("Flammable forest always burns.");
            }
        }
        return ModelScore.victory();
    }
}
