package com.example.cellfire.services;

import com.example.cellfire.algorithm.Algorithm;
import com.example.cellfire.algorithm.ProbabilisticAlgorithm;
import com.example.cellfire.algorithm.ThermalAlgorithm;
import com.example.cellfire.models.Domain;
import com.example.cellfire.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ForecastService {
    private final TerrainService terrainService;
    private final WeatherService weatherService;
    private final ThermalAlgorithm thermalAlgorithm;
    private final ProbabilisticAlgorithm probabilisticAlgorithm;

    private final ScenarioConditions DEMO_CONDITIONS = new ScenarioConditions(300);
    private final FireFactors DEMO_FACTORS = new FireFactors(0, 20, 10, 1, 3);

    @Autowired
    public ForecastService(TerrainService terrainService, WeatherService weatherService, ThermalAlgorithm thermalAlgorithm, ProbabilisticAlgorithm probabilisticAlgorithm) {
        this.terrainService = terrainService;
        this.weatherService = weatherService;
        this.thermalAlgorithm = thermalAlgorithm;
        this.probabilisticAlgorithm = probabilisticAlgorithm;
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

        thermalAlgorithm.refine(draftForecast, scenario.getConditions());

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
}
