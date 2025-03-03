package com.example.cellfire;

import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Grid;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.SimulationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SimulationManagerTest {
    @Test
    void testSimulationAddition() {
        SimulationManager manager = new SimulationManager();

        List<Simulation> simulations = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Simulation simulation = createSimulation();
            simulations.add(simulation);
            manager.addSimulation(simulation);
        }

        for (Simulation simulation : simulations) {
            Assertions.assertEquals(simulation, manager.findSimulation(simulation.getId()));
        }
    }

    @Test
    void testSimulationRemoval() {
        SimulationManager manager = new SimulationManager();

        List<Simulation> simulations = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Simulation simulation = createSimulation();
            simulations.add(simulation);
            manager.addSimulation(simulation);
        }

        manager.removeSimulation(simulations.get(0).getId());
        Assertions.assertNull(manager.findSimulation(simulations.get(0).getId()));

        for (int i = 1; i < 20; i++) {
            Simulation simulation = simulations.get(i);
            Assertions.assertEquals(simulation, manager.findSimulation(simulation.getId()));

            manager.removeSimulation(simulation.getId());
            Assertions.assertNull(manager.findSimulation(simulation.getId()));
        }
    }

    @Test
    void testManagerOverflow() {
        SimulationManager manager = new SimulationManager();
        Simulation simulation;

        List<Simulation> simulations = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            simulation = createSimulation();
            simulations.add(simulation);
            manager.addSimulation(simulation);
        }

        simulation = createSimulation();
        manager.addSimulation(simulation);

        Assertions.assertNull(manager.findSimulation(simulations.get(0).getId()));
        Assertions.assertEquals(simulations.get(1), manager.findSimulation(simulations.get(1).getId()));
        Assertions.assertEquals(simulations.get(19), manager.findSimulation(simulations.get(19).getId()));
        Assertions.assertEquals(simulation, manager.findSimulation(simulation.getId()));
    }

    @Test
    void testManagerOverflowAccessed() {
        SimulationManager manager = new SimulationManager();
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

        Assertions.assertNull(manager.findSimulation(simulations.get(2).getId()));
        Assertions.assertEquals(simulations.get(0), manager.findSimulation(simulations.get(0).getId()));
        Assertions.assertEquals(simulations.get(1), manager.findSimulation(simulations.get(1).getId()));
        Assertions.assertEquals(simulations.get(3), manager.findSimulation(simulations.get(3).getId()));
        Assertions.assertEquals(simulations.get(19), manager.findSimulation(simulations.get(19).getId()));
        Assertions.assertEquals(simulation, manager.findSimulation(simulation.getId()));
    }

    @Test
    void testManagerOverflowAllAccessed() {
        SimulationManager manager = new SimulationManager();
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

        Assertions.assertNull(manager.findSimulation(simulations.get(17).getId()));
        Assertions.assertEquals(simulations.get(16), manager.findSimulation(simulations.get(16).getId()));
        Assertions.assertEquals(simulations.get(18), manager.findSimulation(simulations.get(18).getId()));
        Assertions.assertEquals(simulations.get(19), manager.findSimulation(simulations.get(19).getId()));
        Assertions.assertEquals(simulation, manager.findSimulation(simulation.getId()));
    }

    private static Simulation createSimulation() {
        return new Simulation(new Grid(0), new Coordinates(0, 0), Duration.ofDays(1),
                Duration.ofDays(1), Instant.now(), new Simulation.Conditions(0, 0),
                Simulation.Algorithm.THERMAL);
    }
}
