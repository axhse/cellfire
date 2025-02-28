package com.example.cellfire.tuner.services;

import com.example.cellfire.services.MosaicTerrainService;
import com.example.cellfire.services.TerrainService;
import com.google.maps.model.LatLng;

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
    public double getIgnitionTemperature(LatLng point) {
        return MosaicTerrainService.determineIgnitionTemperature(this.forestType);
    }

    @Override
    public double getActivationEnergy(LatLng point) {
        return MosaicTerrainService.determineActivationEnergy(this.forestType);
    }

    @Override
    public double getFuel(LatLng point) {
        return this.fuel;
    }

    @Override
    public double getElevation(LatLng point) {
        return this.elevation;
    }
}
