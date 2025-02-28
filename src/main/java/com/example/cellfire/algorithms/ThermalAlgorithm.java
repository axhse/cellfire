package com.example.cellfire.algorithms;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.models.*;

import java.util.Arrays;

public final class ThermalAlgorithm implements Algorithm {
    /**
     * Varies from 150k to 250k.
     */
    public static final double DEFAULT_COMBUSTION_RATE = 7.0;
    public static final double DEFAULT_ENERGY_EMISSION = 10000.0;
    /**
     * 1-2.
     */
    public static final double DEFAULT_AIR_HUMIDITY_EFFECT = 1.5;
    /**
     * += 3.
     * 3.5 in some research.
     */
    public static final double DEFAULT_SLOPE_EFFECT = 1.5;
    /**
     * 0.1-0.3.
     * 0.13 in some research.
     */
    public static final double DEFAULT_WIND_EFFECT = 0.15;
    public static final double DEFAULT_CONVECTION_RATE = 0.3;
    public static final double DEFAULT_RADIATION_RATE = 2 * Math.pow(10, -11);
    public static final double DEFAULT_DISTANCE_EFFECT = 1.0 / 200;

    private final double combustionRate;
    private final double energyEmission;
    private final double airHumidityEffect;
    private final double slopeEffect;
    private final double windEffect;
    private final double convectionRate;
    private final double radiationRate;
    private final double distanceEffect;

    public ThermalAlgorithm(
            double combustionRate, double energyEmission, double airHumidityEffect, double slopeEffect,
            double windEffect, double convectionRate, double radiationRate, double distanceEffect) {
        this.combustionRate = combustionRate;
        this.energyEmission = energyEmission;
        this.airHumidityEffect = airHumidityEffect;
        this.slopeEffect = slopeEffect;
        this.windEffect = windEffect;
        this.convectionRate = convectionRate;
        this.radiationRate = radiationRate;
        this.distanceEffect = distanceEffect;
    }

    public ThermalAlgorithm() {
        this(DEFAULT_COMBUSTION_RATE,
                DEFAULT_ENERGY_EMISSION,
                DEFAULT_AIR_HUMIDITY_EFFECT,
                DEFAULT_SLOPE_EFFECT,
                DEFAULT_WIND_EFFECT,
                DEFAULT_CONVECTION_RATE,
                DEFAULT_RADIATION_RATE,
                DEFAULT_DISTANCE_EFFECT
        );
    }

    public ThermalAlgorithm(double... parameters) {
        this(parameters[0], parameters[1], parameters[2], parameters[3],
                parameters[4], parameters[5], parameters[6], parameters[7]);
    }

    @Override
    public void refineDraftStep(Simulation.Step draftStep, Simulation simulation) {
        draftStep.getCells().forEach((cell) -> {
            burnFuel(cell, simulation);
        });
        draftStep.getCells().forEach((cell) -> {
            transferEnergy(cell, simulation);
        });
        draftStep.getCells().forEach(this::wasteHeat);
    }

    private void burnFuel(Cell cell, Simulation simulation) {
        // FIXME: Do not ignore weather.
        float burnedFraction = (float) calculateBurnedFraction(cell, simulation);
        float energy = (float) calculateCombustionEnergy(cell, burnedFraction);
        float fuel = cell.getState().getFuel() * (1 - burnedFraction);
        if (fuel < ModelSettings.SIGNIFICANT_FUEL) {
            fuel = 0;
        }

        setEmittedEnergy(energy, cell);
        cell.getState().setFuel(fuel);
    }

    private void transferEnergy(Cell cell, Simulation simulation) {
        float energy = getEmittedEnergy(cell);
        if (energy == 0) {
            return;
        }

        double[] proximity = new double[9];
        Grid grid = simulation.getGrid();
        double averageDistance = estimateAverageDistance(grid, cell.getCoordinates(), cell.getCoordinates());
        proximity[8] = 1.0 / averageDistance;
        int neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            averageDistance = estimateAverageDistance(grid, cell.getCoordinates(), neighbour.getCoordinates());
            double environmentalEffect = calculateEnvironmentalEffect(grid, cell, neighbour);
            proximity[neighbourIndex++] = environmentalEffect / averageDistance
                    * simulation.getGrid().getScale() / distanceEffect;
        }
        double totalProximity = Arrays.stream(proximity).sum();

        double emittedEnergy = getEmittedEnergy(cell);
        double heat = cell.getState().getHeat() + emittedEnergy * proximity[8] / totalProximity;
        cell.getState().setHeat((float) heat);
        neighbourIndex = 0;
        for (Cell neighbour : cell.iterateNeighbors()) {
            heat = neighbour.getState().getHeat() + emittedEnergy * proximity[neighbourIndex] / totalProximity;
            neighbour.getState().setHeat((float) heat);
            neighbourIndex++;
        }
    }

    private double toAbsoluteTemperature(double celsiusTemperature) {
        return Domain.CELSIUS_ZERO_TEMPERATURE + celsiusTemperature;
    }

    public void wasteHeat(Cell cell) {
        double heat = cell.getState().getHeat();
        double absoluteTemperature = toAbsoluteTemperature(heat);
        double optimum = Math.pow((1 - convectionRate) / 4 / radiationRate, 1.0 / 3);
        if (absoluteTemperature > optimum) {
            heat -= absoluteTemperature - optimum;
        }
        heat -= convectionRate * (heat - cell.getWeather().getAirTemperature())
                + radiationRate * Math.pow(toAbsoluteTemperature(heat), 4);
        cell.getState().setHeat((float) heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return energyEmission * cell.getState().getFuel() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell, Simulation simulation) {
        double stepDuration = simulation.getStepDuration().toSeconds();
        return Math.min(1, calculateCombustionRate(cell, simulation.getConditions()) * stepDuration);
    }

    private double calculateCombustionRate(Cell cell, Simulation.Conditions conditions) {
        if (cell.getState().getFuel() == 0 || cell.getState().getHeat() <= conditions.getIgnitionTemperature()
                || cell.getWeather().getAirTemperature() <= 0) {
            return 0;
        }
        double firePower = -conditions.getActivationEnergy() / Domain.UNIVERSAL_GAS_CONSTANT / toAbsoluteTemperature(cell.getState().getHeat());
        double airHumidityInfluence = Math.exp(-airHumidityEffect * cell.getWeather().getAirHumidity());
        return airHumidityInfluence * combustionRate * Math.exp(firePower);
    }

    private double estimateAverageDistance(Grid grid, Coordinates first, Coordinates second) {
        double localCos = Math.cos(Math.toRadians(grid.toLatLng(first).lat));
        double distanceX = Math.abs(first.getX() - second.getX()) * localCos;
        double distanceY = Math.abs(first.getY() - second.getY());
        distanceX += distanceX == 0 ? 0.5 * localCos : 0;
        distanceY += distanceY == 0 ? 0.5 : 0;
        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }

    private double calculateEnvironmentalEffect(Grid grid, Cell cell, Cell otherCell) {
        return calculateSlopeEffect(grid, cell, otherCell) * calculateWindEffect(cell, otherCell);
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
        return Math.exp(slopeEffect * slope);
    }

    private double calculateWindEffect(Cell cell, Cell otherCell) {
        double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
        double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
        double windX = cell.getWeather().getWindX();
        double windY = cell.getWeather().getWindX();
        double windSpeed = (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windEffect * windSpeed);
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, new CellState(energy, 0), null));
    }

    private float getEmittedEnergy(Cell cell) {
        return cell.getTwin().getState().getHeat();
    }
}
