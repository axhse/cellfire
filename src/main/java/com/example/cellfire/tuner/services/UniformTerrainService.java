package com.example.cellfire.tuner.services;

import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.services.TerrainMapService;
import com.example.cellfire.services.TerrainService;

public final class UniformTerrainService implements TerrainService {
    private final byte forestType;
    private final double fuel;
    private final double elevation;

    public UniformTerrainService(byte forestType, double fuel, double elevation) {
        this.forestType = forestType;
        this.fuel = fuel;
        this.elevation = elevation;
    }

    @Override
    public double getIgnitionTemperature(CellCoordinates coordinates) {
        return TerrainMapService.determineIgnitionTemperature(this.forestType);
    }

    @Override
    public double getActivationEnergy(CellCoordinates coordinates) {
        return TerrainMapService.determineActivationEnergy(this.forestType);
    }

    @Override
    public double getFuel(CellCoordinates coordinates) {
        return this.fuel;
    }

    @Override
    public double getElevation(CellCoordinates coordinates) {
        return this.elevation;
    }
}
