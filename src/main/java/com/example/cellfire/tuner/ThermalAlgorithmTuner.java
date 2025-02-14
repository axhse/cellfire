package com.example.cellfire.tuner;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.tuner.cases.FlammableForestDoesNotBurnUnderHumidAir;
import com.example.cellfire.tuner.cases.ResilientForestBurnsUnderModerateFactors;
import com.example.cellfire.tuner.cases.TuneCase;

import java.util.List;

public final class ThermalAlgorithmTuner {
    public void tune() {
        tune(new ThermalAlgorithm());
    }

    private void tune(Algorithm algorithm) {
        Experiment experiment = createExperiment(algorithm);
        experiment.run();
        experiment.printResults();
    }

    private Experiment createExperiment(Algorithm algorithm) {
        return new Experiment("Thermal algorithm", true, createTuneCases(algorithm), createParameters());
    }

    private List<TuneCase> createTuneCases(Algorithm algorithm) {
        return List.of(
            new FlammableForestDoesNotBurnUnderHumidAir(algorithm),
            new ResilientForestBurnsUnderModerateFactors(algorithm)
        );
    }

    private List<Experiment.ModelParameter> createParameters() {
        Class<?> fieldClass = ThermalAlgorithm.class;
        return List.of(
            new Experiment.ModelParameter(
                    fieldClass,
                    "COMBUSTION_RATE",
                    Experiment.ModelParameter.range(5, 9, 10)
            ),
            new Experiment.ModelParameter(
                    fieldClass,
                    "ENERGY_EMISSION",
                    Experiment.ModelParameter.range(10000 * 0.5, 10000 * 20, 10)),
            new Experiment.ModelParameter(
                    fieldClass,
                    "AIR_HUMIDITY_EFFECT",
                    Experiment.ModelParameter.range(0.5, 5, 20))
        );
    }
}
