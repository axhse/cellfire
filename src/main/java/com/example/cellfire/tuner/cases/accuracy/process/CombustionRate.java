package com.example.cellfire.tuner.cases.accuracy.process;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.models.*;
import com.example.cellfire.tuner.experiment.Assessment;
import com.example.cellfire.tuner.experiment.TuneCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public final class CombustionRate extends TuneCase {
    @Override
    public void assess(ThermalAlgorithm algorithm, Assessment assessment) throws TuneCaseFailedException {
        try {
            Method rateCalculator = ThermalAlgorithm.class.getDeclaredMethod(
                    "calculateCombustionRate", Cell.class, Simulation.Conditions.class
            );
            rateCalculator.setAccessible(true);

            int forestType = ForestTypeConditions.ForestType.MIXED;
            double activationEnergy = ForestTypeConditions.determineActivationEnergy(forestType);
            Simulation.Conditions conditions = new Simulation.Conditions(activationEnergy);
            long duration = Duration.ofMinutes(30).toSeconds();

            Cell initiallCell = createCell(1000, 0.5f);
            double rate = (double) rateCalculator.invoke(algorithm, initiallCell, conditions);
            assessment.requireMoreThan(rate * duration, 0.3, "Initial rate");

            Cell burningCell = createCell(800, 0.2f);
            rate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            assessment.requireInRange(rate * duration, 0, 0.8, "Intensive rate");

            burningCell = createCell(750, 0.4f);
            rate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            assessment.requireInRange(rate * duration, 0.1, 0.5, "Moderate rate");

            burningCell = createCell(700, 0.2f);
            rate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            assessment.requireInRange(rate * duration, 0.1, 0.5, "Moderate rate");

            Cell smolderingCell = createCell(600, 0.3f);
            rate = (double) rateCalculator.invoke(algorithm, smolderingCell, conditions);
            assessment.requireInRange(rate * duration, 0, 0.1, "Smoldering rate");

            Cell boilingCell = createCell(800, 0.8f);
            rate = (double) rateCalculator.invoke(algorithm, boilingCell, conditions);
            assessment.requireInRange(rate * duration, 0, 0.1, "Boiling rate");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            assessment.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private static Cell createCell(double heat, double airHumidity) {
        return new Cell(
                new Coordinates(0, 0),
                new Cell.State(heat, 0, true),
                new Cell.Factors(0, new Weather(0, airHumidity, 0, 0))
        );
    }
}
