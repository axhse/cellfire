package ru.cellularwildfire.tuner.experiment;

import java.util.ArrayList;
import java.util.List;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;

public final class TuneTask {
  private final String name;
  private final List<Criterion> criteria;
  private final List<ModelParameter> parameters;

  public TuneTask(String name, List<Criterion> criteria, List<ModelParameter> targetParameters) {
    this.name = name;
    this.criteria = criteria;
    this.parameters = specifyParameters(targetParameters);
  }

  public static List<ModelParameter> specifyParameters(List<ModelParameter> targetParameters) {
    ArrayList<ModelParameter> parameters = createDefaultParameters();
    for (ModelParameter parameter : targetParameters) {
      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).getName().equals(parameter.getName())) {
          parameters.set(i, parameter);
        }
      }
    }
    return parameters;
  }

  private static ArrayList<ModelParameter> createDefaultParameters() {
    return new ArrayList<>(
        List.of(
            new ModelParameter(
                ModelParameter.COMBUSTION_INTENSITY, ThermalAlgorithm.DEFAULT_COMBUSTION_INTENSITY),
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, ThermalAlgorithm.DEFAULT_ENERGY_EMISSION),
            new ModelParameter(
                ModelParameter.AIR_HUMIDITY_EFFECT, ThermalAlgorithm.DEFAULT_AIR_HUMIDITY_EFFECT),
            new ModelParameter(ModelParameter.SLOPE_EFFECT, ThermalAlgorithm.DEFAULT_SLOPE_EFFECT),
            new ModelParameter(ModelParameter.WIND_EFFECT, ThermalAlgorithm.DEFAULT_WIND_EFFECT),
            new ModelParameter(
                ModelParameter.DISTANCE_EFFECT, ThermalAlgorithm.DEFAULT_DISTANCE_EFFECT),
            new ModelParameter(
                ModelParameter.HEAT_REGULATION_INTENSITY,
                ThermalAlgorithm.DEFAULT_HEAT_REGULATION_INTENSITY),
            new ModelParameter(
                ModelParameter.RADIATION_PREVALENCE,
                ThermalAlgorithm.DEFAULT_RADIATION_PREVALENCE)));
  }

  public String getName() {
    return name;
  }

  public List<Criterion> getCriteria() {
    return criteria;
  }

  public List<ModelParameter> getParameters() {
    return parameters;
  }
}
