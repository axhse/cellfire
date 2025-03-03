package com.example.cellfire;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.SimulationManager;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public final class SimulationTest {
    @Test
    public void testAdditionToManager() {
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
    public void testRemovalFromManager() {
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
    public void testManagerOverflow() {
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
    public void testManagerOverflowAccessed() {
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
    public void testManagerOverflowAllAccessed() {
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

    @Test
    public void testSimulationSteps() {
        Simulator simulator = createSimulator();
        Simulation simulation = createSimulation();

        Assertions.assertEquals(0, simulation.getSteps().size());

        simulator.startSimulation(simulation);
        Assertions.assertEquals(1, simulation.getSteps().size());

        simulator.progressSimulation(simulation, 4);
        Assertions.assertEquals(5, simulation.getSteps().size());

        simulator.progressSimulation(simulation, 6);
        Assertions.assertEquals(7, simulation.getSteps().size());
    }

    @Test
    public void testSimulationStepCells() {
        Simulator simulator = createSimulator();
        Simulation simulation = createSimulation();

        simulator.startSimulation(simulation);
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
                    Coordinates expectedCoordinates = simulation.getGrid().getNeighbor(cell.getCoordinates(), dX, dY);
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
        Simulator simulator = createSimulator();
        Simulation simulation = simulator.createDefaultSimulation(
                new LatLng(-90 + 0.00001, 0),
                Simulation.Algorithm.THERMAL
        );

        simulator.startSimulation(simulation);
        simulator.progressSimulation(simulation, 1);

        Assertions.assertEquals(6, simulation.getSteps().get(1).getCells().size());

        for (Cell cell : simulation.getSteps().get(1).getCells()) {
            Assertions.assertTrue(-1 <= cell.getCoordinates().getX() && cell.getCoordinates().getX() <= 1);
        }
    }

    private static Simulator createSimulator() {
        return new Simulator(
                new UniformTerrainService((byte) 1, 10, 0),
                new UniformWeatherService(10000, 0, 0, 0),
                new ThermalAlgorithm()
        );
    }

    private static Simulation createSimulation() {
        return createSimulator().createDefaultSimulation(
                new LatLng(0, -180 + 0.00001),
                Simulation.Algorithm.THERMAL
        );
    }
}
