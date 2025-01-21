package com.example.cellfire.api;

import com.example.cellfire.api.params.ScenarioCreationParams;
import com.example.cellfire.api.params.ScenarioForecastParams;
import com.example.cellfire.api.params.ScenarioIdParams;
import com.example.cellfire.entity.InstantForecast;
import com.example.cellfire.entity.Scenario;
import com.example.cellfire.service.ForecastService;
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
    private final ForecastService forecastService;

    @Autowired
    public ScenarioController(ScenarioService scenarioService, ForecastService forecastService) {
        this.scenarioService = scenarioService;
        this.forecastService = forecastService;
    }

    @PostMapping("/scenario/create")
    public Map<String, Object> create(@RequestBody ScenarioCreationParams params) {
        Scenario scenario = new Scenario(params.getStartPoint(), params.getStartDate(), forecastService.createInitialFire());
        scenarioService.addScenario(scenario);

        Map<String, Object> response = new HashMap<>();
        response.put("scenarioId", scenario.getId());
        return response;
    }

    @PostMapping("/scenario/remove")
    public void removeScenario(@RequestBody ScenarioIdParams params) {
        scenarioService.removeScenario(params.getScenarioId());
    }

    @PostMapping("/scenario/forecast")
    public Map<String, Object> forecastScenario(@RequestBody ScenarioForecastParams params) {
        Map<String, Object> response = new HashMap<>();

        if (!scenarioService.hasScenario(params.getScenarioId())) {
            // TODO: return 4xx
            return response;
        }

        Scenario scenario = scenarioService.getScenario(params.getScenarioId());
        InstantForecast forecast = forecastService.forecast(scenario, params.getActualDate());
        response.put("forecast", forecast);
        return response;
    }
}
