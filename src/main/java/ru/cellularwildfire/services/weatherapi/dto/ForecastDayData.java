package ru.cellularwildfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public final class ForecastDayData {
  @JsonProperty("date_epoch")
  private long dateEpoch;

  @JsonProperty("hour")
  private List<WeatherData> hourlyWeather;

  public Instant getLocalDate() {
    return Instant.ofEpochSecond(dateEpoch);
  }

  public List<WeatherData> getHourlyWeather() {
    return hourlyWeather;
  }

  public void setHourlyWeather(List<WeatherData> hourlyWeather) {
    this.hourlyWeather = hourlyWeather;
  }

  public void setDateEpoch(long dateEpoch) {
    this.dateEpoch = dateEpoch;
  }
}
