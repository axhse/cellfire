package com.example.cellfire.tuner;

import com.example.cellfire.tuner.cases.*;
import com.example.cellfire.tuner.experiment.Experiment;
import com.example.cellfire.tuner.experiment.ModelParameter;
import com.example.cellfire.tuner.experiment.TuneTask;

import java.util.List;

public final class Tuner {
    public void run() {
        new Experiment(true, createTask()).run().print();
    }

    private TuneTask createTask() {
        return tuneDraftStepCreationAlgorithm();
    }

    private TuneTask validateDefault() {
        return new TuneTask(
                "Default model validation",
                List.of(
                        new HeatExchangesProperly(),
                        new FuelCombustsWithReasonableRate(),
                        new FlammableForestDoesNotBurnUnderHumidAir(),
                        new ResilientForestBurnsUnderModerateFactors()
                ),
                List.of()
        );
    }

    private TuneTask tuneDraftStepCreationAlgorithm() {
        return new TuneTask(
                "Draft step creation algorithm",
                List.of(new DraftStepCreatesQuickly(
                        DraftStepCreatesQuickly.CopyingAlgorithm.RANDOM_POINTER_NEIGHBOR_HASHMAP
                )),
                List.of()
        );
    }

    private TuneTask tuneCombustionRate() {
        return new TuneTask(
                "Combustion rate",
                List.of(new FuelCombustsWithReasonableRate()),
                List.of(
                        new ModelParameter(
                                ModelParameter.COMBUSTION_RATE,
                                ModelParameter.logRange(1, 100, 1000)
                        ),
                        new ModelParameter(
                                ModelParameter.AIR_HUMIDITY_EFFECT,
                                ModelParameter.logRange(1, 10, 100)
                        )
                )
        );
    }

    private TuneTask tuneHeatExchange() {
        return new TuneTask(
                "Heat exchange",
                List.of(new HeatExchangesProperly()),
                List.of(
                        new ModelParameter(
                                ModelParameter.HEAT_REGULATION_DURATION,
                                0.16
                        ),
                        new ModelParameter(
                                ModelParameter.RADIATION_PREVALENCE,
                                ModelParameter.logRange(Math.pow(10, -10), 0.01, 100, 1000)
                        )
                )
        );
    }

    private TuneTask tuneHumidityEffect() {
        return new TuneTask(
                "Humidity effect",
                List.of(
                        new FuelCombustsWithReasonableRate(),
                        new FlammableForestDoesNotBurnUnderHumidAir(),
                        new ResilientForestBurnsUnderModerateFactors()
                ),
                List.of(
                        new ModelParameter(
                                ModelParameter.COMBUSTION_RATE,
                                ModelParameter.logRange(1, 100, 1000)
                        ),
                        new ModelParameter(
                                ModelParameter.ENERGY_EMISSION,
                                ModelParameter.logRange(Math.pow(10, 7), 0.1, 50, 30)
                        ),
                        new ModelParameter(
                                ModelParameter.AIR_HUMIDITY_EFFECT,
                                ModelParameter.logRange(2, 20, 30)
                        )
                )
        );
    }
}
