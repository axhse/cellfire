package ru.cellularwildfire.tuner.services;

import ru.cellularwildfire.data.ForestTypeConditions;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.services.TerrainService;

public final class UniformTerrainService implements TerrainService {
  private final int forestType;
  private final double fuel;
  private final double elevation;

  public UniformTerrainService(int forestType, double fuel, double elevation) {
    this.forestType = forestType;
    this.fuel = fuel;
    this.elevation = elevation;
  }

  @Override
  public double getActivationEnergy(LatLng point) {
    return ForestTypeConditions.determineActivationEnergy(this.forestType);
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
