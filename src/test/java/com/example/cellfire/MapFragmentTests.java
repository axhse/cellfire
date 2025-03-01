package com.example.cellfire;

import com.example.cellfire.data.MapFragment;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MapFragmentTests {
    @Test
    void testMapFragmentBorders() {
        byte[][] data = new byte[7 * 2][7 * 3];
        for (int x = 0; x < 7 * 2; x++) {
            for (int y = 0; y < 7 * 3; y++) {
                data[x][y] = 123;
            }
        }
        MapFragment fragment = new MapFragment(data, 7, 45, 67, 2, 3);

        for (double lat = 67 + 0.5 / 7; lat < 67 + 3; lat += 1.0 / 7) {
            for (double lng = 45 + 0.5 / 7; lng < 45 + 2; lng += 1.0 / 7) {
                LatLng point = new LatLng(lat, lng);
                Assertions.assertTrue(fragment.has(point));
                Assertions.assertEquals(123, fragment.at(point));
            }
        }

        LatLng point = new LatLng(67 + 0.5 / 7, 45 - 0.5 / 7);
        Assertions.assertFalse(fragment.has(point));

        point = new LatLng(67 - 0.5 / 7, 45 + 0.5 / 7);
        Assertions.assertFalse(fragment.has(point));

        point = new LatLng(67 + 3 + 0.5 / 7, 45 + 2 - 0.5 / 7);
        Assertions.assertFalse(fragment.has(point));

        point = new LatLng(67 + 3 - 0.5 / 7, 45 + 2 + 0.5 / 7);
        Assertions.assertFalse(fragment.has(point));
    }
}
