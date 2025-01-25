package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.Fire;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class Algorithm {
    public final double PROPAGATION_RATE = 0.3;

    public Fire flame(Cell cell, List<Cell> neighbours, LatLng startPoint) {
        // FIXME: Do not ignore weather
        double heat = cell.getFire().getHeat();
        double resource = cell.getFire().getResource();
        double ignitionTemperature = cell.getEnvironment().getIgnitionTemperature();
        if (heat > ignitionTemperature) {
            heat += resource * 0.1 * 100000;
            resource *= 0.7;
        }
        for (Cell neighbour : neighbours) {
            if (heat < neighbour.getFire().getHeat()) {
                heat += (neighbour.getFire().getHeat() - heat) * 1;
            }
        }
        heat += (cell.getEnvironment().getWeatherTemperature() - heat) * 0.5;

        return new Fire(heat, resource);
    }
}
