package ru.cellularwildfire;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.data.FullSmoothMap;
import ru.cellularwildfire.data.MapFragment;
import ru.cellularwildfire.data.SmoothMapFragment;
import ru.cellularwildfire.models.LatLng;

public final class MapFragmentTests {
  @Test
  public void testBoundaries() {
    byte[][] data = new byte[7 * 2][7 * 3];
    for (int x = 0; x < 7 * 2; x++) {
      for (int y = 0; y < 7 * 3; y++) {
        data[x][y] = x == 11 && y == 13 ? (byte) 111 : 123;
      }
    }
    MapFragment fragment = new MapFragment(data, 7, 45, 67, 2, 3);
    LatLng point;

    for (int x = 0; x < 7 * 2; x++) {
      for (int y = 0; y < 7 * 3; y++) {
        point = new LatLng(67 + (y + 0.5) / 7, 45 + (x + 0.5) / 7);
        Assertions.assertTrue(fragment.has(point));
        Assertions.assertEquals(x == 11 && y == 13 ? 111 : 123, fragment.at(point));
      }
    }

    point = new LatLng(67 + 0.5 / 7, 45 - 0.5 / 7);
    Assertions.assertFalse(fragment.has(point));

    point = new LatLng(67 - 0.5 / 7, 45 + 0.5 / 7);
    Assertions.assertFalse(fragment.has(point));

    point = new LatLng(67 + 3 + 0.5 / 7, 45 + 2 - 0.5 / 7);
    Assertions.assertFalse(fragment.has(point));

    point = new LatLng(67 + 3 - 0.5 / 7, 45 + 2 + 0.5 / 7);
    Assertions.assertFalse(fragment.has(point));
  }

  @Test
  public void testValues() {
    byte[][] data = new byte[256][1];
    for (int x = 0; x < 256; x++) {
      data[x][0] = (byte) x;
    }
    MapFragment fragment = new MapFragment(data, 1, -180, 0, 256, 1);

    for (int x = 0; x < 256; x++) {
      LatLng point = new LatLng(0, -179.5 + x);
      Assertions.assertEquals(x, fragment.at(point));
    }
  }

  @Test
  public void testSmoothBoundaries() {
    byte[][] data = new byte[7 * 2][7 * 3];
    data[0][0] = 10;
    data[0][1] = 40;
    data[1][0] = 100;
    MapFragment fragment = new SmoothMapFragment(data, 7, 0, 0, 2, 3);
    LatLng point;

    point = new LatLng(0.4 / 7, -0.1 / 7);
    Assertions.assertFalse(fragment.has(point));

    point = new LatLng(0.4 / 7, 0.1 / 7);
    Assertions.assertTrue(fragment.has(point));
    Assertions.assertEquals(10, fragment.at(point));

    point = new LatLng(1 / 7.0, 0.1 / 7);
    Assertions.assertTrue(fragment.has(point));
    Assertions.assertEquals((10 + 40) / 2, fragment.at(point));

    point = new LatLng(0.1 / 7, 1 / 7.0);
    Assertions.assertTrue(fragment.has(point));
    Assertions.assertEquals((10 + 100) / 2, fragment.at(point));
  }

  @Test
  public void testSmoothValuesNearCenters() {
    byte[][] data = new byte[7 * 2][7 * 3];
    for (int x = 0; x < 7 * 2; x++) {
      for (int y = 0; y < 7 * 3; y++) {
        data[x][y] = x == 11 && y == 13 ? (byte) 9 : 88;
      }
    }
    MapFragment fragment = new SmoothMapFragment(data, 7, 45, 67, 2, 3);
    LatLng point;

    point = new LatLng(67 + (13 + 0.5) / 7, 45 + (11 + 0.5) / 7);
    Assertions.assertEquals(9, fragment.at(point));

    point = new LatLng(67 + (13 + 0.5 + 0.04) / 7, 45 + (11 + 0.5 + 0.04) / 7);
    Assertions.assertEquals(9, fragment.at(point));

    point = new LatLng(67 + (13 + 0.5 + 1) / 7, 45 + (11 + 0.5 + 0.04) / 7);
    Assertions.assertEquals(88, fragment.at(point));

    point = new LatLng(67 + (13 + 0.5 + 0.11) / 7, 45 + (11 + 0.5) / 7);
    int leftValue = fragment.at(point);
    Assertions.assertTrue(9 < leftValue);

    point = new LatLng(67 + (13 + 0.5 + 0.2) / 7, 45 + (11 + 0.5 + 0.8) / 7);
    int diagonalValue = fragment.at(point);
    Assertions.assertTrue(leftValue < diagonalValue);
    Assertions.assertTrue(diagonalValue < 88);
  }

