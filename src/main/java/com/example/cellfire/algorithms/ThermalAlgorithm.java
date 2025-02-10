package com.example.cellfire.algorithms;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.models.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Service
public final class ThermalAlgorithm implements Algorithm {
    // -- Combustion --
    /**
     * Varies from 150k to 250k.
     */
    private static final double COMBUSTION_FREQUENCY = 7.0;
    private static final double ENERGY_EMISSION = 10000.0;

    /**
     * 1-2.
     */
    private static final double AIR_HUMIDITY_EFFECT = 1.5;

    /**
     * += 3.
     * 3.5 in some research.
     */
    private static final double SLOPE_EFFECT = 1.5;

    /**
     * 0.1-0.3.
     * 0.13 in some research.
     */
    private static final double WIND_EFFECT = 0.15;

    // -- Heat exchange --
    private static final double CONVENTION_RATE = 0.3 / Duration.ofMinutes(30).toSeconds();
    private static final double RADIATION_RATE = 2 * Math.pow(10, -11);

    // -- Derived --
    private static final double PHASE_DURATION = (double) ModelSettings.FORECAST_STEP.toSeconds();
    private static final double CONVENTION_PROGRESS = Math.min(1, CONVENTION_RATE * PHASE_DURATION);

    @Override
    public void refine(Forecast draftForecast, ScenarioConditions conditions) {
        draftForecast.getCells().forEach((cell) -> {
            burnFuel(cell, conditions);
        });
        draftForecast.getCells().forEach(this::transferEnergy);
        draftForecast.getCells().forEach(this::wasteHeat);
    }

    private void burnFuel(Cell cell, ScenarioConditions conditions) {
        // FIXME: Do not ignore weather.
        float burnedFraction = (float)calculateBurnedFraction(cell, conditions);
        float energy = (float)calculateCombustionEnergy(cell, burnedFraction);
        float fuel = cell.getFire().getFuel() * (1 - burnedFraction);
        if (fuel < ModelSettings.SIGNIFICANT_FUEL) {
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

        double[] proximity = new double[9];
        double averageDistance = calculateAverageDistance(cell.getCoordinates(), cell.getCoordinates());
        proximity[8] = 1.0 / averageDistance;
        int neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            averageDistance = calculateAverageDistance(cell.getCoordinates(), neighbour.getCoordinates());
            double distanceEffect = calculateDistanceEffect(cell, neighbour);
            proximity[neighbourIndex++] = distanceEffect / averageDistance;
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

    private double toAbsoluteTemperature(double celsiusTemperature) {
        return Domain.CELSIUS_ZERO_TEMPERATURE + celsiusTemperature;
    }

    public void wasteHeat(Cell cell) {
        double heat = cell.getFire().getHeat();
        double absoluteTemperature = toAbsoluteTemperature(heat);
        double optimum = Math.pow((1 - CONVENTION_PROGRESS) / 4 / RADIATION_RATE, 1.0 / 3);
        if (absoluteTemperature > optimum) {
            heat -= absoluteTemperature - optimum;
        }
        heat -= CONVENTION_PROGRESS * (heat - cell.getFactors().getAirTemperature())
                + RADIATION_RATE * Math.pow(toAbsoluteTemperature(heat), 4);
        cell.getFire().setHeat((float)heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return ENERGY_EMISSION * cell.getFire().getFuel() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell, ScenarioConditions conditions) {
        return Math.min(1, calculateCombustionRate(cell, conditions) * PHASE_DURATION);
    }

    private double calculateCombustionRate(Cell cell, ScenarioConditions conditions) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() <= conditions.getIgnitionTemperature()
                || cell.getFactors().getAirTemperature() <= 0) {
            return 0;
        }
        double firePower = -conditions.getActivationEnergy() / Domain.UNIVERSAL_GAS_CONSTANT / toAbsoluteTemperature(cell.getFire().getHeat());
        double airHumidityEffect = Math.exp(-AIR_HUMIDITY_EFFECT * cell.getFactors().getAirHumidity());
        return airHumidityEffect * COMBUSTION_FREQUENCY * Math.exp(firePower);
    }

    private double calculateAverageDistance(CellCoordinates first, CellCoordinates second) {
        double localCos = Math.cos(Math.toRadians(first.toGeoPoint().lat));
        double distanceX = Math.abs(first.getX() - second.getX()) * localCos;
        double distanceY = Math.abs(first.getY() - second.getY());
        distanceX += distanceX == 0 ? 0.5 * localCos : 0;
        distanceY += distanceY == 0 ? 0.5 : 0;
        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }

    private double calculateDistanceEffect(Cell cell, Cell otherCell) {
        return calculateSlopeEffect(cell, otherCell) * calculateWindEffect(cell, otherCell);
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
        double windSpeed = (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(WIND_EFFECT * windSpeed);
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getEmittedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }
}
