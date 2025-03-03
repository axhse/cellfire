package com.example.cellfire;

import com.example.cellfire.data.FullSmoothMap;
import com.example.cellfire.data.MapFragment;
import com.example.cellfire.data.MapSmoothFragment;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MapFragmentTests {
    @Test
    public void testMapFragmentBoundaries() {
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
    public void testSmoothFragmentValuesNearCenters() {
        byte[][] data = new byte[7 * 2][7 * 3];
        for (int x = 0; x < 7 * 2; x++) {
            for (int y = 0; y < 7 * 3; y++) {
                data[x][y] = x == 11 && y == 13 ? (byte) 9 : 88;
            }
        }
        MapFragment fragment = new MapSmoothFragment(data, 7, 45, 67, 2, 3);
        LatLng point;

        point = new LatLng(67 + (13 + 0.5) / 7, 45 + (11 + 0.5) / 7);
        Assertions.assertEquals(9, fragment.at(point));

        point = new LatLng(67 + (13 + 0.5 + 0.04) / 7, 45 + (11 + 0.5 + 0.04) / 7);
        Assertions.assertEquals(9, fragment.at(point));

        point = new LatLng(67 + (13 + 0.5 + 1) / 7, 45 + (11 + 0.5 + 0.04) / 7);
        Assertions.assertEquals(88, fragment.at(point));

        point = new LatLng(67 + (13 + 0.5 + 0.11) / 7, 45 + (11 + 0.5) / 7);
        byte leftValue = fragment.at(point);
        Assertions.assertTrue(9 < leftValue && leftValue < 88);

        point = new LatLng(67 + (13 + 0.5 + 0.11) / 7, 45 + (11 + 0.5) / 7);
        byte rightValue = fragment.at(point);
        Assertions.assertEquals(leftValue, rightValue);

        point = new LatLng(67 + (13 + 0.5 + 0.2) / 7, 45 + (11 + 0.5 + 0.8) / 7);
        byte diagonalValue = fragment.at(point);
        Assertions.assertTrue(leftValue < diagonalValue && diagonalValue < 88);
    }

    @Test
    public void testSmoothFragmentSkewedValues() {
        byte[][] data = new byte[3 * 2][3 * 2];
        for (int x = 0; x < 3 * 2; x++) {
            for (int y = 0; y < 3 * 2; y++) {
                data[x][y] = (byte) ((x + 1) * (y + 2) + x * 7);
            }
        }
        int x = 2;
        int y = 3;
        byte[] d = new byte[]{data[x - 1][y - 1], data[x - 1][y], data[x][y - 1], data[x][y]};
        MapFragment fragment = new MapSmoothFragment(data, 3, 0, 0, 2, 2);
        LatLng point;
        byte expectedValue;

        expectedValue = (byte) Math.round((d[0] + d[1] + d[2] + d[3]) / 4.0);
        point = new LatLng(y / 3.0 + 0.001, x / 3.0 + 0.001);
        Assertions.assertEquals(expectedValue, fragment.at(point));
        point = new LatLng(y / 3.0 - 0.001, x / 3.0 - 0.001);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((d[0] + d[1]) / 2.0);
        point = new LatLng(y / 3.0 + 0.001, (x - 0.5) / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((d[1] + d[3]) / 2.0);
        point = new LatLng((y + 0.5) / 3, x / 3.0);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((d[1] + 3 * d[3]) / 4.0);
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
    public void testSmoothFragmentBoundaryValues() {
        byte[][] data = new byte[3 * 2][3 * 2];
        for (int x = 0; x < 3 * 2; x++) {
            for (int y = 0; y < 3 * 2; y++) {
                data[x][y] = (byte) ((x + 1) * (y + 2) + x * 7);
            }
        }
        MapFragment fragment = new MapSmoothFragment(data, 3, 0, 0, 2, 2);
        LatLng point;
        byte expectedValue;

        expectedValue = data[0][0];
        point = new LatLng(0.123 / 3, 0.3 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((data[0][0] + data[1][0]) / 2.0);
        point = new LatLng(0.3 / 3, 1.01 / 3.0);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((3 * data[0][0] + data[1][0]) / 4.0);
        point = new LatLng(0.499 / 3, 0.75 / 3.0);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = (byte) Math.round((data[0][0] + 3 * data[1][0]) / 4.0);
        point = new LatLng(0.499 / 3, 1.25 / 3.0);
        Assertions.assertEquals(expectedValue, fragment.at(point));

        expectedValue = data[3 * 2 - 1][3 * 2 - 1];
        point = new LatLng(2 - 0.123 / 3, 2 - 0.3 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentLeftWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[0][3 * 123] = 71;
        data[3 * 360 - 1][3 * 123] = 36;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round((3 * 71 + 36) / 4.0);
        LatLng point = new LatLng(123 - 90 + 0.5 / 3, -180 + 0.25 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentRightWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[0][3 * 123] = 71;
        data[3 * 360 - 1][3 * 123] = 36;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round((71 + 3 * 36) / 4.0);
        LatLng point = new LatLng(123 - 90 + 0.5 / 3, 180 - 0.25 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentTopWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[3 * 234][3 * 180 - 1] = 71;
        data[3 * (234 - 180)][3 * 180 - 1] = 36;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round((3 * 71 + 36) / 4.0);
        LatLng point = new LatLng(90 - 0.25 / 3, 234 - 180 + 0.5 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentTopSkewedWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[0][3 * 180 - 1] = 71;
        data[3 * 360 - 1][3 * 180 - 1] = 36;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round(0.4 * (71 + 36) / 2);
        LatLng point = new LatLng(90 - 0.1 / 3, -0.25 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentBottomWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[3 * 123][0] = 71;
        data[3 * (123 + 180)][0] = 36;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round((6 * 71 + 4 * 36) / 10.0);
        LatLng point = new LatLng(-90 + 0.1 / 3, 123 - 180 + 0.5 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }

    @Test
    public void testSmoothFragmentTopRightWraparound() {
        byte[][] data = new byte[3 * 360][3 * 180];
        data[3 * 360 - 1][3 * 180 - 1] = 10;
        data[0][3 * 180 - 1] = 24;
        data[3 * 180 - 1][3 * 180 - 1] = 55;
        data[3 * 180][3 * 180 - 1] = 98;
        MapFragment fragment = new FullSmoothMap(data, 3);

        byte expectedValue = (byte) Math.round((10 + 24 + 55 + 97) / 4.0);
        LatLng point = new LatLng(90 - 0.001 / 3, 180 - 0.001 / 3);
        Assertions.assertEquals(expectedValue, fragment.at(point));
    }
}
