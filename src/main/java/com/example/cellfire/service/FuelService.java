package com.example.cellfire.service;

import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FuelService {
    private final Random random = new Random();

    public double getFlammability(LatLng point) {
        return random.nextDouble(200, 300);
    }

    public double getCombustibility(LatLng point) {
        return random.nextDouble(0.3, 0.7);
    }
}
