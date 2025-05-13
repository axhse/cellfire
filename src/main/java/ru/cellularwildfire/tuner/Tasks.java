package ru.cellularwildfire.tuner;

import java.util.List;
import ru.cellularwildfire.tuner.cases.Performance;
import ru.cellularwildfire.tuner.cases.process.CombustionRate;
import ru.cellularwildfire.tuner.cases.process.HeatRegulation;
import ru.cellularwildfire.tuner.cases.simulation.AlgarveFire;
import ru.cellularwildfire.tuner.cases.simulation.DryResilientForest;
import ru.cellularwildfire.tuner.cases.simulation.DryWindlessMixedForest;
import ru.cellularwildfire.tuner.cases.simulation.HumidFlammableForest;
import ru.cellularwildfire.tuner.cases.simulation.SparseMixedForest;
import ru.cellularwildfire.tuner.cases.simulation.StColomaFire;
import ru.cellularwildfire.tuner.experiment.Criterion;
import ru.cellularwildfire.tuner.experiment.ModelParameter;
import ru.cellularwildfire.tuner.experiment.TuneTask;

public final class Tasks {
  public static TuneTask validateDefault() {
    return new TuneTask(
        "Default model",
        List.of(
            new Criterion(new CombustionRate()),
            new Criterion(new HeatRegulation()),
            new Criterion(new HumidFlammableForest()),
            new Criterion(new DryResilientForest()),
            new Criterion(new SparseMixedForest()),
            new Criterion(new DryWindlessMixedForest()),
            new Criterion(new AlgarveFire()),
            new Criterion(new StColomaFire())),
        List.of());
  }

  public static TuneTask evaluatePerformance() {
    return new TuneTask("Performance", List.of(new Criterion(new Performance())), List.of());
  }

  public static TuneTask tuneHeatRegulation() {
    return new TuneTask(
        "Heat regulation",
        List.of(new Criterion(new HeatRegulation())),
        List.of(
            new ModelParameter(
                ModelParameter.CONVECTION_INTENSITY,
                ModelParameter.logUnitRange(0.3, 0.1, 10, 100)),
            new ModelParameter(
                ModelParameter.RADIATION_INTENSITY,
                ModelParameter.logUnitRange(4 * Math.pow(10, -11), 0.05, 10, 10))));
  }

  public static TuneTask balanceCombustion() {
    return new TuneTask(
        "Combustion",
        List.of(
            new Criterion(new CombustionRate()),
            new Criterion(new HumidFlammableForest()),
            new Criterion(new DryResilientForest()),
            new Criterion(new SparseMixedForest()),
            new Criterion(new DryWindlessMixedForest()),
            new Criterion(new AlgarveFire()),
            new Criterion(new StColomaFire())),
        List.of(
            new ModelParameter(
                ModelParameter.COMBUSTION_INTENSITY,
                ModelParameter.logUnitRange(25_000_000, 0.5, 2, 30)),
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, ModelParameter.logUnitRange(29_000, 0.5, 2, 30)),
            new ModelParameter(
                ModelParameter.PROPAGATION_INTENSITY,
                ModelParameter.logUnitRange(0.11, 0.1, 10, 30)),
            new ModelParameter(
                ModelParameter.HUMIDITY_EFFECT, ModelParameter.logRange(0.5, 10, 20))));
  }
}
