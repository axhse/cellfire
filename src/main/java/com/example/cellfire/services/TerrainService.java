package com.example.cellfire.services;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.CellCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class TerrainService {
    private final byte[][] canopyHeight = loadData();
    private final int SECTOR_LNG = 48;
    private final int SECTOR_LAT = 36;

    public float getFuel(CellCoordinates coordinates) {
        if (coordinates.getX() < SECTOR_LNG * Domain.Settings.GRID_SCALE_FACTOR
                || (SECTOR_LNG + 3) * Domain.Settings.GRID_SCALE_FACTOR <= coordinates.getX()) {
            return 0;
        }
        if (coordinates.getY() < SECTOR_LAT * Domain.Settings.GRID_SCALE_FACTOR
                || (SECTOR_LAT + 3) * Domain.Settings.GRID_SCALE_FACTOR <= coordinates.getY()) {
            return 0;
        }
        int x = coordinates.getX() - SECTOR_LNG * Domain.Settings.GRID_SCALE_FACTOR;
        int y = coordinates.getY() - SECTOR_LAT * Domain.Settings.GRID_SCALE_FACTOR;

        float fuel = calculateFuel(canopyHeight[x / 2][y / 2]);
        return fuel < Domain.Settings.SIGNIFICANT_FUEL ? 0 : fuel;
    }

    public float getIgnitionTemperature(CellCoordinates coordinates) {
        return 200;
    }

    private float calculateFuel(float canopyHeight) {
        return (1 * canopyHeight * canopyHeight + 5 * canopyHeight) / 500;
    }

    private byte[][] loadData() {
        byte[][] loadedData = new byte[300][300];
        String resourceName = "canopy_height_map/N%dE0%d.bin".formatted(SECTOR_LAT, SECTOR_LNG);
        try (InputStream inputStream = TerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return loadedData;
            }
            byte[] bytes = inputStream.readAllBytes();
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    // FIXME: Why [y][300 - 1 - x], not [x][y]??
                    loadedData[y][300 - 1 - x] = bytes[300 * x + y];
                }
            }
        }
        catch (Exception exception) {
            return loadedData;
        }
        return loadedData;
    }

    private static class Map {
        private final List<MapFragment> fragments;

        public Map(List<MapFragment> fragments) {
            this.fragments = fragments;
        }

        public byte getValueFor(CellCoordinates coordinates) {
            for (MapFragment fragment : fragments) {
                if (fragment.hasValueFor(coordinates)) {
                    return fragment.getValueFor(coordinates);
                }
            }
            throw new IllegalArgumentException("Map has value for (%d, %d).".formatted(coordinates.getX(), coordinates.getY()));
        }
    }

    private static class MapFragment {
        private final byte[][] data;
        private final int scale;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        public MapFragment(byte[][] data, int scale, int x, int y, int width, int height) {
            this.data = data;
            this.scale = scale;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean hasValueFor(CellCoordinates coordinates) {
            int cellX = coordinates.getX();
            int cellY = coordinates.getY();
            int scaleFactor = Domain.Settings.GRID_SCALE_FACTOR;
            return x * scaleFactor <= cellX && cellX < (x + width) * scaleFactor
                    && y * scaleFactor <= cellY && cellY < (y + height) * scaleFactor;
        }

        public byte getValueFor(CellCoordinates coordinates) {
            int cellX = coordinates.getX();
            int cellY = coordinates.getY();
            int scaleFactor = Domain.Settings.GRID_SCALE_FACTOR;
            int valueX = (cellX - x * scaleFactor) * scale / scaleFactor;
            int valueY = (cellY - y * scaleFactor) * scale / scaleFactor;
            return data[valueX][valueY];
        }

        public static MapFragment load(String name, int scale, int x, int y, int width, int height) {
            byte[][] data = new byte[width * scale][width * scale];
            String resourceName = "%s/%s%d_%s%d_%d_%d_%d.bin".formatted(name, x < 0 ? "W" : "E", Math.abs(x), y < 0 ? "S" : "N", Math.abs(y), scale, width, height);
            try (InputStream inputStream = TerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
                if (inputStream == null) {
                    throw new IllegalArgumentException("Resource not found.");
                }
                byte[] bytes = inputStream.readAllBytes();
                for (int xi = 0; xi < width * scale; xi++) {
                    System.arraycopy(bytes, 300 * xi, data[xi], 0, height * scale);
                }
            }
            catch (IOException exception) {
                throw new IllegalArgumentException("Cannot read resource bytes.");
            }
            return new MapFragment(data, scale, x, y, width, height);
        }

        public static MapFragment load(String name, int scale, int x, int y, int size) {
            return load(name, scale, x, y, size, size);
        }
    }

    private static final class MapWholeFragment extends MapFragment {
        public MapWholeFragment(byte[][] data, int scale) {
            super(data, scale, -180, -90, 360, 180);
        }

        @Override
        public boolean hasValueFor(CellCoordinates coordinates) {
            return true;
        }
    }
}
