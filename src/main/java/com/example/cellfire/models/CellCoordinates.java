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
        return new LatLng((y + 0.5) * Domain.Settings.CELL_SIZE, (x + 0.5) * Domain.Settings.CELL_SIZE);
    }

    public CellCoordinates createRelative(int offsetX, int offsetY) {
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        if (y < -Domain.Settings.GRID_SCALE / 4) {
            y = -Domain.Settings.GRID_SCALE / 2 + y;
            x += Domain.Settings.GRID_SCALE / 2;
        }
        if (Domain.Settings.GRID_SCALE / 4 <= y) {
            y = Domain.Settings.GRID_SCALE / 2 - y - 1;
            x += Domain.Settings.GRID_SCALE / 2;
        }
        if (x <= -Domain.Settings.GRID_SCALE / 2) {
            x += Domain.Settings.GRID_SCALE;
        }
        if (Domain.Settings.GRID_SCALE / 2 < x) {
            x -= Domain.Settings.GRID_SCALE;
        }
        return new CellCoordinates(x, y);
    }

    public double calculateCellArea() {
        double dLat = Math.toRadians(Domain.Settings.CELL_SIZE);
        double dSinLng = Math.sin(Math.toRadians((x + 1.0) * Domain.Settings.CELL_SIZE)) - Math.sin(Math.toRadians((x + 0.0) * Domain.Settings.CELL_SIZE));
        return Domain.EARTH_RADIUS * Domain.EARTH_RADIUS * dLat * dSinLng;
    }
}
