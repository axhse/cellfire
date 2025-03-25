package ru.cellularwildfire;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.models.Weather;

public class CellTests {
  @Test
  public void testAirTemperatureCompression() {
    Weather weather;

    weather = new Weather(1000, 0, 0, 0);
    Assertions.assertEquals(Byte.MAX_VALUE, weather.getAirTemperature());

    weather = new Weather(-1000, 0, 0, 0);
    Assertions.assertEquals(Byte.MIN_VALUE, weather.getAirTemperature());

    weather = new Weather(0.6, 0, 0, 0);
    Assertions.assertEquals(1, weather.getAirTemperature());

    weather = new Weather(0.4, 0, 0, 0);
    Assertions.assertEquals(0, weather.getAirTemperature());

    weather = new Weather(-1.7, 0, 0, 0);
    Assertions.assertEquals(-2, weather.getAirTemperature());
  }

  @Test
  public void testAirHumidityCompression() {
    Weather weather;

    weather = new Weather(0, 0, 0, 0);
    Assertions.assertEquals(0, weather.getAirHumidity());

    weather = new Weather(0, 1, 0, 0);
    Assertions.assertEquals(100 / 100.0, weather.getAirHumidity());

    weather = new Weather(0, 0.234, 0, 0);
    Assertions.assertEquals(23 / 100.0, weather.getAirHumidity());

    weather = new Weather(0, 0.236, 0, 0);
    Assertions.assertEquals(24 / 100.0, weather.getAirHumidity());
  }

  @Test
  public void testWindCompression() {
    Weather weather;

    weather = new Weather(0, 0, 1000, 1000);
    Assertions.assertEquals(Byte.MAX_VALUE / 10.0, weather.getWindX());
    Assertions.assertEquals(Byte.MAX_VALUE / 10.0, weather.getWindY());

    weather = new Weather(0, 0, -1000, -1000);
    Assertions.assertEquals(Byte.MIN_VALUE / 10.0, weather.getWindX());
    Assertions.assertEquals(Byte.MIN_VALUE / 10.0, weather.getWindY());

    weather = new Weather(0, 0, -12.34, 4.56);
    Assertions.assertEquals(-123 / 10.0, weather.getWindX());
    Assertions.assertEquals(46 / 10.0, weather.getWindY());
  }

  @Test
  public void testFactorComparison() {
    Weather weather1 = new Weather(55, 0.123, -0.04, 10);
    Weather weather2 = new Weather(55.4, 0.116, 0.04, 10);
    Weather weather3 = new Weather(55.6, 0.116, 0.04, 10);

    Assertions.assertEquals(weather1, weather2);
    Assertions.assertNotEquals(weather1, weather3);
  }
}
