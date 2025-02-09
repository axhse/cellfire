package com.example.cellfire.models;

public final class FireFactors {
    private final float elevation;
    private final float airTemperature;
    private final float airHumidity;
    private final float windX;
    private final float windY;

    public FireFactors(float elevation, float airTemperature, float airHumidity, float windX, float windY) {
        this.elevation = elevation;
        this.airTemperature = airTemperature;
        this.airHumidity = airHumidity;
        this.windX = windX;
        this.windY = windY;
    }

    public float getElevation() {
        return elevation;
    }

    public float getAirTemperature() {
        return airTemperature;
    }

    public float getAirHumidity() {
        return airHumidity;
    }

    public float getWindX() {
        return windX;
    }

    public float getWindY() {
        return windY;
    }
}
