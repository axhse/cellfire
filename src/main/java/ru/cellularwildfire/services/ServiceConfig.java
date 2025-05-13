package ru.cellularwildfire.services;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cellularwildfire.data.MapFragment;
import ru.cellularwildfire.data.MapLoader;
import ru.cellularwildfire.data.Mosaic;
import ru.cellularwildfire.services.weatherapi.WeatherApiService;

@Configuration
public class ServiceConfig {
  private static final int SIMULATION_MANAGER_CAPACITY = 20;
  private static final int FORECASTED_WEATHER_DAYS = 3;

  @Value("${WEATHER_API_KEY:}")
  private String weatherApiKey;

  @Value("${WEATHER_REQUEST_LIMIT:-1}")
  private Integer weatherRequestLimit;

  @Value("${SIMULATOR_STEP_LIMIT_MILLI:-1}")
  private Integer simulatorStepLimitMilli;

  public static TerrainService terrainService() {
    List<MapRegion> mapRegions =
        List.of(
            new MapRegion(-180, 51, 360, 21),
            new MapRegion(-161, 13, 109, 39),
            new MapRegion(-88, -56, 54, 69),
            new MapRegion(-26, 10, 89, 42),
            new MapRegion(-15, -35, 66, 45),
            new MapRegion(63, 25, 96, 27),
            new MapRegion(66, -11, 102, 36),
            new MapRegion(112, -48, 68, 37));

    List<MapFragment> elevationFragments = new ArrayList<>();
    for (MapRegion region : mapRegions) {
      elevationFragments.add(
          MapLoader.loadMapSmoothFragment(
              "Elevation", 120, region.x, region.y, region.width, region.height));
    }
    Mosaic elevationMap = new Mosaic(elevationFragments);

    List<MapFragment> forestDensityFragments = new ArrayList<>();
    for (MapRegion region : mapRegions) {
      forestDensityFragments.add(
          MapLoader.loadMapFragment(
              "ForestDensity", 200, region.x, region.y, region.width, region.height));
    }
    Mosaic forestDensityMap = new Mosaic(forestDensityFragments);

    Mosaic forestTypeMap = new Mosaic(MapLoader.loadFullMap("ForestType", 10));

    return new MosaicTerrainService(elevationMap, forestTypeMap, forestDensityMap);
  }

  @Bean
  public SimulationManager simulationManager() {
    return new SimulationManager(SIMULATION_MANAGER_CAPACITY);
  }

  @Bean
  public Simulator simulator() {
    return new Simulator(
        terrainService(), weatherService(), new AutomatonAlgorithm(), simulatorStepLimitMilli);
  }

  public WeatherService weatherService() {
    if (weatherApiKey.isEmpty()) {
      return new StandaloneWeatherService();
    }
    return new WeatherApiService(weatherApiKey, FORECASTED_WEATHER_DAYS, weatherRequestLimit);
  }

  private static class MapRegion {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private MapRegion(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
  }
}
