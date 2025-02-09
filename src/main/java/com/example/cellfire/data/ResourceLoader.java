package com.example.cellfire.data;

import com.example.cellfire.services.TerrainService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ResourceLoader {

    public static byte[][] loadFragmentData(String name, int scale, int x, int y, int width, int height) {
        byte[][] data = new byte[width * scale][height * scale];
        String resourceName = "%s/%s%d%s%dWidth%dHeight%dScale%d.bin".formatted(name, x < 0 ? "West" : "East", Math.abs(x), y < 0 ? "South" : "North", Math.abs(y), width, height, scale);
        try (InputStream inputStream = TerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found.");
            }
            byte[] bytes = inputStream.readAllBytes();

            for (int xi = 0; xi < width * scale; xi++) {
                System.arraycopy(bytes, height * scale * xi, data[xi], 0, height * scale);
            }
        }
        catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read resource bytes.");
        }
        return data;
    }

    public static MapFragment loadFragment(String name, int scale, int x, int y, int width, int height) {
        byte[][] data = loadFragmentData(name, scale, x, y, width, height);
        return new MapFragment(data, scale, x, y, width, height);
    }

    public static MapFragment loadFragment(String name, int scale, int x, int y, int size) {
        return loadFragment(name, scale, x, y, size, size);
    }

    public static MapFullFragment loadFullFragment(String name, int scale) {
        byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180);
        return new MapFullFragment(data, scale);
    }

    public static TerrainMap loadElevationMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadFullFragment("Elevation", 30));
        return new TerrainMap(fragments);
    }

    public static TerrainMap loadForestTypeClusterMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadFullFragment("ForestTypeCluster", 10));
        return new TerrainMap(fragments);
    }

    public static TerrainMap loadCanopyHeightMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadFragment("CanopyHeight", 1000, 48, 36, 3));
        return new TerrainMap(fragments);
    }
}
