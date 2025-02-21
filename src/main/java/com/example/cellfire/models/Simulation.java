package com.example.cellfire.models;

import java.util.ArrayList;
import java.util.List;

public final class Simulation {
    private final List<SimulationStep> steps = new ArrayList<>();

    public List<SimulationStep> getSteps() {
        return steps;
    }

    public boolean hasStep(int step)
    {
        return step < steps.size();
    }
}
