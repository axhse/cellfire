package ru.cellularwildfire.algorithms;

import ru.cellularwildfire.models.Simulation;

public interface Algorithm {
  void refineDraftStep(Simulation.Step draftStep, Simulation simulation);
}
