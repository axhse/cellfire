package com.example.cellfire.services;

import com.example.cellfire.data.ResourceLoader;
import com.example.cellfire.data.TerrainMap;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

@Service
public class TerrainService {
    private final TerrainMap forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
    private final TerrainMap canopyHeightMap = ResourceLoader.loadCanopyHeightMap();

    public float getFuel(CellCoordinates coordinates) {
        float canopyHeight = canopyHeightMap.getValueFor(coordinates, (byte)0);
        return calculateFuel(canopyHeight);
    }

    public float getIgnitionTemperature(CellCoordinates coordinates) {
        return 200;
    }

    private float calculateFuel(float canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }
}
