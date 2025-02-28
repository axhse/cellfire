package com.example.cellfire.services;

import com.example.cellfire.data.ForestConditions;
import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.data.ResourceLoader;
import com.example.cellfire.data.Mosaic;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

@Service
public final class MosaicTerrainService implements TerrainService {
    private final Mosaic elevationMap = ResourceLoader.loadElevationMap();
    private final Mosaic forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
    private final Mosaic canopyHeightMap = ResourceLoader.loadCanopyHeightMap();

    @Override
    public double getIgnitionTemperature(LatLng point) {
        byte forestType = forestTypeClusterMap.at(point, (byte) 0);
        return ForestConditions.determineIgnitionTemperature(forestType);
    }

    @Override
    public double getActivationEnergy(LatLng point) {
        byte forestType = forestTypeClusterMap.at(point, (byte) 0);
        return ForestConditions.determineActivationEnergy(forestType);
    }

    @Override
    public double getFuel(LatLng point) {
        double canopyHeight = canopyHeightMap.at(point, (byte) 0);
        double fuel = calculateFuel(canopyHeight);
        return fuel < ModelSettings.SIGNIFICANT_FUEL ? 0 : fuel;
    }

    @Override
    public double getElevation(LatLng point) {
        double elevation = elevationMap.at(point, (byte) 0);
        return elevation * 6400 / 255;
    }

    private double calculateFuel(double canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }
}
