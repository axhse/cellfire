package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public final class StandaloneWeatherService implements WeatherService {
    @Override
    public double getAirTemperature(CellCoordinates coordinates, Instant date) {
        return 20;
    }

    @Override
    public double getAirHumidity(CellCoordinates coordinates, Instant date) {
        return 0.2;
    }

    @Override
    public double getWindX(CellCoordinates coordinates, Instant date) {
        return 1;
    }

    @Override
    public double getWindY(CellCoordinates coordinates, Instant date) {
        return 3;
    }
}
