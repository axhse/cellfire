package com.example.cellfire.api;

import com.example.cellfire.api.params.ScenarioCreationParams;
import com.example.cellfire.api.params.ScenarioRemovalParams;
import com.example.cellfire.entity.Scenario;
import com.example.cellfire.service.ScenarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ScenarioController {

    private final ScenarioService scenarioService;

    @Autowired
    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping("/scenario/create")
    public Map<String, String> create(@RequestBody ScenarioCreationParams params) {
        Scenario scenario = new Scenario(params.getStartPoint(), params.getStartDate());
        scenarioService.addScenario(scenario);

        Map<String, String> response = new HashMap<>();
        response.put("id", scenario.getId());
        return response;
    }

    @PostMapping("/scenario/remove")
    public void removeScenario(ScenarioRemovalParams params) {
        scenarioService.removeScenario(params.getId());
    }
}
