package ru.cellularwildfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.cellularwildfire.models.Weather;

public final class WeatherData {
  @JsonProperty("humidity")
  private int humidity;

  @JsonProperty("precip_mm")
  private double precipitation;

  @JsonProperty("temp_c")
  private double temperature;

  @JsonProperty("wind_degree")
  private double windDegree;

  @JsonProperty("wind_kph")
  private double windSpeed;

  public Weather getWeather() {
    return new Weather(
        temperature,
        precipitation > 0 ? 1 : humidity / 100.0,
        windSpeed / 3.6 * Math.cos(Math.toRadians(windDegree)),
        windSpeed / 3.6 * Math.sin(Math.toRadians(windDegree)));
  }

  public void setHumidity(int humidity) {
    this.humidity = humidity;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public void setWindDegree(double windDegree) {
    this.windDegree = windDegree;
  }

  public void setWindSpeed(double windSpeed) {
    this.windSpeed = windSpeed;
  }
}
