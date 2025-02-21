package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;

import java.util.List;

public class Mosaic {
    protected final List<MapFragment> fragments;

    public Mosaic(List<MapFragment> fragments) {
        this.fragments = fragments;
    }

    public byte at(CellCoordinates coordinates) {
        for (MapFragment fragment : fragments) {
            if (fragment.has(coordinates)) {
                return fragment.at(coordinates);
            }
        }
        throw new IllegalArgumentException("Mosaic has no value for (%d, %d).".formatted(coordinates.getX(), coordinates.getY()));
    }

    public byte at(CellCoordinates coordinates, byte defaultValue) {
        for (MapFragment fragment : fragments) {
            if (fragment.has(coordinates)) {
                return fragment.at(coordinates);
            }
        }
        return defaultValue;
    }
}
