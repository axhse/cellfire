package ru.cellularwildfire.tuner.experiment;

import java.util.ArrayList;
import java.util.List;
import ru.cellularwildfire.services.AutomatonAlgorithm;

public final class Experiment {
  private static final int ITERATION_LIMIT = 1_000_000;
  private final boolean isFastToFail;
  private final TuneTask tuneTask;

  public Experiment(TuneTask tuneTask, boolean isFastToFail) {
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
      List<Criterion.ModelScore> caseScores = new ArrayList<>();
      List<Double> parameterValues = new ArrayList<>();
      List<Integer> parameterValueIndices = new ArrayList<>();
      for (ModelParameter parameter : tuneTask.getParameters()) {
        int parameterValueIndex = n % parameter.getVariations().size();
        double parameterValue = parameter.getVariations().get(parameterValueIndex);
        parameterValues.add(parameterValue);
        parameterValueIndices.add(parameterValueIndex);
        n /= parameter.getVariations().size();
      }
      AutomatonAlgorithm algorithm =
          new AutomatonAlgorithm(
              parameterValues.stream().mapToDouble(Double::doubleValue).toArray());
      for (Criterion criterion : tuneTask.getCriteria()) {
        Criterion.ModelScore modelScore = criterion.assess(algorithm);
        caseScores.add(modelScore);
        if (modelScore.isWeightedFailure() && isFastToFail) {
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
