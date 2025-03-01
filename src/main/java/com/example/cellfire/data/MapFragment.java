package com.example.cellfire.data;

import com.google.maps.model.LatLng;

public class MapFragment {
    private final byte[][] data;
    private final int scale;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public MapFragment(byte[][] data, int scale, int x, int y, int width, int height) {
        this.data = data;
        this.scale = scale;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean has(LatLng point) {
        int valueX = (int) Math.round((point.lng - x) * scale - 0.5);
        int valueY = (int) Math.round((point.lat - y) * scale - 0.5);
        return 0 <= valueX && valueX < width * scale && 0 <= valueY && valueY < height * scale;
    }

    public byte at(LatLng point) {
        int valueX = (int) Math.round((point.lng - x) * scale - 0.5);
        int valueY = (int) Math.round((point.lat - y) * scale - 0.5);
        return data[valueX][valueY];
    }
}
