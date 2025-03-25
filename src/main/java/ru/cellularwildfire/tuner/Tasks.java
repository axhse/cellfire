package ru.cellularwildfire.tuner;

import java.util.List;
import ru.cellularwildfire.tuner.cases.accuracy.process.CombustionRate;
import ru.cellularwildfire.tuner.cases.accuracy.process.HeatExchange;
import ru.cellularwildfire.tuner.cases.accuracy.simulation.FlammableForestHumidAir;
import ru.cellularwildfire.tuner.cases.accuracy.simulation.ResilientForestModerateFactors;
import ru.cellularwildfire.tuner.cases.efficiency.DraftStepCreation;
import ru.cellularwildfire.tuner.experiment.Criterion;
import ru.cellularwildfire.tuner.experiment.ModelParameter;
import ru.cellularwildfire.tuner.experiment.TuneTask;

public final class Tasks {
  public static TuneTask validateDefault() {
    return new TuneTask(
        "Default model validation",
        List.of(
            new Criterion(new CombustionRate()),
            new Criterion(new HeatExchange()),
            new Criterion(new FlammableForestHumidAir()),
            new Criterion(new ResilientForestModerateFactors())),
        List.of());
  }

  public static TuneTask optimizeDraftStepCreation() {
    DraftStepCreation.CopyingAlgorithm copyingAlgorithm =
        DraftStepCreation.CopyingAlgorithm.RANDOM_POINTER_NEIGHBOR_HASHMAP;

    return new TuneTask(
        "Draft step creation",
        List.of(new Criterion(new DraftStepCreation(copyingAlgorithm))),
        List.of());
  }

  public static TuneTask tuneHeatExchange() {
    return new TuneTask(
        "Heat exchange",
        List.of(new Criterion(new HeatExchange())),
        List.of(
            new ModelParameter(
                ModelParameter.HEAT_REGULATION_INTENSITY,
                ModelParameter.logRange(0.0001, 0.01, 100, 100)),
            new ModelParameter(
                ModelParameter.RADIATION_PREVALENCE,
                ModelParameter.logRange(Math.pow(10, -10), 0.01, 100, 100))));
  }

  public static TuneTask tuneCombustionRate() {
    return new TuneTask(
        "Combustion rate",
        List.of(new Criterion(new CombustionRate())),
        List.of(
            new ModelParameter(
                ModelParameter.COMBUSTION_INTENSITY,
                ModelParameter.logRange(10000, 0.1, 1000, 200)),
            new ModelParameter(
                ModelParameter.AIR_HUMIDITY_EFFECT, ModelParameter.logRange(1, 10, 20))));
  }

  public static TuneTask adjustHumidityEffect() {
    return new TuneTask(
        "Humidity effect",
        List.of(
            new Criterion(new CombustionRate()),
            new Criterion(new FlammableForestHumidAir()),
            new Criterion(new ResilientForestModerateFactors())),
        List.of(
            new ModelParameter(
                ModelParameter.COMBUSTION_INTENSITY, ModelParameter.logRange(10000, 0.5, 20, 20)),
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, ModelParameter.logRange(10000, 0.5, 20, 100)),
            new ModelParameter(
                ModelParameter.AIR_HUMIDITY_EFFECT, ModelParameter.logRange(3, 10, 50))));
  }
}
