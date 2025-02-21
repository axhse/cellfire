package com.example.cellfire.algorithms;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.models.*;

import java.util.Arrays;

public final class ThermalAlgorithm implements Algorithm {
    // -- Combustion --
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

    // -- Heat exchange --
    public static final double DEFAULT_CONVECTION_RATE = 0.3;
    public static final double DEFAULT_RADIATION_RATE = 2 * Math.pow(10, -11);

    // -- Sizing --
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
    public void refine(SimulationStep draftSimulationStep, ScenarioConditions conditions) {
        draftSimulationStep.getCells().forEach((cell) -> {
            burnFuel(cell, conditions);
        });
        draftSimulationStep.getCells().forEach(this::transferEnergy);
        draftSimulationStep.getCells().forEach(this::wasteHeat);
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
            double environmentalEffect = calculateEnvironmentalEffect(cell, neighbour);
            proximity[neighbourIndex++] = environmentalEffect / averageDistance
                    * ModelSettings.GRID_SCALE / distanceEffect;
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
        double optimum = Math.pow((1 - convectionRate) / 4 / radiationRate, 1.0 / 3);
        if (absoluteTemperature > optimum) {
            heat -= absoluteTemperature - optimum;
        }
        heat -= convectionRate * (heat - cell.getFactors().getAirTemperature())
                + radiationRate * Math.pow(toAbsoluteTemperature(heat), 4);
        cell.getFire().setHeat((float)heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return energyEmission * cell.getFire().getFuel() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell, ScenarioConditions conditions) {
        double phaseDuration = ModelSettings.STEP_DURATION.toSeconds();
        return Math.min(1, calculateCombustionRate(cell, conditions) * phaseDuration);
    }

    private double calculateCombustionRate(Cell cell, ScenarioConditions conditions) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() <= conditions.getIgnitionTemperature()
                || cell.getFactors().getAirTemperature() <= 0) {
            return 0;
        }
        double firePower = -conditions.getActivationEnergy() / Domain.UNIVERSAL_GAS_CONSTANT / toAbsoluteTemperature(cell.getFire().getHeat());
        double airHumidityFactor = Math.exp(-airHumidityEffect * cell.getFactors().getAirHumidity());
        return airHumidityFactor * combustionRate * Math.exp(firePower);
    }

    private double calculateAverageDistance(CellCoordinates first, CellCoordinates second) {
        double localCos = Math.cos(Math.toRadians(first.toGeoPoint().lat));
        double distanceX = Math.abs(first.getX() - second.getX()) * localCos;
        double distanceY = Math.abs(first.getY() - second.getY());
        distanceX += distanceX == 0 ? 0.5 * localCos : 0;
        distanceY += distanceY == 0 ? 0.5 : 0;
        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }

    private double calculateEnvironmentalEffect(Cell cell, Cell otherCell) {
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
        return Math.exp(slopeEffect * slope);
    }

    private double calculateWindEffect(Cell cell, Cell otherCell) {
        double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
        double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
        double windX = cell.getFactors().getWindX();
        double windY = cell.getFactors().getWindX();
        double windSpeed = (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windEffect * windSpeed);
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getEmittedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }
}
