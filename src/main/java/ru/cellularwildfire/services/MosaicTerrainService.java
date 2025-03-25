package ru.cellularwildfire.services;

import org.springframework.stereotype.Service;
import ru.cellularwildfire.data.ForestTypeConditions;
import ru.cellularwildfire.data.Mosaic;
import ru.cellularwildfire.data.ResourceLoader;
import ru.cellularwildfire.models.LatLng;

@Service
public final class MosaicTerrainService implements TerrainService {
  private final Mosaic elevationMap = ResourceLoader.loadElevationMap();
  private final Mosaic forestTypeClusterMap = ResourceLoader.loadForestTypeClusterMap();
  private final Mosaic forestDensityMap = ResourceLoader.loadForestDensityMap();

  @Override
  public double getActivationEnergy(LatLng point) {
    return ForestTypeConditions.determineActivationEnergy(forestTypeClusterMap.at(point, 0));
  }

  @Override
  public double getFuel(LatLng point) {
    return forestDensityMap.at(point, 0) / 255.0;
  }

  @Override
  public double getElevation(LatLng point) {
    return elevationMap.at(point, 0) * 6400.0 / 255;
  }
}
