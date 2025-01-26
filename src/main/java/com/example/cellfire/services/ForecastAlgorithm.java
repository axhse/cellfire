package com.example.cellfire.services;

import com.example.cellfire.DomainSettings;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Fire;
import com.example.cellfire.models.Forecast;
import org.springframework.stereotype.Service;

@Service
public final class ForecastAlgorithm {
    private final double PROPAGATION_RATE = 0.3;
    private final double F_FACTOR = 50000;
    private final double A_FACTOR = Math.pow(10, 9) / F_FACTOR;
    private final double K_FACTOR = 200 * 1000 / 8.3;
    private final double H_FACTOR = 100000;

    public void refine(Forecast draftForecast) {
        draftForecast.getCells().forEach(this::combust);
        draftForecast.getCells().forEach(this::regulate);
    }

    private void combust(Cell cell) {
        // FIXME: Do not ignore weather
        double resource = cell.getFire().getResource();
        double energy = calculateCombustionEnergy(cell);
        setGeneratedEnergy(energy, cell);
        long duration = DomainSettings.FORECAST_STEP.toSeconds();
        var x = duration * calculateCombustionRate(cell);
        if (x > 1) {
            x = 1;
        }
        resource *= (1 - x);

        cell.setFire(new Fire(cell.getFire().getHeat(), resource));
    }

    public void regulate(Cell cell) {
        double heat = cell.getFire().getHeat();

        heat += getGeneratedEnergy(cell) * 0.2;

        for (Cell neighbour : cell.iterateNeighbors()) {
            double distance = 1;
            if (neighbour.getCoordinates().getX() != cell.getCoordinates().getX() && neighbour.getCoordinates().getY() != cell.getCoordinates().getY()) {
                distance = Math.sqrt(2);
            }
            heat += getGeneratedEnergy(neighbour) * 0.05 / Math.pow(distance, 3);
        }

        heat += (cell.getEnvironment().getWeatherTemperature() - heat) * 0.2;

        cell.setFire(new Fire(heat, cell.getFire().getResource()));
    }

    private void setGeneratedEnergy(double energy, Cell cell) {
        cell.setTwin(new Cell(null, null, null));
        cell.getTwin().setFire(new Fire(energy, 0));
    }

    private double getGeneratedEnergy(Cell cell) {
        return cell.getTwin().getFire().getHeat();
    }

    private double calculateCombustionRate(Cell cell) {
        if (cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
            return 0;
        }
        return A_FACTOR * Math.exp(-K_FACTOR / (273 + cell.getFire().getHeat()));
    }

    private double calculateCombustionEnergy(Cell cell) {
        long duration = DomainSettings.FORECAST_STEP.toSeconds();
        double burnedPart = calculateCombustionRate(cell) * duration;
        if (burnedPart > 1) {
            burnedPart = 1;
        }
        double energy = H_FACTOR * cell.getFire().getResource() * burnedPart;
        return energy;
    }
}
