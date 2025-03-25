package com.example.cellfire.services;

import com.example.cellfire.models.LatLng;

public interface TerrainService {
  double getActivationEnergy(LatLng point);

  double getFuel(LatLng point);

  double getElevation(LatLng point);
}
