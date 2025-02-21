package com.example.cellfire.models;

public final class SimulationConditions {
    private final double ignitionTemperature;
    private final double activationEnergy;

    public SimulationConditions(double ignitionTemperature, double activationEnergy) {
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
