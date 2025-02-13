package com.example.cellfire.tuner.services;

import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.services.TerrainMapService;
import com.example.cellfire.services.TerrainService;

public final class UniformTerrainService implements TerrainService {
    private final double elevation;
    private final double fuel;
    private final byte forestType;

    public UniformTerrainService(double elevation, double fuel, byte forestType) {
        this.elevation = elevation;
        this.fuel = fuel;
        this.forestType = forestType;
    }

    @Override
    public double getElevation(CellCoordinates coordinates) {
        return this.elevation;
    }

    @Override
    public double getFuel(CellCoordinates coordinates) {
        return this.fuel;
    }

    @Override
    public double getIgnitionTemperature(CellCoordinates coordinates) {
        return TerrainMapService.determineIgnitionTemperature(this.forestType);
    }

    @Override
    public double getActivationEnergy(CellCoordinates coordinates) {
        return TerrainMapService.determineActivationEnergy(this.forestType);
    }
}
