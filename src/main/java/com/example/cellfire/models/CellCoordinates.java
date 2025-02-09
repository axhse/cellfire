package com.example.cellfire.models;

import com.google.maps.model.LatLng;

import java.util.Objects;

public final class CellCoordinates {
    private final int x;
    private final int y;

    public CellCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellCoordinates otherCoordinates = (CellCoordinates) o;
        return x == otherCoordinates.x && y == otherCoordinates.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public LatLng toGeoPoint() {
        return new LatLng((y + 0.5) * ModelSettings.GRID_CELL_SIZE, (x + 0.5) * ModelSettings.GRID_CELL_SIZE);
    }

    public CellCoordinates createRelative(int offsetX, int offsetY) {
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        if (y < -ModelSettings.GRID_X_TICKS / 4) {
            y = -ModelSettings.GRID_X_TICKS / 2 + y;
            x += ModelSettings.GRID_X_TICKS / 2;
        }
        if (ModelSettings.GRID_X_TICKS / 4 <= y) {
            y = ModelSettings.GRID_X_TICKS / 2 - y - 1;
            x += ModelSettings.GRID_X_TICKS / 2;
        }
        if (x <= -ModelSettings.GRID_X_TICKS / 2) {
            x += ModelSettings.GRID_X_TICKS;
        }
        if (ModelSettings.GRID_X_TICKS / 2 < x) {
            x -= ModelSettings.GRID_X_TICKS;
        }
        return new CellCoordinates(x, y);
    }
}
