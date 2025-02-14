package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;

public interface TerrainService {

    double getIgnitionTemperature(CellCoordinates coordinates);

    double getActivationEnergy(CellCoordinates coordinates);

    double getFuel(CellCoordinates coordinates);

    double getElevation(CellCoordinates coordinates);
}
