package ru.cellularwildfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Grid {
  /** Earth equatorial circumference: 40 075 km. Earth polar circumference: 39 930 km. */
  private static final double EARTH_CIRCUMFERENCE = 40_000_000;

  /**
   * Cell size of 1/{@code scale}° for both latitude and longitude corresponds with height
   * ≈110/{@code scale} km and width ≈110/{@code scale} km near the equator.
   */
  private final int scale;

  @JsonIgnore private final double cellHeight;

  public Grid(int scale) {
    this.scale = scale;
    cellHeight = EARTH_CIRCUMFERENCE / 360 / scale;
  }

  public int getScale() {
    return scale;
  }

  @JsonIgnore
  public double getCellHeight() {
    return cellHeight;
  }

  public Coordinates coordinatesOf(LatLng point) {
    long x = Math.round(point.lng * scale - 0.5);
    long y = Math.round(point.lat * scale - 0.5);
    if (x == 180L * scale || x + 1 == -180L * scale) {
      x = -180L * scale;
    }
    if (y == 90L * scale) {
      y = 90L * scale - 1;
    }
    if (y + 1 == -90L * scale) {
      y = -90L * scale;
    }
    return new Coordinates((int) x, (int) y);
  }

  public LatLng pointOf(Coordinates coordinates) {
    return new LatLng((coordinates.getY() + 0.5) / scale, (coordinates.getX() + 0.5) / scale);
  }

  public Coordinates getNeighbor(Coordinates coordinates, int offsetX, int offsetY) {
    assert -1 <= offsetX && offsetX <= 1 && -1 <= offsetY && offsetY <= 1;
    int y = coordinates.getY() + offsetY;
    if (y < -90 * scale || 90 * scale <= y) {
      y = (y < 0 ? -1 : 1) * 180 * scale - y - 1;
      offsetX = 180 * scale - offsetX;
    }
    int x = coordinates.getX() + offsetX;
    if (x < -180 * scale) {
      x += 360 * scale;
    }
    if (180 * scale <= x) {
      x -= 360 * scale;
    }
    return new Coordinates(x, y);
  }

  public double estimateCellArea(Coordinates cellCoordinates) {
    double lat = pointOf(cellCoordinates).lat;
    return cellHeight * cellHeight * Math.cos(Math.toRadians(lat));
  }
}
