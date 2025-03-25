package ru.cellularwildfire.data;

import ru.cellularwildfire.models.LatLng;

public class MapFragment {
  protected final byte[][] data;
  protected final int scale;
  protected final int x;
  protected final int y;
  protected final int width;
  protected final int height;

  public MapFragment(byte[][] data, int scale, int x, int y, int width, int height) {
    this.data = data;
    this.scale = scale;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public boolean has(LatLng point) {
    int valueX = (int) Math.round((point.lng - x) * scale - 0.5);
    int valueY = (int) Math.round((point.lat - y) * scale - 0.5);
    return 0 <= valueX && valueX < width * scale && 0 <= valueY && valueY < height * scale;
  }

  public int at(LatLng point) {
    int valueX = (int) Math.round((point.lng - x) * scale - 0.5);
    int valueY = (int) Math.round((point.lat - y) * scale - 0.5);
    return data[valueX][valueY] & 0xFF;
  }
}
