package com.example.cellfire.tuner.cases;


import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.services.TerrainService;
import com.example.cellfire.services.WeatherService;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

import java.util.List;

public final class ResilientForestBurnsUnderModerateFactors extends TuneCase {
    private static final byte FOREST_TYPE = 1;
    private static final double FUEL = 1;
    private static final double AIR_TEMPERATURE = 30;
    private static final double AIR_HUMIDITY = 0.1;
    private static final double WIND_X = 4;
    private static final double WIND_Y = 1;

    public ResilientForestBurnsUnderModerateFactors(Algorithm algorithm, double weight, boolean isObligatory) {
        super(algorithm, weight, isObligatory);
    }

    public ResilientForestBurnsUnderModerateFactors(Algorithm algorithm, double weight) {
        super(algorithm, weight);
    }

    public ResilientForestBurnsUnderModerateFactors(Algorithm algorithm) {
        super(algorithm);
    }

    @Override
    public double evaluate(Scenario scenario) {
        int limitSteps = 10;
        this.forecastService.forecast(scenario, limitSteps);
        for (int step = 2; step <= limitSteps; step++) {
            this.forecastService.forecast(scenario, step);
            int damagedCellCount = 0;
            for (Cell cell : scenario.getForecastLog().getForecasts().get(step).getCells()) {
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

    @Override
    protected TerrainService createTerrainService() {
        return new UniformTerrainService(FOREST_TYPE, FUEL, 0);
    }

    @Override
    protected WeatherService createWeatherService() {
        return new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y);
    }
}
