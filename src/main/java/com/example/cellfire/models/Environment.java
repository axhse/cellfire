package com.example.cellfire.models;

public final class Environment {
    private final float ignitionTemperature;
    private final float weatherTemperature;
    private final float humidity;
    private final float[] wind;

    public Environment(float ignitionTemperature, float weatherTemperature, float humidity, float[] wind) {
        this.ignitionTemperature = ignitionTemperature;
        this.weatherTemperature = weatherTemperature;
        this.humidity = humidity;
        this.wind = wind;
    }

    public float getIgnitionTemperature() {
        return ignitionTemperature;
    }

    public float getWeatherTemperature() {
        return weatherTemperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public float[] getWind() {
        return wind;
    }
}
