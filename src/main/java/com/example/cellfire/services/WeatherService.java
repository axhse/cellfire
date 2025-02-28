package com.example.cellfire.services;

import com.google.maps.model.LatLng;

import java.time.Instant;

public interface WeatherService {
    double getAirTemperature(LatLng point, Instant date);

    double getAirHumidity(LatLng point, Instant date);

    double getWindX(LatLng point, Instant date);

    double getWindY(LatLng point, Instant date);
}
