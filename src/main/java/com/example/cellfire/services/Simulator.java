package com.example.cellfire.services;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ProbabilisticAlgorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.*;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public final class Simulator {
    private final TerrainService terrainService;
    private final WeatherService weatherService;
    private final Algorithm algorithm;

    public Simulator(TerrainService terrainService, WeatherService weatherService, Algorithm algorithm) {
        this.terrainService = terrainService;
        this.weatherService = weatherService;
        this.algorithm = algorithm;
    }

    @Autowired
    public Simulator(TerrainService terrainService, WeatherService weatherService) {
        this(terrainService, weatherService, new ThermalAlgorithm());
    }

    public Simulation createSimulation(
            Grid grid, LatLng startPoint, Duration stepDuration,
            Duration limitDuration, Instant startDate, String algorithm
    ) {
        return new Simulation(
                grid, grid.fromLatLng(startPoint), stepDuration,
                limitDuration, startDate, determineConditions(startPoint), algorithm
        );
    }

    public Simulation createDefaultSimulation(LatLng startPoint, Instant startDate, String algorithm) {
        Grid grid = new Grid(ModelSettings.DEFAULT_GRID_SCALE);
        return createSimulation(
                grid, startPoint, ModelSettings.DEFAULT_STEP_DURATION,
                ModelSettings.DEFAULT_LIMIT_DURATION, startDate, algorithm
        );
    }

    public void startSimulation(Simulation simulation) {
        Coordinates startCoordinates = simulation.getStartCoordinates();
        LatLng startPoint = simulation.getGrid().toLatLng(startCoordinates);
        Simulation.Step initialStep = new Simulation.Step();
        simulation.getSteps().add(initialStep);
        float fuel = (float) terrainService.getFuel(startPoint);
        CellState cellState = new CellState(ModelSettings.INITIAL_HEAT, fuel);
        Weather weather = determineWeather(startPoint, simulation.getStartDate());
        Cell initialCell = new Cell(startCoordinates, cellState, weather);
        initialStep.getCells().add(initialCell);
    }

    public synchronized void progressSimulation(Simulation simulation, int endStep) {
        while (!simulation.hasStep(endStep)) {
            Simulation.Step draftStep = createDraftStep(simulation);
            selectAlgorithm(simulation).refineDraftStep(draftStep, simulation);
            simulation.getSteps().add(draftStep);
        }
    }

    private Simulation.Step createDraftStep(Simulation simulation) {
        Grid grid = simulation.getGrid();
        Simulation.Step draftStep = new Simulation.Step();
        Simulation.Step lastStep = simulation.getSteps().getLast();
        Duration period = simulation.getStepDuration().multipliedBy(simulation.getSteps().size());
        Instant date = simulation.getStartDate().plus(period);

        lastStep.getCells().forEach(cell -> {
            Weather weather = determineWeather(grid.toLatLng(cell.getCoordinates()), date);
            if (weather.equals(cell.getWeather())) {
                weather = cell.getWeather();
            }
            CellState lastCellState = cell.getState();
            boolean isDamaged = lastCellState.isDamaged()
                    || simulation.getConditions().getIgnitionTemperature() < lastCellState.getHeat();
            CellState draftCellState = new CellState(lastCellState.getHeat(), lastCellState.getFuel(), isDamaged);
            Cell draftCell = new Cell(cell.getCoordinates(), draftCellState, weather);
            draftCell.setTwin(cell);
            cell.setTwin(draftCell);
            draftStep.getCells().add(draftCell);
        });

        lastStep.getCells().forEach(cell -> {
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

        draftStep.getCells().forEach(cell -> {
            cell.setTwin(null);
        });

        lastStep.getCells().forEach(previousCell -> {
            Cell cell = previousCell.getTwin();
            if (cell.getState().getHeat() <= simulation.getConditions().getIgnitionTemperature()) {
                return;
            }
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    Coordinates neighborCoordinates = grid.getNeighbor(cell.getCoordinates(), offsetX, offsetY);
                    LatLng neighborPoint = grid.toLatLng(neighborCoordinates);
                    float fuel = (float) terrainService.getFuel(neighborPoint);
                    Weather weather = determineWeather(neighborPoint, date);
                    if (weather.equals(cell.getWeather())) {
                        weather = cell.getWeather();
                    }
                    CellState cellState = new CellState(weather.getAirTemperature(), fuel);
                    Cell neighbor = new Cell(neighborCoordinates, cellState, weather);

                    // neighbor.setNeighbor(-offsetX, -offsetY, cell);
                    // cell.setNeighbor(offsetX, offsetY, neighbor);
                    // TODO: optimize
                    draftStep.getCells().forEach(otherCell -> {
                        int distanceX = otherCell.getCoordinates().getX() - neighbor.getCoordinates().getX();
                        int distanceY = otherCell.getCoordinates().getY() - neighbor.getCoordinates().getY();
                        if (Math.abs(distanceX) <= 1 && Math.abs(distanceY) <= 1) {
                            neighbor.setNeighbor(distanceX, distanceY, otherCell);
                            otherCell.setNeighbor(-distanceX, -distanceY, neighbor);
                        }
                    });

                    draftStep.getCells().add(neighbor);
                }
            }
        });

        return draftStep;
    }

    // TODO: Remove.
    private Algorithm selectAlgorithm(Simulation simulation) {
        if (simulation.getAlgorithm().equals(Simulation.Algorithm.PROBABILISTIC)) {
            return new ProbabilisticAlgorithm();
        }
        return algorithm;
    }

    private Simulation.Conditions determineConditions(LatLng startPoint) {
        return new Simulation.Conditions(
                terrainService.getIgnitionTemperature(startPoint),
                terrainService.getActivationEnergy(startPoint)
        );
    }

    private Weather determineWeather(LatLng point, Instant date) {
        return new Weather(
                (float) terrainService.getElevation(point),
                (float) weatherService.getAirTemperature(point, date),
                (float) weatherService.getAirHumidity(point, date),
                (float) weatherService.getWindX(point, date),
                (float) weatherService.getWindY(point, date)
        );
    }
}
