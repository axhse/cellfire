package ru.cellularwildfire.data;

import ru.cellularwildfire.models.LatLng;

public class SmoothMapFragment extends MapFragment {
  public SmoothMapFragment(byte[][] data, int scale, int x, int y, int width, int height) {
    super(data, scale, x, y, width, height);
  }

  @Override
  public int at(LatLng point) {
    LatLng center =
        new LatLng(
            (Math.floor(point.lat * scale) + 0.5) / scale,
            (Math.floor(point.lng * scale) + 0.5) / scale);
    int[] dLngValues =
        Math.abs(point.lng - center.lng) < 0.05 / scale ? new int[] {0} : new int[] {-1, 1};
    int[] dLatValues =
        Math.abs(point.lat - center.lat) < 0.05 / scale ? new int[] {0} : new int[] {-1, 1};
    if (dLngValues.length + dLatValues.length == 2) {
      return super.at(point);
    }
    double value = 0;
    double weight = 0;
    for (int dLng : dLngValues) {
      for (int dLat : dLatValues) {
        LatLng referencePoint = getReferencePoint(point, dLng, dLat);
        if (!has(referencePoint)) {
          continue;
        }
        double distanceLat = dLat * (center.lat - point.lat);
        double distanceLng = dLng * (center.lng - point.lng);
        if (distanceLat < 0) {
          distanceLat += 1.0 / scale;
        }
        if (distanceLng < 0) {
          distanceLng += 1.0 / scale;
        }
        distanceLng *= Math.cos(Math.toRadians(point.lat));
        double referenceWeight =
            1 / Math.sqrt(distanceLat * distanceLat + distanceLng * distanceLng);
        weight += referenceWeight;
        value += referenceWeight * super.at(referencePoint);
      }
    }
    return (int) Math.round(value / weight);
  }

  private LatLng getReferencePoint(LatLng point, int dLng, int dLat) {
    double lat = point.lat + 0.5 * dLat / scale;
    if (lat < -90 || 90 <= lat) {
      lat = (lat < 0 ? -1 : 1) * 180 - lat;
      dLng = 360 * scale - dLng;
    }
    double lng = point.lng + 0.5 * dLng / scale;
    if (lng < -180) {
      lng += 360;
    }
    if (180 <= lng) {
      lng -= 360;
    }
    return new LatLng(lat, lng);
  }
}
