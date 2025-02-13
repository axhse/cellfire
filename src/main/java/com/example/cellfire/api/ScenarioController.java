package com.example.cellfire.api;

import com.example.cellfire.api.params.ScenarioCreationParams;
import com.example.cellfire.api.params.ScenarioForecastParams;
import com.example.cellfire.api.params.ScenarioIdParams;
import com.example.cellfire.models.ForecastLog;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.services.ForecastService;
import com.example.cellfire.services.ScenarioService;

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
    public Map<String, Object> createScenario(@RequestBody ScenarioCreationParams params) {
        Scenario scenario = new Scenario(params.getAlgorithm(),
                params.getStartDate(), forecastService.determineConditions(
                params.getStartCoordinates()));
        forecastService.initiate(scenario, params.getStartCoordinates());
        scenarioService.addScenario(scenario);

        Map<String, Object> response = new HashMap<>();
        response.put("scenarioId", scenario.getId());
        response.put("conditions", scenario.getConditions());
        response.put("forecastLog", scenario.getForecastLog());
        return response;
    }

    @PostMapping("/scenario/remove")
    public void removeScenario(@RequestBody ScenarioIdParams params) {
        scenarioService.removeScenario(params.getScenarioId());
    }

    @PostMapping("/scenario/forecast")
    public Map<String, Object> forecastScenario(@RequestBody ScenarioForecastParams params) {
        Map<String, Object> response = new HashMap<>();

        Scenario scenario = scenarioService.getScenario(params.getScenarioId());
        if (scenario == null) {
            // TODO: return 4xx
            return response;
        }
        forecastService.forecast(scenario, params.getEndStep());
        ForecastLog partialForecastLog = new ForecastLog();
        partialForecastLog.getForecasts().addAll(scenario.getForecastLog().getForecasts().subList(
                params.getStartStep(), params.getEndStep() + 1));
        response.put("partialForecastLog", partialForecastLog);
        return response;
    }
}
