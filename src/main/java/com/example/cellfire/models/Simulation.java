package com.example.cellfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Simulation {
    private final List<Step> steps = new ArrayList<>();
    private final String id = UUID.randomUUID().toString();
    @JsonIgnore
    private final Instant creationDate = Instant.now();
    @JsonIgnore
    private final Instant startDate;
    @JsonIgnore
    private final Duration stepDuration;
    private final int limitDurationSteps;
    private final Grid grid;
    private final Coordinates startCoordinates;
    private final Conditions conditions;
    private final String algorithm;

    public Simulation(
            Grid grid, Coordinates startCoordinates, Duration stepDuration, Duration limitDuration,
            Instant startDate, Conditions conditions, String algorithm) {
        this.grid = grid;
        this.startCoordinates = startCoordinates;
        this.stepDuration = stepDuration;
        this.limitDurationSteps = (int)(limitDuration.toSeconds() / stepDuration.toSeconds());
        this.startDate = roundStartDate(startDate, stepDuration);
        this.conditions = conditions;
        this.algorithm = algorithm;
    }

    private static Instant roundStartDate(Instant startDate, Duration stepDuration) {
        long duration = stepDuration.toSeconds();
        return Instant.ofEpochSecond(startDate.getEpochSecond() / duration * duration);
    }

    public boolean hasStep(int step) {
        return step < steps.size();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public Instant getCreationDate() {
        return creationDate;
    }

    @JsonIgnore
    public Instant getStartDate() {
        return startDate;
    }

    public long getStartDateMs() {
        return startDate.toEpochMilli();
    }

    @JsonIgnore
    public Duration getStepDuration() {
        return stepDuration;
    }

    public long getStepDurationMs() {
        return stepDuration.toMillis();
    }

    public int getLimitDurationSteps() {
        return limitDurationSteps;
    }

    public Grid getGrid() {
        return grid;
    }

    public Coordinates getStartCoordinates() {
        return startCoordinates;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public static final class Step {
        private final List<Cell> cells = new ArrayList<>();

        public List<Cell> getCells() {
            return cells;
        }
    }

    public static final class Conditions {
        private final double ignitionTemperature;
        private final double activationEnergy;

        public Conditions(double ignitionTemperature, double activationEnergy) {
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

    // TODO: Remove.
    public static final class Algorithm {
        public static String THERMAL = "thermal";
        public static String PROBABILISTIC = "probabilistic";
    }
}
