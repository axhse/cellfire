package ru.cellularwildfire.tuner.experiment;

import java.util.ArrayList;
import java.util.List;
import ru.cellularwildfire.services.AutomatonAlgorithm;

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
                ModelParameter.COMBUSTION_INTENSITY,
                AutomatonAlgorithm.DEFAULT_COMBUSTION_INTENSITY),
            new ModelParameter(
                ModelParameter.ENERGY_EMISSION, AutomatonAlgorithm.DEFAULT_ENERGY_EMISSION),
            new ModelParameter(
                ModelParameter.PROPAGATION_INTENSITY,
                AutomatonAlgorithm.DEFAULT_PROPAGATION_INTENSITY),
            new ModelParameter(
                ModelParameter.CONVECTION_INTENSITY,
                AutomatonAlgorithm.DEFAULT_CONVECTION_INTENSITY),
            new ModelParameter(
                ModelParameter.RADIATION_INTENSITY, AutomatonAlgorithm.DEFAULT_RADIATION_INTENSITY),
            new ModelParameter(
                ModelParameter.HUMIDITY_EFFECT, AutomatonAlgorithm.DEFAULT_HUMIDITY_EFFECT),
            new ModelParameter(
                ModelParameter.SLOPE_EFFECT, AutomatonAlgorithm.DEFAULT_SLOPE_EFFECT),
            new ModelParameter(
                ModelParameter.WIND_EFFECT, AutomatonAlgorithm.DEFAULT_WIND_EFFECT)));
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
