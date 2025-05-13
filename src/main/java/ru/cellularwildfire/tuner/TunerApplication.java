package ru.cellularwildfire.tuner;

import ru.cellularwildfire.tuner.experiment.Experiment;

public final class TunerApplication {
  public static void main(String[] args) {
    validateDefault();
  }

  private static void tune() {
    Experiment experiment = new Experiment(Tasks.balanceCombustion(), true);
    experiment.run().print();
  }

  private static void validateDefault() {
    Experiment experiment = new Experiment(Tasks.validateDefault(), false);
    experiment.run().print();
  }

  private static void evaluatePerformance() {
    Experiment experiment = new Experiment(Tasks.evaluatePerformance(), false);
    experiment.run().print();
  }
}
