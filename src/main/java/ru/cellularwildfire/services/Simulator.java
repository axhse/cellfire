package ru.cellularwildfire.services;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.cellularwildfire.models.*;

@Service
public final class Simulator {
  public static final int DEFAULT_GRID_SCALE = 200;
  public static final Duration DEFAULT_STEP_DURATION = Duration.ofMinutes(30);
  public static final Duration DEFAULT_LIMIT_DURATION = Duration.ofDays(7);
  public static final double INITIAL_HEAT = 1000;
  public static final double SIGNIFICANT_FUEL = 0.01F;

  private final TerrainService terrainService;
  private final WeatherService weatherService;
  private final AutomatonAlgorithm algorithm;
  private final Integer limitStepMilli;

  public Simulator(
      TerrainService terrainService, WeatherService weatherService, AutomatonAlgorithm algorithm) {
    this(terrainService, weatherService, algorithm, -1);
  }

  public Simulator(
      TerrainService terrainService,
      WeatherService weatherService,
      AutomatonAlgorithm algorithm,
      Integer limitStepMilli) {
    this.terrainService = terrainService;
    this.weatherService = weatherService;
    this.algorithm = algorithm;
    this.limitStepMilli = limitStepMilli;
  }

  public Simulation createSimulation(LatLng startPoint) {
    return new Simulation(
        new Simulation.MarkedGrid(DEFAULT_GRID_SCALE, startPoint),
        new Simulation.Timeline(Instant.now(), DEFAULT_STEP_DURATION, DEFAULT_LIMIT_DURATION));
  }

  public boolean tryStartSimulation(Simulation simulation) {
    Coordinates startCoordinates = simulation.getGrid().getStartCoordinates();
    LatLng startPoint = simulation.getGrid().pointOf(startCoordinates);

    Cell.State initialState = new Cell.State(determineFuel(startPoint), INITIAL_HEAT);
    try {
      Cell.Factors factors = determineFactors(startPoint, simulation.getTimeline().getStartDate());

      Cell initialCell = new Cell(startCoordinates, initialState, factors);

      Simulation.Step initialStep = new Simulation.Step();
      if (!initialCell.isBurning()) {
        initialStep.markAsFinal();
      }
      initialStep.getCells().add(initialCell);
      simulation.getSteps().add(initialStep);
      return true;
    } catch (SimulatorException exception) {
      return false;
    }
  }

  public void progressSimulation(Simulation simulation, int endTick) {
    synchronized (simulation.getId()) {
      int limitTicks = simulation.getTimeline().getLimitTicks();
      while (!simulation.hasStep(endTick)
          && !simulation.getSteps().getLast().isFinal()
          && simulation.getSteps().size() <= limitTicks) {
        try {
          Instant startTime = Instant.now();
          Simulation.Step draftStep = createDraftStep(simulation);
          algorithm.refineDraftStep(draftStep, simulation);
          simulation.getSteps().add(draftStep);
          if (draftStep.getCells().stream().noneMatch(Cell::isBurning)) {
            draftStep.markAsFinal();
          }
          long elapsedMilli = Instant.now().toEpochMilli() - startTime.toEpochMilli();
          if (0 < limitStepMilli && limitStepMilli < elapsedMilli) {
            draftStep.markAsFinal();
          }
        } catch (SimulatorException exception) {
          simulation.getSteps().getLast().markAsFinal();
        }
      }
    }
  }

  private Simulation.Step createDraftStep(Simulation simulation) throws SimulatorException {
    Grid grid = simulation.getGrid();
    Simulation.Step draftStep = new Simulation.Step();
    Simulation.Step lastStep = simulation.getSteps().getLast();
    Duration period =
        simulation.getTimeline().getStepDuration().multipliedBy(simulation.getSteps().size());
    Instant date = simulation.getTimeline().getStartDate().plus(period);

    for (Cell cell : lastStep.getCells()) {
      Cell.Factors factors = determineFactors(grid.pointOf(cell.getCoordinates()), date);
      if (factors.equals(cell.getFactors())) {
        factors = cell.getFactors();
      }
      Cell.State cellState = cell.getState();
      Cell.State draftCellState =
          new Cell.State(
              cellState.getInitialFuel(),
              cellState.getHeat(),
              cellState.getCumulativeCombustionRate());
      Cell draftCell = new Cell(cell.getCoordinates(), draftCellState, factors);
      draftCell.setTwin(cell);
      cell.setTwin(draftCell);
      draftStep.getCells().add(draftCell);
    }

    for (Cell cell : lastStep.getCells()) {
      for (int offsetX = -1; offsetX <= 1; offsetX++) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
          if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) == null) {
            continue;
          }
          cell.getTwin()
              .setNeighbor(offsetX, offsetY, cell.getNeighbor(offsetX, offsetY).getTwin());
        }
      }
    }

    draftStep.getCells().forEach(cell -> cell.setTwin(null));

    Map<Coordinates, Cell> draftCellMap = new HashMap<>(draftStep.getCells().size());
    draftStep.getCells().forEach(cell -> draftCellMap.put(cell.getCoordinates(), cell));

    for (Cell previousCell : lastStep.getCells()) {
      Cell cell = previousCell.getTwin();
      if (!cell.isBurning()) {
        continue;
      }
      for (int offsetX = -1; offsetX <= 1; offsetX++) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
          if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
            continue;
          }
          Coordinates neighborCoordinates =
              grid.getNeighbor(cell.getCoordinates(), offsetX, offsetY);
          if (cell.getCoordinates().getY() == neighborCoordinates.getY() && offsetY != 0) {
            // Cell neighborhood through the poles is not expected.
            continue;
          }
          LatLng neighborPoint = grid.pointOf(neighborCoordinates);
          double fuel = determineFuel(neighborPoint);
          if (fuel < SIGNIFICANT_FUEL) {
            continue;
          }
          Cell.Factors factors = determineFactors(neighborPoint, date);
          if (factors.equals(cell.getFactors())) {
            factors = cell.getFactors();
          }
          Cell.State neighborState = new Cell.State(fuel, factors.getAirTemperature());
          Cell neighbor = new Cell(neighborCoordinates, neighborState, factors);

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
    }

    return draftStep;
  }

  private Cell.Factors determineFactors(LatLng point, Instant date) throws SimulatorException {
    Optional<Weather> weather = weatherService.getWeather(point, date);
    if (weather.isEmpty()) {
      throw new SimulatorException();
    }
    return new Cell.Factors(
        weather.get(), terrainService.getElevation(point), terrainService.getForestType(point));
  }

  private double determineFuel(LatLng point) {
    double fuel = terrainService.getFuel(point);
    if (fuel < SIGNIFICANT_FUEL) {
      fuel = 0;
    }
    return fuel;
  }

  private static final class SimulatorException extends Exception {}
}
