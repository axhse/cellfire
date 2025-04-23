package ru.cellularwildfire.tuner.experiment;

import java.util.Optional;
import ru.cellularwildfire.services.AutomatonAlgorithm;

public final class Criterion {
  private static final double FAILURE_SCORE = -1;
  private final TuneCase tuneCase;
  private final double weight;
  private final boolean isObligatory;

  private Criterion(TuneCase tuneCase, double weight, boolean isObligatory) {
    this.tuneCase = tuneCase;
    this.weight = Math.min(Math.max(0, weight), 1);
    this.isObligatory = isObligatory && this.weight != 0;
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

  public ModelScore assess(AutomatonAlgorithm algorithm) {
    Assessment assessment = new Assessment();
    try {
      tuneCase.assess(algorithm, assessment);
      double score = assessment.getScore();
      double weightedScore = weightScore(score);
      Optional<String> message = assessment.getMessage();
      return message
          .map(text -> new ModelScore(score, weightedScore, text))
          .orElseGet(() -> new ModelScore(score, weightedScore));
    } catch (TuneCase.TuneCaseFailedException exception) {
      return new ModelScore(FAILURE_SCORE, weightScore(FAILURE_SCORE), exception.getMessage());
    }
  }

  private double weightScore(double score) {
    if (!isObligatory && score == FAILURE_SCORE) {
      score = 0;
    }
    return score * weight;
  }

  public static final class ModelScore {
    private final double score;
    private final double weightedScore;
    private final Optional<String> message;

    private ModelScore(double score, double weightedScore, String message) {
      this.score = score;
      this.weightedScore = weightedScore;
      this.message = Optional.of(message);
    }

    private ModelScore(double score, double weightedScore) {
      this.score = score;
      this.weightedScore = weightedScore;
      this.message = Optional.empty();
    }

    public boolean isFailure() {
      return score == FAILURE_SCORE;
    }

    public boolean isWeightedFailure() {
      return weightedScore < 0;
    }

    public double getScore() {
      return score;
    }

    public double getWeightedScore() {
      return weightedScore;
    }

    public Optional<String> getMessage() {
      return message;
    }
  }
}
