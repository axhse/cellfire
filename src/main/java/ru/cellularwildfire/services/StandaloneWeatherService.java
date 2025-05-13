package ru.cellularwildfire.services;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Weather;

public final class StandaloneWeatherService implements WeatherService {
  private static final Random periodicalRandom = new Random();
  private static final Random random = new Random();

  public Optional<Weather> getWeather(LatLng point, Instant date) {
    long duration = Simulator.DEFAULT_STEP_DURATION.toSeconds() * random.nextInt(1, 10);
    periodicalRandom.setSeed(date.getEpochSecond() / duration);
    double windTrendX = 7 * (periodicalRandom.nextDouble() - 0.5) * 2;
    double windTrendY = 5 * (periodicalRandom.nextDouble() - 0.5) * 2;
    double temperature = 30 - 30 * Math.abs(point.lat) / 90 + 5 * (random.nextDouble() - 0.5) * 2;
    double humidity = 0.2 + 0.4 * Math.abs(point.lat) / 90 + 0.1 * (random.nextDouble() - 0.5) * 2;
    double windX = windTrendX + 2 * (random.nextDouble() - 0.5) * 2;
    double windY = windTrendY + 3 * (random.nextDouble() - 0.5) * 2;
    return Optional.of(new Weather(temperature, humidity, windX, windY));
  }
}
