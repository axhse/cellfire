package com.example.cellfire.service;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public final class WeatherService {
    private final Random random = new Random();

    public double getTemperature(LatLng point, Instant date) {
        return random.nextDouble(30, 70);
    }

    public double getHumidity(LatLng point, Instant date) {
        return random.nextDouble(10, 30);
    }

    public double[] getWind(LatLng point, Instant date) {
        return new double[] {
                random.nextDouble(-5, 10),
                random.nextDouble(-7, 7)
        };
    }
}
