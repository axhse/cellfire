package com.example.cellfire.tuner.cases.accuracy.process;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.models.*;
import com.example.cellfire.tuner.experiment.TuneCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public final class CombustionRate extends TuneCase {
    public CombustionRate(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public CombustionRate(double weight) {
        super(weight);
    }

    public CombustionRate(boolean isObligatory) {
        super(isObligatory);
    }

    public CombustionRate() {
        super();
    }

    @Override
    protected ModelScore score(ThermalAlgorithm algorithm) {
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
            double initialCombustionRate = (double) rateCalculator.invoke(algorithm, initiallCell, conditions);
            if (initialCombustionRate * duration < 0.3) {
                return ModelScore.failure("Initial combustion is too slow.");
            }

            Cell burningCell = createCell(800, 0.2f);
            double highCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (0.8 < highCombustionRate * duration) {
                return ModelScore.failure("Intensive combustion is too fast.");
            }

            burningCell = createCell(750, 0.4f);
            double moderateCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (moderateCombustionRate * duration < 0.1) {
                return ModelScore.failure("Moderate combustion is too slow.");
            }
            if (0.5 < moderateCombustionRate * duration) {
                return ModelScore.failure("Moderate combustion is too fast.");
            }

            burningCell = createCell(700, 0.2f);
            moderateCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (moderateCombustionRate * duration < 0.1) {
                return ModelScore.failure("Moderate combustion is too slow.");
            }
            if (0.5 < moderateCombustionRate * duration) {
                return ModelScore.failure("Moderate combustion is too fast.");
            }

            Cell smolderingCell = createCell(600, 0.3f);
            double smolderingRate = (double) rateCalculator.invoke(algorithm, smolderingCell, conditions);
            if (0.1 < smolderingRate * duration) {
                return ModelScore.failure("Smoldering is too fast.");
            }

            Cell boilingCell = createCell(800, 0.8f);
            double boilingRate = (double) rateCalculator.invoke(algorithm, boilingCell, conditions);
            if (0.1 < boilingRate * duration) {
                return ModelScore.failure("Boiling is too fast.");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            return ModelScore.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        return ModelScore.victory();
    }

    private static Cell createCell(float heat, float airHumidity) {
        return new Cell(
                new Coordinates(0, 0),
                new CellState(heat, 0, true),
                new CellFactors(0, 0, airHumidity, 0, 0)
        );
    }
}
