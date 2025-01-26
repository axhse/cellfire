package com.example.cellfire.services;

import com.example.cellfire.DomainSettings;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Random;

@Service
public class FuelService {
    private final Random random = new Random();
    private final byte[][] data = loadData();
    private final int SECTOR_LNG = 48;
    private final int SECTOR_LAT = 36;

    public double getResource(CellCoordinates coordinates) {
        if (coordinates.getX() < SECTOR_LNG * DomainSettings.SCALE_FACTOR
                || (SECTOR_LNG + 3) * DomainSettings.SCALE_FACTOR <= coordinates.getX()) {
            return 0;
        }
        if (coordinates.getY() < SECTOR_LAT * DomainSettings.SCALE_FACTOR
                || (SECTOR_LAT + 3) * DomainSettings.SCALE_FACTOR <= coordinates.getY()) {
            return 0;
        }
        int x = coordinates.getX() - SECTOR_LNG * DomainSettings.SCALE_FACTOR;
        int y = coordinates.getY() - SECTOR_LAT * DomainSettings.SCALE_FACTOR;

        return data[x][y] / 40.0;
        // return random.nextDouble(0.3, 0.7);
    }

    public double getIgnitionTemperature(CellCoordinates coordinates) {
        if (getResource(coordinates) == 0) {
            return DomainSettings.INFINITE_IGNITION_TEMPERATURE;
        }
        return random.nextDouble(200, 300);
    }

    private byte[][] loadData() {
        byte[][] loadedData = new byte[300][300];
        String resourceName = "fuel_resource_map/N%dE0%d.bin".formatted(SECTOR_LAT, SECTOR_LNG);
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
