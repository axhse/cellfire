package com.example.cellfire.services;

import com.example.cellfire.data.ResourceLoader;
import com.example.cellfire.data.TerrainMap;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

@Service
public class TerrainService {
    private final TerrainMap elevationMap = ResourceLoader.loadElevationMap();
    private final TerrainMap forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
    private final TerrainMap canopyHeightMap = ResourceLoader.loadCanopyHeightMap();

    public float getElevation(CellCoordinates coordinates) {
        float elevation = elevationMap.getValueFor(coordinates, (byte)0);
        return elevation * 6400 / 255;
    }

    public float getFuel(CellCoordinates coordinates) {
        float canopyHeight = canopyHeightMap.getValueFor(coordinates, (byte)0);
        return calculateFuel(canopyHeight);
    }

    public float getIgnitionTemperature(CellCoordinates coordinates) {
        return switch (forestTypeClusterMap.getValueFor(coordinates, (byte) 0)) {
            case 1 -> 100;
            case 2 -> 200;
            case 3 -> 300;
            case 4 -> 400;
            case 5 -> 500;
            default -> -1;
        };
    }

    public float getActivationEnergy(CellCoordinates coordinates) {
        return switch (forestTypeClusterMap.getValueFor(coordinates, (byte) 0)) {
            case 1 -> 100;
            case 2 -> 200;
            case 3 -> 300;
            case 4 -> 400;
            case 5 -> 500;
            default -> -1;
        };
    }

    private float calculateFuel(float canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }
}
