package com.example.cellfire.algorithms;

import com.example.cellfire.models.*;

import java.util.Arrays;

public final class ThermalAlgorithm implements Algorithm {
    /**
     * Varies from 150k to 250k.
     */
    public static final double DEFAULT_COMBUSTION_RATE = 31;
    public static final double DEFAULT_ENERGY_EMISSION = 4.7 * Math.pow(10, 7);
    public static final double DEFAULT_AIR_HUMIDITY_EFFECT = 4.4;
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
    public static final double DEFAULT_DISTANCE_EFFECT = 1.0 / 200;
    public static final double DEFAULT_HEAT_REGULATION_DURATION = 0.16;
    public static final double DEFAULT_RADIATION_PREVALENCE = 7 * Math.pow(10, -10);

    private static final double UNIVERSAL_GAS_CONSTANT = 8.3;
    private static final double CELSIUS_ZERO_TEMPERATURE = 273;

    private static final double HEAT_EXCHANGE_ITERATION_FRACTION = 0.2;
    private static final double HEAT_CHANGE_LIMIT = 0.1;

    private final double combustionRate;
    private final double energyEmission;
    private final double airHumidityEffect;
    private final double slopeEffect;
    private final double windEffect;
    private final double distanceEffect;
    private final double heatRegulationDuration;
    private final double radiationPrevalence;

    public ThermalAlgorithm(
            double combustionRate, double energyEmission, double airHumidityEffect, double slopeEffect,
            double windEffect, double distanceEffect, double heatRegulationDuration, double radiationPrevalence) {
        this.combustionRate = combustionRate;
        this.energyEmission = energyEmission;
        this.airHumidityEffect = airHumidityEffect;
        this.slopeEffect = slopeEffect;
        this.windEffect = windEffect;
        this.distanceEffect = distanceEffect;
        this.heatRegulationDuration = heatRegulationDuration;
        this.radiationPrevalence = radiationPrevalence;
    }

    public ThermalAlgorithm() {
        this(DEFAULT_COMBUSTION_RATE,
                DEFAULT_ENERGY_EMISSION,
                DEFAULT_AIR_HUMIDITY_EFFECT,
                DEFAULT_SLOPE_EFFECT,
                DEFAULT_WIND_EFFECT,
                DEFAULT_DISTANCE_EFFECT,
                DEFAULT_HEAT_REGULATION_DURATION,
                DEFAULT_RADIATION_PREVALENCE
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
        draftStep.getCells().forEach(this::regulateHeat);
    }

    private void burnFuel(Cell cell, Simulation simulation) {
        if (cell.getState().getFuel() == 0
                || cell.getState().getHeat() < simulation.getConditions().getIgnitionTemperature()
                || cell.getWeather().getAirTemperature() <= 0) {
            return;
        }
        double combustionRate = calculateCombustionRate(cell, simulation.getConditions());
        float burnedFraction = (float) calculateBurnedFraction(combustionRate, simulation);
        float energy = (float) calculateCombustionEnergy(cell, combustionRate);
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
        int neighborIndex = 0;
        for (Cell neighbor : cell.iterateNeighbors()) {
            averageDistance = estimateAverageDistance(grid, cell.getCoordinates(), neighbor.getCoordinates());
            double environmentalEffect = calculateEnvironmentalEffect(grid, cell, neighbor);
            proximity[neighborIndex++] = environmentalEffect / averageDistance
                    * simulation.getGrid().getScale() / distanceEffect;
        }
        double totalProximity = Arrays.stream(proximity).sum();

        double emittedEnergy = getEmittedEnergy(cell);
        double heat = cell.getState().getHeat() + emittedEnergy * proximity[8] / totalProximity;
        cell.getState().setHeat((float) heat);
        neighborIndex = 0;
        for (Cell neighbor : cell.iterateNeighbors()) {
            heat = neighbor.getState().getHeat() + emittedEnergy * proximity[neighborIndex] / totalProximity;
            neighbor.getState().setHeat((float) heat);
            neighborIndex++;
        }
    }

    private void regulateHeat(Cell cell) {
        double heat = toKelvin(Math.min(cell.getState().getHeat(), 2000));
        double airTemperature = toKelvin(cell.getWeather().getAirTemperature());
        double phase = 0;
        while (phase < 0.999) {
            double heatChangeRate = -radiationPrevalence * Math.pow(heat, 4) - heat + airTemperature;
            double phaseFraction = HEAT_EXCHANGE_ITERATION_FRACTION;
            double iterationDuration = phaseFraction * heatRegulationDuration;
            double heatChange = heatChangeRate * iterationDuration;
            if (Math.abs(heatChange) > heat * HEAT_CHANGE_LIMIT) {
                heatChange = heat * HEAT_CHANGE_LIMIT * (heatChange < 0 ? -1 : 1);
                iterationDuration = heatChange / heatChangeRate;
                phaseFraction = iterationDuration / heatRegulationDuration;
            }
            if (phase + phaseFraction > 1) {
                phaseFraction = 1 - phase;
                iterationDuration = phaseFraction * heatRegulationDuration;
                heatChange = heatChangeRate * iterationDuration;
            }
            phase += phaseFraction;
            heat += heatChange;
            if (heat < 0) {
                heat = 0;
            }
        }
        cell.getState().setHeat((float) toCelsius(heat));
    }

    private double calculateCombustionEnergy(Cell cell, double combustionRate) {
        return energyEmission * combustionRate * cell.getState().getFuel();
    }

    private double calculateBurnedFraction(double combustionRate, Simulation simulation) {
        double stepDuration = simulation.getStepDuration().toSeconds();
        return Math.min(1, combustionRate * stepDuration);
    }

    private double calculateCombustionRate(Cell cell, Simulation.Conditions conditions) {
        double firePower = -conditions.getActivationEnergy()
                / UNIVERSAL_GAS_CONSTANT / toKelvin(cell.getState().getHeat());
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
        double windY = cell.getWeather().getWindY();
        double windSpeed = (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windEffect * windSpeed);
    }

    private double toKelvin(double celsiusTemperature) {
        return celsiusTemperature + CELSIUS_ZERO_TEMPERATURE;
    }

    private double toCelsius(double kelvinTemperature) {
        return kelvinTemperature - CELSIUS_ZERO_TEMPERATURE;
    }

    private void setEmittedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, new CellState(energy, 0, false), null));
    }

    private float getEmittedEnergy(Cell cell) {
        if (cell.getTwin() == null) {
            return 0;
        }
        return cell.getTwin().getState().getHeat();
    }
}
