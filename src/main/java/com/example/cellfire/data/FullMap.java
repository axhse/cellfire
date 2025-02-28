package com.example.cellfire.data;

import com.google.maps.model.LatLng;

public final class FullMap extends MapFragment {
    public FullMap(byte[][] data, int scale) {
        super(data, scale, -180, -90, 360, 180);
    }

    @Override
    public boolean has(LatLng point) {
        return true;
    }
}