  @Test
  public void testSmoothSkewedValues() {
    byte[][] data = new byte[3 * 2][3 * 2];
    for (int x = 0; x < 3 * 2; x++) {
      for (int y = 0; y < 3 * 2; y++) {
        data[x][y] = (byte) ((x + 1) * (y + 2) + x * 7);
      }
    }
    int x = 2;
    int y = 3;
    byte[] d = new byte[] {data[x - 1][y - 1], data[x - 1][y], data[x][y - 1], data[x][y]};
    MapFragment fragment = new SmoothMapFragment(data, 3, 0, 0, 2, 2);
    LatLng point;
    int expectedValue;

    expectedValue = (int) Math.round((d[0] + d[1] + d[2] + d[3]) / 4.0);
    point = new LatLng(y / 3.0 + 0.001, x / 3.0 + 0.001);
    Assertions.assertEquals(expectedValue, fragment.at(point));
    point = new LatLng(y / 3.0 - 0.001, x / 3.0 - 0.001);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((d[0] + d[1]) / 2.0);
    point = new LatLng(y / 3.0 + 0.001, (x - 0.5) / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((d[1] + d[3]) / 2.0);
    point = new LatLng((y + 0.5) / 3, x / 3.0);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((d[1] + 3 * d[3]) / 4.0);
    point = new LatLng((y + 0.5 - 0.04) / 3, (x + 0.25) / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    point = new LatLng(y / 3.0, (x + 0.2) / 3);
    Assertions.assertEquals(23, fragment.at(point));

    point = new LatLng((y + 0.25) / 3, (x - 1.0 / 3) / 3);
    Assertions.assertEquals(20, fragment.at(point));

    point = new LatLng((y + 0.3) / 3, (x + 0.4) / 3);
    Assertions.assertEquals(25, fragment.at(point));
  }

  @Test
  public void testSmoothBoundaryValues() {
    byte[][] data = new byte[3 * 2][3 * 2];
    for (int x = 0; x < 3 * 2; x++) {
      for (int y = 0; y < 3 * 2; y++) {
        data[x][y] = (byte) ((x + 1) * (y + 2) + x * 7);
      }
    }
    MapFragment fragment = new SmoothMapFragment(data, 3, 0, 0, 2, 2);
    LatLng point;
    int expectedValue;

    expectedValue = data[0][0];
    point = new LatLng(0.123 / 3, 0.3 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((data[0][0] + data[1][0]) / 2.0);
    point = new LatLng(0.3 / 3, 1.01 / 3.0);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((3 * data[0][0] + data[1][0]) / 4.0);
    point = new LatLng(0.499 / 3, 0.75 / 3.0);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = (int) Math.round((data[0][0] + 3 * data[1][0]) / 4.0);
    point = new LatLng(0.499 / 3, 1.25 / 3.0);
    Assertions.assertEquals(expectedValue, fragment.at(point));

    expectedValue = data[3 * 2 - 1][3 * 2 - 1];
    point = new LatLng(2 - 0.123 / 3, 2 - 0.3 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothLeftWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[0][3 * 123] = 71;
    data[3 * 360 - 1][3 * 123] = 36;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round((3 * 71 + 36) / 4.0);
    LatLng point = new LatLng(123 - 90 + 0.5 / 3, -180 + 0.25 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothRightWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[0][3 * 123] = 71;
    data[3 * 360 - 1][3 * 123] = 36;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round((71 + 3 * 36) / 4.0);
    LatLng point = new LatLng(123 - 90 + 0.5 / 3, 180 - 0.25 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothTopWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[3 * 234][3 * 180 - 1] = 71;
    data[3 * (234 - 180)][3 * 180 - 1] = 36;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round((3 * 71 + 36) / 4.0);
    LatLng point = new LatLng(90 - 0.25 / 3, 234 - 180 + 0.5 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothTopSkewedWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[0][3 * 180 - 1] = 71;
    data[3 * 360 - 1][3 * 180 - 1] = 36;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round(0.4 * (71 + 36) / 2);
    LatLng point = new LatLng(90 - 0.1 / 3, -0.25 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothBottomWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[3 * 123][0] = 71;
    data[3 * (123 + 180)][0] = 36;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round((6 * 71 + 4 * 36) / 10.0);
    LatLng point = new LatLng(-90 + 0.1 / 3, 123 - 180 + 0.5 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }

  @Test
  public void testSmoothTopRightWraparound() {
    byte[][] data = new byte[3 * 360][3 * 180];
    data[3 * 360 - 1][3 * 180 - 1] = 10;
    data[0][3 * 180 - 1] = 24;
    data[3 * 180 - 1][3 * 180 - 1] = 55;
    data[3 * 180][3 * 180 - 1] = 98;
    MapFragment fragment = new FullSmoothMap(data, 3);

    int expectedValue = (int) Math.round((10 + 24 + 55 + 97) / 4.0);
    LatLng point = new LatLng(90 - 0.001 / 3, 180 - 0.001 / 3);
    Assertions.assertEquals(expectedValue, fragment.at(point));
  }
}
