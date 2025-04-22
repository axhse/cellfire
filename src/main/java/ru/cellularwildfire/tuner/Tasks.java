package ru.cellularwildfire.tuner;

import java.util.List;
import ru.cellularwildfire.tuner.cases.process.CombustionRate;
import ru.cellularwildfire.tuner.cases.process.HeatRegulation;
import ru.cellularwildfire.tuner.cases.simulation.DryResilientForest;
import ru.cellularwildfire.tuner.cases.simulation.DryWindlessMixedForest;
import ru.cellularwildfire.tuner.cases.simulation.HumidFlammableForest;
import ru.cellularwildfire.tuner.cases.simulation.MediterraneanLargeFireEvent;
import ru.cellularwildfire.tuner.cases.simulation.SparseMixedForest;
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
            new Criterion(new MediterraneanLargeFireEvent())),
        List.of());
  }

  public static TuneTask tuneHeatRegulation() {
    return new TuneTask(
        "Heat regulation",
        List.of(new Criterion(new HeatRegulation())),
        List.of(
            new ModelParameter(
                ModelParameter.CONVECTION_INTENSITY,
                ModelParameter.logUnitRange(0.0001, 0.01, 100, 100)),
            new ModelParameter(
                ModelParameter.RADIATION_INTENSITY,
                ModelParameter.logUnitRange(Math.pow(10, -14), 0.01, 100, 100))));
  }

  public static TuneTask adjustScaleEffect() {
    return new TuneTask(
        "Scale effect",
        List.of(new Criterion(new HumidFlammableForest()), new Criterion(new DryResilientForest())),
        List.of(
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, ModelParameter.logUnitRange(25000, 0.2, 5, 20)),
            new ModelParameter(
                ModelParameter.SCALE_EFFECT, ModelParameter.logRange(100, 1000, 20))));
  }

  public static TuneTask adjustHumidityEffect() {
    return new TuneTask(
        "Humidity effect",
        List.of(
            new Criterion(new CombustionRate()),
            new Criterion(new HumidFlammableForest()),
            new Criterion(new DryResilientForest()),
            new Criterion(new SparseMixedForest()),
            new Criterion(new MediterraneanLargeFireEvent())),
        List.of(
            new ModelParameter(
                ModelParameter.COMBUSTION_INTENSITY, ModelParameter.logRange(150, 150, 30)),
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, ModelParameter.logRange(10000, 50000, 30)),
            new ModelParameter(ModelParameter.SCALE_EFFECT, ModelParameter.logRange(10, 1000, 30)),
            new ModelParameter(
                ModelParameter.AIR_HUMIDITY_EFFECT, ModelParameter.logRange(3, 10, 30))));
  }
}
