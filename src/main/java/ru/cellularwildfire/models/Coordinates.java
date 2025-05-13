package ru.cellularwildfire.models;

import java.util.Objects;

public final class Coordinates {
  private final int x;
  private final int y;

  public Coordinates(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    Coordinates otherCoordinates = (Coordinates) other;
    return x == otherCoordinates.x && y == otherCoordinates.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
