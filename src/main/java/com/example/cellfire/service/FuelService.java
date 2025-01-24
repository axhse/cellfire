package com.example.cellfire.service;

import com.example.cellfire.entity.Domain;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Random;

@Service
public class FuelService {
    private final Random random = new Random();
    private final byte[][] data = loadData();
    private final int SECTOR_LAT = 36;
    private final int SECTOR_LNG = 48;

    public double getResource(LatLng point) {
        if (point.lat < SECTOR_LAT || SECTOR_LAT + 3 <= point.lat) {
            return 0;
        }
        if (point.lng < SECTOR_LNG || SECTOR_LNG + 3 <= point.lng) {
            return 0;
        }
        int y = (int)Math.round((point.lat - SECTOR_LAT) * 100);
        int x = (int)Math.round((point.lng - SECTOR_LNG) * 100);

        return data[x][y] / 40.0;
        // return random.nextDouble(0.3, 0.7);
    }

    public double getFlammability(LatLng point) {
        if (getResource(point) == 0) {
            return Domain.INFINITE_FLAMMABILITY;
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
