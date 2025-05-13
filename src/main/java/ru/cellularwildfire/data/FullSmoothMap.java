package ru.cellularwildfire.data;

public final class FullSmoothMap extends SmoothMapFragment {
  public FullSmoothMap(byte[][] data, int scale) {
    super(data, scale, -180, -90, 360, 180);
  }
}
