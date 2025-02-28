package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.data.ForestConditions;
import com.example.cellfire.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public final class FuelCombustsWithReasonableRate extends TuneCase {
    public FuelCombustsWithReasonableRate(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public FuelCombustsWithReasonableRate(double weight) {
        super(weight);
    }

    public FuelCombustsWithReasonableRate(boolean isObligatory) {
        super(isObligatory);
    }

    public FuelCombustsWithReasonableRate() {
        super();
    }

    @Override
    protected ModelScore score(Algorithm algorithm) {
        String rateCalculatorName = "calculateCombustionRate";
        try {
            Method rateCalculator = algorithm.getClass().getDeclaredMethod(
                    rateCalculatorName, Cell.class, Simulation.Conditions.class
            );
            rateCalculator.setAccessible(true);

            byte forestType = ForestConditions.ForestType.MIXED;
            Simulation.Conditions conditions = new Simulation.Conditions(
                    ForestConditions.determineIgnitionTemperature(forestType),
                    ForestConditions.determineActivationEnergy(forestType)
            );
            Cell initiallCell = createCell(1000, 0.2f);
            double initialCombustionRate = (double) rateCalculator.invoke(algorithm, initiallCell, conditions);
            if (initialCombustionRate * Duration.ofMinutes(30).toSeconds() < 0.3) {
                return ModelScore.failure("Initial combustion is too slow.");
            }
            if (0.7 < initialCombustionRate * Duration.ofMinutes(30).toSeconds()) {
                return ModelScore.failure("Initial combustion is too fast.");
            }

            Cell burningCell = createCell(800, 0.2f);
            double moderateCombustionRate = (double) rateCalculator.invoke(algorithm, burningCell, conditions);
            if (moderateCombustionRate * Duration.ofMinutes(30).toSeconds() < 0.1) {
                return ModelScore.failure("Initial combustion is too slow.");
            }
            if (0.4 < moderateCombustionRate * Duration.ofMinutes(30).toSeconds()) {
                return ModelScore.failure("Initial combustion is too fast.");
            }

            Cell smolderingCell = createCell(600, 0.3f);
            double smolderingRate = (double) rateCalculator.invoke(algorithm, smolderingCell, conditions);
            if (0.1 < smolderingRate * Duration.ofMinutes(30).toSeconds()) {
                return ModelScore.failure("Smoldering combustion is too fast.");
            }

            Cell boilingCell = createCell(850, 0.6f);
            double boilingRate = (double) rateCalculator.invoke(algorithm, boilingCell, conditions);
            if (0.1 < boilingRate * Duration.ofMinutes(30).toSeconds()) {
                return ModelScore.failure("Boiling combustion is too fast.");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            return ModelScore.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        return ModelScore.victory();
    }

    private Cell createCell(float heat, float airHumidity) {
        return new Cell(
                new Coordinates(0, 0),
                new CellState(heat, 0, true),
                new Weather(0, 0, airHumidity, 0, 0)
        );
    }
}
