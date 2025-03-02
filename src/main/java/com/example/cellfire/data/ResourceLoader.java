package com.example.cellfire.data;

import com.example.cellfire.services.MosaicTerrainService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ResourceLoader {
    public static Mosaic loadElevationMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadMapSmoothFragment("Elevation", 120, 48, 36, 3));
        fragments.add(loadFullSmoothMap("Elevation", 30));
        return new Mosaic(fragments);
    }

    public static Mosaic loadForestTypeClusterMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadFullMap("ForestTypeCluster", 10));
        return new Mosaic(fragments);
    }

    public static Mosaic loadCanopyHeightMap() {
        List<MapFragment> fragments = new ArrayList<>();
        fragments.add(loadMapFragment("CanopyHeight", 1000, 48, 36, 3));
        return new Mosaic(fragments);
    }

    private static byte[][] loadFragmentData(String name, int scale, int x, int y, int width, int height) {
        byte[][] data = new byte[width * scale][height * scale];
        String resourceName = "%s/%s".formatted(name, buildResourceFileName(scale, x, y, width, height));
        try (InputStream inputStream = MosaicTerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found.");
            }
            byte[] bytes = inputStream.readAllBytes();
            for (int xi = 0; xi < width * scale; xi++) {
                System.arraycopy(bytes, height * scale * xi, data[xi], 0, height * scale);
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Cannot read resource bytes.");
        }
        return data;
    }

    private static MapFragment loadMapFragment(String name, int scale, int x, int y, int width, int height) {
        byte[][] data = loadFragmentData(name, scale, x, y, width, height);
        return new MapFragment(data, scale, x, y, width, height);
    }

    private static MapFragment loadMapFragment(String name, int scale, int x, int y, int size) {
        return loadMapFragment(name, scale, x, y, size, size);
    }

    private static MapFragment loadMapSmoothFragment(String name, int scale, int x, int y, int width, int height) {
        byte[][] data = loadFragmentData(name, scale, x, y, width, height);
        return new MapSmoothFragment(data, scale, x, y, width, height);
    }

    private static MapFragment loadMapSmoothFragment(String name, int scale, int x, int y, int size) {
        return loadMapSmoothFragment(name, scale, x, y, size, size);
    }

    private static MapFragment loadFullMap(String name, int scale) {
        byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180);
        return new FullMap(data, scale);
    }

    private static MapFragment loadFullSmoothMap(String name, int scale) {
        byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180);
        return new FullSmoothMap(data, scale);
    }

    private static String buildResourceFileName(int scale, int x, int y, int width, int height) {
        String resourceName = "x" + scale;
        if (width < 360) {
            resourceName += "_lon" + x + "+" + width;
        }
        if (height < 180) {
            resourceName += "_lat" + y + "+" + height;
        }
        resourceName += ".bin";
        return resourceName;
    }
}
