package ru.cellularwildfire.tuner;

import ru.cellularwildfire.tuner.experiment.Experiment;

public final class TunerApplication {
  public static void main(String[] args) {
    validateDefault();
  }

  private static void tune() {
    Experiment experiment = new Experiment(true, Tasks.adjustHumidityEffect());
    experiment.run().print();
  }

  private static void validateDefault() {
    Experiment experiment = new Experiment(false, Tasks.validateDefault());
    experiment.run().print();
  }
}
