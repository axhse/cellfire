package ru.cellularwildfire.tuner.services;

import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.services.TerrainService;

public final class SlopedTerrainService implements TerrainService {
  private final byte forestType;
  private final double fuel;
  private final double slope;
  private final double[] slopeVector;

  public SlopedTerrainService(
      int forestType, double fuel, double slopeInDegrees, double slopeDirectionInDegrees) {
    this.forestType = (byte) forestType;
    this.fuel = fuel;
    this.slope = calculateSlope(slopeInDegrees);
    this.slopeVector = calculateSlopeVector(slopeDirectionInDegrees);
  }

  private static double calculateSlope(double slopeAngleInDegrees) {
    return Math.tan(Math.toRadians(slopeAngleInDegrees));
  }

  private static double[] calculateSlopeVector(double slopeDirectionInDegrees) {
    double angle = Math.toRadians(slopeDirectionInDegrees);
    return new double[] {Math.cos(angle), Math.sin(angle)};
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
    double distance = point.lat * slopeVector[0] + point.lng * slopeVector[1];
    return distance * slope;
  }
}
