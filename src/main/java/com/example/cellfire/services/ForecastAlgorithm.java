package com.example.cellfire.services;

import com.example.cellfire.models.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public final class ForecastAlgorithm {
    // -- Algorithm --
    private static final int PHASE_QUANTITY = 1;

    // -- Combustion --
    /**
     * Varies from 150k to 250k
     */
    private static final double ACTIVATION_ENERGY = 200 * 1000.0;
    private static final double COMBUSTION_FREQUENCY = Math.pow(10, 9) / 50000 / 2;
    private static final double ENERGY_EMISSION = 10000.0;

    // -- Heat exchange --
    private static final double HEAT_EXCHANGE_RATE = 1.0 / 1800 / 2;

    // -- Derived --
    private static final double PHASE_DURATION = (double)Domain.Settings.FORECAST_STEP.toSeconds() / PHASE_QUANTITY;
    private static final double ACTIVATION_ENERGY_TERM = -ACTIVATION_ENERGY / 8.3;
    /**
     * FORECAST_STEP = 30 min ; PHASE_QUANTITY = 1 : PHASE_DURATION = 1800
     */
    private static final double HEAT_EXCHANGE_PROGRESS = Math.min(1, HEAT_EXCHANGE_RATE * PHASE_DURATION);

    public void refine(Forecast draftForecast, ScenarioConditions conditions) {
        for (int i = 0; i < PHASE_QUANTITY; i++) {
            draftForecast.getCells().forEach((cell) -> {
                burnFuel(cell, conditions);
            });
            draftForecast.getCells().forEach(this::transferEnergy);
            draftForecast.getCells().forEach(this::wasteHeat);
        }
    }

    private void burnFuel(Cell cell, ScenarioConditions conditions) {
        // FIXME: Do not ignore weather.

        float burnedFraction = (float)calculateBurnedFraction(cell, conditions);
        float energy = (float)calculateCombustionEnergy(cell, burnedFraction);
        float fuel = cell.getFire().getFuel() * (1 - burnedFraction);
        if (fuel < Domain.Settings.SIGNIFICANT_FUEL) {
            fuel = 0;
        }

        setEmittedEnergy(energy, cell);
        cell.getFire().setFuel(fuel);
    }

    private void transferEnergy(Cell cell) {
        float energy = getEmittedEnergy(cell);
        if (energy == 0) {
            return;
        }

        // FIXME: Consider slope and wind.

        double[] proximity = new double[9];
        proximity[8] = 1 / evaluateDistance(cell.getCoordinates(), cell.getCoordinates());
        int neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            proximity[neighbourIndex++] = 1 / evaluateDistance(cell.getCoordinates(), neighbour.getCoordinates());
        }
        double totalProximity = Arrays.stream(proximity).sum();

        double emittedEnergy = getEmittedEnergy(cell);
        double heat = cell.getFire().getHeat() + emittedEnergy * proximity[8] / totalProximity;
        cell.getFire().setHeat((float)heat);
        neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            heat = neighbour.getFire().getHeat() + emittedEnergy * proximity[neighbourIndex] / totalProximity;
            neighbour.getFire().setHeat((float)heat);
            neighbourIndex++;
        }

    }

    public void wasteHeat(Cell cell) {
        double heat = cell.getFire().getHeat();
        heat += HEAT_EXCHANGE_PROGRESS * (cell.getFactors().getAirTemperature() - heat);
        cell.getFire().setHeat((float)heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return ENERGY_EMISSION * cell.getFire().getFuel() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell, ScenarioConditions conditions) {
        return Math.min(1, calculateCombustionRate(cell, conditions) * PHASE_DURATION);
    }

    private double calculateCombustionRate(Cell cell, ScenarioConditions conditions) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() <= conditions.getIgnitionTemperature()) {
            return 0;
        }
        return COMBUSTION_FREQUENCY * Math.exp(ACTIVATION_ENERGY_TERM / (273 + cell.getFire().getHeat()));
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getEmittedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }

    private double evaluateDistance(CellCoordinates coordinates, CellCoordinates otherCoordinates) {
        int deltaX = Math.abs(coordinates.getX() - otherCoordinates.getX());
        int deltaY = Math.abs(coordinates.getY() - otherCoordinates.getY());
        double distanceX = deltaX * Math.cos(Math.toRadians(coordinates.toGeoPoint().lng));
        double distance = Math.sqrt(distanceX * distanceX + deltaY * deltaY);
        if (deltaY == 1 && deltaX == 0) {
            distance += 0.04;
        }
        if (deltaY == 0) {
            double correction = 0.38 * Math.sqrt(distanceX * distanceX + 1) / Math.sqrt(2);
            if (deltaX != 0) {
                correction *= Math.exp(-2*Math.sqrt(distanceX));
            }
            distance += correction;
        }
        return distance * Domain.EARTH_EQUATORIAL_CIRCUMFERENCE / Domain.Settings.GRID_SCALE;
    }
}
