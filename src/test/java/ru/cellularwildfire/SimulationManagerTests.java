package ru.cellularwildfire;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.cellularwildfire.algorithms.ThermalAlgorithm;
import ru.cellularwildfire.data.ForestTypeConditions;
import ru.cellularwildfire.models.LatLng;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.SimulationManager;
import ru.cellularwildfire.services.Simulator;
import ru.cellularwildfire.tuner.services.UniformTerrainService;
import ru.cellularwildfire.tuner.services.UniformWeatherService;

public final class SimulationManagerTests {
  private static Simulation createSimulation(Duration stepDuration, Duration limitDuration) {
    return new Simulation(
        new Simulation.MarkedGrid(1, new LatLng(0, 0)),
        new Simulation.Timeline(Instant.now(), stepDuration, limitDuration),
        new Simulation.Conditions(100000),
        Simulation.Algorithm.THERMAL);
  }

  private static Simulation createSimulation() {
    Simulator simulator =
        new Simulator(
            new UniformTerrainService(ForestTypeConditions.ForestType.MIXED, 0, 0),
            new UniformWeatherService(200, 0, 0, 0),
            new ThermalAlgorithm());

    return simulator.createSimulation(new LatLng(0, 0), Simulation.Algorithm.THERMAL);
  }

  @Test
  public void testAdditionToManager() {
    SimulationManager manager = new SimulationManager();
    Optional<Simulation> result;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
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
    SimulationManager manager = new SimulationManager();
    Optional<Simulation> result;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Simulation simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    manager.removeSimulation(simulations.get(0).getId());
    result = manager.findSimulation(simulations.get(0).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int i = 1; i < 20; i++) {
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
    SimulationManager manager = new SimulationManager();
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
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

    for (int index : new int[] {1, 19}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }

  @Test
  public void testManagerOverflowAccessed() {
    SimulationManager manager = new SimulationManager();
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
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

    for (int index : new int[] {0, 1, 3, 19}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }

  @Test
  public void testManagerOverflowAllAccessed() {
    SimulationManager manager = new SimulationManager();
    Optional<Simulation> result;
    Simulation simulation;

    List<Simulation> simulations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      simulation = createSimulation();
      simulations.add(simulation);
      manager.addSimulation(simulation);
    }

    for (int i = 19; 0 <= i; i--) {
      manager.findSimulation(simulations.get(i).getId());
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    manager.findSimulation(simulations.get(18).getId());
    manager.findSimulation(simulations.get(19).getId());

    simulation = createSimulation();
    manager.addSimulation(simulation);

    result = manager.findSimulation(simulation.getId());
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(simulation, result.get());

    result = manager.findSimulation(simulations.get(17).getId());
    Assertions.assertTrue(result.isEmpty());

    for (int index : new int[] {16, 18, 19}) {
      result = manager.findSimulation(simulations.get(index).getId());
      Assertions.assertTrue(result.isPresent());
      Assertions.assertEquals(simulations.get(index), result.get());
    }
  }
}
