package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;

public interface TerrainService {
    double getElevation(CellCoordinates coordinates);

    double getFuel(CellCoordinates coordinates);

    double getIgnitionTemperature(CellCoordinates coordinates);

    double getActivationEnergy(CellCoordinates coordinates);
}
