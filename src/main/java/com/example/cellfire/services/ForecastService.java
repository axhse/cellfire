package com.example.cellfire.services;

import com.example.cellfire.DomainSettings;
import com.example.cellfire.models.*;
import com.example.cellfire.forecast.Algorithm;
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

    private final Environment demoEnvironment = new Environment(250, 20, 10, new double[]{ 1, 1 });

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
        Instant date  = scenario.getStartDate().plus(DomainSettings.FORECAST_STEP.multipliedBy(furtherStepNumber));

        List<Cell> conditions = new ArrayList<>();
        previousForecast.getCells().forEach(cell -> {
            Environment environment = createEnvironment(cell.getCoordinates(), date);
            conditions.add(new Cell(cell.getCoordinates(), cell.getFire(), environment));
        });

        previousForecast.getCells().forEach(cell -> {
            if (cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
                return;
            }
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    CellCoordinates newCoordinates = cell.getCoordinates().getRelative(offsetX, offsetY);
                    if (conditions.stream().anyMatch(previousCell -> newCoordinates.equals(previousCell.getCoordinates()))) {
                        continue;
                    }
                    Fire fire = new Fire(0, fuelService.getResource(cell.getCoordinates()));
                    Environment environment = createEnvironment(cell.getCoordinates(), date);
                    conditions.add(new Cell(newCoordinates, fire, environment));
                }
            }
        });

        return conditions;
    }

    private void forecastFurther(Scenario scenario) {
        InstantForecast furtherForecast = new InstantForecast();
        List<Cell> conditions = determineFurtherForecastConditions(scenario);

        conditions.forEach(cell -> {
            List<Cell> neighbours = conditions.stream().filter(
                    otherCell -> Math.abs(cell.getCoordinates().getX() - otherCell.getCoordinates().getX()) <= 1
                            && Math.abs(cell.getCoordinates().getY() - otherCell.getCoordinates().getY()) <= 1
                            && !otherCell.equals(cell)).toList();

            Fire flame = algorithm.flame(cell, neighbours);
//            if (flame.getHeat() < cell.getEnvironment().getWeatherTemperature() + DomainSettings.IGNITION_HEAT_DELTA) {
//                return;
//            }
            furtherForecast.getCells().add(new Cell(cell.getCoordinates(), flame, cell.getEnvironment()));
        });

        scenario.getForecast().getInstantForecasts().add(furtherForecast);
    }

    public Cell createInitialCell(CellCoordinates coordinates, Instant date) {
        Fire fire = new Fire(DomainSettings.INITIAL_FIRE_HEAT, fuelService.getResource(coordinates));
        return new Cell(coordinates, fire, createEnvironment(coordinates, date));
    }

    private Environment createEnvironment(CellCoordinates coordinates, Instant date) {
        return demoEnvironment;
//        return new Environment(
//                fuelService.getIgnitionTemperature(coordinates),
//                weatherService.getTemperature(coordinates, date),
//                weatherService.getHumidity(coordinates, date),
//                weatherService.getWind(coordinates, date)
//        );
    }
}
