package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.Domain;
import com.example.cellfire.entity.Fire;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class Algorithm {
    public final double PROPAGATION_RATE = 0.3;
    public final double F = 50000;
    public final double A = Math.pow(10, 9) / F;
    public final double K = 200 * 1000 / 8.3;
    public final double H = 100000;

    private double calculateBurnRate(Cell cell) {
        if (cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
            return 0;
        }
        double burnRate = A * Math.exp(-K / (273 + cell.getFire().getHeat()));
        return burnRate;
    }

    private double calculateBurnEnergy(Cell cell) {
        long duration = Domain.FORECAST_STEP.toSeconds();
        double burnedPart = calculateBurnRate(cell) * duration;
        if (burnedPart > 1) {
            burnedPart = 1;
        }
        double energy = H * cell.getFire().getResource() * burnedPart;
        return energy;
    }

    public Fire flame(Cell cell, List<Cell> neighbours, LatLng startPoint) {
        // FIXME: Do not ignore weather
        double heat = cell.getFire().getHeat();
        double resource = cell.getFire().getResource();
        heat += 0.2 * calculateBurnEnergy(cell);
        for (Cell neighbour : neighbours) {
            double distance = 1;
            if (neighbour.getX() != cell.getX() && neighbour.getY() != cell.getY()) {
                distance = Math.sqrt(2);
            }
            heat += 0.05 * calculateBurnEnergy(neighbour) / Math.pow(distance, 3);
        }
        heat += (cell.getEnvironment().getWeatherTemperature() - heat) * 0.5;
        long duration = Domain.FORECAST_STEP.toSeconds();
        var x = duration * calculateBurnRate(cell);
        if (x > 1) {
            x = 1;
        }
        resource *= (1 - x);

        return new Fire(heat, resource);
    }
}
