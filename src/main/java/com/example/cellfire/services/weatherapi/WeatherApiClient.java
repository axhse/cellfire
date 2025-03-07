package com.example.cellfire.services.weatherapi;

import com.example.cellfire.models.Weather;
import com.example.cellfire.services.weatherapi.dto.ForecastDayData;
import com.example.cellfire.services.weatherapi.dto.ForecastResponseData;
import com.example.cellfire.services.weatherapi.dto.WeatherData;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public final class WeatherApiClient {
    private static final String FORECAST_BASE_URL = "http://api.weatherapi.com/v1/forecast.json";
    private final String apiKey;
    private final WebClient webClient = WebClient.create();

    public WeatherApiClient(@Value("${WEATHER_API_KEY}") String apiKey) {
        this.apiKey = apiKey;
    }

    public WeatherForecast requestForecast(LatLng point) {
        BigDecimal lat = BigDecimal.valueOf(point.lat);
        BigDecimal lng = BigDecimal.valueOf(point.lng);
        String uri = "%s?days=3&key=%s&q=%s,%s".formatted(FORECAST_BASE_URL, apiKey, lat, lng);
        try {
            return webClient.post()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(ForecastResponseData.class)
                    .map(this::retrieveForecast)
                    .block();
        } catch (Exception exception) {
            return null;
        }
    }

    private WeatherForecast retrieveForecast(ForecastResponseData response) {
        List<ForecastDayData> forecastDays = response.getForecast().getDays();
        Instant forecastStartDate = forecastDays.isEmpty() ? null : forecastDays.get(0).getDate();
        List<Weather> hourlyForecastedWeather = forecastDays.stream()
                .flatMap(day -> day.getHourlyWeather().stream())
                .map(WeatherData::getWeather)
                .toList();
        return new WeatherForecast(forecastStartDate, hourlyForecastedWeather, response.getFactual().getWeather());
    }
}
