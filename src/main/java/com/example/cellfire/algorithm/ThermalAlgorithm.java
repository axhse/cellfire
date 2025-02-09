package com.example.cellfire.algorithm;

import com.example.cellfire.models.*;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Service
public final class ThermalAlgorithm implements Algorithm {
    // -- Combustion --
    /**
     * Varies from 150k to 250k.
     */
    private static final double ACTIVATION_ENERGY = 200 * Math.pow(10, 3);
    private static final double COMBUSTION_FREQUENCY = Math.pow(10, 9) / 5000;
    private static final double ENERGY_EMISSION = 10000.0 * 1;

    // -- Wind --
    private static final double WIND_FORCE = 100.0;
    /**
     * Varies from 1 for surface fires to 1.5-2 for crown fires.
     */
    private static final double WIND_EFFECT = 1.7;

    // -- Heat exchange --
    private static final double CONVENTION_RATE = 0.3 / Duration.ofMinutes(30).toSeconds();
    private static final double RADIATION_RATE = 2 * Math.pow(10, -11);

    // -- Derived --
    private static final double PHASE_DURATION = (double)Domain.Settings.FORECAST_STEP.toSeconds();
    private static final double ACTIVATION_ENERGY_TERM = -ACTIVATION_ENERGY / 8.3;
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
        float burnedFraction = (float)evaluateBurnedFraction(cell, conditions);
        float energy = (float)evaluateCombustionEnergy(cell, burnedFraction);
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

        LatLng fireAnchor = evaluateFireAnchor(cell);

        double[] proximity = new double[9];
        proximity[8] = 1 / evaluateDistanceToCell(fireAnchor, cell.getCoordinates().toGeoPoint());
        int neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            proximity[neighbourIndex++] = 1 / evaluateDistanceToCell(fireAnchor, neighbour.getCoordinates().toGeoPoint());
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
        double absoluteTemperature = toAbsoluteTemperature(heat);
        double optimum = Math.pow((1 - CONVENTION_PROGRESS) / 4 / RADIATION_RATE, 1.0 / 3);
        if (absoluteTemperature > optimum) {
            heat -= absoluteTemperature - optimum;
        }
        heat -= CONVENTION_PROGRESS * (heat - cell.getFactors().getAirTemperature())
                + RADIATION_RATE * Math.pow(toAbsoluteTemperature(heat), 4);
        cell.getFire().setHeat((float)heat);
    }

    private double evaluateCombustionEnergy(Cell cell, double burnedFraction) {
        return ENERGY_EMISSION * cell.getFire().getFuel() * burnedFraction;
    }

    private double evaluateBurnedFraction(Cell cell, ScenarioConditions conditions) {
        return Math.min(1, evaluateCombustionRate(cell, conditions) * PHASE_DURATION);
    }

    private double evaluateCombustionRate(Cell cell, ScenarioConditions conditions) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() <= conditions.getIgnitionTemperature()) {
            return 0;
        }
        var exp = Math.exp(ACTIVATION_ENERGY_TERM / (toAbsoluteTemperature(cell.getFire().getHeat())));
        return COMBUSTION_FREQUENCY * Math.exp(ACTIVATION_ENERGY_TERM / (toAbsoluteTemperature(cell.getFire().getHeat())));
    }

    private LatLng evaluateFireAnchor(Cell cell) {
        // FIXME: Consider slope.
        LatLng point = cell.getCoordinates().toGeoPoint();
        double cellSize = Domain.Settings.CELL_SIZE;
        double anchorDiffLat = evaluateWindInfluence(cell.getFactors().getWind()[0]) / Domain.Settings.GRID_SIZE;
        double anchorDiffLng = evaluateWindInfluence(cell.getFactors().getWind()[1]) / Domain.Settings.GRID_SIZE / Math.cos(Math.toRadians(point.lng));
        if (cellSize < anchorDiffLat) {
            anchorDiffLat = cellSize;
        }
        if (anchorDiffLat < -cellSize) {
            anchorDiffLat = -cellSize;
        }
        if (cellSize < anchorDiffLng) {
            anchorDiffLng = cellSize;
        }
        if (anchorDiffLng < -cellSize) {
            anchorDiffLng = -cellSize;
        }
        return new LatLng(point.lat + anchorDiffLat, point.lng + anchorDiffLng);
    }

    private double evaluateWindInfluence(double windSpeed) {
        return WIND_FORCE * Math.pow(windSpeed, WIND_EFFECT);
    }

    private double evaluateDistanceToCell(LatLng basicPoint, LatLng cellPoint) {
        double diffLat = Math.abs(basicPoint.lat - cellPoint.lat);
        double diffLng = Math.abs(basicPoint.lng - cellPoint.lng);
        if (Domain.Settings.CELL_SIZE / 2 <= Math.max(diffLat, diffLng)) {
            return evaluateDistance(diffLat, diffLng, basicPoint.lng);
        }
        double weightedDistance = 0;
        double totalWeight = 0;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                double cornerLat = cellPoint.lat + i * Domain.Settings.CELL_SIZE / 2;
                double cornerLng = cellPoint.lng + j * Domain.Settings.CELL_SIZE / 2;
                diffLat = Math.abs(basicPoint.lat - cornerLat);
                diffLng = Math.abs(basicPoint.lng - cornerLng);
                double quarterDistance = evaluateDistance(diffLat, diffLng, basicPoint.lng);
                double quarterWeight = quarterDistance * quarterDistance;
                weightedDistance += quarterDistance * quarterWeight;
                totalWeight += quarterWeight;
            }
        }
        return weightedDistance / totalWeight;
    }

    private double evaluateDistance(double diffLat, double diffLng, double basicLng) {
        double distanceLat = diffLat * Math.cos(Math.toRadians(basicLng));
        return Math.sqrt(distanceLat * distanceLat + diffLng * diffLng);
    }

    private double toAbsoluteTemperature(double celsiusTemperature) {
        return 273 + celsiusTemperature;
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getEmittedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }
}
