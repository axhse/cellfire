package com.example.cellfire.tuner;

import com.example.cellfire.tuner.cases.FlammableForestDoesNotBurnUnderHumidAir;
import com.example.cellfire.tuner.cases.ResilientForestBurnsUnderModerateFactors;
import com.example.cellfire.tuner.experiment.Experiment;
import com.example.cellfire.tuner.experiment.ModelParameter;

import java.util.List;

public final class ThermalAlgorithmTuner {
    public void run() {
        new Experiment(true, createTask()).run().print();
    }

    private TuneTask createTask() {
        return validate();
    }

    private TuneTask validate() {
        return new TuneTask(
                "Validate",
                List.of(
                        new FlammableForestDoesNotBurnUnderHumidAir(),
                        new ResilientForestBurnsUnderModerateFactors()
                ),
                List.of()
        );
    }

    private TuneTask tuneHumidityEffect() {
        return new TuneTask(
                "Tune Humidity Effect",
                List.of(
                        new FlammableForestDoesNotBurnUnderHumidAir(),
                        new ResilientForestBurnsUnderModerateFactors()
                ),
                List.of(
                        new ModelParameter(ModelParameter.COMBUSTION_RATE, 5, 9, 10),
                        new ModelParameter(ModelParameter.ENERGY_EMISSION, 10000 * 0.5, 10000 * 20, 10),
                        new ModelParameter(ModelParameter.AIR_HUMIDITY_EFFECT, 0.5, 5, 20)                )
        );
    }
}
