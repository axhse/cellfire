package ru.cellularwildfire.tuner.services;

import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.services.TerrainService;

public final class UniformTerrainService implements TerrainService {
  private final byte forestType;
  private final double fuel;
  private final double elevation;

  public UniformTerrainService(int forestType, double fuel, double elevation) {
    this.forestType = (byte) forestType;
    this.fuel = fuel;
    this.elevation = elevation;
  }

  @Override
  public byte getForestType(LatLng point) {
    return this.forestType;
  }

  @Override
  public double getFuel(LatLng point) {
    return this.fuel;
  }

  @Override
  public double getElevation(LatLng point) {
    return this.elevation;
  }
}
