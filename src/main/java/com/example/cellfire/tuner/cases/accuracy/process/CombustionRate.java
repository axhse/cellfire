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

            byte forestType = ForestTypeConditions.ForestType.MIXED;
            Simulation.Conditions conditions = new Simulation.Conditions(
                    ForestTypeConditions.determineIgnitionTemperature(forestType),
                    ForestTypeConditions.determineActivationEnergy(forestType)
            );
            long duration = Duration.ofMinutes(30).toSeconds();

            Cell initiallCell = createCell(1000, 0.5f);
            double initialCombustionRate = (double) rateCalculator.invoke(algorithm, initiallCell, conditions);
            if (initialCombustionRate * duration < 0.3) {
                return ModelScore.failure("Initial combustion is too slow.");
            }

            Cell burningCell = createCell(800, 0.15f);
            double highCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (0.8 < highCombustionRate * duration) {
                return ModelScore.failure("Intensive combustion is too fast.");
            }

            burningCell = createCell(800, 0.3f);
            double moderateCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (moderateCombustionRate * duration < 0.1) {
                return ModelScore.failure("Moderate combustion is too slow.");
            }
            if (0.5 < moderateCombustionRate * duration) {
                return ModelScore.failure("Moderate combustion is too fast.");
            }

            burningCell = createCell(650, 0.15f);
            moderateCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (moderateCombustionRate * duration < 0.1) {
                return ModelScore.failure("Moderate combustion is too slow.");
            }
            if (0.6 < moderateCombustionRate * duration) {
                return ModelScore.failure("Moderate combustion is too fast.");
            }

            Cell smolderingCell = createCell(500, 0.3f);
            double smolderingRate = (double) rateCalculator.invoke(algorithm, smolderingCell, conditions);
            if (0.1 < smolderingRate * duration) {
                return ModelScore.failure("Smoldering is too fast.");
            }

            Cell boilingCell = createCell(850, 0.8f);
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
                new Weather(0, 0, airHumidity, 0, 0)
        );
    }
}
