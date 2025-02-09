package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;

import java.time.Instant;

public interface WeatherService {
    double getAirTemperature(CellCoordinates coordinates, Instant date);

    double getAirHumidity(CellCoordinates coordinates, Instant date);

    double getWindX(CellCoordinates coordinates, Instant date);

    double getWindY(CellCoordinates coordinates, Instant date);
}
