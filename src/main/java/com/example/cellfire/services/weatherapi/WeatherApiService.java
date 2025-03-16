package com.example.cellfire.services.weatherapi;

import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Grid;
import com.example.cellfire.models.Weather;
import com.example.cellfire.services.WeatherService;
import com.google.maps.model.LatLng;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class WeatherApiService implements WeatherService {
    private static final Grid grid = new Grid(10);
    private static final long timeScale = Duration.ofHours(1).toSeconds();
    private final SortedMap<Long, Map<Coordinates, Weather>> cache = new TreeMap<>();
    private final WeatherApiClient weatherApiClient;

    public WeatherApiService(String apiKey) {
        this.weatherApiClient = new WeatherApiClient(apiKey);
    }

    public synchronized Optional<Weather> getWeather(LatLng point, Instant date) {
        Coordinates coordinates = coordinatesOf(point);
        long currentTimePoint = timePointOf(Instant.now());
        while (!cache.isEmpty() && cache.firstKey() < currentTimePoint) {
            cache.remove(cache.firstKey());
        }
        // If given date is beyond forecast period, weather at the latest forecasted moment is returned.
        long timePoint = Math.min(timePointOf(date), determineLatestTimePoint());
        if (timePoint < currentTimePoint - 1) {
            return Optional.empty();
        }
        if (timePoint == currentTimePoint - 1) {
            timePoint = currentTimePoint;
        }
        Optional<Weather> cachedWeather = findCached(coordinates, timePoint);
        if (cachedWeather.isPresent()) {
            return cachedWeather;
        }
        Optional<WeatherForecast> optionalForecast = weatherApiClient.requestForecast(point);
        if (optionalForecast.isEmpty()) {
            return Optional.empty();
        }
        WeatherForecast forecast = optionalForecast.get();
        for (int hourIndex = forecast.getHourlyForecastedWeather().size() - 1; 0 <= hourIndex; hourIndex--) {
            long period = Duration.ofHours(hourIndex).toSeconds();
            long hourTimePoint = timePointOf(forecast.getForecastStartDate().plusSeconds(period));
            if (hourTimePoint < currentTimePoint) {
                break;
            }
            if (!cache.containsKey(hourTimePoint)) {
                putTimePoint(hourTimePoint);
            }
            cache.get(hourTimePoint).put(coordinates, forecast.getHourlyForecastedWeather().get(hourIndex));
        }
        return findCached(coordinates, timePoint);
    }

    private void putTimePoint(long timePoint) {
        cache.put(timePoint, new HashMap<>());
    }

    private Optional<Weather> findCached(Coordinates coordinates, long timePoint) {
        if (cache.containsKey(timePoint)) {
            Map<Coordinates, Weather> serialData = cache.get(timePoint);
            if (serialData.containsKey(coordinates)) {
                return Optional.of(serialData.get(coordinates));
            }
        }
        return Optional.empty();
    }

    private long determineLatestTimePoint() {
        Duration forecastedPeriod = Duration.ofDays(WeatherApiClient.FORECASTED_DAYS).minusSeconds(1);
        long dayDuration = Duration.ofDays(1).toSeconds();
        long latestTs = Instant.now().plus(forecastedPeriod).getEpochSecond() / dayDuration * dayDuration;
        return timePointOf(Instant.ofEpochSecond(latestTs));
    }

    private Coordinates coordinatesOf(LatLng point) {
        return grid.coordinatesOf(point);
    }

    private long timePointOf(Instant date) {
        return date.getEpochSecond() / timeScale;
    }
}
