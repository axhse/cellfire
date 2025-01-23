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

    public double getFlammability(LatLng point) {
        if (point.lat < 54 || 54 + 3 <= point.lat) {
            return 0;
        }
        if (point.lng < 36 || 36 + 3 <= point.lng) {
            return 0;
        }
        int y = (int)Math.round((point.lat - 54) * 100);
        int x = (int)Math.round((point.lng - 36) * 100);

        double combustibility = data[x][y];
        if (combustibility == 0) {
            return Domain.INFINITE_FLAMMABILITY;
        }
        return random.nextDouble(200, 300);
    }

    public double getCombustibility(LatLng point) {
        if (point.lat < 54 || 54 + 3 <= point.lat) {
            return 0;
        }
        if (point.lng < 36 || 36 + 3 <= point.lng) {
            return 0;
        }
        int y = (int)Math.round((point.lat - 54) * 100);
        int x = (int)Math.round((point.lng - 36) * 100);

        return data[x][y];
        // return random.nextDouble(0.3, 0.7);
    }

    private byte[][] loadData() {
        byte[][] loadedData = new byte[300][300];
        try (InputStream inputStream = FuelService.class.getClassLoader().getResourceAsStream("fuel_map.bin")) {
            if (inputStream == null) {
                return loadedData;
            }
            byte[] bytes = inputStream.readAllBytes();
            for (int y = 0; y < 300; y++) {
                for (int x = 0; x < 300; x++) {
                    loadedData[x][y] = bytes[300 * y + x];
                }
            }
        }
        catch (Exception exception) {
            return loadedData;
        }
        return loadedData;
    }
}
