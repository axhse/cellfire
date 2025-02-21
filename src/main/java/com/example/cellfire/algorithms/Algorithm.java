package com.example.cellfire.algorithms;

import com.example.cellfire.models.SimulationStep;
import com.example.cellfire.models.ScenarioConditions;

public interface Algorithm {
    void refine(SimulationStep draftSimulationStep, ScenarioConditions conditions);
}
