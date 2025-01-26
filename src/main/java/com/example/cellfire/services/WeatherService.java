package com.example.cellfire.services;

import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public final class WeatherService {
    private final Random random = new Random();

    public double getTemperature(CellCoordinates coordinates, Instant date) {
        return 20;
//        return random.nextDouble(15, 30);
    }

    public double getHumidity(CellCoordinates coordinates, Instant date) {
        return 10;
//        return random.nextDouble(10, 30);
    }

    public double[] getWind(CellCoordinates coordinates, Instant date) {
        return new double[] { 1, 1 };
//        return new double[] {
//                random.nextDouble(-2, 3),
//                random.nextDouble(-2.5, 2.5)
//        };
    }
}
