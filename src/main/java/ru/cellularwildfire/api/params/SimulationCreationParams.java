package ru.cellularwildfire.api.params;

import ru.cellularwildfire.models.LatLng;

public final class SimulationCreationParams {
  public double[] startLonLat;

  public LatLng getStartPoint() {
    return new LatLng(startLonLat[1], startLonLat[0]);
  }
}
