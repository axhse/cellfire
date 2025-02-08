package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;

import java.util.List;

public final class TerrainMap {
    private final List<MapFragment> fragments;

    public TerrainMap(List<MapFragment> fragments) {
        this.fragments = fragments;
    }

    public byte getValueFor(CellCoordinates coordinates) {
        for (MapFragment fragment : fragments) {
            if (fragment.hasValueFor(coordinates)) {
                return fragment.getValueFor(coordinates);
            }
        }
        throw new IllegalArgumentException("TerrainMap has no value for (%d, %d).".formatted(coordinates.getX(), coordinates.getY()));
    }

    public byte getValueFor(CellCoordinates coordinates, byte defaultValue) {
        for (MapFragment fragment : fragments) {
            if (fragment.hasValueFor(coordinates)) {
                return fragment.getValueFor(coordinates);
            }
        }
        return defaultValue;
    }
}
