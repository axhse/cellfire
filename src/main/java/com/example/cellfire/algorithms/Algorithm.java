package com.example.cellfire.algorithms;

import com.example.cellfire.models.SimulationStep;
import com.example.cellfire.models.SimulationConditions;

public interface Algorithm {
    void refine(SimulationStep draftSimulationStep, SimulationConditions conditions);
}
