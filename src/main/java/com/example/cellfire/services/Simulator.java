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
import java.util.HashMap;
import java.util.Map;

@Service
public final class Simulator {
    private static final int DEFAULT_GRID_SCALE = 200;
    private static final Duration DEFAULT_STEP_DURATION = Duration.ofMinutes(30);
    private static final Duration DEFAULT_LIMIT_DURATION = Duration.ofDays(7);
    private static final float INITIAL_HEAT = 1000;
    private static final float SIGNIFICANT_FUEL = 0.01F;

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
        Grid grid = new Grid(DEFAULT_GRID_SCALE);
        return createSimulation(grid, startPoint, DEFAULT_STEP_DURATION, DEFAULT_LIMIT_DURATION, startDate, algorithm);
    }

    public void startSimulation(Simulation simulation) {
        Coordinates startCoordinates = simulation.getStartCoordinates();
        LatLng startPoint = simulation.getGrid().toLatLng(startCoordinates);
        Simulation.Step initialStep = new Simulation.Step();
        simulation.getSteps().add(initialStep);
        float fuel = determineFuel(startPoint);
        CellState initialState = new CellState(INITIAL_HEAT, fuel, true);
        Weather weather = determineWeather(startPoint, simulation.getStartDate());
        Cell initialCell = new Cell(startCoordinates, initialState, weather);
        initialStep.getCells().add(initialCell);
    }

    public synchronized void progressSimulation(Simulation simulation, int endStep) {
        while (!simulation.hasStep(endStep)) {
            Simulation.Step draftStep = createDraftStep(simulation);
            selectAlgorithm(simulation).refineDraftStep(draftStep, simulation);
            for (Cell cell : draftStep.getCells()) {
                if (cell.getState().getFuel() < SIGNIFICANT_FUEL) {
                    cell.getState().setFuel(0);
                }
            }
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

        draftStep.getCells().forEach(cell -> cell.setTwin(null));

        Map<Coordinates, Cell> draftCellMap = new HashMap<>(draftStep.getCells().size());
        draftStep.getCells().forEach(cell -> draftCellMap.put(cell.getCoordinates(), cell));

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
                    float fuel = determineFuel(neighborPoint);
                    Weather weather = determineWeather(neighborPoint, date);
                    if (weather.equals(cell.getWeather())) {
                        weather = cell.getWeather();
                    }
                    CellState neighborState = new CellState(weather.getAirTemperature(), fuel, false);
                    Cell neighbor = new Cell(neighborCoordinates, neighborState, weather);

                    for (int dX = -1; dX <= 1; dX++) {
                        for (int dY = -1; dY <= 1; dY++) {
                            Coordinates otherCoordinates = grid.getNeighbor(neighborCoordinates, dX, dY);
                            if (draftCellMap.containsKey(otherCoordinates)) {
                                Cell otherCell = draftCellMap.get(otherCoordinates);
                                neighbor.setNeighbor(dX, dY, otherCell);
                                otherCell.setNeighbor(-dX, -dY, neighbor);
                            }
                        }
                    }

                    draftStep.getCells().add(neighbor);
                    draftCellMap.put(neighbor.getCoordinates(), neighbor);
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

    private float determineFuel(LatLng point) {
        double fuel = terrainService.getFuel(point);
        if (fuel < SIGNIFICANT_FUEL) {
            fuel = 0;
        }
        return (float) fuel;
    }
}
