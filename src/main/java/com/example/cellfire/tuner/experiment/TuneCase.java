package com.example.cellfire.tuner.experiment;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.Simulator;
import com.google.maps.model.LatLng;

public abstract class TuneCase {
    private final double weight;
    private final boolean isObligatory;

    public TuneCase(double weight, boolean isObligatory) {
        this.weight = Math.max(0, weight);
        this.isObligatory = isObligatory && weight != 0;
    }

    public TuneCase(double weight) {
        this(weight, true);
    }

    public TuneCase(boolean isObligatory) {
        this(1, isObligatory);
    }

    public TuneCase() {
        this(true);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public final ModelScore evaluate(Algorithm algorithm) {
        return ModelScore.weight(score(algorithm), weight, isObligatory);
    }

    protected abstract ModelScore score(Algorithm algorithm);

    protected static Simulation startDefaultSimulation(Simulator simulator, Algorithm algorithm) {
        String algorithmName = algorithm instanceof ThermalAlgorithm
                ? Simulation.Algorithm.THERMAL : Simulation.Algorithm.PROBABILISTIC;
        LatLng startPoint = new LatLng(0, 0);
        Simulation simulation = simulator.createDefaultSimulation(startPoint, algorithmName);
        simulator.startSimulation(simulation);
        return simulation;
    }

    public static final class ModelScore {
        private final double score;
        private final String description;

        private ModelScore(double score, String description) {
            this.score = Math.max(-1, Math.min(1, score));
            this.description = description;
        }

        public boolean isFailure() {
            return score == -1;
        }

        public boolean isSuccess() {
            return !isFailure();
        }

        public double getScore() {
            return score;
        }

        public String getDescription() {
            return description;
        }

        public static ModelScore victory() {
            return success(1);
        }

        public static ModelScore success(double score) {
            return new ModelScore(score, null);
        }

        public static ModelScore failure(String description) {
            return new ModelScore(-1, description);
        }

        private static ModelScore weight(ModelScore modelScore, double weight, boolean isObligatory) {
            double score = modelScore.getScore();
            if (!isObligatory && score == -1) {
                score = 0;
            }
            if (score > 0) {
                score *= weight;
            }
            return new ModelScore(score, modelScore.getDescription());
        }
    }
}
