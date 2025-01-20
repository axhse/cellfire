package com.example.cellfire.entity;

import com.google.maps.model.LatLng;

import java.util.Date;
import java.util.UUID;

public final class Scenario {
    private final Date creationDate = new Date();
    private final String id = UUID.randomUUID().toString();
    private final FireForecast fireForecast = new FireForecast();
    private final LatLng startPoint;
    private final Date startDate;

    public Scenario(LatLng startPoint, Date startDate) {
        this.startPoint = startPoint;
        this.startDate = startDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getId() {
        return id;
    }

    public FireForecast getForecast() {
        return fireForecast;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public Date getStartDate() {
        return startDate;
    }
}
