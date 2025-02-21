package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.services.Simulator;

import java.time.Instant;

public abstract class TuneCase {
    private final double weight;
    private final boolean isObligatory;

    public TuneCase(double weight, boolean isObligatory) {
        this.weight = weight;
        this.isObligatory = isObligatory;
    }

    public TuneCase(double weight) {
        this(weight, true);
    }

    public TuneCase() {
        this(1);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public final double evaluate(Algorithm algorithm) {
        if (weight == 0) {
            return 0;
        }
        double score = score(algorithm);
        score = score < 0 ? (this.isObligatory ? -1 : 0) : Math.min(1, score);
        return score * this.weight;
    }

    protected abstract double score(Algorithm algorithm);

    protected CellCoordinates getStartCoordinates() {
        return new CellCoordinates(0, 0);
    }

    protected Scenario createAndStartScenario(Simulator simulator, Algorithm algorithm) {
        String algorithmName = algorithm instanceof ThermalAlgorithm
                ? Scenario.Algorithm.THERMAL : Scenario.Algorithm.PROBABILISTIC;
        // FIXME: round to step duration.
        Instant startTime = Instant.now();
        Scenario scenario = simulator.createScenario(algorithmName, getStartCoordinates(), startTime);
        simulator.startScenario(scenario);
        return scenario;
    }
}
