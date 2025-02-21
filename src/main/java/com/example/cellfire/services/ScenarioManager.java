package com.example.cellfire.services;

import com.example.cellfire.models.Scenario;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public final class ScenarioManager {
    private final List<Scenario> scenarios = new ArrayList<>();

    public Scenario getScenario(String id)
    {
        for (Scenario scenario : scenarios) {
            if (scenario.getId().equals(id)) {
                return scenario;
            }
        }
        return null;
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
    }

    public void removeScenario(String id)
    {
        scenarios.removeIf(scenario -> scenario.getId().equals(id));
    }

    public void revise()
    {
        scenarios.removeIf(scenario -> Duration.between(scenarios.get(0).getCreationDate(), Instant.now()).compareTo(ServiceSettings.SCENARIO_LIFETIME) > 0);
    }
}
