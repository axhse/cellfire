package ru.cellularwildfire.tuner.experiment;

import java.util.List;

public final class ExperimentIteration {
  private final List<Integer> parameterValueIndices;
  private final List<Criterion.ModelScore> caseScores;
  private final int failureCount;
  private final int weightedFailureCount;
  private final double totalScore;

  public ExperimentIteration(
      List<Integer> parameterValueIndices, List<Criterion.ModelScore> caseScores) {
    this.parameterValueIndices = parameterValueIndices;
    this.caseScores = caseScores;
    this.failureCount = (int) caseScores.stream().filter(Criterion.ModelScore::isFailure).count();
    this.weightedFailureCount =
        (int) caseScores.stream().filter(Criterion.ModelScore::isWeightedFailure).count();
    this.totalScore =
        caseScores.stream().map(Criterion.ModelScore::getWeightedScore).reduce(0.0, Double::sum);
  }

  public static int compareByScore(ExperimentIteration iteration1, ExperimentIteration iteration2) {
    int result = -Double.compare(iteration1.weightedFailureCount, iteration2.weightedFailureCount);
    if (result != 0) {
      return result;
    }
    result = -Double.compare(iteration1.failureCount, iteration2.failureCount);
    if (result != 0) {
      return result;
    }
    return Double.compare(iteration1.totalScore, iteration2.totalScore);
  }

  public List<Integer> getParameterValueIndices() {
    return parameterValueIndices;
  }

  public List<Criterion.ModelScore> getCaseScores() {
    return caseScores;
  }

  public boolean hasFailures() {
    return failureCount > 0;
  }

  public boolean hasWeightedFailures() {
    return weightedFailureCount > 0;
  }

  public double countScore() {
    return totalScore;
  }
}
