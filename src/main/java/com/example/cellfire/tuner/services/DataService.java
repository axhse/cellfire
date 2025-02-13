package com.example.cellfire.tuner.services;

import com.example.cellfire.models.CellCoordinates;

import java.util.HashMap;
import java.util.Map;

public final class DataService<TValue> {
    private final TValue defaultValue;
    private final Map<Long, TValue> specificValues = new HashMap<Long, TValue>();

    public DataService(TValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public TValue getValue(CellCoordinates coordinates) {
        long key = coordinates.getX() * 123L + coordinates.getY();
        return specificValues.getOrDefault(key, defaultValue);
    }
}
