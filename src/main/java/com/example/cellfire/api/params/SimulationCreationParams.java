package com.example.cellfire.api.params;

import com.google.maps.model.LatLng;

public final class SimulationCreationParams {
    private final double[] startLonLat;
    private final String algorithm;

    public SimulationCreationParams(double[] startLonLat, String algorithm) {
        this.startLonLat = startLonLat;
        this.algorithm = algorithm;
    }

    public LatLng getStartPoint() {
        return new LatLng(startLonLat[1], startLonLat[0]);
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
