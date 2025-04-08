package ru.cellularwildfire.services.weatherapi;

import java.time.Instant;
import java.util.List;
import ru.cellularwildfire.models.Weather;

public final class WeatherForecast {
  private final Instant forecastStartDate;
  private final List<Weather> hourlyForecastedWeather;
  private final Weather factualWeather;

  public WeatherForecast(
      Instant forecastStartDate, List<Weather> hourlyForecastedWeather, Weather factualWeather) {
    this.forecastStartDate = forecastStartDate;
    this.hourlyForecastedWeather = hourlyForecastedWeather;
    this.factualWeather = factualWeather;
  }

  public Instant getForecastStartDate() {
    return forecastStartDate;
  }

  public List<Weather> getHourlyForecastedWeather() {
    return hourlyForecastedWeather;
  }

  public Weather getFactualWeather() {
    return factualWeather;
  }
}
