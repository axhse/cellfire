package com.example.cellfire.services;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Random;

@Service
public class FuelService {
    private final Random random = new Random();
    private final byte[][] canopyHeight = loadData();
    private final int SECTOR_LNG = 48;
    private final int SECTOR_LAT = 36;

    public float getFuel(CellCoordinates coordinates) {
        if (coordinates.getX() < SECTOR_LNG * Domain.Settings.GRID_SCALE_FACTOR
                || (SECTOR_LNG + 3) * Domain.Settings.GRID_SCALE_FACTOR <= coordinates.getX()) {
            return 0;
        }
        if (coordinates.getY() < SECTOR_LAT * Domain.Settings.GRID_SCALE_FACTOR
                || (SECTOR_LAT + 3) * Domain.Settings.GRID_SCALE_FACTOR <= coordinates.getY()) {
            return 0;
        }
        int x = coordinates.getX() - SECTOR_LNG * Domain.Settings.GRID_SCALE_FACTOR;
        int y = coordinates.getY() - SECTOR_LAT * Domain.Settings.GRID_SCALE_FACTOR;

        float fuel = calculateFuel(canopyHeight[x / 2][y / 2]);
        return fuel < Domain.Settings.SIGNIFICANT_FUEL ? 0 : fuel;
    }

    public float getIgnitionTemperature(CellCoordinates coordinates) {
        return random.nextFloat(200, 300);
    }

    private float calculateFuel(float canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }

    private byte[][] loadData() {
        byte[][] loadedData = new byte[300][300];
        String resourceName = "canopy_height_map/N%dE0%d.bin".formatted(SECTOR_LAT, SECTOR_LNG);
        try (InputStream inputStream = FuelService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return loadedData;
            }
            byte[] bytes = inputStream.readAllBytes();
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    // FIXME: Why [y][300 - 1 - x], not [x][y]??
                    loadedData[y][300 - 1 - x] = bytes[300 * x + y];
                }
            }
        }
        catch (Exception exception) {
            return loadedData;
        }
        return loadedData;
    }
}
