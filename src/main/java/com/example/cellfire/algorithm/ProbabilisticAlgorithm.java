package com.example.cellfire.algorithm;

import com.example.cellfire.models.*;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public final class ProbabilisticAlgorithm implements Algorithm {
    private static final Random random = new Random();

    private static final double BASIC_PROBABILITY = 0.58;
    private static final double SLOPE_EFFECT = 0.078;
    private static final double WIND_SPEED_EFFECT = 0.045;
    private static final double WIND_COS_EFFECT = 0.131;

    @Override
    public void refine(Forecast draftForecast, ScenarioConditions conditions) {
        draftForecast.getCells().forEach(this::setDefaultMark);
        draftForecast.getCells().forEach(this::applyRules);
        draftForecast.getCells().forEach(this::propagate);
    }

    private void setDefaultMark(Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(0, 0)));
    }

    private void applyRules(Cell cell) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() != ModelSettings.INITIAL_HEAT) {
            return;
        }
        propagateFireToNeighbours(cell);
        cell.getFire().setHeat(0);
        cell.getFire().setFuel(0);
    }

    private void propagate(Cell cell) {
        if (cell.getTwin().getFire().getHeat() == ModelSettings.INITIAL_HEAT) {
            cell.getFire().setHeat(ModelSettings.INITIAL_HEAT);
        }
    }

    private void propagateFireToNeighbours(Cell cell) {
        for (Cell neighbour : cell.iterateNeighbors()) {
            if (neighbour.getFire().getHeat() == ModelSettings.INITIAL_HEAT || neighbour.getFire().getFuel() == 0) {
                continue;
            }
            var density = (1 + calculateFuelDensityEffect(neighbour));
            var wind = calculateWindEffect(cell, neighbour);
            var slope = calculateSlopeEffect(cell, neighbour);
            double probability = BASIC_PROBABILITY * 1.4 * (1 + calculateFuelDensityEffect(neighbour))
                    * calculateWindEffect(cell, neighbour)
                    * calculateSlopeEffect(cell, neighbour);
            probability = Math.min(1, probability);
            if (random.nextDouble() < probability) {
                neighbour.getTwin().getFire().setHeat(ModelSettings.INITIAL_HEAT);
            }
        }
    }

    private double calculateFuelDensityEffect(Cell cell) {
        double fuel = cell.getFire().getFuel();
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

    private double calculateSlopeEffect(Cell cell, Cell otherCell) {
        double elevation = otherCell.getFactors().getElevation() - cell.getFactors().getElevation();
        if (elevation == 0) {
            return 1;
        }
        double localCos = Math.cos(Math.toRadians(cell.getCoordinates().toGeoPoint().lat));
        double distanceX = Math.abs(cell.getCoordinates().getX() - otherCell.getCoordinates().getX()) * localCos;
        double distanceY = Math.abs(cell.getCoordinates().getY() - otherCell.getCoordinates().getY());
        double distance = ModelSettings.CELL_HEIGHT * Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        double slope = elevation / distance;
        return Math.exp(SLOPE_EFFECT * slope);
    }

    private double calculateWindEffect(Cell cell, Cell otherCell) {
        double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
        double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
        double windX = cell.getFactors().getWindX();
        double windY = cell.getFactors().getWindX();
        double windSpeed = Math.sqrt(windX * windX + windY * windY);
        if (windSpeed == 0) {
            return 1;
        }
        double windCos = (windX * vectorX + windY * vectorY) / windSpeed / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windSpeed * (WIND_SPEED_EFFECT + WIND_COS_EFFECT * windCos));
    }
}
