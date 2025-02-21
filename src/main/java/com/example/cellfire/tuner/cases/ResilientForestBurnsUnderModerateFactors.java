package com.example.cellfire.tuner.cases;


import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

public final class ResilientForestBurnsUnderModerateFactors extends TuneCase {
    private static final byte FOREST_TYPE = 1;
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

    public ResilientForestBurnsUnderModerateFactors() {
        super();
    }

    @Override
    protected double score(Algorithm algorithm) {
        Simulator simulator = new Simulator(
                new UniformTerrainService(FOREST_TYPE, FUEL, 0),
                new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y),
                algorithm
        );
        Scenario scenario = createAndStartScenario(simulator, algorithm);

        int limitSteps = 10;
        for (int step = 2; step <= limitSteps; step++) {
            simulator.simulate(scenario, step);
            int damagedCellCount = 0;
            for (Cell cell : scenario.getSimulation().getSteps().get(step).getCells()) {
                if (cell.getFire().getIsDamaged()) {
                    damagedCellCount++;
                }
            }
            if (9 <= damagedCellCount) {
                return 1;
            }
        }
        return -1;
    }
}
