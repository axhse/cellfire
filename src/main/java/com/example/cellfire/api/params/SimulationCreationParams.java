package com.example.cellfire.api.params;

import com.google.maps.model.LatLng;

import java.time.Instant;

public final class SimulationCreationParams {
    private final double[] startLonLat;
    private final long startDateMs;
    private final String algorithm;

    public SimulationCreationParams(double[] startLonLat, long startDateMs, String algorithm) {
        this.startLonLat = startLonLat;
        this.startDateMs = startDateMs;
        this.algorithm = algorithm;
    }

    public LatLng getStartPoint() {
        return new LatLng(startLonLat[1], startLonLat[0]);
    }

    public Instant getStartDate() {
        return Instant.ofEpochMilli(startDateMs);
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
