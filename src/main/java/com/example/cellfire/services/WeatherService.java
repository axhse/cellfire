package com.example.cellfire.services;

import com.example.cellfire.models.LatLng;
import com.example.cellfire.models.Weather;

import java.time.Instant;
import java.util.Optional;

public interface WeatherService {
    Optional<Weather> getWeather(LatLng point, Instant date);
}
