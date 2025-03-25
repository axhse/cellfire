package com.example.cellfire.tuner.experiment;

import com.example.cellfire.algorithms.ThermalAlgorithm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TuneTask {
    private final String name;
    private final List<Criterion> criteria;
    private final List<ModelParameter> parameters;

    public TuneTask(String name, List<Criterion> criteria, List<ModelParameter> targetParameters) {
        this.name = name;
        this.criteria = criteria;
        this.parameters = specifyParameters(targetParameters);
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

    public static List<ModelParameter> specifyParameters(List<ModelParameter> targetParameters) {
        List<ModelParameter> parameters = createDefaultParameters();
        for (ModelParameter parameter : targetParameters) {
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getName().equals(parameter.getName())) {
                    parameters.set(i, parameter);
                }
            }
        }
        return parameters;
    }

    private static List<ModelParameter> createDefaultParameters() {
        List<ModelParameter> parameters = new ArrayList<>();
        Constructor<?>[] constructors = ThermalAlgorithm.class.getConstructors();
        Constructor<?> explicitConstructor = Arrays.stream(constructors).filter(c -> c.getParameterCount() > 1).findFirst().orElseThrow();
        for (Parameter parameter : explicitConstructor.getParameters()) {
            try {
                Field field = ThermalAlgorithm.class.getDeclaredField(parameter.getName());
                field.setAccessible(true);
                double defaultValue = field.getDouble(new ThermalAlgorithm());
                parameters.add(new ModelParameter(parameter.getName(), defaultValue));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return parameters;
    }
}
