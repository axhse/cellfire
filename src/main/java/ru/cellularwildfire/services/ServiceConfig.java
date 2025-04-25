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
  private static final int SIMULATION_MANAGER_CAPACITY = 50;

  @Value("${WEATHER_API_KEY:}")
  private String weatherApiKey;

  @Value("${WEATHER_REQUEST_LIMIT:-1}")
  private Integer weatherRequestLimit;

  @Bean
  public static TerrainService terrainService() {
    List<MapFragment> elevationFragments = new ArrayList<>();
    elevationFragments.add(MapLoader.loadMapSmoothFragment("Elevation", 120, -180, -56, 360, 128));
    Mosaic elevationMap = new Mosaic(elevationFragments);

    List<MapFragment> forestTypeFragments = new ArrayList<>();
    forestTypeFragments.add(MapLoader.loadFullMap("ForestType", 10));
    Mosaic forestTypeMap = new Mosaic(forestTypeFragments);

    List<MapFragment> forestDensityFragments = new ArrayList<>();
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, -180, 51, 360, 21));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, -161, 13, 109, 39));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, -88, -56, 54, 69));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, -26, 10, 89, 42));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, -15, -35, 66, 45));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, 63, 25, 96, 27));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, 66, -11, 102, 36));
    forestDensityFragments.add(MapLoader.loadMapSmoothFragment("ForestDensity", 200, 112, -48, 68, 37));
    Mosaic forestDensityMap = new Mosaic(forestDensityFragments);

    return new MosaicTerrainService(elevationMap, forestTypeMap, forestDensityMap);
  }

  @Bean
  public SimulationManager simulationManager() {
    return new SimulationManager(SIMULATION_MANAGER_CAPACITY);
  }

  @Bean
  public WeatherService weatherService() {
    if (weatherApiKey.isEmpty()) {
      return new StandaloneWeatherService();
    }
    return new WeatherApiService(weatherApiKey, weatherRequestLimit);
  }
}
