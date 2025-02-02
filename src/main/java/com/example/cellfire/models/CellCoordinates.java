package com.example.cellfire.models;

import com.example.cellfire.DomainSettings;
import com.google.maps.model.LatLng;

import java.util.Objects;

public final class CellCoordinates {
    private final int x;
    private final int y;

    public CellCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static CellCoordinates fromGeoPoint(LatLng geoPoint) {
        return new CellCoordinates(
                (int)Math.round(geoPoint.lng * DomainSettings.SCALE_FACTOR - 0.5),
                (int)Math.max(-90 * DomainSettings.SCALE_FACTOR, Math.round(geoPoint.lat * DomainSettings.SCALE_FACTOR - 0.5))
        );
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
        return new LatLng(
                (y + 0.5) / (double)DomainSettings.SCALE_FACTOR,
                (x + 0.5) / (double)DomainSettings.SCALE_FACTOR
        );
    }

    public CellCoordinates shift(int offsetX, int offsetY) {
        int x = this.x + offsetX;
        int y = this.y + offsetY;
        if (y < -DomainSettings.AXES_SCALE / 4) {
            y = -DomainSettings.AXES_SCALE / 2 + y;
            x += DomainSettings.AXES_SCALE / 2;
        }
        if (DomainSettings.AXES_SCALE / 4 <= y) {
            y = DomainSettings.AXES_SCALE / 2 - y - 1;
            x += DomainSettings.AXES_SCALE / 2;
        }
        if (x <= -DomainSettings.AXES_SCALE / 2) {
            x += DomainSettings.AXES_SCALE;
        }
        if (DomainSettings.AXES_SCALE / 2 < x) {
            x -= DomainSettings.AXES_SCALE;
        }
        return new CellCoordinates(x, y);
    }

    public double calculatePhysicalDistanceTo(CellCoordinates otherCoordinates) {
        // FIXME
        if (x != otherCoordinates.getX() && y != otherCoordinates.getY()) {
            return Math.sqrt(2);
        }
        return 1;
    }
}
