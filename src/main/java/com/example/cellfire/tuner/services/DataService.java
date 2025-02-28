package com.example.cellfire.tuner.services;

import com.google.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public final class DataService<TValue> {
    private final TValue defaultValue;
    private final Map<LatLng, TValue> specificValues = new HashMap<LatLng, TValue>();

    public DataService(TValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public TValue getValue(LatLng point) {
        return specificValues.getOrDefault(point, defaultValue);
    }
}
