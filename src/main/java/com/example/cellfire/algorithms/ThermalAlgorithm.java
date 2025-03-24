package com.example.cellfire.algorithms;

import com.example.cellfire.models.*;

import java.util.Arrays;
import java.util.List;

public final class ThermalAlgorithm implements Algorithm {
    public static final double DEFAULT_COMBUSTION_INTENSITY = 20000;
    public static final double DEFAULT_ENERGY_EMISSION = 33000;
    public static final double DEFAULT_AIR_HUMIDITY_EFFECT = 4;
    /**
     * 3.5 in some research.
     */
    public static final double DEFAULT_SLOPE_EFFECT = 3;
    /**
     * 0.13 in some research.
     */
    public static final double DEFAULT_WIND_EFFECT = 0.15;
    public static final double DEFAULT_DISTANCE_EFFECT = 1.0 / 200;
    public static final double DEFAULT_HEAT_REGULATION_INTENSITY = 0.00018;
    public static final double DEFAULT_RADIATION_PREVALENCE = 4 * Math.pow(10, -10);

    private static final double UNIVERSAL_GAS_CONSTANT = 8.3;
    private static final double CELSIUS_ZERO_TEMPERATURE = 273;

    private static final double HEAT_CHANGE_LIMIT = 0.15;

    private final double combustionIntensity;
    private final double energyEmission;
    private final double airHumidityEffect;
    private final double slopeEffect;
    private final double windEffect;
    private final double distanceEffect;
    private final double heatRegulationIntensity;
    private final double radiationPrevalence;

    public ThermalAlgorithm(
            double combustionIntensity, double energyEmission, double airHumidityEffect, double slopeEffect,
            double windEffect, double distanceEffect, double heatRegulationIntensity, double radiationPrevalence) {
        this.combustionIntensity = combustionIntensity;
        this.energyEmission = energyEmission;
        this.airHumidityEffect = airHumidityEffect;
        this.slopeEffect = slopeEffect;
        this.windEffect = windEffect;
        this.distanceEffect = distanceEffect;
        this.heatRegulationIntensity = heatRegulationIntensity;
        this.radiationPrevalence = radiationPrevalence;
    }

    public ThermalAlgorithm() {
        this(
                DEFAULT_COMBUSTION_INTENSITY,
                DEFAULT_ENERGY_EMISSION,
                DEFAULT_AIR_HUMIDITY_EFFECT,
                DEFAULT_SLOPE_EFFECT,
                DEFAULT_WIND_EFFECT,
                DEFAULT_DISTANCE_EFFECT,
                DEFAULT_HEAT_REGULATION_INTENSITY,
                DEFAULT_RADIATION_PREVALENCE
        );
    }

    public ThermalAlgorithm(double... parameters) {
        this(
                parameters[0], parameters[1], parameters[2], parameters[3],
                parameters[4], parameters[5], parameters[6], parameters[7]
        );
    }

    @Override
    public void refineDraftStep(Simulation.Step draftStep, Simulation simulation) {
        List<Cell> burningCells = draftStep.getCells().stream().filter(cell -> isBurning(cell, simulation)).toList();
        burningCells.forEach((cell) -> burnFuel(cell, simulation));
        burningCells.forEach((cell) -> transferEnergy(cell, simulation));
        draftStep.getCells().forEach((cell) -> regulateHeat(cell, simulation));
    }

    private static boolean isBurning(Cell cell, Simulation simulation) {
        return cell.getState().getFuel() > 0
                && simulation.getConditions().getIgnitionTemperature() <= cell.getState().getHeat()
                && cell.getFactors().getAirTemperature() > 0;
    }

    private static double estimateAverageDistance(Grid grid, Coordinates first, Coordinates second) {
        double localCos = Math.cos(Math.toRadians(grid.pointOf(first).lat));
        // Cells neighboring through the poles are not expected.
        double distanceX = Math.abs(first.getX() - second.getX()) * localCos;
        double distanceY = Math.abs(first.getY() - second.getY());
        distanceX += distanceX == 0 ? 0.5 * localCos : 0;
        distanceY += distanceY == 0 ? 0.5 : 0;
        return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }

    private static double toKelvin(double celsiusTemperature) {
        return celsiusTemperature + CELSIUS_ZERO_TEMPERATURE;
    }

    private static double toCelsius(double kelvinTemperature) {
        return kelvinTemperature - CELSIUS_ZERO_TEMPERATURE;
    }

    private static void setEmittedEnergy(double energy, Cell cell) {
        cell.setTwin(new Cell(null, new Cell.State(energy, 0, false), null));
    }

