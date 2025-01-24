package com.example.cellfire.service;

import com.example.cellfire.entity.*;
import com.example.cellfire.model.forecast.Algorithm;
import com.example.cellfire.model.forecast.FlameOutcome;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastService {
    private final Algorithm algorithm;
    private final FuelService fuelService;
    private final WeatherService weatherService;

    @Autowired
    public ForecastService(Algorithm algorithm, FuelService fuelService, WeatherService weatherService) {
        this.algorithm = algorithm;
        this.fuelService = fuelService;
        this.weatherService = weatherService;
    }

    public InstantForecast forecast(Scenario scenario, Instant date) {
        while (!scenario.hasInstantForecast(date)) {
            forecastFurther(scenario);
        }
        return scenario.getInstantForecast(date);
    }

    private List<Cell> determineFurtherForecastConditions(Scenario scenario) {
        InstantForecast previousForecast = scenario.getForecast().getInstantForecasts().getLast();
        int furtherStepNumber = scenario.getForecast().getInstantForecasts().size();
        Instant date  = scenario.getStartDate().plus(Domain.FORECAST_STEP.multipliedBy(furtherStepNumber));

        List<Cell> conditions = new ArrayList<>();
        previousForecast.getCells().forEach(cell -> {
            WeatherCell weatherCell = createWeatherCell(getPoint(cell, scenario), date);
            conditions.add(new Cell(cell.getX(), cell.getY(), cell.getFireCell(), cell.getFuelCell(), weatherCell));
        });

        previousForecast.getCells().forEach(cell -> {
            if (cell.getFireCell().getHeat() <= cell.getFuelCell().getFlammability()) {
                return;
            }
            for (int x = cell.getX() - 1; x <= cell.getX() + 1; x++) {
                for (int y = cell.getY() - 1; y <= cell.getY() + 1; y++) {
                    int newX = x;
                    int newY = y;
                    if (conditions.stream().anyMatch(previousCell -> previousCell.getX() == newX && previousCell.getY() == newY)) {
                        continue;
                    }
                    LatLng point = getPoint(cell, scenario);
                    FuelCell fuelCell = createFuelCell(point);
                    WeatherCell weatherCell = createWeatherCell(point, date);
                    conditions.add(new Cell(x, y, new FireCell(0), fuelCell ,weatherCell));
                }
            }
        });

        return conditions;
    }

    private void forecastFurther(Scenario scenario) {
        InstantForecast furtherForecast = new InstantForecast();
        List<Cell> conditions = determineFurtherForecastConditions(scenario);

        conditions.forEach(cell -> {
            List<Cell> neighbours =  conditions.stream().filter(
                    otherCell -> Math.abs(cell.getX() - otherCell.getX()) <= 1
                            && Math.abs(cell.getY() - otherCell.getY()) <= 1
                            && !otherCell.equals(cell)).toList();

            FlameOutcome outcome = algorithm.flame(cell, neighbours, scenario.getStartPoint());
            if (outcome.getFireCell().getHeat() < cell.getWeatherCell().getTemperature() + Domain.IGNITION_HEAT_DELTA) {
                return;
            }
            furtherForecast.getCells().add(new Cell(cell.getX(), cell.getY(), outcome.getFireCell(), outcome.getFuelCell(), cell.getWeatherCell()));
        });

        scenario.getForecast().getInstantForecasts().add(furtherForecast);
    }

    public Cell createInitialCell(LatLng point) {
        return new Cell(0, 0, new FireCell(Domain.INITIAL_FIRE_HEAT), createFuelCell(point), null);
    }

    private FuelCell createFuelCell(LatLng point) {
        return new FuelCell(
                fuelService.getResource(point),
                fuelService.getFlammability(point)
        );
    }

    private WeatherCell createWeatherCell(LatLng point, Instant date) {
        return new WeatherCell(
                weatherService.getTemperature(point, date),
                weatherService.getHumidity(point, date),
                weatherService.getWind(point, date)
        );
    }

    private LatLng getPoint(Cell cell, Scenario scenario) {
        // TODO: Fix
        return new LatLng(scenario.getStartPoint().lat + cell.getY() * Domain.CELL_SIZE, scenario.getStartPoint().lng + cell.getX() * Domain.CELL_SIZE);
    }
}
