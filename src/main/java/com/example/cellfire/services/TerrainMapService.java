package com.example.cellfire.services;

import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.data.ResourceLoader;
import com.example.cellfire.data.TerrainMap;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

@Service
public class TerrainMapService implements TerrainService {
    private final TerrainMap elevationMap = ResourceLoader.loadElevationMap();
    private final TerrainMap forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
    private final TerrainMap canopyHeightMap = ResourceLoader.loadCanopyHeightMap();

    public static double determineIgnitionTemperature(byte forestType) {
        return switch (forestType) {
            case 1 -> 325;
            case 2 -> 275;
            case 3 -> 305;
            case 4 -> 235;
            case 5 -> 285;
            default -> 10_000;
        };
    }

    public static double determineActivationEnergy(byte forestType) {
        return switch (forestType) {
            case 1 -> 125_000;
            case 2 -> 105_000;
            case 3 -> 115_000;
            case 4 -> 95_000;
            case 5 -> 110_000;
            default -> 10_000_000;
        };
    }

    @Override
    public double getIgnitionTemperature(CellCoordinates coordinates) {
        byte forestType = forestTypeClusterMap.getValueFor(coordinates, (byte) 0);
        return TerrainMapService.determineIgnitionTemperature(forestType);
    }

    @Override
    public double getActivationEnergy(CellCoordinates coordinates) {
        byte forestType = forestTypeClusterMap.getValueFor(coordinates, (byte) 0);
        return TerrainMapService.determineActivationEnergy(forestType);
    }

    @Override
    public double getFuel(CellCoordinates coordinates) {
        double canopyHeight = canopyHeightMap.getValueFor(coordinates, (byte)0);
        double fuel = calculateFuel(canopyHeight);
        return fuel < ModelSettings.SIGNIFICANT_FUEL ? 0 : fuel;
    }

    @Override
    public double getElevation(CellCoordinates coordinates) {
        double elevation = elevationMap.getValueFor(coordinates, (byte)0);
        return elevation * 6400 / 255;
    }

    private double calculateFuel(double canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }
}
