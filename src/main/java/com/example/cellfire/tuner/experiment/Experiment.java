package com.example.cellfire.tuner.experiment;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.tuner.cases.TuneCase;
import com.example.cellfire.tuner.TuneTask;

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
            List<Double> caseScores = new ArrayList<>();
            List<Double> parameterValues = new ArrayList<>();
            List<Integer> parameterValueIndices = new ArrayList<>();
            for (ModelParameter parameter : tuneTask.getParameters()) {
                int parameterValueIndex = n % parameter.getVariations().size();
                double parameterValue = parameter.getVariations().get(parameterValueIndex);
                parameterValues.add(parameterValue);
                parameterValueIndices.add(parameterValueIndex);
                n /= parameter.getVariations().size();
            }
            Algorithm algorithm = new ThermalAlgorithm(
                    parameterValues.stream().mapToDouble(Double::doubleValue).toArray()
            );
            for (TuneCase tuneCase : tuneTask.getTuneCases()) {
                double score = tuneCase.evaluate(algorithm);
                caseScores.add(score);
                if (score < 0 && isFastToFail) {
                    break;
                }
            }
            iterations.add(new ExperimentIteration(parameterValueIndices, caseScores));
        }
        return new ExperimentResult(this, iterations);
    }

    private int countIterations() {
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
