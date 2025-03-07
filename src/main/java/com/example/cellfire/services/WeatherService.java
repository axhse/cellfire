package com.example.cellfire.services;

import com.example.cellfire.models.Weather;
import com.google.maps.model.LatLng;

import java.time.Instant;
import java.util.Optional;

public interface WeatherService {
    Optional<Weather> getWeather(LatLng point, Instant date);
}
