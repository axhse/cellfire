package com.example.cellfire.services;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public final class StandaloneWeatherService implements WeatherService {
    @Override
    public double getAirTemperature(LatLng point, Instant date) {
        return 25;
    }

    @Override
    public double getAirHumidity(LatLng point, Instant date) {
        return 0.4;
    }

    @Override
    public double getWindX(LatLng point, Instant date) {
        return 5;
    }

    @Override
    public double getWindY(LatLng point, Instant date) {
        return -2;
    }
}
