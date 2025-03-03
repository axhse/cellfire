package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.CellState;
import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Weather;
import com.example.cellfire.tuner.experiment.TuneCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class HeatExchangesProperly extends TuneCase {
    public HeatExchangesProperly(double weight, boolean isObligatory) {
        super(weight, isObligatory);
    }

    public HeatExchangesProperly(double weight) {
        super(weight);
    }

    public HeatExchangesProperly(boolean isObligatory) {
        super(isObligatory);
    }

    public HeatExchangesProperly() {
        super();
    }

    @Override
    protected ModelScore score(Algorithm algorithm) {
        String heatRegulatorMethodName = "regulateHeat";
        try {
            Method heatRegulator = algorithm.getClass().getDeclaredMethod(heatRegulatorMethodName, Cell.class);
            heatRegulator.setAccessible(true);

            Cell overheatedCell = createCell(2000);
            heatRegulator.invoke(algorithm, overheatedCell);
            double overheatedCellHeat = overheatedCell.getState().getHeat();
            if (overheatedCellHeat < 750) {
                return ModelScore.failure("Overheated cell cools down too fast.");
            }
            if (900 < overheatedCellHeat) {
                return ModelScore.failure("Overheated cell cools down too slow.");
            }

            Cell hotCell = createCell(1200);
            heatRegulator.invoke(algorithm, hotCell);
            double hotCellHeat = hotCell.getState().getHeat();
            if (hotCellHeat < 750) {
                return ModelScore.failure("Hot cell cools down too fast.");
            }
            if (850 < hotCellHeat) {
                return ModelScore.failure("Hot cell cools down too slow.");
            }

            Cell warmCell = createCell(700);
            heatRegulator.invoke(algorithm, warmCell);
            double warmCellHeat = warmCell.getState().getHeat();
            if (warmCellHeat < 400) {
                return ModelScore.failure("Warm cell cools down too fast.");
            }
            if (550 < warmCellHeat) {
                return ModelScore.failure("Warm cell cools down too slow.");
            }

            Cell coldCell = createCell(200);
            heatRegulator.invoke(algorithm, coldCell);
            double coldCellHeat = coldCell.getState().getHeat();
            if (coldCellHeat < 130) {
                return ModelScore.failure("Cold cell cools down too fast.");
            }
            if (170 < coldCellHeat) {
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
                new Weather(0, 30, 0, 0, 0)
        );
    }
}
