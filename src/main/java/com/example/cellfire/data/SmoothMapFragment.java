package com.example.cellfire.data;

import com.google.maps.model.LatLng;

public final class SmoothMapFragment extends MapFragment {
    public SmoothMapFragment(byte[][] data, int scale, int x, int y, int width, int height) {
        super(data, scale, x, y, width, height);
    }

    @Override
    public byte at(LatLng point) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
