package com.example.cellfire.entity;

public final class WeatherCell {
    private final double temperature;
    private final double humidity;
    private final double[] wind;

    public WeatherCell(double temperature, double humidity, double[] wind) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.wind = wind;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double[] getWind() {
        return wind;
    }
}
