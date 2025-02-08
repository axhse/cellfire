package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;

public final class MapFullFragment extends MapFragment {
    public MapFullFragment(byte[][] data, int scale) {
        super(data, scale, -180, -90, 360, 180);
    }

    @Override
    public boolean hasValueFor(CellCoordinates coordinates) {
        return true;
    }
}
