package com.example.cellfire.api;

import com.example.cellfire.api.params.SimulationCreationParams;
import com.example.cellfire.api.params.SimulationIdParams;
import com.example.cellfire.api.params.SimulationProgressParams;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.SimulationManager;
import com.example.cellfire.services.Simulator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        Map<String, Object> response = new HashMap<>();

        Simulation simulation = simulator.createSimulation(params.getStartPoint(), params.getAlgorithm());

        if (simulator.tryStartSimulation(simulation)) {
            response.put("success", true);
            response.put("simulation", simulation);
            simulationManager.addSimulation(simulation);
        } else {
            response.put("success", false);
        }

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
        if (simulation.isEmpty()) {
            response.put("success", false);
            return response;
        }

        if (simulator.tryProgressSimulation(simulation.get(), params.getEndTick())) {
            response.put("success", true);
            List<Simulation.Step> steps = simulation.get().getSteps();
            int toIndex = Math.min(params.getEndTick() + 1, steps.size());
            List<Simulation.Step> lastSteps = steps.subList(params.getStartTick(), toIndex).stream().toList();
            response.put("steps", lastSteps);
        } else {
            response.put("success", false);
        }
        return response;
    }
}
