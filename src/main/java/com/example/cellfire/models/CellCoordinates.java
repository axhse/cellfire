package com.example.cellfire.models;

import com.example.cellfire.DomainSettings;
import com.google.maps.model.LatLng;

import java.util.Objects;

public final class CellCoordinates {
    private final short x;
    private final short y;

    public CellCoordinates(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public static CellCoordinates fromGeoPoint(LatLng geoPoint) {
        return new CellCoordinates(
                (short)Math.round(geoPoint.lng * DomainSettings.SCALE_FACTOR - 0.5),
                (short)Math.max(-90 * DomainSettings.SCALE_FACTOR, Math.round(geoPoint.lat * DomainSettings.SCALE_FACTOR - 0.5))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellCoordinates cellCoordinates = (CellCoordinates) o;
        return x == cellCoordinates.x && y == cellCoordinates.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public LatLng toGeoPoint() {
        return new LatLng(
                (y + 0.5) / (double)DomainSettings.SCALE_FACTOR,
                (x + 0.5) / (double)DomainSettings.SCALE_FACTOR
        );
    }

    public CellCoordinates getRelative(int offsetX, int offsetY) {
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
        return new CellCoordinates((short) x, (short) y);
    }

    public double calculatePhysicalDistanceTo(CellCoordinates cellCoordinates) {
        // TODO
        return 1;
    }
}
