package com.example.cellfire.data;

import com.google.maps.model.LatLng;

import java.util.List;

public class Mosaic {
    protected final List<MapFragment> fragments;

    public Mosaic(List<MapFragment> fragments) {
        this.fragments = fragments;
    }

    public byte at(LatLng point) {
        for (MapFragment fragment : fragments) {
            if (fragment.has(point)) {
                return fragment.at(point);
            }
        }
        throw new IllegalArgumentException("Mosaic has no value for (%s).".formatted(point));
    }

    public byte at(LatLng point, byte defaultValue) {
        for (MapFragment fragment : fragments) {
            if (fragment.has(point)) {
                return fragment.at(point);
            }
        }
        return defaultValue;
    }
}
