package com.example.cellfire.models;

public final class ScenarioConditions {
    private final double ignitionTemperature;
    private final double activationEnergy;
    private final String algorithm;

    public ScenarioConditions(String algorithm, double ignitionTemperature, double activationEnergy) {
        this.algorithm = algorithm;
        this.ignitionTemperature = ignitionTemperature;
        this.activationEnergy = activationEnergy;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public double getIgnitionTemperature() {
        return ignitionTemperature;
    }

    public double getActivationEnergy() {
        return activationEnergy;
    }

    public static final class Algorithm {
        public static String THERMAL = "thermal";
        public static String PROBABILISTIC = "probabilistic";
    }
}
