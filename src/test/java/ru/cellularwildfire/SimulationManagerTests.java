package ru.cellularwildfire;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.models.Simulation.MarkedGrid;
import ru.cellularwildfire.models.Simulation.Timeline;
import ru.cellularwildfire.services.SimulationManager;

public final class SimulationManagerTests {
  private static Simulation createSimulation() {
    return new Simulation(
        new MarkedGrid(100, new LatLng(0, 0)),
        new Timeline(Instant.now(), Duration.ofHours(1), Duration.ofHours(1)));
  }

  @Test
  public void testAdditionToManager() {
    SimulationManager manager = new SimulationManager(10);
    Optional<Simulation> result;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Simulation simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    for (Simulation simulation : simulations) {
      result = manager.findSimulation(simulation.getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulation, result.get());
    }
  }

  @Test
  public void testRemovalFromManager() {
    SimulationManager manager = new SimulationManager(10);
    Optional<Simulation> result;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Simulation simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    manager.removeSimulation(simulations.get(0).getId());
    result = manager.findSimulation(simulations.get(0).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int i = 1; i < 10; i++) {
      Simulation simulation = simulations.get(i);

      result = manager.findSimulation(simulation.getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulation, result.get());

      manager.removeSimulation(simulation.getId());
      result = manager.findSimulation(simulation.getId());
      Assertions.assertTrue(result.isEmpty());
    }
  }

  @Test
  public void testManagerOverflow() {
    SimulationManager manager = new SimulationManager(10);
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    simulation = createSimulation();
    manager.addSimulation(simulation);

    result = manager.findSimulation(simulation.getId());
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(simulation, result.get());

    result = manager.findSimulation(simulations.get(0).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int index : new int[] {1, 9}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }

  @Test
  public void testManagerOverflowAccessed() {
    SimulationManager manager = new SimulationManager(10);
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    manager.findSimulation(simulations.get(0).getId());
    manager.findSimulation(simulations.get(1).getId());

    simulation = createSimulation();
    manager.addSimulation(simulation);

    result = manager.findSimulation(simulation.getId());
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(simulation, result.get());

    result = manager.findSimulation(simulations.get(2).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int index : new int[] {0, 1, 3, 9}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }

  @Test
  public void testManagerOverflowAllAccessed() {
    SimulationManager manager = new SimulationManager(10);
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    for (int i = 9; 0 <= i; i--) {
      manager.findSimulation(simulations.get(i).getId());
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    manager.findSimulation(simulations.get(9).getId());
    manager.findSimulation(simulations.get(8).getId());

    simulation = createSimulation();
    manager.addSimulation(simulation);

    result = manager.findSimulation(simulation.getId());
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(simulation, result.get());

    result = manager.findSimulation(simulations.get(7).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int index : new int[] {9, 8, 6}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }
}
