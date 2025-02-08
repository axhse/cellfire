package com.example.cellfire.data;

import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.models.Domain;

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

    public boolean hasValueFor(CellCoordinates coordinates) {
        int cellX = coordinates.getX();
        int cellY = coordinates.getY();
        int scaleFactor = Domain.Settings.GRID_SCALE_FACTOR;
        return x * scaleFactor <= cellX && cellX < (x + width) * scaleFactor
                && y * scaleFactor <= cellY && cellY < (y + height) * scaleFactor;
    }

    public byte getValueFor(CellCoordinates coordinates) {
        int cellX = coordinates.getX();
        int cellY = coordinates.getY();
        int scaleFactor = Domain.Settings.GRID_SCALE_FACTOR;
        int valueX = (cellX - x * scaleFactor) * scale / scaleFactor;
        int valueY = (cellY - y * scaleFactor) * scale / scaleFactor;
        return data[valueX][valueY];
    }
}
