package ru.cellularwildfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Iterator;
import java.util.Objects;

public final class Cell {
  public static final double IGNITION_TEMPERATURE = 500;
  public static final double CUMULATIVE_COMBUSTION_RATE_THRESHOLD = 5;

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
    return state.initialFuel > 0
        && state.getCumulativeCombustionRate() < CUMULATIVE_COMBUSTION_RATE_THRESHOLD
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
    private final float initialFuel;
    private float heat;
    private float cumulativeCombustionRate;

    public State(double initialFuel, double heat, double cumulativeCombustionRate) {
      this.initialFuel = (float) initialFuel;
      this.heat = (float) heat;
      this.cumulativeCombustionRate = (float) cumulativeCombustionRate;
    }

    public State(double initialFuel, double heat) {
      this(initialFuel, heat, 0);
    }

    public double getInitialFuel() {
      return initialFuel;
    }

    public double getHeat() {
      return heat;
    }

    public void setHeat(double heat) {
      this.heat = (float) heat;
    }

    @JsonIgnore
    public float getCumulativeCombustionRate() {
      return cumulativeCombustionRate;
    }

    public boolean isDamaged() {
      return cumulativeCombustionRate > 0;
    }

    public double getFuel() {
      return initialFuel * (2 - 2 / (1 + Math.exp(-cumulativeCombustionRate)));
    }

    public void accountCombustionStep(double combustionRate) {
      this.cumulativeCombustionRate += (float) combustionRate;
    }
  }

  public static final class Factors extends Weather {
    private final short elevation;
    private final byte forestType;

    public Factors(Weather weather, double elevation, byte forestType) {
      super(
          weather.getAirTemperature(),
          weather.getAirHumidity(),
          weather.getWindX(),
          weather.getWindY());
      this.elevation = compressElevation(elevation);
      this.forestType = forestType;
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
      return (airTemperature == otherFactors.airTemperature
          && airHumidity == otherFactors.airHumidity
          && windX == otherFactors.windX
          && windY == otherFactors.windY
          && elevation == otherFactors.elevation
          && forestType == otherFactors.forestType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(airTemperature, airHumidity, windX, windY, elevation, forestType);
    }

    public double getElevation() {
      return decompressElevation(elevation);
    }

    public byte getForestType() {
      return forestType;
    }
  }
}
