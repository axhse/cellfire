package com.example.cellfire.services;

import com.example.cellfire.models.Simulation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public final class SimulationManager {
    private final List<Simulation> simulations = new ArrayList<>();

    public Simulation getSimulation(String id) {
        for (Simulation simulation : simulations) {
            if (simulation.getId().equals(id)) {
                return simulation;
            }
        }
        return null;
    }

    public void addSimulation(Simulation simulation) {
        simulations.add(simulation);
    }

    public void removeSimulation(String id) {
        simulations.removeIf(simulation -> simulation.getId().equals(id));
    }

    public void revise() {
        simulations.removeIf(simulation -> Duration.between(simulations.get(0).getCreationDate(), Instant.now()).compareTo(ServiceSettings.SIMULATION_LIFETIME) > 0);
    }
}