    private static double getEmittedEnergy(Cell cell) {
        return cell.getTwin().getState().getHeat();
    }

    private void burnFuel(Cell cell, Simulation simulation) {
        double initialFuel = cell.getState().getFuel();
        if (initialFuel == 0 || !simulation.isBurning(cell) || cell.getFactors().getAirTemperature() <= 0) {
            return;
        }
        double burnedFraction = calculateBurnedFraction(cell, simulation);
        double energy = calculateCombustionEnergy(cell, burnedFraction);
        double fuel = initialFuel * (1 - burnedFraction);
        setEmittedEnergy(energy, cell);
        cell.getState().setFuel(fuel);
    }

    private void transferEnergy(Cell cell, Simulation simulation) {
        Grid grid = simulation.getGrid();
        double[] proximity = new double[9];
        double averageDistance = estimateAverageDistance(grid, cell.getCoordinates(), cell.getCoordinates());
        proximity[8] = 1.0 / averageDistance;
        int neighborIndex = 0;
        for (Cell neighbor : cell.iterateNeighbors()) {
            averageDistance = estimateAverageDistance(grid, cell.getCoordinates(), neighbor.getCoordinates());
            double environmentalEffect = calculateEnvironmentalEffect(grid, cell, neighbor);
            proximity[neighborIndex++] = environmentalEffect / averageDistance * grid.getScale() / distanceEffect;
        }
        double totalProximity = Arrays.stream(proximity).sum();

        double emittedEnergy = getEmittedEnergy(cell);
        double heat = cell.getState().getHeat() + emittedEnergy * proximity[8] / totalProximity;
        cell.getState().setHeat(heat);
        neighborIndex = 0;
        for (Cell neighbor : cell.iterateNeighbors()) {
            heat = neighbor.getState().getHeat() + emittedEnergy * proximity[neighborIndex] / totalProximity;
            neighbor.getState().setHeat(heat);
            neighborIndex++;
        }
    }

    private void regulateHeat(Cell cell, Simulation simulation) {
        double stepDuration = simulation.getTimeline().getStepDuration().toSeconds();
        double heatRegulationDuration = heatRegulationIntensity * stepDuration;
        double heat = toKelvin(cell.getState().getHeat());
        double airTemperature = toKelvin(cell.getFactors().getAirTemperature());
        double phase = 0;
        while (phase < 0.999) {
            double heatChangeRate = -radiationPrevalence * Math.pow(heat, 4) - heat + airTemperature;
            double phaseFraction = 1;
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
        cell.getState().setHeat(toCelsius(heat));
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return energyEmission * burnedFraction * cell.getState().getFuel();
    }

    private double calculateBurnedFraction(Cell cell, Simulation simulation) {
        double stepDuration = simulation.getTimeline().getStepDuration().toSeconds();
        return Math.min(1, stepDuration * calculateCombustionRate(cell, simulation.getConditions()));
    }

    private double calculateCombustionRate(Cell cell, Simulation.Conditions conditions) {
        double temperature = toKelvin(cell.getState().getHeat());
        double firePower = -conditions.getActivationEnergy() / UNIVERSAL_GAS_CONSTANT / temperature;
        double airHumidityInfluence = Math.exp(-airHumidityEffect * cell.getFactors().getAirHumidity());
        return airHumidityInfluence * combustionIntensity * Math.exp(firePower);
    }

    private double calculateEnvironmentalEffect(Grid grid, Cell cell, Cell otherCell) {
        return calculateSlopeEffect(grid, cell, otherCell) * calculateWindEffect(cell, otherCell);
    }

    private double calculateSlopeEffect(Grid grid, Cell cell, Cell otherCell) {
        double elevation = otherCell.getFactors().getElevation() - cell.getFactors().getElevation();
        if (elevation == 0) {
            return 1;
        }
        double localCos = Math.cos(Math.toRadians(grid.pointOf(cell.getCoordinates()).lat));
        double distanceX = Math.abs(cell.getCoordinates().getX() - otherCell.getCoordinates().getX()) * localCos;
        double distanceY = Math.abs(cell.getCoordinates().getY() - otherCell.getCoordinates().getY());
        double distance = grid.getCellHeight() * Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        double slope = elevation / distance;
        return Math.exp(slopeEffect * slope);
    }

    private double calculateWindEffect(Cell cell, Cell otherCell) {
        double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
        double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
        double windX = cell.getFactors().getWindX();
        double windY = cell.getFactors().getWindY();
        double windSpeed = (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
        return Math.exp(windEffect * windSpeed);
    }
}
