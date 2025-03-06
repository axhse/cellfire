package com.example.cellfire.services;

import com.google.maps.model.LatLng;

public interface TerrainService {
    double getActivationEnergy(LatLng point);

    double getFuel(LatLng point);

    double getElevation(LatLng point);
}
