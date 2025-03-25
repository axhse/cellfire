package com.example.cellfire.services.weatherapi;

import com.example.cellfire.models.LatLng;
import com.example.cellfire.models.Weather;
import com.example.cellfire.services.weatherapi.dto.ForecastDayData;
import com.example.cellfire.services.weatherapi.dto.ForecastResponseData;
import com.example.cellfire.services.weatherapi.dto.WeatherData;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.web.reactive.function.client.WebClient;

public final class WeatherApiClient {
    public static final int FORECASTED_DAYS = 3;
    private final String apiKey;
    private final WebClient webClient = WebClient.create();

    public WeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public Optional<WeatherForecast> requestForecast(LatLng point) {
        try {
            WeatherForecast forecast = webClient.post()
                    .uri(buildForecastUri(point))
                    .retrieve()
                    .bodyToMono(ForecastResponseData.class)
                    .map(this::retrieveForecast)
                    .block();
            return forecast == null ? Optional.empty() : Optional.of(forecast);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String buildForecastUri(LatLng point) {
        String baseUri = "http://api.weatherapi.com/v1/forecast.json";

        BigDecimal lat = BigDecimal.valueOf(point.lat);
        BigDecimal lng = BigDecimal.valueOf(point.lng);

        return "%s?key=%s&days=%s&q=%s,%s".formatted(baseUri, apiKey, FORECASTED_DAYS, lat, lng);
    }

    private WeatherForecast retrieveForecast(ForecastResponseData response) {
        List<ForecastDayData> forecastDays = response.getForecast().getDays();
        if (forecastDays.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Instant startDate = forecastDays.get(0).getLocalDate().minusMillis(response.getTimezoneId().getRawOffset());
        List<Weather> hourlyForecastedWeather = forecastDays.stream()
                .flatMap(day -> day.getHourlyWeather().stream())
                .map(WeatherData::getWeather)
                .toList();
        Weather factualWeather = response.getFactual().getWeather();
        return new WeatherForecast(startDate, hourlyForecastedWeather, factualWeather);
    }
}
