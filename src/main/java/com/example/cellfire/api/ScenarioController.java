package com.example.cellfire.api;

import com.example.cellfire.api.params.ScenarioCreationParams;
import com.example.cellfire.api.params.SimulationParams;
import com.example.cellfire.api.params.ScenarioIdParams;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.models.SimulationStep;
import com.example.cellfire.services.ScenarioManager;
import com.example.cellfire.services.Simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ScenarioController {
    private final ScenarioManager scenarioManager;
    private final Simulator simulator;

    @Autowired
    public ScenarioController(ScenarioManager scenarioManager, Simulator simulator) {
        this.scenarioManager = scenarioManager;
        this.simulator = simulator;
    }

    @PostMapping("/scenario/create")
    public Map<String, Object> createScenario(@RequestBody ScenarioCreationParams params) {
        Scenario scenario = simulator.createScenario(params.getAlgorithm(),
                params.getStartCoordinates(),
                params.getStartDate()
                );
        simulator.startScenario(scenario);
        scenarioManager.addScenario(scenario);

        Map<String, Object> response = new HashMap<>();
        response.put("id", scenario.getId());
        response.put("simulation", scenario.getSimulation());
        return response;
    }

    @PostMapping("/scenario/remove")
    public void removeScenario(@RequestBody ScenarioIdParams params) {
        scenarioManager.removeScenario(params.getScenarioId());
    }

    @PostMapping("/scenario/simulate")
    public Map<String, Object> simulateScenario(@RequestBody SimulationParams params) {
        Map<String, Object> response = new HashMap<>();

        Scenario scenario = scenarioManager.getScenario(params.getScenarioId());
        if (scenario == null) {
            // TODO: return 4xx
            return response;
        }
        simulator.simulate(scenario, params.getEndStep());
        List<SimulationStep> steps = scenario.getSimulation().getSteps().subList(
                params.getStartStep(), params.getEndStep() + 1).stream().toList();
        response.put("steps", steps);
        return response;
    }
}
