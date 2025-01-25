package com.example.cellfire.service;

import com.example.cellfire.entity.*;
import com.example.cellfire.model.forecast.Algorithm;
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
        Instant date  = scenario.getStartDate().plus(Domain.FORECAST_STEP.multipliedBy(furtherStepNumber));

        List<Cell> conditions = new ArrayList<>();
        previousForecast.getCells().forEach(cell -> {
            Environment environment = createEnvironment(getPoint(cell, scenario), date);
            conditions.add(new Cell(cell.getX(), cell.getY(), cell.getFire(), environment));
        });

        previousForecast.getCells().forEach(cell -> {
            if (cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
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
                    Fire fire = new Fire(0, fuelService.getResource(point));
                    Environment environment = createEnvironment(point, date);
                    conditions.add(new Cell(x, y, fire, environment));
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

            Fire flame = algorithm.flame(cell, neighbours, scenario.getStartPoint());
            // FIXME: Do not remove cells, so wasted resource is not forgiven.
//            if (flame.getHeat() < cell.getEnvironment().getWeatherTemperature() + Domain.IGNITION_HEAT_DELTA) {
//                return;
//            }
            furtherForecast.getCells().add(new Cell(cell.getX(), cell.getY(), flame, cell.getEnvironment()));
        });

        scenario.getForecast().getInstantForecasts().add(furtherForecast);
    }

    public Cell createInitialCell(LatLng point, Instant date) {
        Fire fire = new Fire(Domain.INITIAL_FIRE_HEAT, fuelService.getResource(point));
        return new Cell(0, 0, fire, createEnvironment(point, date));
    }

    private Environment createEnvironment(LatLng point, Instant date) {
        return demoEnvironment;
//        return new Environment(
//                fuelService.getIgnitionTemperature(point),
//                weatherService.getTemperature(point, date),
//                weatherService.getHumidity(point, date),
//                weatherService.getWind(point, date)
//        );
    }

    private LatLng getPoint(Cell cell, Scenario scenario) {
        // TODO: Fix
        return new LatLng(scenario.getStartPoint().lat + cell.getY() * Domain.CELL_SIZE, scenario.getStartPoint().lng + cell.getX() * Domain.CELL_SIZE);
    }
}
