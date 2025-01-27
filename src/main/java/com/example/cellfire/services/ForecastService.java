package com.example.cellfire.services;

import com.example.cellfire.DomainSettings;
import com.example.cellfire.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ForecastService {
    private final ForecastAlgorithm forecastAlgorithm;
    private final FuelService fuelService;
    private final WeatherService weatherService;

    private final Environment demoEnvironment = new Environment(200, 20, 10, new float[]{2, 2});

    @Autowired
    public ForecastService(ForecastAlgorithm forecastAlgorithm, FuelService fuelService, WeatherService weatherService) {
        this.forecastAlgorithm = forecastAlgorithm;
        this.fuelService = fuelService;
        this.weatherService = weatherService;
    }

    public void initiate(Scenario scenario, CellCoordinates startCoordinates) {
        Forecast initialForecast = new Forecast();
        scenario.getForecastLog().getForecasts().add(initialForecast);
        float resource = fuelService.getResource(startCoordinates);
        if (resource == 0) {
            return;
        }
        Fire fire = new Fire(DomainSettings.INITIAL_FIRE_HEAT, resource);
        Cell initialCell = new Cell(startCoordinates, createEnvironment(startCoordinates, scenario.getStartDate()), fire);
        initialForecast.getCells().add(initialCell);
    }

    public Forecast forecast(Scenario scenario, Instant date) {
        while (!scenario.hasForecast(date)) {
            forecastFurther(scenario);
        }
        return scenario.getForecast(date);
    }

    private void forecastFurther(Scenario scenario) {
        Forecast draftForecast = new Forecast();
        Forecast lastForecast = scenario.getForecastLog().getForecasts().getLast();
        int furtherStepNumber = scenario.getForecastLog().getForecasts().size();
        Instant date = scenario.getStartDate().plus(DomainSettings.FORECAST_STEP.multipliedBy(furtherStepNumber));

        lastForecast.getCells().forEach(cell -> {
            Environment environment = createEnvironment(cell.getCoordinates(), date);
            Fire lastFire = cell.getFire();
            Fire fire = new Fire(lastFire.getHeat(), lastFire.getInitialResource(), lastFire.getResource());
            Cell draftCell = new Cell(cell.getCoordinates(), environment, fire);
            draftCell.setTwin(cell);
            cell.setTwin(draftCell);
            draftForecast.getCells().add(draftCell);
        });

        lastForecast.getCells().forEach(cell -> {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0) {
                        continue;
                    }
                    Cell neighbor = cell.getNeighbor(offsetX, offsetY);
                    if (neighbor == null) {
                        continue;
                    }
                    cell.getTwin().setNeighbor(offsetX, offsetY, cell.getNeighbor(offsetX, offsetY).getTwin());
                }
            }
        });

        draftForecast.getCells().forEach(cell -> {
            cell.setTwin(null);
        });

        lastForecast.getCells().forEach(lastForecastCell -> {
            Cell cell = lastForecastCell.getTwin();
            if (cell.getFire().getHeat() <= cell.getEnvironment().getIgnitionTemperature()) {
                return;
            }
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    CellCoordinates neighborCoordinates = cell.getCoordinates().shift(offsetX, offsetY);
                    float resource = fuelService.getResource(neighborCoordinates);
                    if (resource == 0) {
                        continue;
                    }
                    Environment environment = createEnvironment(neighborCoordinates, date);
                    Fire fire = new Fire(environment.getWeatherTemperature(), resource);
                    Cell neighbor = new Cell(neighborCoordinates, environment, fire);

//                    neighbor.setNeighbor(-offsetX, -offsetY, cell);
//                    cell.setNeighbor(offsetX, offsetY, neighbor);
                    // FIXME: optimize
                    draftForecast.getCells().forEach(otherCell -> {
                        int distanceX = otherCell.getCoordinates().getX() - neighbor.getCoordinates().getX();
                        int distanceY = otherCell.getCoordinates().getY() - neighbor.getCoordinates().getY();
                        if (Math.abs(distanceX) <= 1 && Math.abs(distanceY) <= 1) {
                            neighbor.setNeighbor(distanceX, distanceY, otherCell);
                            otherCell.setNeighbor(-distanceX, -distanceY, neighbor);
                        }
                    });

                    draftForecast.getCells().add(neighbor);
                }
            }
        });

        forecastAlgorithm.refine(draftForecast);

        draftForecast.getCells().stream().filter(this::isUnaffected).forEach(
                cell -> {
                    for (Cell neighbor : cell.iterateNeighbors()) {
                        neighbor.setNeighbor(
                                cell.getCoordinates().getX() - neighbor.getCoordinates().getX(),
                                cell.getCoordinates().getY() - neighbor.getCoordinates().getY(),
                                null
                        );
                    }
                }
        );
        draftForecast.getCells().removeIf(this::isUnaffected);

        scenario.getForecastLog().getForecasts().add(draftForecast);
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

    private boolean isUnaffected(Cell cell) {
        return cell.getFire().getResource() == cell.getFire().getInitialResource()
                && cell.getFire().getHeat() - cell.getEnvironment().getWeatherTemperature() < DomainSettings.SIGNIFICANT_OVERHEAT;
    }
}
