package ru.cellularwildfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.TimeZone;

public final class ForecastResponseData {
  @JsonProperty("location")
  private Location location;

  @JsonProperty("current")
  private WeatherData factual;

  @JsonProperty("forecast")
  private ForecastData forecast;

  public TimeZone getTimezoneId() {
    return TimeZone.getTimeZone(location.timezoneId);
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public WeatherData getFactual() {
    return factual;
  }

  public void setFactual(WeatherData factual) {
    this.factual = factual;
  }

  public ForecastData getForecast() {
    return forecast;
  }

  public void setForecast(ForecastData forecast) {
    this.forecast = forecast;
  }

  public static final class Location {
    @JsonProperty("tz_id")
    private String timezoneId;

    public void setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
    }
  }
}
