package com.example.cellfire.models;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FireFactors factors = (FireFactors) o;
        return Float.compare(elevation, factors.elevation) == 0 && Float.compare(airTemperature, factors.airTemperature) == 0 && Float.compare(airHumidity, factors.airHumidity) == 0 && Float.compare(windX, factors.windX) == 0 && Float.compare(windY, factors.windY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elevation, airTemperature, airHumidity, windX, windY);
    }
}
