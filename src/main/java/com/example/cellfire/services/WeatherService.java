package com.example.cellfire.services;

import com.example.cellfire.models.Weather;
import com.google.maps.model.LatLng;

import java.time.Instant;

public interface WeatherService {
    Weather getWeather(LatLng point, Instant date);
}
