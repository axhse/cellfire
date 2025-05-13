package ru.cellularwildfire.data;

import java.util.List;
import ru.cellularwildfire.models.LatLng;

public final class Mosaic {
  private final List<MapFragment> fragments;

  public Mosaic(List<MapFragment> fragments) {
    this.fragments = fragments;
  }

  public Mosaic(MapFragment singleFragment) {
    this(List.of(singleFragment));
  }

  public int at(LatLng point) {
    for (MapFragment fragment : fragments) {
      if (fragment.has(point)) {
        return fragment.at(point);
      }
    }
    throw new IllegalArgumentException("Mosaic has no value for (%s).".formatted(point));
  }

  public int at(LatLng point, int defaultValue) {
    for (MapFragment fragment : fragments) {
      if (fragment.has(point)) {
        return fragment.at(point);
      }
    }
    return defaultValue;
  }
}
