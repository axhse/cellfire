package com.example.cellfire.services.weatherapi;

import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Grid;
import com.example.cellfire.models.Weather;
import com.example.cellfire.services.WeatherService;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

//@Service
public final class WeatherApiService implements WeatherService {
    private static final Grid grid = new Grid(10);
    private static final long timeScale = Duration.ofHours(2).toSeconds();
    private final Map<Long, Map<Coordinates, Weather>> cache = new HashMap<>();
    private final WeatherApiClient weatherApiClient;

    public WeatherApiService(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    public synchronized Weather getWeather(LatLng point, Instant date) {
        Coordinates coordinates = coordinatesOf(point);
        long timePoint = timePointOf(date);
        if (cache.containsKey(timePoint)) {
            Map<Coordinates, Weather> serialData = cache.get(timePoint);
            if (serialData.containsKey(coordinates)) {
                return serialData.get(coordinates);
            }
        }
        WeatherForecast forecast = weatherApiClient.requestForecast(point);
        if (forecast == null) {
            return null;
        }
        for (int hourIndex = forecast.getHourlyForecastedWeather().size() - 1; 0 <= hourIndex; hourIndex--) {
            long period = Duration.ofHours(hourIndex).toSeconds();
            long hourTimePoint = timePointOf(forecast.getForecastStartDate().plusSeconds(period));
            if (!cache.containsKey(hourTimePoint)) {
                cache.put(hourTimePoint, new HashMap<>());
            }
            cache.get(hourTimePoint).put(coordinates, forecast.getHourlyForecastedWeather().get(hourIndex));
        }
        if (cache.containsKey(timePoint)) {
            Map<Coordinates, Weather> serialData = cache.get(timePoint);
            if (serialData.containsKey(coordinates)) {
                return serialData.get(coordinates);
            }
        }
        return null;
    }

    private Coordinates coordinatesOf(LatLng point) {
        return grid.coordinatesOf(point);
    }

    private long timePointOf(Instant date) {
        return date.getEpochSecond() / timeScale;
    }
}
