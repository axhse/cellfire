package com.example.cellfire.models;

public final class FireFactors {
    private final float[] slope;
    private final float airTemperature;
    private final float airHumidity;
    private final float[] wind;

    public FireFactors(float[] slope, float airTemperature, float airHumidity, float[] wind) {
        this.slope = slope;
        this.airTemperature = airTemperature;
        this.airHumidity = airHumidity;
        this.wind = wind;
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
