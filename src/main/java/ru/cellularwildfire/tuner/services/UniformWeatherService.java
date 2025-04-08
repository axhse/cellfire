package ru.cellularwildfire.tuner.services;

import java.time.Instant;
import java.util.Optional;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.services.WeatherService;

public final class UniformWeatherService implements WeatherService {
  private final Weather weather;

  public UniformWeatherService(
      double airTemperature, double airHumidity, double windX, double windY) {
    this.weather = new Weather(airTemperature, airHumidity, windX, windY);
  }

  public Optional<Weather> getWeather(LatLng point, Instant date) {
    return Optional.of(weather);
  }
}
