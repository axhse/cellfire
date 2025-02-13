package com.example.cellfire.services;

import com.example.cellfire.models.ModelSettings;
import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ProbabilisticAlgorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
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

    @Autowired
    public ForecastService(TerrainService terrainService, StandaloneWeatherService weatherService, ThermalAlgorithm thermalAlgorithm, ProbabilisticAlgorithm probabilisticAlgorithm) {
        this.terrainService = terrainService;
        this.weatherService = weatherService;
        this.thermalAlgorithm = thermalAlgorithm;
        this.probabilisticAlgorithm = probabilisticAlgorithm;
    }

    public ScenarioConditions determineConditions(CellCoordinates startCoordinates) {
        return new ScenarioConditions(
                terrainService.getIgnitionTemperature(startCoordinates),
                terrainService.getActivationEnergy(startCoordinates)
        );
    }

    public void initiate(Scenario scenario, CellCoordinates startCoordinates) {
        Forecast initialForecast = new Forecast();
        scenario.getForecastLog().getForecasts().add(initialForecast);
        float fuel = (float)terrainService.getFuel(startCoordinates);
        Fire fire = new Fire(ModelSettings.INITIAL_HEAT, fuel);
        FireFactors factors = determineFactors(startCoordinates, scenario.getStartDate());
        Cell initialCell = new Cell(startCoordinates, factors, fire);
        initialForecast.getCells().add(initialCell);
    }

    public synchronized void forecast(Scenario scenario, int step) {
        while (!scenario.hasForecast(step)) {
            forecastFurther(scenario);
        }
    }

    private void forecastFurther(Scenario scenario) {
        Forecast draftForecast = new Forecast();
        Forecast lastForecast = scenario.getForecastLog().getForecasts().getLast();
        int furtherStepNumber = scenario.getForecastLog().getForecasts().size();
        Instant date = scenario.getStartDate().plus(ModelSettings.STEP_DURATION.multipliedBy(furtherStepNumber));

        lastForecast.getCells().forEach(cell -> {
            FireFactors fireFactors = determineFactors(cell.getCoordinates(), date);
            if (fireFactors.equals(cell.getFactors())) {
                fireFactors = cell.getFactors();
            }
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
                    float fuel = (float)terrainService.getFuel(neighborCoordinates);
                    FireFactors fireFactors = determineFactors(neighborCoordinates, date);
                    if (fireFactors.equals(cell.getFactors())) {
                        fireFactors = cell.getFactors();
                    }
                    Fire fire = new Fire(fireFactors.getAirTemperature(), fuel);
                    Cell neighbor = new Cell(neighborCoordinates, fireFactors, fire);

//                    neighbor.setNeighbor(-offsetX, -offsetY, cell);
//                    cell.setNeighbor(offsetX, offsetY, neighbor);
                    // TODO: optimize
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

        selectAlgorithm(scenario).refine(draftForecast, scenario.getConditions());

        scenario.getForecastLog().getForecasts().add(draftForecast);
    }

    private Algorithm selectAlgorithm(Scenario scenario) {
        String algorithmName = scenario.getAlgorithm();
        if (algorithmName.equals(Scenario.Algorithm.THERMAL)) {
            return thermalAlgorithm;
        }
        if (algorithmName.equals(Scenario.Algorithm.PROBABILISTIC)) {
            return probabilisticAlgorithm;
        }
        return thermalAlgorithm;
    }

    private FireFactors determineFactors(CellCoordinates coordinates, Instant date) {
        return new FireFactors(
                (float)terrainService.getElevation(coordinates),
                (float)weatherService.getAirTemperature(coordinates, date),
                (float)weatherService.getAirHumidity(coordinates, date),
                (float)weatherService.getWindX(coordinates, date),
                (float)weatherService.getWindY(coordinates, date)
        );
    }
}
