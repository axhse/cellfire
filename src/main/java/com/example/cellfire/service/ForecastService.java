package com.example.cellfire.service;

import com.example.cellfire.entity.*;
import com.example.cellfire.model.forecast.Algorithm;
import com.google.maps.model.LatLng;
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
            LatLng point = getPoint(cell, scenario);
            FuelCell fuelCell = new FuelCell(fuelService.getFlammability(point), fuelService.getCombustibility(point));
            furtherForecast.getCells().add(new Cell(cell.getX(), cell.getY(), cell.getFireCell(), fuelCell,null));
        });
        previousForecast.getCells().forEach(cell -> {
            for (int x = cell.getX() - 1; x <= cell.getX() + 1; x++) {
                for (int y = cell.getY() - 1; y <= cell.getY() + 1; y++) {
                    int newX = x;
                    int newY = y;
                    if (furtherForecast.getCells().stream().anyMatch(c -> c.getX() == newX && c.getY() == newY)) {
                        continue;
                    }
                    LatLng point = getPoint(cell, scenario);
                    FuelCell fuelCell = new FuelCell(fuelService.getFlammability(point), fuelService.getCombustibility(point));
                    furtherForecast.getCells().add(new Cell(x, y, cell.getFireCell(), fuelCell ,null));
                }
            }
        });

        scenario.getForecast().getInstantForecasts().add(furtherForecast);
    }

    private LatLng getPoint(Cell cell, Scenario scenario) {
        // TODO: Fix
        return new LatLng(scenario.getStartPoint().lat + cell.getY() * Domain.CELL_SIZE, scenario.getStartPoint().lng + cell.getX() * Domain.CELL_SIZE);
    }
}
