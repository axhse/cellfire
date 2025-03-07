package com.example.cellfire.api;

import com.example.cellfire.api.params.SimulationCreationParams;
import com.example.cellfire.api.params.SimulationProgressParams;
import com.example.cellfire.api.params.SimulationIdParams;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.SimulationManager;
import com.example.cellfire.services.Simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public final class SimulationController {
    private final SimulationManager simulationManager;
    private final Simulator simulator;

    @Autowired
    public SimulationController(SimulationManager simulationManager, Simulator simulator) {
        this.simulationManager = simulationManager;
        this.simulator = simulator;
    }

    @PostMapping("/simulation/create")
    public Map<String, Object> createSimulation(@RequestBody SimulationCreationParams params) {
        Simulation simulation = simulator.createSimulation(params.getStartPoint(), params.getAlgorithm());
        simulator.startSimulation(simulation);
        simulationManager.addSimulation(simulation);

        Map<String, Object> response = new HashMap<>();
        response.put("simulation", simulation);
        return response;
    }

    @PostMapping("/simulation/remove")
    public void removeSimulation(@RequestBody SimulationIdParams params) {
        simulationManager.removeSimulation(params.getSimulationId());
    }

    @PostMapping("/simulation/progress")
    public Map<String, Object> progressSimulation(@RequestBody SimulationProgressParams params) {
        Map<String, Object> response = new HashMap<>();
        Optional<Simulation> simulation = simulationManager.findSimulation(params.getSimulationId());
        response.put("hasResult", simulation.isPresent());
        if (simulation.isEmpty()) {
            return response;
        }
        simulator.progressSimulation(simulation.get(), params.getEndTick());
        List<Simulation.Step> steps =
                simulation.get().getSteps().subList(params.getStartTick(), params.getEndTick() + 1).stream().toList();
        response.put("steps", steps);
        return response;
    }
}
