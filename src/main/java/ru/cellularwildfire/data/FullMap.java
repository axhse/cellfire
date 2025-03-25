package ru.cellularwildfire.data;

public final class FullMap extends MapFragment {
  public FullMap(byte[][] data, int scale) {
    super(data, scale, -180, -90, 360, 180);
  }
}
