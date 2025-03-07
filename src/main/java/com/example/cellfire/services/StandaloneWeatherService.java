package com.example.cellfire.services;

import com.example.cellfire.models.Weather;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public final class StandaloneWeatherService implements WeatherService {
    public Weather getWeather(LatLng point, Instant date) {
        return new Weather(25, 0.25, 3, -2);
    }
}
