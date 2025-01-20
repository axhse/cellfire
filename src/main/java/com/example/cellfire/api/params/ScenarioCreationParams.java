package com.example.cellfire.api.params;


import com.google.maps.model.LatLng;

import java.util.Date;

public final class ScenarioCreationParams {
    private final double[] startPoint;
    private final long startTs;

    public ScenarioCreationParams(double[] startPoint, long startTs){
        this.startPoint = startPoint;
        this.startTs = startTs;
    }

    public LatLng getStartPoint() {
        return Converter.fromGeoPoint(startPoint);
    }

    public Date getStartDate() {
        return new Date(startTs);
    }
}
