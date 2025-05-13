package ru.cellularwildfire.services.weatherapi;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Weather;
import ru.cellularwildfire.services.weatherapi.dto.ErrorBody;
import ru.cellularwildfire.services.weatherapi.dto.ForecastDayData;
import ru.cellularwildfire.services.weatherapi.dto.ForecastResponseData;
import ru.cellularwildfire.services.weatherapi.dto.WeatherData;

public final class WeatherApiClient {
  private final String apiKey;
  private final int forecastedDays;
  private final WebClient webClient = WebClient.create();

  public WeatherApiClient(String apiKey, int forecastedDays) {
    this.apiKey = apiKey;
    this.forecastedDays = forecastedDays;
  }

  public Optional<WeatherForecast> requestForecast(LatLng point, Instant currentTime) {
    WeatherForecast forecast =
        webClient
            .post()
            .uri(buildForecastUri(point))
            .exchangeToMono(
                response -> {
                  if (response.statusCode().value() == 400) {
                    return response
                        .bodyToMono(ErrorBody.class)
                        .defaultIfEmpty(new ErrorBody())
                        .map(
                            errorBody -> {
                              if (errorBody.getError().isNoMatchingLocation()) {
                                return new WeatherForecast(
                                    currentTime, List.of(), new Weather(0, 1, 0, 0));
                              } else {
                                return null;
                              }
                            });
                  } else if (response.statusCode().is2xxSuccessful()) {
                    return response
                        .bodyToMono(ForecastResponseData.class)
                        .map(this::retrieveForecast);
                  } else {
                    return null;
                  }
                })
            .block();
    return forecast == null ? Optional.empty() : Optional.of(forecast);
  }

  private WeatherForecast retrieveForecast(ForecastResponseData response) {
    List<ForecastDayData> forecastDays = response.getForecast().getDays();
    if (forecastDays.isEmpty()) {
      throw new IllegalArgumentException();
    }
    Instant startDate =
        forecastDays.get(0).getLocalDate().minusMillis(response.getTimezoneId().getRawOffset());
    List<Weather> hourlyForecastedWeather =
        forecastDays.stream()
            .flatMap(day -> day.getHourlyWeather().stream())
            .map(WeatherData::getWeather)
            .toList();
    Weather factualWeather = response.getFactual().getWeather();
    return new WeatherForecast(startDate, hourlyForecastedWeather, factualWeather);
  }

  private String buildForecastUri(LatLng point) {
    String baseUri = "http://api.weatherapi.com/v1/forecast.json";

    BigDecimal lat = BigDecimal.valueOf(point.lat);
    BigDecimal lng = BigDecimal.valueOf(point.lng);

    return "%s?key=%s&days=%s&q=%s,%s".formatted(baseUri, apiKey, forecastedDays, lat, lng);
  }
}
