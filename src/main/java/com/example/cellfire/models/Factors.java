package com.example.cellfire.models;

public final class Factors {
    private final float ignitionTemperature;
    private final float airTemperature;
    private final float airHumidity;
    private final float[] wind;

    public Factors(float ignitionTemperature, float airTemperature, float airHumidity, float[] wind) {
        this.ignitionTemperature = ignitionTemperature;
        this.airTemperature = airTemperature;
        this.airHumidity = airHumidity;
        this.wind = wind;
    }

    public float getIgnitionTemperature() {
        return ignitionTemperature;
    }

    public float getAirTemperature() {
        return airTemperature;
    }

    public float getAirHumidity() {
        return airHumidity;
    }

    public float[] getWind() {
        return wind;
    }
}
