package com.example.cellfire.services;

import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.data.ResourceLoader;
import com.example.cellfire.data.Mosaic;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

@Service
public final class MosaicTerrainService implements TerrainService {
    private final Mosaic elevationMap = ResourceLoader.loadElevationMap();
    private final Mosaic forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
    private final Mosaic forestDensityMap = ResourceLoader.loadForestDensityMap();

    @Override
    public double getIgnitionTemperature(LatLng point) {
        int forestType = forestTypeClusterMap.at(point, 0);
        return ForestTypeConditions.determineIgnitionTemperature(forestType);
    }

    @Override
    public double getActivationEnergy(LatLng point) {
        int forestType = forestTypeClusterMap.at(point, 0);
        return ForestTypeConditions.determineActivationEnergy(forestType);
    }

    @Override
    public double getFuel(LatLng point) {
        return forestDensityMap.at(point, 0) / 255.0;
    }

    @Override
    public double getElevation(LatLng point) {
        double elevation = elevationMap.at(point, 0);
        return elevation * 6400 / 255;
    }
}
