package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;

public final class FullMap extends MapFragment {
    public FullMap(byte[][] data, int scale) {
        super(data, scale, -180, -90, 360, 180);
    }

    @Override
    public boolean has(CellCoordinates coordinates) {
        return true;
    }
}
