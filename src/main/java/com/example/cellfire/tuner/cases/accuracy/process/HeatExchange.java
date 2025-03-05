package com.example.cellfire.tuner.cases.accuracy.process;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.*;
import com.example.cellfire.tuner.experiment.TuneCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public final class HeatExchange extends TuneCase {
    public HeatExchange(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public HeatExchange(double weight) {
        super(weight);
    }

    public HeatExchange(boolean isObligatory) {
        super(isObligatory);
    }

    public HeatExchange() {
        super();
    }

    @Override
    protected ModelScore score(ThermalAlgorithm algorithm) {
        try {
            Method heatRegulator = ThermalAlgorithm.class.getDeclaredMethod(
                    "regulateHeat", Cell.class, Simulation.class
            );
            heatRegulator.setAccessible(true);

            Simulation simulation = createDefaultSimulation(Duration.ofMinutes(30));

            Cell overheatedCell = createCell(5000);
            heatRegulator.invoke(algorithm, overheatedCell, simulation);
            double overheatedCellHeat = overheatedCell.getState().getHeat();
            if (overheatedCellHeat < 700) {
                return ModelScore.failure("Overheated cell cools down too fast.");
            }
            if (900 < overheatedCellHeat) {
                return ModelScore.failure("Overheated cell cools down too slow.");
            }

            Cell hotCell = createCell(1300);
            heatRegulator.invoke(algorithm, hotCell, simulation);
            double hotCellHeat = hotCell.getState().getHeat();
            if (hotCellHeat < 600) {
                return ModelScore.failure("Hot cell cools down too fast.");
            }
            if (850 < hotCellHeat) {
                return ModelScore.failure("Hot cell cools down too slow.");
            }

            Cell warmCell = createCell(700);
            heatRegulator.invoke(algorithm, warmCell, simulation);
            double warmCellHeat = warmCell.getState().getHeat();
            if (warmCellHeat < 400) {
                return ModelScore.failure("Warm cell cools down too fast.");
            }
            if (550 < warmCellHeat) {
                return ModelScore.failure("Warm cell cools down too slow.");
            }

            Cell coldCell = createCell(200);
            heatRegulator.invoke(algorithm, coldCell, simulation);
            double coldCellHeat = coldCell.getState().getHeat();
            if (coldCellHeat < 100) {
                return ModelScore.failure("Cold cell cools down too fast.");
            }
            if (150 < coldCellHeat) {
                return ModelScore.failure("Cold cell cools down too slow.");
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            return ModelScore.failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        return ModelScore.victory();
    }

    private static Cell createCell(float heat) {
        return new Cell(
                new Coordinates(0, 0),
                new CellState(heat, 0, true),
                new CellFactors(0, 30, 0, 0, 0)
        );
    }
}
