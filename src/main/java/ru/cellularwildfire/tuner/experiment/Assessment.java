package ru.cellularwildfire.tuner.experiment;

public final class Assessment {
  private double totalScore = 0;
  private double measureCount = 0;

  public double getScore() {
    return measureCount == 0 ? 0 : totalScore / measureCount;
  }

  public void score(double score) {
    totalScore += Math.max(0, Math.min(score, 1));
    measureCount += 1;
  }

  public void victory() {
    score(1);
  }

  public void failure(String description) throws TuneCase.TuneCaseFailedException {
    throw new TuneCase.TuneCaseFailedException(description);
  }

  public void requireLessThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    if (threshold < value) {
      failure("%s is too high.".formatted(valueName));
    }
    victory();
  }

  public void requireMoreThan(double value, double threshold, String valueName)
      throws TuneCase.TuneCaseFailedException {
    if (value < threshold) {
      failure("%s is too low.".formatted(valueName));
    }
    victory();
  }

  public void requireInRange(double value, double rangeStart, double rangeEnd, String valueName)
      throws TuneCase.TuneCaseFailedException {
    if (value < rangeStart) {
      failure("%s is too low.".formatted(valueName));
    }
    if (rangeEnd < value) {
      failure("%s is too high.".formatted(valueName));
    }
    victory();
  }
}
