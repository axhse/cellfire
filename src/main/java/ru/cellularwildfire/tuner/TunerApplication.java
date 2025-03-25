package ru.cellularwildfire.tuner;

import ru.cellularwildfire.tuner.experiment.Experiment;

public final class TunerApplication {
  public static void main(String[] args) {
    runExperiment();
  }

  private static void runExperiment() {
    Experiment experiment = new Experiment(true, Tasks.validateDefault());
    experiment.run().print();
  }
}
