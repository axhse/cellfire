package ru.cellularwildfire.services;

import java.time.Instant;
import java.util.Optional;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Weather;

public final class StandaloneWeatherService implements WeatherService {
  public Optional<Weather> getWeather(LatLng point, Instant date) {
    return Optional.of(new Weather(25, 0.25, 3, -2));
  }
}
