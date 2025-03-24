package com.example.cellfire.tuner.services;

import com.example.cellfire.models.LatLng;
import com.example.cellfire.models.Weather;
import com.example.cellfire.services.WeatherService;

import java.time.Instant;
import java.util.Optional;

public final class UniformWeatherService implements WeatherService {
    private final Optional<Weather> weather;

    public UniformWeatherService(double airTemperature, double airHumidity, double windX, double windY) {
        this.weather = Optional.of(new Weather(airTemperature, airHumidity, windX, windY));
    }

    public Optional<Weather> getWeather(LatLng point, Instant date) {
        return weather;
    }
}
