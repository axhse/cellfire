package com.example.cellfire.algorithm;

import com.example.cellfire.models.Forecast;
import com.example.cellfire.models.ScenarioConditions;

public interface Algorithm {
    void refine(Forecast draftForecast, ScenarioConditions conditions);
}
