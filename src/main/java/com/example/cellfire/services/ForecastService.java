package com.example.cellfire.services;

import com.example.cellfire.models.Domain;
import com.example.cellfire.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ForecastService {
    private final ForecastAlgorithm forecastAlgorithm;
    private final TerrainService terrainService;
    private final WeatherService weatherService;

    private final ScenarioConditions DEMO_CONDITIONS = new ScenarioConditions(200);
    private final FireFactors DEMO_FACTORS = new FireFactors(new float[]{0, 0}, 20, 10, new float[]{2, 2});

    @Autowired
    public ForecastService(ForecastAlgorithm forecastAlgorithm, TerrainService terrainService, WeatherService weatherService) {
        this.forecastAlgorithm = forecastAlgorithm;
        this.terrainService = terrainService;
        this.weatherService = weatherService;
    }

    public ScenarioConditions determineConditions(CellCoordinates startCoordinates) {
        return DEMO_CONDITIONS;
    }

    public void initiate(Scenario scenario, CellCoordinates startCoordinates) {
        Forecast initialForecast = new Forecast();
        scenario.getForecastLog().getForecasts().add(initialForecast);
        float fuel = terrainService.getFuel(startCoordinates);
        if (fuel == 0) {
            return;
        }
        Fire fire = new Fire(Domain.Settings.INITIAL_FIRE_HEAT, fuel);
        Cell initialCell = new Cell(startCoordinates, getFactors(startCoordinates, scenario.getStartDate()), fire);
        initialForecast.getCells().add(initialCell);
    }

    public synchronized Forecast forecast(Scenario scenario, Instant date) {
        while (!scenario.hasForecast(date)) {
            forecastFurther(scenario);
        }
        return scenario.getForecast(date);
    }

    private void forecastFurther(Scenario scenario) {
        Forecast draftForecast = new Forecast();
        Forecast lastForecast = scenario.getForecastLog().getForecasts().getLast();
        int furtherStepNumber = scenario.getForecastLog().getForecasts().size();
        Instant date = scenario.getStartDate().plus(Domain.Settings.FORECAST_STEP.multipliedBy(furtherStepNumber));

        lastForecast.getCells().forEach(cell -> {
            FireFactors fireFactors = getFactors(cell.getCoordinates(), date);
            Fire lastFire = cell.getFire();
            boolean isDamaged = lastFire.getIsDamaged()
                    || scenario.getConditions().getIgnitionTemperature() < lastFire.getHeat();
            Fire draftFire = new Fire(lastFire.getHeat(), lastFire.getFuel(), isDamaged);
            Cell draftCell = new Cell(cell.getCoordinates(), fireFactors, draftFire);
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
            if (cell.getFire().getHeat() <= scenario.getConditions().getIgnitionTemperature()) {
                return;
            }
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    CellCoordinates neighborCoordinates = cell.getCoordinates().createRelative(offsetX, offsetY);
                    float fuel = terrainService.getFuel(neighborCoordinates);
                    if (fuel == 0) {
                        continue;
                    }
                    FireFactors fireFactors = getFactors(neighborCoordinates, date);
                    Fire fire = new Fire(fireFactors.getAirTemperature(), fuel);
                    Cell neighbor = new Cell(neighborCoordinates, fireFactors, fire);

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

        forecastAlgorithm.refine(draftForecast, scenario.getConditions());

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

    private FireFactors getFactors(CellCoordinates coordinates, Instant date) {
        return DEMO_FACTORS;
//        return new FireFactors(
//                terrainService.getIgnitionTemperature(coordinates),
//                weatherService.getTemperature(coordinates, date),
//                weatherService.getHumidity(coordinates, date),
//                weatherService.getWind(coordinates, date)
//        );
    }

    private boolean isUnaffected(Cell cell) {
        return !cell.getFire().getIsDamaged()
                && cell.getFire().getHeat() - cell.getFactors().getAirTemperature() < Domain.Settings.SIGNIFICANT_OVERHEAT;
    }
}
