package com.example.cellfire.models;

public class ScenarioConditions {
    private final float ignitionTemperature;

    public ScenarioConditions(float ignitionTemperature) {
        this.ignitionTemperature = ignitionTemperature;
    }

    public float getIgnitionTemperature() {
        return ignitionTemperature;
    }
}
