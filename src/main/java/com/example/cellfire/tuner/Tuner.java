package com.example.cellfire.tuner;

import com.example.cellfire.tuner.cases.accuracy.simulation.FlammableForestHumidAir;
import com.example.cellfire.tuner.cases.accuracy.simulation.ResilientForestModerateFactors;
import com.example.cellfire.tuner.cases.accuracy.process.CombustionRate;
import com.example.cellfire.tuner.cases.accuracy.process.HeatExchange;
import com.example.cellfire.tuner.cases.efficiency.DraftStepCreation;
import com.example.cellfire.tuner.experiment.Experiment;
import com.example.cellfire.tuner.experiment.ModelParameter;
import com.example.cellfire.tuner.experiment.TuneTask;

import java.util.List;

public final class Tuner {
    public void run() {
        createExperiment().run().print();
    }

    private Experiment createExperiment() {
        return new Experiment(true, validateDefault());
    }

    private TuneTask validateDefault() {
        return new TuneTask(
                "Default model validation",
                List.of(
                        new CombustionRate(),
                        new HeatExchange(),
                        new FlammableForestHumidAir(),
                        new ResilientForestModerateFactors()
                ),
                List.of()
        );
    }

    private TuneTask optimizeDraftStepCreation() {
        return new TuneTask(
                "Draft step creation",
                List.of(new DraftStepCreation(DraftStepCreation.CopyingAlgorithm.RANDOM_POINTER_NEIGHBOR_HASHMAP)),
                List.of()
        );
    }

    private TuneTask tuneHeatExchange() {
        return new TuneTask(
                "Heat exchange",
                List.of(new HeatExchange()),
                List.of(
                        new ModelParameter(
                                ModelParameter.HEAT_REGULATION_INTENSITY,
                                ModelParameter.logRange(0.0001, 0.01, 100, 100)
                        ),
                        new ModelParameter(
                                ModelParameter.RADIATION_PREVALENCE,
                                ModelParameter.logRange(Math.pow(10, -10), 0.01, 100, 100)
                        )
                )
        );
    }

    private TuneTask tuneCombustionRate() {
        return new TuneTask(
                "Combustion rate",
                List.of(new CombustionRate()),
                List.of(
                        new ModelParameter(
                                ModelParameter.COMBUSTION_INTENSITY,
                                ModelParameter.logRange(10000, 0.1, 1000, 200)
                        ),
                        new ModelParameter(
                                ModelParameter.AIR_HUMIDITY_EFFECT,
                                ModelParameter.logRange(1, 10, 20)
                        )
                )
        );
    }

    private TuneTask adjustHumidityEffect() {
        return new TuneTask(
                "Humidity effect",
                List.of(
                        new CombustionRate(),
                        new FlammableForestHumidAir(),
                        new ResilientForestModerateFactors()
                ),
                List.of(
                        new ModelParameter(
                                ModelParameter.COMBUSTION_INTENSITY,
                                ModelParameter.logRange(10000, 0.5, 20, 20)
                        ),
                        new ModelParameter(
                                ModelParameter.ENERGY_EMISSION,
                                ModelParameter.logRange(10000, 0.5, 20, 100)
                        ),
                        new ModelParameter(
                                ModelParameter.AIR_HUMIDITY_EFFECT,
                                ModelParameter.logRange(3, 10, 50)
                        )
                )
        );
    }
}
