package ru.cellularwildfire.services;

import java.time.Instant;
import java.util.Optional;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Weather;

public interface WeatherService {
  Optional<Weather> getWeather(LatLng point, Instant date);
}
