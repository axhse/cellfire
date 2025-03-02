package com.example.cellfire.data;

public final class FullSmoothMap extends MapSmoothFragment {
    public FullSmoothMap(byte[][] data, int scale) {
        super(data, scale, -180, -90, 360, 180);
    }
}
