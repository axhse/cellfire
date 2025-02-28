package com.example.cellfire.tuner.services;

import com.example.cellfire.services.WeatherService;
import com.google.maps.model.LatLng;

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
    public double getAirTemperature(LatLng point, Instant date) {
        return this.airTemperature;
    }

    @Override
    public double getAirHumidity(LatLng point, Instant date) {
        return this.airHumidity;
    }

    @Override
    public double getWindX(LatLng point, Instant date) {
        return this.windX;
    }

    @Override
    public double getWindY(LatLng point, Instant date) {
        return this.windY;
    }
}
