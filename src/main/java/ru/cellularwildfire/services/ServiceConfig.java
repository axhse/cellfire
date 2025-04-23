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
    elevationFragments.add(MapLoader.loadMapSmoothFragment("Elevation", 30, -180, -56, 360, 126));
    Mosaic elevationMap = new Mosaic(elevationFragments);

    List<MapFragment> forestTypeFragments = new ArrayList<>();
    forestTypeFragments.add(MapLoader.loadFullMap("ForestType", 10));
    Mosaic forestTypeMap = new Mosaic(forestTypeFragments);

    List<MapFragment> forestDensityFragments = new ArrayList<>();
    forestDensityFragments.add(MapLoader.loadMapFragment("ForestDensity", 200, -26, 10, 89, 42));
    forestDensityFragments.add(MapLoader.loadMapFragment("ForestDensity", 200, -15, -35, 66, 45));
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
