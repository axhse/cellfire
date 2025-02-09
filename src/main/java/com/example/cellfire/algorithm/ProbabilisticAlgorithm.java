package com.example.cellfire.algorithm;

import com.example.cellfire.models.*;
import org.springframework.stereotype.Service;

@Service
public final class ProbabilisticAlgorithm implements Algorithm {
    private static final double BASIC_PROBABILITY = 0.6;
    private static final double SLOPE_EFFECT = 3;
    private static final double WIND_EFFECT = 0.15;

    @Override
    public void refine(Forecast draftForecast, ScenarioConditions conditions) {
        draftForecast.getCells().forEach(this::applyRules);
    }

    private void applyRules(Cell cell) {
        if (cell.getFire().getFuel() == 0) {
            return;
        }
    }
}
