package ru.cellularwildfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class ForecastData {
  @JsonProperty("forecastday")
  private List<ForecastDayData> days;

  public List<ForecastDayData> getDays() {
    return days;
  }

  public void setDays(List<ForecastDayData> days) {
    this.days = days;
  }
}
