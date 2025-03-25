package com.example.cellfire.services;

import com.example.cellfire.services.weatherapi.WeatherApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
  @Value("${WEATHER_API_KEY:}")
  private String weatherApiKey;

  @Value("${WEATHER_REQUEST_LIMIT:-1}")
  private Integer weatherRequestLimit;

  @Bean
  public WeatherService weatherService() {
    if (weatherApiKey.isEmpty()) {
      return new StandaloneWeatherService();
    }
    return new WeatherApiService(weatherApiKey, weatherRequestLimit);
  }
}
