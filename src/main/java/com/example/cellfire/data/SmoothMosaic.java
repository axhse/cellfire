package com.example.cellfire.data;

import com.google.maps.model.LatLng;

import java.util.List;

public final class SmoothMosaic extends Mosaic {
    public SmoothMosaic(List<MapFragment> fragments) {
        super(fragments);
    }

    @Override
    public byte at(LatLng point) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte at(LatLng point, byte defaultValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
