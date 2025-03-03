package com.example.cellfire.algorithms;

import com.example.cellfire.models.*;

import java.util.Random;

public final class ProbabilisticAlgorithm implements Algorithm {
    private static final float INITIAL_HEAT = 1000;

    private static final Random random = new Random();

    private static final double BASIC_PROBABILITY = 0.58 / 3;
    private static final double SLOPE_EFFECT = 0.078;
    private static final double WIND_SPEED_EFFECT = 0.045;
    private static final double WIND_COS_EFFECT = 0.131;

    @Override
    public void refineDraftStep(Simulation.Step draftStep, Simulation simulation) {
        draftStep.getCells().forEach(this::setDefaultMark);
        draftStep.getCells().forEach((cell) -> {
            applyRules(cell, simulation);
        });
        draftStep.getCells().forEach(this::propagate);
    }

    private void setDefaultMark(Cell cell) {
        cell.setTwin(new Cell(null, new CellState(0, 0, false), null));
    }

    private void applyRules(Cell cell, Simulation simulation) {
        if (cell.getState().getFuel() == 0 || cell.getState().getHeat() != INITIAL_HEAT) {
            return;
        }
        propagateFireToNeighbors(cell, simulation);
        cell.getState().setHeat(0);
        cell.getState().setFuel(0);
    }

    private void propagate(Cell cell) {
        if (cell.getTwin().getState().getHeat() == INITIAL_HEAT) {
            cell.getState().setHeat(INITIAL_HEAT);
        }
    }

    private void propagateFireToNeighbors(Cell cell, Simulation simulation) {
        for (Cell neighbor : cell.iterateNeighbors()) {
            if (neighbor.getState().getHeat() == INITIAL_HEAT || neighbor.getState().getFuel() == 0) {
                continue;
            }
            double probability = BASIC_PROBABILITY * 1.4 * (1 + calculateFuelDensityEffect(neighbor))
                    * calculateWindEffect(cell, neighbor)
                    * calculateSlopeEffect(simulation.getGrid(), cell, neighbor);
            probability = Math.min(1, probability);
            if (random.nextDouble() < probability) {
                neighbor.getTwin().getState().setHeat(INITIAL_HEAT);
            }
        }
    }

    private double calculateFuelDensityEffect(Cell cell) {
        double fuel = cell.getState().getFuel();
        if (fuel > 2) {
            return 0.3;
        }
        if (fuel > 1) {
            return 0;
        }
        if (fuel > 0.5) {
            return -0.3;
        }
        return -1;
    }

    private double calculateSlopeEffect(Grid grid, Cell cell, Cell otherCell) {
        double elevation = otherCell.getWeather().getElevation() - cell.getWeather().getElevation();
        if (elevation == 0) {
            return 1;
        }
        double localCos = Math.cos(Math.toRadians(grid.toLatLng(cell.getCoordinates()).lat));
        double distanceX = Math.abs(cell.getCoordinates().getX() - otherCell.getCoordinates().getX()) * localCos;
        double distanceY = Math.abs(cell.getCoordinates().getY() - otherCell.getCoordinates().getY());
        double distance = grid.getCellHeight() * Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        double slope = elevation / distance;
        return Math.exp(SLOPE_EFFECT * slope);
    }

    private double calculateWindEffect(Cell cell, Cell otherCell) {
        double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
        double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
        double windX = cell.getWeather().getWindX();
        double windY = cell.getWeather().getWindY();
        double windSpeed = Math.sqrt(windX * windX + windY * windY);
        if (windSpeed == 0) {
            return 1;
        }
        double windCos = (windX * vectorX + windY * vectorY) / windSpeed / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windSpeed * (WIND_SPEED_EFFECT + WIND_COS_EFFECT * windCos));
    }
}
