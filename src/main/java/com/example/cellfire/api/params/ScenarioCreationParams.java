package com.example.cellfire.api.params;


import com.google.maps.model.LatLng;

import java.time.Instant;

public final class ScenarioCreationParams {
    private final double[] startPoint;
    private final long startTs;

    public ScenarioCreationParams(double[] startPoint, long startTs){
        this.startPoint = startPoint;
        this.startTs = startTs;
    }

    public LatLng getStartPoint() {
        return Converter.fromOpenLayerPoint(startPoint);
    }

    public Instant getStartDate() {
        return Instant.ofEpochMilli(startTs);
    }
}
