package com.example.cellfire.models;

public final class ScenarioConditions {
    private final double ignitionTemperature;
    private final double activationEnergy;

    public ScenarioConditions(double ignitionTemperature, double activationEnergy) {
        this.ignitionTemperature = ignitionTemperature;
        this.activationEnergy = activationEnergy;
    }

    public double getIgnitionTemperature() {
        return ignitionTemperature;
    }

    public double getActivationEnergy() {
        return activationEnergy;
    }
}
