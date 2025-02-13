package com.example.cellfire.tuner;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.tuner.cases.FlammableForestDoesNotBurnUnderHumidAir;
import com.example.cellfire.tuner.cases.ResilientForestBurnsUnderModerateFactors;
import com.example.cellfire.tuner.cases.TuneCase;

import java.util.ArrayList;
import java.util.List;

public final class ThermalAlgorithmTuner {
    public void tune() {
        ThermalAlgorithm algorithm = new ThermalAlgorithm();
        tune(algorithm);
    }

    private void tune(ThermalAlgorithm algorithm) {
        int failures = 0;
        double score = 0;
        List<TuneCase<ThermalAlgorithm>> tuneCases = createTuneCases(algorithm);
        for (TuneCase<ThermalAlgorithm> tuneCase : tuneCases) {
            tuneCase.evaluate();
            failures += tuneCase.getScore() < 0 ? 1 : 0;
            score += tuneCase.getScore() < 0 ? 0 : tuneCase.getWeightedScore();
        }
        System.out.println(score);
        System.out.println(failures);
        System.out.println(tuneCases.size());
    }

    private List<TuneCase<ThermalAlgorithm>> createTuneCases(ThermalAlgorithm algorithm) {
        List<TuneCase<ThermalAlgorithm>> tuneCases = new ArrayList<>();
        tuneCases.add(new FlammableForestDoesNotBurnUnderHumidAir(algorithm));
        tuneCases.add(new ResilientForestBurnsUnderModerateFactors(algorithm));
        return tuneCases;
    }
}
