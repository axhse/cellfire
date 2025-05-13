package ru.cellularwildfire.services;

import ru.cellularwildfire.data.Mosaic;
import ru.cellularwildfire.models.LatLng;

public final class MosaicTerrainService implements TerrainService {
  private final Mosaic elevationMap;
  private final Mosaic forestTypeMap;
  private final Mosaic forestDensityMap;

  public MosaicTerrainService(Mosaic elevationMap, Mosaic forestTypeMap, Mosaic forestDensityMap) {
    this.elevationMap = elevationMap;
    this.forestTypeMap = forestTypeMap;
    this.forestDensityMap = forestDensityMap;
  }

  @Override
  public byte getForestType(LatLng point) {
    return (byte) forestTypeMap.at(point, 0);
  }

  @Override
  public double getFuel(LatLng point) {
    return forestDensityMap.at(point, 0) / 255.0;
  }

  @Override
  public double getElevation(LatLng point) {
    return elevationMap.at(point, 0) * 6400.0 / 255;
  }
}
