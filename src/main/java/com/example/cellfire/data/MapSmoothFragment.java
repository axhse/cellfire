package com.example.cellfire.data;

import com.google.maps.model.LatLng;

public class MapSmoothFragment extends MapFragment {
    public MapSmoothFragment(byte[][] data, int scale, int x, int y, int width, int height) {
        super(data, scale, x, y, width, height);
    }

    @Override
    public byte at(LatLng point) {
        LatLng center = new LatLng(
                (Math.floor(point.lat * scale) + 0.5) / scale,
                (Math.floor(point.lng * scale) + 0.5) / scale
        );
        int[] dxValues = Math.abs(point.lng - center.lng) < 0.05 / scale ? new int[]{0} : new int[]{-1, 1};
        int[] dyValues = Math.abs(point.lat - center.lat) < 0.05 / scale ? new int[]{0} : new int[]{-1, 1};
        if (dxValues.length + dyValues.length == 2) {
            return super.at(point);
        }
        double value = 0;
        double weight = 0;
        for (int dx : dxValues) {
            for (int dy : dyValues) {
                LatLng referencePoint = getReferencePoint(point, dx, dy);
                if (!has(referencePoint)) {
                    continue;
                }
                double distanceLat = dy * (center.lat - point.lat);
                double distanceLng = dx * (center.lng - point.lng);
                if (distanceLat < 0) {
                    distanceLat += 1.0 / scale;
                }
                if (distanceLng < 0) {
                    distanceLng += 1.0 / scale;
                }
                distanceLng *= Math.cos(Math.toRadians(point.lat));
                var v = super.at(referencePoint);
                var d = Math.sqrt(distanceLat * distanceLat + distanceLng * distanceLng);
                double referenceWeight = 1 / Math.sqrt(distanceLat * distanceLat + distanceLng * distanceLng);
                weight += referenceWeight;
                value += referenceWeight * super.at(referencePoint);
            }
        }
        var x = value / weight;
        return (byte) Math.round(value / weight);
    }

    private LatLng getReferencePoint(LatLng point, int dx, int dy) {
        double lat = point.lat + 0.5 * dy / scale;
        double lng = point.lng + 0.5 * dx / scale;
        if (lat < -90) {
            lat = -180 - lat;
            lng += 180;
        }
        if (90 <= lat) {
            lat = 180 - lat;
            lng += 180;
        }
        if (lng < -180) {
            lng += 360;
        }
        if (180 <= lng) {
            lng -= 360;
        }
        return new LatLng(lat, lng);
    }
}
