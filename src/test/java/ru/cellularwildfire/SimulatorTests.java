package ru.cellularwildfire;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.data.ForestTypeFactors;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.AutomatonAlgorithm;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.services.UniformTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public final class SimulatorTests {
  private static Simulation createSimulation(Simulator simulator) {
    return simulator.createSimulation(new LatLng(0, 0));
  }

  private static Simulation createSimulation(Simulator simulator, LatLng startPoint) {
    return simulator.createSimulation(startPoint);
  }

  private static Simulation createSimulation(Duration stepDuration, Duration limitDuration) {
    return new Simulation(
        new Simulation.MarkedGrid(200, new LatLng(0, 0)),
        new Simulation.Timeline(Instant.now(), stepDuration, limitDuration));
  }

  private static Simulator createSimulator(double fuel) {
    return new Simulator(
        new UniformTerrainService(ForestTypeFactors.ForestType.MIXED, fuel, 0),
        new UniformWeatherService(200, 0, 0, 0),
        new AutomatonAlgorithm());
  }

  @Test
  public void testSimulationSteps() {
    Simulator simulator = createSimulator(1_000_000_000);
    Simulation simulation = createSimulation(simulator);

    Assertions.assertEquals(0, simulation.getSteps().size());

    simulator.tryStartSimulation(simulation);
    Assertions.assertEquals(1, simulation.getSteps().size());

    simulator.progressSimulation(simulation, 4);
    Assertions.assertEquals(5, simulation.getSteps().size());

    simulator.progressSimulation(simulation, 6);
    Assertions.assertEquals(7, simulation.getSteps().size());
  }

  @Test
  public void testSimulationStepCells() {
    Simulator simulator = createSimulator(1_000_000_000);
    Simulation simulation = createSimulation(simulator, new LatLng(0, -180 + 0.00001));

    simulator.tryStartSimulation(simulation);
    simulator.progressSimulation(simulation, 2);

    Assertions.assertEquals(1, simulation.getSteps().get(0).getCells().size());
    Assertions.assertEquals(9, simulation.getSteps().get(1).getCells().size());
    Assertions.assertEquals(25, simulation.getSteps().get(2).getCells().size());

    for (Cell cell : simulation.getSteps().get(2).getCells()) {
      for (int dX = -1; dX <= 1; dX++) {
        for (int dY = -1; dY <= 1; dY++) {
          if (dX == 0 && dY == 0 || cell.getNeighbor(dX, dY) == null) {
            continue;
          }
          Coordinates expectedCoordinates =
              simulation.getGrid().getNeighbor(cell.getCoordinates(), dX, dY);
          Cell neighbor = cell.getNeighbor(dX, dY);

          Assertions.assertNotEquals(cell, neighbor);
          Assertions.assertNotEquals(cell.getCoordinates(), neighbor.getCoordinates());
          Assertions.assertEquals(expectedCoordinates, neighbor.getCoordinates());

          Cell twin = neighbor.getNeighbor(-dX, -dY);
          Assertions.assertEquals(cell, twin);
        }
      }
    }
  }

  @Test
  public void testPolarNeighboringCells() {
    Simulator simulator = createSimulator(1_000_000_000);
    Simulation simulation = createSimulation(simulator, new LatLng(-90 + 0.00001, 0));

    simulator.tryStartSimulation(simulation);
    simulator.progressSimulation(simulation, 1);

    Assertions.assertEquals(6, simulation.getSteps().get(1).getCells().size());

    for (Cell cell : simulation.getSteps().get(1).getCells()) {
      Assertions.assertTrue(-1 <= cell.getCoordinates().getX());
      Assertions.assertTrue(cell.getCoordinates().getX() <= 1);
    }
  }

  @Test
  public void testSimulationWithoutFuel() {
    Simulator simulator = createSimulator(0);
    Simulation simulation = createSimulation(simulator);

    simulator.tryStartSimulation(simulation);
    simulator.progressSimulation(simulation, 10);
    Assertions.assertEquals(1, simulation.getSteps().size());
  }

  @Test
  public void testSimulationStepCount() {
    Simulator simulator = createSimulator(1_000_000_000);

    Simulation simulation1 = createSimulation(Duration.ofMinutes(30), Duration.ofHours(4));
    simulator.tryStartSimulation(simulation1);
    simulator.progressSimulation(simulation1, 3);
    Assertions.assertEquals(1 + 3, simulation1.getSteps().size());
    simulator.progressSimulation(simulation1, 100000);
    Assertions.assertEquals(1 + 2 * 4, simulation1.getSteps().size());

    Duration limitDuration = Duration.ofHours(4).plusMinutes(29);
    Simulation simulation2 = createSimulation(Duration.ofMinutes(30), limitDuration);
    simulator.tryStartSimulation(simulation2);
    simulator.progressSimulation(simulation2, 100000);
    Assertions.assertEquals(1 + 2 * 4, simulation2.getSteps().size());
  }
}
