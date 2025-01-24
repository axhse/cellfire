package com.example.cellfire.service;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public final class WeatherService {
    private final Random random = new Random();

    public double getTemperature(LatLng point, Instant date) {

        return 20;
//        return random.nextDouble(15, 30);
    }

    public double getHumidity(LatLng point, Instant date) {
        return 10;
//        return random.nextDouble(10, 30);
    }

    public double[] getWind(LatLng point, Instant date) {
        return new double[] { 1, 1 };
//        return new double[] {
//                random.nextDouble(-2, 3),
//                random.nextDouble(-2.5, 2.5)
//        };
    }
}
