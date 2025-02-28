package com.example.cellfire;

import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Grid;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class GridTests {
    @Test
    void testCenterFromLatLng() {
        Coordinates coordinates = new Grid(123).fromLatLng(new LatLng(0, 0));
        Assertions.assertEquals(new Coordinates(0, 0), coordinates);
    }

    @Test
    void testLeftBottomCornerFromLatLng() {
        Coordinates coordinates = new Grid(10).fromLatLng(new LatLng(-90, -180));
        Assertions.assertEquals(new Coordinates(-1800, -900), coordinates);
    }

    @Test
    void testTopRightCornerFromLatLng() {
        Coordinates coordinates = new Grid(10).fromLatLng(new LatLng(90, 179.999));
        Assertions.assertEquals(new Coordinates(1799, 899), coordinates);
    }

    @Test
    void testOverlappedRightCoordinatesFromLatLng() {
        Coordinates coordinates = new Grid(10).fromLatLng(new LatLng(0, 180));
        Assertions.assertEquals(new Coordinates(-1800, 0), coordinates);
    }

    @Test
    void testPositiveCoordinatesFromLatLng() {
        Coordinates coordinates = new Grid(67).fromLatLng(new LatLng(45.001, 123.001));
        Assertions.assertEquals(new Coordinates(67 * 123, 67 * 45), coordinates);
    }

    @Test
    void testNegativeCoordinatesFromLatLng() {
        Coordinates coordinates = new Grid(67).fromLatLng(new LatLng(-45.001, -123.001));
        Assertions.assertEquals(new Coordinates(67 * -123 - 1, 67 * -45 - 1), coordinates);
    }

    @Test
    void testLeftBottomCornerToLatLng() {
        Grid grid = new Grid(10);
        Coordinates coordinates = new Coordinates(-1800, -900);
        LatLng latLng = grid.toLatLng(coordinates);
        Assertions.assertEquals(coordinates, grid.fromLatLng(latLng));
    }

    @Test
    void testTopRightCornerToLatLng() {
        Grid grid = new Grid(10);
        Coordinates coordinates = new Coordinates(1799, 899);
        LatLng latLng = grid.toLatLng(coordinates);
        Assertions.assertEquals(coordinates, grid.fromLatLng(latLng));
    }

    @Test
    void testPositiveCoordinatesToLatLng() {
        Grid grid = new Grid(67);
        Coordinates coordinates = new Coordinates(67 * 123, 67 * 45);
        LatLng latLng = grid.toLatLng(coordinates);
        Assertions.assertEquals(coordinates, grid.fromLatLng(latLng));
    }

    @Test
    void testNegativeCoordinatesToLatLng() {
        Grid grid = new Grid(67);
        Coordinates coordinates = new Coordinates(67 * -123, 67 * -45);
        LatLng latLng = grid.toLatLng(coordinates);
        Assertions.assertEquals(coordinates, grid.fromLatLng(latLng));
    }

    @Test
    void testPositiveCoordinatesNeighbor() {
        Assertions.assertEquals(
                new Coordinates(-1, 0),
                new Grid(10).getNeighbour(new Coordinates(0, 1), -1, -1)
        );
    }

    @Test
    void testNegativeCoordinatesNeighbor() {
        Assertions.assertEquals(
                new Coordinates(0, -54),
                new Grid(10).getNeighbour(new Coordinates(-1, -55), 1, 1)
        );
    }

    @Test
    void testRightOverlap() {
        Assertions.assertEquals(
                new Coordinates(-1800, 124),
                new Grid(10).getNeighbour(new Coordinates(1799, 123), 1, 1)
        );
    }

    @Test
    void testLeftOverlap() {
        Assertions.assertEquals(
                new Coordinates(1799, -124),
                new Grid(10).getNeighbour(new Coordinates(-1800, -123), -1, -1)
        );
    }

    @Test
    void testTopOverlap() {
        Assertions.assertEquals(
                new Coordinates(1, 899),
                new Grid(10).getNeighbour(new Coordinates(-1800, 899), 1, 1)
        );
    }

    @Test
    void testBottomOverlap() {
        Assertions.assertEquals(
                new Coordinates(1799, -900),
                new Grid(10).getNeighbour(new Coordinates(0, -900), -1, -1)
        );
    }
}
