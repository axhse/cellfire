package com.example.cellfire.service;

import com.google.maps.model.LatLng;

import java.util.Date;
import java.util.Random;

public final class WeatherService {
    private final Random random = new Random();

    public double getTemperature(LatLng point, Date date) {
        return random.nextDouble(30, 70);
    }

    public double getHumidity(LatLng point, Date date) {
        return random.nextDouble(10, 30);
    }

    public double[] getWind(LatLng point, Date date) {
        return new double[] {
                random.nextDouble(-5, 10),
                random.nextDouble(-7, 7)
        };
    }
}
