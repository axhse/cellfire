package com.example.cellfire;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.data.ForestTypeConditions;
import com.example.cellfire.models.Cell;
import com.example.cellfire.models.Coordinates;
import com.example.cellfire.models.Simulation;
import com.example.cellfire.services.Simulator;
import com.example.cellfire.tuner.services.UniformTerrainService;
import com.example.cellfire.tuner.services.UniformWeatherService;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public final class SimulatorTests {
    @Test
    public void testSimulationSteps() {
        Simulator simulator = createSimulator(10);
        Simulation simulation = createSimulation(simulator);

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
        Simulator simulator = createSimulator(1000000000);
        Simulation simulation = createSimulation(simulator, new LatLng(0, -180 + 0.00001));

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
        Simulator simulator = createSimulator(1000);
        Simulation simulation = createSimulation(simulator, new LatLng(-90 + 0.00001, 0));

        simulator.startSimulation(simulation);
        simulator.progressSimulation(simulation, 1);

        Assertions.assertEquals(6, simulation.getSteps().get(1).getCells().size());

        for (Cell cell : simulation.getSteps().get(1).getCells()) {
            Assertions.assertTrue(-1 <= cell.getCoordinates().getX() && cell.getCoordinates().getX() <= 1);
        }
    }

    @Test
    public void testSimulationWithoutFuel() {
        Simulator simulator = createSimulator(0);
        Simulation simulation = createSimulation(simulator);

        simulator.startSimulation(simulation);
        simulator.progressSimulation(simulation, 3);
        Assertions.assertEquals(1, simulation.getSteps().get(3).getCells().size());
    }

    @Test
    public void testSimulationStepCount() {
        Simulator simulator = createSimulator(0);

        Simulation simulation1 = createSimulation(Duration.ofHours(4), Duration.ofDays(3));
        simulator.startSimulation(simulation1);
        simulator.progressSimulation(simulation1, 3);
        Assertions.assertEquals(1 + 3, simulation1.getSteps().size());
        simulator.progressSimulation(simulation1, 100000);
        Assertions.assertEquals(1 + 24 / 4 * 3, simulation1.getSteps().size());

        Duration limitDuration = Duration.ofDays(3).plusMinutes(3 * 60 + 50);
        Simulation simulation2 = createSimulation(Duration.ofHours(4), limitDuration);
        simulator.startSimulation(simulation2);
        simulator.progressSimulation(simulation2, 100000);
        Assertions.assertEquals(1 + 24 / 4 * 3, simulation2.getSteps().size());
    }

    private static Simulation createSimulation(Duration stepDuration, Duration limitDuration) {
        return new Simulation(
                new Simulation.MarkedGrid(1, new LatLng(0, 0)),
                new Simulation.Timeline(Instant.now(), stepDuration, limitDuration),
                new Simulation.Conditions(100000),
                Simulation.Algorithm.THERMAL
        );
    }

    private static Simulation createSimulation(Simulator simulator) {
        return simulator.createSimulation(new LatLng(0, 0), Simulation.Algorithm.THERMAL);
    }

    private static Simulation createSimulation(Simulator simulator, LatLng startPoint) {
        return simulator.createSimulation(startPoint, Simulation.Algorithm.THERMAL);
    }

    private static Simulator createSimulator(double fuel) {
        return new Simulator(
                new UniformTerrainService(ForestTypeConditions.ForestType.MIXED, fuel, 0),
                new UniformWeatherService(200, 0, 0, 0),
                new ThermalAlgorithm()
        );
    }
}
