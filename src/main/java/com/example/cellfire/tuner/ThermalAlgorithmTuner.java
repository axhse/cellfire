package com.example.cellfire.tuner;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.tuner.cases.FlammableForestDoesNotBurnUnderHumidAir;
import com.example.cellfire.tuner.cases.ResilientForestBurnsUnderModerateFactors;
import com.example.cellfire.tuner.cases.TuneCase;

import java.util.List;

public final class ThermalAlgorithmTuner {
    public void tune() {
        Experiment experiment = createExperiment();
        experiment.run();
        experiment.printResults();
    }

    private Experiment createExperiment() {
        return new Experiment("Thermal algorithm", true, createTuneCases(), createParameters());
    }

    private List<TuneCase> createTuneCases() {
        return List.of(
            new FlammableForestDoesNotBurnUnderHumidAir(),
            new ResilientForestBurnsUnderModerateFactors()
        );
    }

    private List<Experiment.ModelParameter> createParameters() {
        return List.of(
            new Experiment.ModelParameter(
                    "COMBUSTION_RATE",
                    Experiment.ModelParameter.range(5, 9, 10)
            ),
            new Experiment.ModelParameter(
                    "ENERGY_EMISSION",
                    Experiment.ModelParameter.range(10000 * 0.5, 10000 * 20, 10)),
            new Experiment.ModelParameter(
                    "AIR_HUMIDITY_EFFECT",
                    Experiment.ModelParameter.range(0.5, 5, 20)),
            new Experiment.ModelParameter(
                    "SLOPE_EFFECT",
                    Experiment.ModelParameter.singleValue(ThermalAlgorithm.DEFAULT_SLOPE_EFFECT)
            ),
            new Experiment.ModelParameter(
                    "WIND_EFFECT",
                    Experiment.ModelParameter.singleValue(ThermalAlgorithm.DEFAULT_WIND_EFFECT)
            ),
            new Experiment.ModelParameter(
                    "CONVECTION_RATE",
                    Experiment.ModelParameter.singleValue(ThermalAlgorithm.DEFAULT_CONVECTION_RATE)
            ),
            new Experiment.ModelParameter(
                    "RADIATION_RATE",
                    Experiment.ModelParameter.singleValue(ThermalAlgorithm.DEFAULT_RADIATION_RATE)
            ),
            new Experiment.ModelParameter(
                    "DISTANCE_EFFECT",
                    Experiment.ModelParameter.singleValue(ThermalAlgorithm.DEFAULT_DISTANCE_EFFECT)
            )
        );
    }
}
