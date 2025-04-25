package ru.cellularwildfire.data;

import java.io.IOException;
import java.io.InputStream;
import ru.cellularwildfire.services.MosaicTerrainService;

public final class MapLoader {
  public static MapFragment loadMapFragment(
      String name, int scale, int x, int y, int width, int height) {
    byte[][] data = loadFragmentData(name, scale, x, y, width, height);
    return new MapFragment(data, scale, x, y, width, height);
  }

  public static MapFragment loadMapSmoothFragment(
      String name, int scale, int x, int y, int width, int height) {
    byte[][] data = loadFragmentData(name, scale, x, y, width, height);
    return new SmoothMapFragment(data, scale, x, y, width, height);
  }

  public static MapFragment loadFullMap(String name, int scale) {
    byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180);
    return new FullMap(data, scale);
  }

  public static MapFragment loadFullSmoothMap(String name, int scale) {
    byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180);
    return new FullSmoothMap(data, scale);
  }

  private static byte[][] loadFragmentData(
      String name, int scale, int x, int y, int width, int height) {
    byte[][] data = new byte[width * scale][height * scale];
    String resourceName =
        "maps/%s/%s".formatted(name, buildResourceFileName(scale, x, y, width, height));
    try (InputStream inputStream =
        MosaicTerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
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
