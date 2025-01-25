package com.example.cellfire.entity;

public final class Environment {
    private final double ignitionTemperature;
    private final double weatherTemperature;
    private final double humidity;
    private final double[] wind;

    public Environment(double ignitionTemperature, double weatherTemperature, double humidity, double[] wind) {
        this.ignitionTemperature = weatherTemperature;
        this.weatherTemperature = weatherTemperature;
        this.humidity = humidity;
        this.wind = wind;
    }

    public double getIgnitionTemperature() {
        return ignitionTemperature;
    }

    public double getWeatherTemperature() {
        return weatherTemperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double[] getWind() {
        return wind;
    }
}
