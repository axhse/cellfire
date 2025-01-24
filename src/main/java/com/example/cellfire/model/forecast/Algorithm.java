package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.FireCell;
import com.example.cellfire.entity.FuelCell;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class Algorithm {
    public final double PROPAGATION_RATE = 0.3;

    public FlameOutcome flame(Cell cell, List<Cell> neighbours, LatLng startPoint) {
        // FIXME: Do not ignore weather
        double heat = cell.getFireCell().getHeat();
        double resource = cell.getFuelCell().getResource();
        double flammability = cell.getFuelCell().getResource();
        if (heat > flammability) {
            heat += resource * 0.1 * 100000;
            resource *= 0.7;
        }
        for (Cell neighbour : neighbours) {
            if (heat < neighbour.getFireCell().getHeat()) {
                heat += (neighbour.getFireCell().getHeat() - heat) * 1;
            }
        }
        heat += (cell.getWeatherCell().getTemperature() - heat) * 0.5;
//        FireCell outcomeFire = ;
//        FuelCell outcomeFuel = new FuelCell(cell.getFuelCell().getResource(), cell.getFuelCell().getFlammability());
        return new FlameOutcome(
                new FireCell(heat),
                new FuelCell(resource, flammability)
        );
    }
}
