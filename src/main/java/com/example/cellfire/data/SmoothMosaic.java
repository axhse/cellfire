package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;

import java.util.List;

public final class SmoothMosaic extends Mosaic {
    public SmoothMosaic(List<MapFragment> fragments) {
        super(fragments);
    }

    public byte at(CellCoordinates coordinates) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public byte at(CellCoordinates coordinates, byte defaultValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
