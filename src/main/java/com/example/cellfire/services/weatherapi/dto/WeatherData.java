package com.example.cellfire.services.weatherapi.dto;

import com.example.cellfire.models.Weather;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class WeatherData {
    @JsonProperty("temp_c")
    private double temperature;

    @JsonProperty("humidity")
    private int humidity;

    @JsonProperty("wind_kph")
    private double windSpeed;

    @JsonProperty("wind_degree")
    private double windDegree;

    public Weather getWeather() {
        return new Weather(
                temperature,
                humidity / 100.0,
                windSpeed / 3.6 * Math.cos(Math.toRadians(windDegree)),
                windSpeed / 3.6 * Math.sin(Math.toRadians(windDegree))
        );
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDegree(double windDegree) {
        this.windDegree = windDegree;
    }
}
