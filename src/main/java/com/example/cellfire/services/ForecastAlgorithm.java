package com.example.cellfire.services;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Fire;
import com.example.cellfire.models.Forecast;
import org.springframework.stereotype.Service;

@Service
public final class ForecastAlgorithm {
    // -- Algorithm --
    private static final int PHASE_QUANTITY = 1;

    // -- Combustion --
    /**
     * Varies from 150k to 250k
     */
    private static final double ACTIVATION_ENERGY = 200 * 1000;
    private static final double COMBUSTION_FREQUENCY = Math.pow(10, 9) / 50000 / 2;
    private static final double ENERGY_EMISSION = 10000;

    // -- Heat exchange --
    private static final double PROPAGATED_HEAT_FRACTION = 0.5;
    private static final double HEAT_EXCHANGE_RATE = 1.0 / 1800 / 2;

    // -- Derived --
    private static final double PHASE_DURATION = (double)Domain.Settings.FORECAST_STEP.toSeconds() / PHASE_QUANTITY;
    private static final double ACTIVATION_ENERGY_TERM = -ACTIVATION_ENERGY / 8.3;
    /**
     * FORECAST_STEP = 30 min ; PHASE_QUANTITY = 1 : PHASE_DURATION = 1800
     */
    private static final double PROPAGATED_HEAT_PORTION = PROPAGATED_HEAT_FRACTION / 4 / (1 + Math.sqrt(2));
    private static final double HEAT_EXCHANGE_PROGRESS = Math.min(1, HEAT_EXCHANGE_RATE * PHASE_DURATION);

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
        float fuel = cell.getFire().getFuel() * (1 - burnedFraction);
        if (fuel < Domain.Settings.SIGNIFICANT_FUEL) {
            fuel = 0;
        }

        setGeneratedEnergy(energy, cell);
        cell.getFire().setFuel(fuel);
    }

    public void regulate(Cell cell) {
        double heat = cell.getFire().getHeat();

        heat += (1 - PROPAGATED_HEAT_FRACTION) * getGeneratedEnergy(cell);

        for (Cell neighbour : cell.iterateNeighbors()) {
            double distance = cell.getCoordinates().calculatePhysicalDistanceTo(neighbour.getCoordinates());
            heat += PROPAGATED_HEAT_PORTION * 1000 / distance * getGeneratedEnergy(neighbour);
        }

        heat +=  HEAT_EXCHANGE_PROGRESS * (cell.getFactors().getAirTemperature() - heat);

        cell.getFire().setHeat((float)heat);
    }

    private double calculateCombustionEnergy(Cell cell, double burnedFraction) {
        return ENERGY_EMISSION * cell.getFire().getFuel() * burnedFraction;
    }

    private double calculateBurnedFraction(Cell cell) {
        return Math.min(1, calculateCombustionRate(cell) * PHASE_DURATION);
    }

    private double calculateCombustionRate(Cell cell) {
        if (cell.getFire().getFuel() == 0 || cell.getFire().getHeat() <= cell.getFactors().getIgnitionTemperature()) {
            return 0;
        }
        return COMBUSTION_FREQUENCY * Math.exp(ACTIVATION_ENERGY_TERM / (273 + cell.getFire().getHeat()));
    }

    private void setGeneratedEnergy(float energy, Cell cell) {
        cell.setTwin(new Cell(null, null, new Fire(energy, 0)));
    }

    private float getGeneratedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }
}
