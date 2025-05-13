package ru.cellularwildfire.services;

import ru.cellularwildfire.models.LatLng;

public interface TerrainService {
  byte getForestType(LatLng point);

  double getFuel(LatLng point);

  double getElevation(LatLng point);
}
