package com.example.cellfire.tuner.cases;


import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.Cell;
import com.example.cellfire.services.TerrainService;
import com.example.cellfire.services.WeatherService;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;

import java.util.List;

public final class FlammableForestDoesNotBurnUnderHumidAir extends TuneCase<ThermalAlgorithm> {
    private static final byte STEP = 10;
    private static final byte ELEVATION = 0;
    private static final byte FUEL = 1;
    private static final byte FOREST_TYPE = 4;
    private static final double AIR_TEMPERATURE = 20;
    private static final double AIR_HUMIDITY = 0.8;
    private static final double WIND_X = 1;
    private static final double WIND_Y = 1;

    public FlammableForestDoesNotBurnUnderHumidAir(ThermalAlgorithm algorithm, double weight, boolean isObligatory) {
        super(algorithm, weight, isObligatory);
    }

    public FlammableForestDoesNotBurnUnderHumidAir(ThermalAlgorithm algorithm, double weight) {
        super(algorithm, weight);
    }

    public FlammableForestDoesNotBurnUnderHumidAir(ThermalAlgorithm algorithm) {
        super(algorithm);
    }

    @Override
    public void evaluate() {
        this.forecastService.forecast(this.scenario, STEP);
        List<Cell> cells = this.scenario.getForecastLog().getForecasts().get(STEP).getCells();
        for (Cell cell : cells) {
            if (this.scenario.getConditions().getIgnitionTemperature() <= cell.getFire().getHeat()) {
                this.score = -1;
                return;
            }
        }
        this.score = 1;
    }

    @Override
    protected TerrainService createTerrainService() {
        return new UniformTerrainService(ELEVATION, FUEL, FOREST_TYPE);
    }

    @Override
    protected WeatherService createWeatherService() {
        return new UniformWeatherService(AIR_TEMPERATURE, AIR_HUMIDITY, WIND_X, WIND_Y);
    }
}
