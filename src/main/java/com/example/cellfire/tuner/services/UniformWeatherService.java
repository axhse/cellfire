package com.example.cellfire.tuner.services;

import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.services.WeatherService;

import java.time.Instant;

public final class UniformWeatherService implements WeatherService {
    private final double airTemperature;
    private final double airHumidity;
    private final double windX;
    private final double windY;

    public UniformWeatherService(double airTemperature, double airHumidity,
                                 double windX, double windY) {
        this.airTemperature = airTemperature;
        this.airHumidity = airHumidity;
        this.windX = windX;
        this.windY = windY;
    }

    @Override
    public double getAirTemperature(CellCoordinates coordinates, Instant date) {
        return this.airTemperature;
    }

    @Override
    public double getAirHumidity(CellCoordinates coordinates, Instant date) {
        return this.airHumidity;
    }

    @Override
    public double getWindX(CellCoordinates coordinates, Instant date) {
        return this.windX;
    }

    @Override
    public double getWindY(CellCoordinates coordinates, Instant date) {
        return this.windY;
    }
}
