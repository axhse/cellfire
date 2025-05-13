package ru.cellularwildfire.data;

import com.github.luben.zstd.ZstdInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import ru.cellularwildfire.services.MosaicTerrainService;

public final class MapLoader {
  public static MapFragment loadMapFragment(
      String name, int scale, int x, int y, int width, int height) {
    return loadMapFragment(name, scale, x, y, width, height, true);
  }

  public static MapFragment loadMapFragment(
      String name, int scale, int x, int y, int width, int height, boolean compressed) {
    byte[][] data = loadFragmentData(name, scale, x, y, width, height, compressed);
    return new MapFragment(data, scale, x, y, width, height);
  }

  public static MapFragment loadMapSmoothFragment(
      String name, int scale, int x, int y, int width, int height) {
    return loadMapSmoothFragment(name, scale, x, y, width, height, true);
  }

  public static MapFragment loadMapSmoothFragment(
      String name, int scale, int x, int y, int width, int height, boolean compressed) {
    byte[][] data = loadFragmentData(name, scale, x, y, width, height, compressed);
    return new SmoothMapFragment(data, scale, x, y, width, height);
  }

  public static MapFragment loadFullMap(String name, int scale) {
    return loadFullMap(name, scale, true);
  }

  public static MapFragment loadFullMap(String name, int scale, boolean compressed) {
    byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180, compressed);
    return new FullMap(data, scale);
  }

  public static MapFragment loadFullSmoothMap(String name, int scale) {
    return loadFullMap(name, scale, true);
  }

  public static MapFragment loadFullSmoothMap(String name, int scale, boolean compressed) {
    byte[][] data = loadFragmentData(name, scale, -180, -90, 360, 180, compressed);
    return new FullSmoothMap(data, scale);
  }

  private static byte[][] loadFragmentData(
      String name, int scale, int x, int y, int width, int height, boolean compressed) {
    byte[][] data = new byte[width * scale][height * scale];
    String resourceName =
        "maps/%s/%s".formatted(name, buildResourceFileName(scale, x, y, width, height, compressed));
    try (InputStream inputStream =
        MosaicTerrainService.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Resource '%s' does not exist.".formatted(resourceName));
      }
      byte[] bytes = inputStream.readAllBytes();
      if (compressed) {
        bytes = decompressBytes(bytes);
      }
      for (int xi = 0; xi < width * scale; xi++) {
        System.arraycopy(bytes, height * scale * xi, data[xi], 0, height * scale);
      }
    } catch (IOException exception) {
      throw new IllegalArgumentException("Cannot read resource bytes.");
    }
    return data;
  }

  private static String buildResourceFileName(
      int scale, int x, int y, int width, int height, boolean compressed) {
    String resourceName = "x" + scale;
    if (width < 360) {
      resourceName += "_lon" + x + "+" + width;
    }
    if (height < 180) {
      resourceName += "_lat" + y + "+" + height;
    }
    resourceName += ".bin";
    if (compressed) {
      resourceName += ".z";
    }
    return resourceName;
  }

  private static byte[] decompressBytes(byte[] bytes) throws IOException {
    ZstdInputStream inputStream = new ZstdInputStream(new ByteArrayInputStream(bytes));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }
    return outputStream.toByteArray();
  }
}
