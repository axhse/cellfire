package com.example.cellfire.services.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ForecastResponseData {
    @JsonProperty("current")
    private WeatherData factual;
    @JsonProperty("forecast")
    private ForecastData forecast;

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
}
