package com.example.cellfire.tuner.services;

import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.services.TerrainService;
import com.google.maps.model.LatLng;

public final class UniformTerrainService implements TerrainService {
    private final int forestType;
    private final double fuel;
    private final double elevation;

    public UniformTerrainService(int forestType, double fuel, double elevation) {
        this.forestType = forestType;
        this.fuel = fuel;
        this.elevation = elevation;
    }

    @Override
    public double getActivationEnergy(LatLng point) {
        return ForestTypeConditions.determineActivationEnergy(this.forestType);
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
