package com.example.cellfire.api.params;

import com.google.maps.model.LatLng;

public final class Converter {
    public static LatLng fromGeoPoint(double[] point) {
        return new LatLng(point[1], point[1]);
    }

    public static double[] toGeoPoint(LatLng point) {
        return new double[]{ point.lng, point.lat };
    }
}
