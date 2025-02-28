package com.example.cellfire.algorithms;

import com.example.cellfire.models.Simulation;

public interface Algorithm {
    void refineDraftStep(Simulation.Step draftStep, Simulation simulation);
}
