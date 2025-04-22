package ru.cellularwildfire.models;

import java.util.Objects;

public class Weather {
  protected final byte airTemperature;
  protected final byte airHumidity;
  protected final byte windX;
  protected final byte windY;

  public Weather(double airTemperature, double airHumidity, double windX, double windY) {
    this.airTemperature = compressTemperature(airTemperature);
    this.airHumidity = compressRelativeHumidity(airHumidity);
    this.windX = compressWindSpeed(windX);
    this.windY = compressWindSpeed(windY);
  }

  private static byte compressTemperature(double temperature) {
    return compactToByte(Math.round(temperature));
  }

  private static byte compressRelativeHumidity(double humidity) {
    return (byte) Math.round(humidity * 100);
  }

  private static byte compressWindSpeed(double speed) {
    return compactToByte(Math.round(speed * 10));
  }

  private static double decompressTemperature(byte temperature) {
    return temperature;
  }

  private static double decompressRelativeHumidity(byte humidity) {
    return humidity / 100.0;
  }

  private static double decompressWindSpeed(byte speed) {
    return speed / 10.0;
  }

  private static byte compactToByte(long value) {
    return (byte) Math.min(Math.max(Byte.MIN_VALUE, value), Byte.MAX_VALUE);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    Weather otherWeather = (Weather) other;
    return (airTemperature == otherWeather.airTemperature
        && airHumidity == otherWeather.airHumidity
        && windX == otherWeather.windX
        && windY == otherWeather.windY);
  }

  @Override
  public int hashCode() {
    return Objects.hash(airTemperature, airHumidity, windX, windY);
  }

  public double getAirTemperature() {
    return decompressTemperature(airTemperature);
  }

  public double getAirHumidity() {
    return decompressRelativeHumidity(airHumidity);
  }

  public double getWindX() {
    return decompressWindSpeed(windX);
  }

  public double getWindY() {
    return decompressWindSpeed(windY);
  }

  public double getWindSpeed() {
    return Math.sqrt(getWindX() * getWindX() + getWindY() * getWindY());
  }
}
