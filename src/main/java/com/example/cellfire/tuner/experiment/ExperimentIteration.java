package com.example.cellfire.tuner.experiment;

import java.util.List;

public final class ExperimentIteration {
    private final List<Integer> parameterValueIndices;
    private final List<TuneCase.ModelScore> caseScores;
    private final int failureCount;
    private final double totalScore;

    public ExperimentIteration(List<Integer> parameterValueIndices, List<TuneCase.ModelScore> caseScores) {
        this.parameterValueIndices = parameterValueIndices;
        this.caseScores = caseScores;
        this.failureCount = (int) caseScores.stream().filter(TuneCase.ModelScore::isFailure).count();
        this.totalScore = caseScores.stream().filter(TuneCase.ModelScore::isSuccess).map(TuneCase.ModelScore::getScore).reduce(0.0, Double::sum);
    }

    public static int compareByScore(ExperimentIteration iteration1, ExperimentIteration iteration2) {
        if (iteration1.failureCount == iteration2.failureCount) {
            if (iteration1.totalScore == iteration2.totalScore) {
                return 0;
            }
            return iteration1.totalScore < iteration2.totalScore ? -1 : 1;
        }
        return iteration1.failureCount > iteration2.failureCount ? -1 : 1;
    }

    public List<Integer> getParameterValueIndices() {
        return parameterValueIndices;
    }

    public List<TuneCase.ModelScore> getCaseScores() {
        return caseScores;
    }

    public boolean hasFailures() {
        return failureCount > 0;
    }

    public int countFailures() {
        return failureCount;
    }

    public double countScore() {
        return totalScore;
    }
}
