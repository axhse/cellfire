package com.example.cellfire.service;

import com.example.cellfire.entity.Scenario;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public final class ScenarioService {
    private final List<Scenario> scenarios = new ArrayList<>();

    @Nullable
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
}
