package ru.cellularwildfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.Objects;

public final class Cell {
  private static final double IGNITION_TEMPERATURE = 500;

  @JsonIgnore private final Cell[] vicinity = new Cell[9];
  private final Coordinates coordinates;
  private final State state;
  private final Factors factors;

  public Cell(Coordinates coordinates, State state, Factors factors) {
    this.coordinates = coordinates;
    this.state = state;
    this.factors = factors;
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  public State getState() {
    return state;
  }

  public Factors getFactors() {
    return factors;
  }

  public boolean isBurning() {
    return state.getFuel() > 0
        && IGNITION_TEMPERATURE <= state.getHeat()
        && factors.getAirHumidity() < 1
        && factors.getAirTemperature() > 0;
  }

  @JsonIgnore
  public Cell getTwin() {
    return vicinity[4];
  }

  public void setTwin(Cell cell) {
    vicinity[4] = cell;
  }

  @JsonIgnore
  public Cell getNeighbor(int offsetX, int offsetY) {
    return vicinity[3 * (offsetX + 1) + offsetY + 1];
  }

  public void setNeighbor(int offsetX, int offsetY, Cell cell) {
    vicinity[3 * (offsetX + 1) + offsetY + 1] = cell;
  }

  public Iterable<Cell> iterateNeighbors() {
    return () ->
        new Iterator<>() {
          private int index = 0;

          @Override
          public boolean hasNext() {
            while (index < 9 && (index == 4 || vicinity[index] == null)) {
              index++;
            }
            return index < 9;
          }

          @Override
          public Cell next() {
            return vicinity[index++];
          }
        };
  }

  public static final class State {
    private final boolean isDamaged;
    private float fuel;
    private float heat;

    public State(double heat, double fuel, boolean isDamaged) {
      this.fuel = (float) fuel;
      this.heat = (float) heat;
      this.isDamaged = isDamaged;
    }

    public boolean isDamaged() {
      return isDamaged;
    }

    public double getFuel() {
      return fuel;
    }

    public void setFuel(double fuel) {
      this.fuel = (float) fuel;
    }

    public double getHeat() {
      return heat;
    }

    public void setHeat(double heat) {
      this.heat = (float) heat;
    }
  }

  public static final class Factors extends Weather {
    private final short elevation;

    public Factors(double elevation, Weather weather) {
      super(
          weather.getAirTemperature(),
          weather.getAirHumidity(),
          weather.getWindX(),
          weather.getWindY());
      this.elevation = compressElevation(elevation);
    }

    private static short compressElevation(double elevation) {
      elevation = Math.min(Math.max(0, elevation), 6400);
      return (short) Math.round(elevation / 6400 * Short.MAX_VALUE);
    }

    private static double decompressElevation(short elevation) {
      return elevation * 6400.0 / Short.MAX_VALUE;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) return true;
      if (other == null || getClass() != other.getClass()) return false;
      Factors otherFactors = (Factors) other;
      return (elevation == otherFactors.elevation
          && airTemperature == otherFactors.airTemperature
          && airHumidity == otherFactors.airHumidity
          && windX == otherFactors.windX
          && windY == otherFactors.windY);
    }

    @Override
    public int hashCode() {
      return Objects.hash(elevation, airTemperature, airHumidity, windX, windY);
    }

    public double getElevation() {
      return decompressElevation(elevation);
    }
  }
}
