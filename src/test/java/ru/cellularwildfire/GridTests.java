package ru.cellularwildfire;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Grid;
import ru.cellularwildfire.models.LatLng;

public final class GridTests {
  @Test
  public void testCenterFromLatLng() {
    Coordinates coordinates = new Grid(123).coordinatesOf(new LatLng(0, 0));
    Assertions.assertEquals(new Coordinates(0, 0), coordinates);
  }

  @Test
  public void testLeftBottomCornerFromLatLng() {
    Coordinates coordinates = new Grid(10).coordinatesOf(new LatLng(-90, -180));
    Assertions.assertEquals(new Coordinates(-1800, -900), coordinates);
  }

  @Test
  public void testTopRightCornerFromLatLng() {
    Coordinates coordinates = new Grid(10).coordinatesOf(new LatLng(90, 179.999));
    Assertions.assertEquals(new Coordinates(1799, 899), coordinates);
  }

  @Test
  public void testRightWraparoundCoordinatesFromLatLng() {
    Coordinates coordinates = new Grid(10).coordinatesOf(new LatLng(0, 180));
    Assertions.assertEquals(new Coordinates(-1800, 0), coordinates);
  }

  @Test
  public void testPositiveCoordinatesFromLatLng() {
    Coordinates coordinates = new Grid(67).coordinatesOf(new LatLng(45.001, 123.001));
    Assertions.assertEquals(new Coordinates(67 * 123, 67 * 45), coordinates);
  }

  @Test
  public void testNegativeCoordinatesFromLatLng() {
    Coordinates coordinates = new Grid(67).coordinatesOf(new LatLng(-45.001, -123.001));
    Assertions.assertEquals(new Coordinates(67 * -123 - 1, 67 * -45 - 1), coordinates);
  }

  @Test
  public void testLeftBottomCornerToLatLng() {
    Grid grid = new Grid(10);
    Coordinates coordinates = new Coordinates(-1800, -900);
    LatLng latLng = grid.pointOf(coordinates);
    Assertions.assertEquals(coordinates, grid.coordinatesOf(latLng));
  }

  @Test
  public void testTopRightCornerToLatLng() {
    Grid grid = new Grid(10);
    Coordinates coordinates = new Coordinates(1799, 899);
    LatLng latLng = grid.pointOf(coordinates);
    Assertions.assertEquals(coordinates, grid.coordinatesOf(latLng));
  }

  @Test
  public void testPositiveCoordinatesToLatLng() {
    Grid grid = new Grid(67);
    Coordinates coordinates = new Coordinates(67 * 123, 67 * 45);
    LatLng latLng = grid.pointOf(coordinates);
    Assertions.assertEquals(coordinates, grid.coordinatesOf(latLng));
  }

  @Test
  public void testNegativeCoordinatesToLatLng() {
    Grid grid = new Grid(67);
    Coordinates coordinates = new Coordinates(67 * -123, 67 * -45);
    LatLng latLng = grid.pointOf(coordinates);
    Assertions.assertEquals(coordinates, grid.coordinatesOf(latLng));
  }

  @Test
  public void testPositiveCoordinatesNeighbor() {
    Assertions.assertEquals(
        new Coordinates(-1, 0), new Grid(10).getNeighbor(new Coordinates(0, 1), -1, -1));
  }

  @Test
  public void testNegativeCoordinatesNeighbor() {
    Assertions.assertEquals(
        new Coordinates(0, -54), new Grid(10).getNeighbor(new Coordinates(-1, -55), 1, 1));
  }

  @Test
  public void testLeftWraparound() {
    Assertions.assertEquals(
        new Coordinates(1799, -124),
        new Grid(10).getNeighbor(new Coordinates(-1800, -123), -1, -1));
  }

  @Test
  public void testRightWraparound() {
    Assertions.assertEquals(
        new Coordinates(-1800, 124), new Grid(10).getNeighbor(new Coordinates(1799, 123), 1, 1));
  }

  @Test
  public void testTopWraparound() {
    Assertions.assertEquals(
        new Coordinates(-1, 899), new Grid(10).getNeighbor(new Coordinates(-1800, 899), 1, 1));
  }

  @Test
  public void testBottomWraparound() {
    Assertions.assertEquals(
        new Coordinates(-1799, -900), new Grid(10).getNeighbor(new Coordinates(0, -900), -1, -1));
  }

  @Test
  public void testTopRightWraparound() {
    Assertions.assertEquals(
        new Coordinates(-2, 899), new Grid(10).getNeighbor(new Coordinates(1799, 899), 1, 1));
  }

  @Test
  public void testBottomLeftWraparound() {
    Assertions.assertEquals(
        new Coordinates(1, -900), new Grid(10).getNeighbor(new Coordinates(-1800, -900), -1, -1));
  }

  @Test
  public void testTopRightSymmetry() {
    Coordinates coordinates = new Coordinates(1799, 899);
    Coordinates neighbor = new Grid(10).getNeighbor(coordinates, 1, 1);
    Coordinates twin = new Grid(10).getNeighbor(neighbor, -1, 1);
    Assertions.assertEquals(coordinates, twin);
  }

  @Test
  public void testBottomLeftSymmetry() {
    Coordinates coordinates = new Coordinates(-1800, -900);
    Coordinates neighbor = new Grid(10).getNeighbor(coordinates, -1, -1);
    Coordinates twin = new Grid(10).getNeighbor(neighbor, 1, -1);
    Assertions.assertEquals(coordinates, twin);
  }
}
