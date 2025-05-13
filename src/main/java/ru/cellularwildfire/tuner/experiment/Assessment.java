package ru.cellularwildfire.tuner.experiment;

import java.util.Optional;

public final class Assessment {
  private double totalScore = 0;
  private double measureCount = 0;
  private String message = null;

  public Optional<String> getMessage() {
    return message == null ? Optional.empty() : Optional.of(message);
  }

  public double getScore() {
    return measureCount == 0 ? 0 : totalScore / measureCount;
  }

  public void message(String text) {
    message = text;
  }

  public void score(double score) {
    totalScore += Math.min(Math.max(0, score), 1);
    measureCount++;
  }

  public void victory() {
    score(1);
  }

  public void failure(String description) throws TuneCase.TuneCaseFailedException {
    throw new TuneCase.TuneCaseFailedException(description);
  }

  public void requireLessThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    assertIsLessThan(value, threshold, valueName);
    victory();
  }

  public void requireMoreThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    assertIsMoreThan(value, threshold, valueName);
    victory();
  }

  public void requireInRange(double value, double rangeStart, double rangeEnd, String valueName)
      throws TuneCase.TuneCaseFailedException {
    assertIsMoreThan(value, rangeStart, valueName);
    assertIsLessThan(value, rangeEnd, valueName);
    victory();
  }

  public void scoreLogAccuracy(double value, double target, double limitDeviation, String valueName)
      throws TuneCase.TuneCaseFailedException {
    scoreInLogRange(value, target / limitDeviation, target * limitDeviation, valueName);
  }

  public void scoreInLogRange(double value, double rangeStart, double rangeEnd, String valueName)
      throws TuneCase.TuneCaseFailedException {
    assertIsMoreThan(value, rangeStart, valueName);
    assertIsLessThan(value, rangeEnd, valueName);
    double proportion = Math.log(value / rangeStart) / Math.log(rangeEnd / rangeStart);
    double logScore = 2 * Math.min(proportion, 1 - proportion);
    score(logScore);
  }

  private void assertIsLessThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    if (value > threshold) {
      failure("%s is too high.".formatted(valueName));
    }
  }

  private void assertIsMoreThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    if (value < threshold) {
      failure("%s is too low.".formatted(valueName));
    }
  }
}
