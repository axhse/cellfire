package com.example.cellfire.services;

import com.example.cellfire.DomainSettings;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Fire;
import com.example.cellfire.models.Forecast;
import org.springframework.stereotype.Service;

@Service
public final class ForecastAlgorithm {
    private static final int PHASE_QUANTITY = 1;
    private static final double PROPAGATION_RATE = 0.3;
    private static final double RATE_LINEAR_FACTOR = Math.pow(10, 9) / 50000 / 2;
    /**
     * Varies from 150k to 250k
     */
    private static final double ACTIVATION_ENERGY = 200 * 1000;
    private static final double ENERGY_EMISSION_FACTOR = 50000;
    private static final double WEATHER_HEAT_REGULATION_FACTOR = 1.0 / 1800 / 2;

    private static final double RATE_EXPONENTIAL_FACTOR = ACTIVATION_ENERGY / 8.3;

    /**
     * FORECAST_STEP = 30 min ; PHASE_QUANTITY = 1 : PHASE_DURATION = 1800
     */
    private static final double PHASE_DURATION = (double)DomainSettings.FORECAST_STEP.toSeconds() / PHASE_QUANTITY;

    public void refine(Forecast draftForecast) {
        for (int i = 0; i < PHASE_QUANTITY; i++) {
            draftForecast.getCells().forEach(this::combust);
            draftForecast.getCells().forEach(this::regulate);
        }
    }

    private void combust(Cell cell) {
        // FIXME: Do not ignore weather
        float burnedFraction = (float)calculateBurnedFraction(cell);
        float energy = (float)calculateCombustionEnergy(cell, burnedFraction);
        float resource = cell.getFire().getResource() * (1 - burnedFraction);
        if (resource < DomainSettings.SIGNIFICANT_RESOURCE) {
            resource = 0;
        }

        setGeneratedEnergy(energy, cell);
        cell.getFire().setResource(resource);
    }

    public void regulate(Cell cell) {
        double heat = cell.getFire().getHeat();

        heat += getGeneratedEnergy(cell) * 0.2;

        for (Cell neighbour : cell.iterateNeighbors()) {
            double distance = cell.getCoordinates().calculatePhysicalDistanceTo(neighbour.getCoordinates());
            heat += getGeneratedEnergy(neighbour) * 0.05 / Math.pow(distance, 3);
        }

        heat += (cell.getEnvironment().getAirTemperature() - heat) * Math.min(1, WEATHER_HEAT_REGULATION_FACTOR * PHASE_DURATION);

        cell.getFire().setHeat((float)heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return ENERGY_EMISSION_FACTOR * cell.getFire().getResource() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell) {
        return Math.min(1, calculateCombustionRate(cell) * PHASE_DURATION);
    }

    private double calculateCombustionRate(Cell cell) {
        if (cell.getFire().getResource() == 0 || cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
            return 0;
        }
        return RATE_LINEAR_FACTOR * Math.exp(-RATE_EXPONENTIAL_FACTOR / (273 + cell.getFire().getHeat()));
    }

    private void setGeneratedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getGeneratedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }
}
