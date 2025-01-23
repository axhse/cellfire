package com.example.cellfire.api.params;

import com.google.maps.model.LatLng;

public final class Converter {
    public static LatLng fromOpenLayerPoint(double[] point) {
        return new LatLng(point[1], point[0]);
    }

    public static double[] toOpenLayerPoint(LatLng point) {
        return new double[]{ point.lng, point.lat };
    }
}
