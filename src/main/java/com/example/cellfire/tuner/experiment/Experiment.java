package com.example.cellfire.tuner.experiment;

import com.example.cellfire.algorithms.ThermalAlgorithm;

import java.util.ArrayList;
import java.util.List;

public final class Experiment {
    private static final int ITERATION_LIMIT = 1_000_000;
    private final boolean isFastToFail;
    private final TuneTask tuneTask;

    public Experiment(boolean isFastToFail, TuneTask tuneTask) {
        this.isFastToFail = isFastToFail;
        this.tuneTask = tuneTask;
    }

    public TuneTask getTuneTask() {
        return tuneTask;
    }

    public ExperimentResult run() {
        int iterationQuantity = countIterations();
        List<ExperimentIteration> iterations = new ArrayList<>(iterationQuantity);
        for (int iterationIndex = 0; iterationIndex < iterationQuantity; iterationIndex++) {
            int n = iterationIndex;
            List<TuneCase.ModelScore> caseScores = new ArrayList<>();
            List<Double> parameterValues = new ArrayList<>();
            List<Integer> parameterValueIndices = new ArrayList<>();
            for (ModelParameter parameter : tuneTask.getParameters()) {
                int parameterValueIndex = n % parameter.getVariations().size();
                double parameterValue = parameter.getVariations().get(parameterValueIndex);
                parameterValues.add(parameterValue);
                parameterValueIndices.add(parameterValueIndex);
                n /= parameter.getVariations().size();
            }
            ThermalAlgorithm algorithm = new ThermalAlgorithm(
                    parameterValues.stream().mapToDouble(Double::doubleValue).toArray()
            );
            for (TuneCase tuneCase : tuneTask.getTuneCases()) {
                TuneCase.ModelScore modelScore = tuneCase.evaluate(algorithm);
                caseScores.add(modelScore);
                if (modelScore.isFailure() && isFastToFail) {
                    break;
                }
            }
            iterations.add(new ExperimentIteration(parameterValueIndices, caseScores));
        }
        return new ExperimentResult(this, iterations);
    }

    public int countIterations() {
        int iterationQuantity = 1;
        for (ModelParameter parameter : tuneTask.getParameters()) {
            iterationQuantity *= parameter.getVariations().size();
            if (iterationQuantity > ITERATION_LIMIT) {
                throw new IllegalArgumentException("Too many iterations");
            }
        }
        return iterationQuantity;
    }
}
