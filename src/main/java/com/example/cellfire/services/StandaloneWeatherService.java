package com.example.cellfire.services;

import com.example.cellfire.models.LatLng;
import com.example.cellfire.models.Weather;

import java.time.Instant;
import java.util.Optional;

public final class StandaloneWeatherService implements WeatherService {
  public Optional<Weather> getWeather(LatLng point, Instant date) {
    return Optional.of(new Weather(25, 0.25, 3, -2));
  }
}
