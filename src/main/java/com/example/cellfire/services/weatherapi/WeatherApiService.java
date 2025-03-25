package com.example.cellfire.services.weatherapi;

import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Grid;
import com.example.cellfire.models.LatLng;
import com.example.cellfire.models.Weather;
import com.example.cellfire.services.WeatherService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

public final class WeatherApiService implements WeatherService {
    private static final Grid GRID = new Grid(20);
    private static final long TIME_SCALE = Duration.ofHours(1).toSeconds();
    public final int monthlyRequestLimit;
    private final SortedMap<Long, Map<Coordinates, Weather>> cache = new TreeMap<>();
    private final WeatherApiClient weatherApiClient;
    public long monthIndex = 0;
    public int requestCount = 0;

    public WeatherApiService(String apiKey, int monthlyRequestLimit) {
        this.weatherApiClient = new WeatherApiClient(apiKey);
        this.monthlyRequestLimit = monthlyRequestLimit;
    }

    public Optional<Weather> getWeather(LatLng point, Instant date) {
        Coordinates coordinates = coordinatesOf(point);
        long currentTimePoint = timePointOf(Instant.now());
        synchronized (cache) {
            while (!cache.isEmpty() && cache.firstKey() < currentTimePoint) {
                cache.remove(cache.firstKey());
            }
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
        Optional<WeatherForecast> optionalForecast = requestForecast(point);
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
            synchronized (cache) {
                if (!cache.containsKey(hourTimePoint)) {
                    putTimePoint(hourTimePoint);
                }
                cache.get(hourTimePoint).put(coordinates, forecast.getHourlyForecastedWeather().get(hourIndex));
            }
        }
        synchronized (cache) {
            if (!cache.containsKey(currentTimePoint)) {
                putTimePoint(currentTimePoint);
            }
            cache.get(currentTimePoint).put(coordinates, forecast.getFactualWeather());
        }
        return findCached(coordinates, timePoint);
    }

    private Optional<WeatherForecast> requestForecast(LatLng point) {
        long currentMonthIndex = ChronoUnit.MONTHS.between(
                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
        );
        if (currentMonthIndex > monthIndex) {
            monthIndex = currentMonthIndex;
            requestCount = 0;
        }
        if (0 <= monthlyRequestLimit && monthlyRequestLimit <= requestCount) {
            return Optional.empty();
        }
        requestCount++;
        return weatherApiClient.requestForecast(point);
    }

    private void putTimePoint(long timePoint) {
        synchronized (cache) {
            cache.put(timePoint, new HashMap<>());
        }
    }

    private Optional<Weather> findCached(Coordinates coordinates, long timePoint) {
        synchronized (cache) {
            if (cache.containsKey(timePoint)) {
                Map<Coordinates, Weather> serialData = cache.get(timePoint);
                if (serialData.containsKey(coordinates)) {
                    return Optional.of(serialData.get(coordinates));
                }
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
        return GRID.coordinatesOf(point);
    }

    private long timePointOf(Instant date) {
        return date.getEpochSecond() / TIME_SCALE;
    }
}
