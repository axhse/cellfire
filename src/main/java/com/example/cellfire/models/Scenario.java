package com.example.cellfire.models;

import java.time.Instant;
import java.util.UUID;

public final class Scenario {
    private final String id = UUID.randomUUID().toString();
    private final Instant creationDate = Instant.now();
    private final CellCoordinates startCoordinates;
    private final Instant startDate;
    private final String algorithm;
    private final Simulation simulation;

    public Scenario(String algorithm, CellCoordinates startCoordinates, Instant startDate, SimulationConditions conditions) {
        this.algorithm = algorithm;
        this.startCoordinates = startCoordinates;
        this.startDate = startDate;
        this.simulation = new Simulation(conditions);
    }

    public String getId() {
        return id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public CellCoordinates getStartCoordinates() {
        return startCoordinates;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    // TODO: Remove.
    public static final class Algorithm {
        public static String THERMAL = "thermal";
        public static String PROBABILISTIC = "probabilistic";
    }
}
