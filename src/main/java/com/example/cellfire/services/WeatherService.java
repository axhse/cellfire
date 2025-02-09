package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public final class WeatherService {
    public double getAirTemperature(CellCoordinates coordinates, Instant date) {
        return 20;
    }

    public double getAirHumidity(CellCoordinates coordinates, Instant date) {
        return 10;
    }

    public double getWindX(CellCoordinates coordinates, Instant date) {
        return 1;
    }

    public double getWindY(CellCoordinates coordinates, Instant date) {
        return 3;
    }
}
