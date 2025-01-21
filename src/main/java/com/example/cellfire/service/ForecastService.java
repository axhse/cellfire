package com.example.cellfire.service;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.FireCell;
import com.example.cellfire.entity.InstantForecast;
import com.example.cellfire.entity.Scenario;
import com.example.cellfire.model.forecast.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ForecastService {
    private final Algorithm algorithm = new Algorithm();

    private final FuelService fuelService;
    private final WeatherService weatherService;

    @Autowired
    public ForecastService(FuelService fuelService, WeatherService weatherService) {
        this.fuelService = fuelService;
        this.weatherService = weatherService;
    }

    public FireCell createInitialFire() {
        return new FireCell(400, 1);
    }

    public InstantForecast forecast(Scenario scenario, Instant date) {
        while (!scenario.hasInstantForecast(date)) {
            forecastFurther(scenario);
        }
        return scenario.getInstantForecast(date);
    }

    private void forecastFurther(Scenario scenario) {
        InstantForecast furtherForecast = new InstantForecast();
        InstantForecast previousForecast = scenario.getForecast().getInstantForecasts().getLast();
        previousForecast.getCells().forEach(cell -> {
            furtherForecast.getCells().add(new Cell(cell.getX(), cell.getY(), cell.getFireCell(), null ,null));
        });
        previousForecast.getCells().forEach(cell -> {
            for (int x = cell.getX() - 1; x <= cell.getX() + 1; x++) {
                for (int y = cell.getY() - 1; y <= cell.getY() + 1; y++) {
                    int newX = x;
                    int newY = y;
                    if (furtherForecast.getCells().stream().anyMatch(c -> c.getX() == newX && c.getY() == newY)) {
                        continue;
                    }
                    furtherForecast.getCells().add(new Cell(x, y, cell.getFireCell(), null ,null));
                }
            }
        });

        scenario.getForecast().getInstantForecasts().add(furtherForecast);
    }
}
