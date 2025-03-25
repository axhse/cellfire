package com.example.cellfire.tuner.experiment;

import com.example.cellfire.algorithms.ThermalAlgorithm;

public final class Criterion {
    private static final double FAILURE_SCORE = -1;
    private final TuneCase tuneCase;
    private final double weight;
    private final boolean isObligatory;

    private Criterion(TuneCase tuneCase, double weight, boolean isObligatory) {
        this.tuneCase = tuneCase;
        this.weight = Math.max(0, weight);
        this.isObligatory = isObligatory && weight != 0;
    }

    public Criterion(TuneCase tuneCase, double weight) {
        this(tuneCase, weight, true);
    }

    public Criterion(TuneCase tuneCase, boolean isObligatory) {
        this(tuneCase, 1, isObligatory);
    }

    public Criterion(TuneCase tuneCase) {
        this(tuneCase, true);
    }

    public String getName() {
        return this.tuneCase.getClass().getSimpleName();
    }

    public ModelScore assess(ThermalAlgorithm algorithm) {
        Assessment assessment = new Assessment();
        try {
            tuneCase.assess(algorithm, assessment);
            return new ModelScore(weightScore(assessment.getScore()), null);
        } catch (TuneCase.TuneCaseFailedException exception) {
            double weightedScore = weightScore(FAILURE_SCORE);
            return new ModelScore(weightedScore, weightedScore < 0 ? exception.getMessage() : null);
        }
    }

    private double weightScore(double score) {
        if (!isObligatory && score == FAILURE_SCORE) {
            score = 0;
        }
        if (score > 0) {
            score *= weight;
        }
        return score;
    }

    public static final class ModelScore {
        private final double score;
        private final String failureDescription;

        private ModelScore(double score, String failureDescription) {
            this.score = score;
            this.failureDescription = failureDescription;
        }

        public boolean isFailure() {
            return score == FAILURE_SCORE;
        }

        public boolean isSuccess() {
            return !isFailure();
        }

        public double getScore() {
            return score;
        }

        public String getFailureDescription() {
            return failureDescription;
        }
    }
}
