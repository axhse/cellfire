package ru.cellularwildfire.api.params;

import java.util.Optional;
import ru.cellularwildfire.models.LatLng;

public final class SimulationCreationParams {
  public double[] startLonLat;

  public Optional<LatLng> getStartPoint() {
    if (startLonLat == null || startLonLat.length != 2) {
      return Optional.empty();
    }
    return Optional.of(new LatLng(startLonLat[1], startLonLat[0]));
  }
}
