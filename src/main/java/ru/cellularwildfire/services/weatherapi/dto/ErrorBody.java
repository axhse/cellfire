package ru.cellularwildfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorBody {
  @JsonProperty("error")
  private Error error;

  public Error getError() {
    return error;
  }

  public static class Error {
    private static final int NO_MATCHING_LOCATION_CODE = 1006;

    @JsonProperty("code")
    private int code;

    public boolean isNoMatchingLocation() {
      return code == NO_MATCHING_LOCATION_CODE;
    }
  }
}
