package com.example.cellfire.services;

import com.example.cellfire.models.Weather;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public final class StandaloneWeatherService implements WeatherService {
    public Optional<Weather> getWeather(LatLng point, Instant date) {
        return Optional.of(new Weather(25, 0.25, 3, -2));
    }
}
