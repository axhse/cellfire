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
public class Simulator {
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

    public Scenario createScenario(String algorithm, CellCoordinates startCoordinates, Instant startDate) {
        return new Scenario(algorithm, startCoordinates, startDate, determineConditions(startCoordinates));
    }

    public void startScenario(Scenario scenario) {
        SimulationStep initialSimulationStep = new SimulationStep();
        scenario.getSimulation().getSteps().add(initialSimulationStep);
        float fuel = (float)terrainService.getFuel(scenario.getStartCoordinates());
        Fire fire = new Fire(ModelSettings.INITIAL_HEAT, fuel);
        FireFactors factors = determineFactors(scenario.getStartCoordinates(), scenario.getStartDate());
        Cell initialCell = new Cell(scenario.getStartCoordinates(), factors, fire);
        initialSimulationStep.getCells().add(initialCell);
    }

    public synchronized void simulate(Scenario scenario, int endStep) {
        while (!scenario.getSimulation().hasStep(endStep)) {
            simulateFurther(scenario);
        }
    }

    private void simulateFurther(Scenario scenario) {
        SimulationStep draftSimulationStep = new SimulationStep();
        SimulationStep lastSimulationStep = scenario.getSimulation().getSteps().getLast();
        int furtherStepNumber = scenario.getSimulation().getSteps().size();
        Instant date = scenario.getStartDate().plus(ModelSettings.STEP_DURATION.multipliedBy(furtherStepNumber));

        lastSimulationStep.getCells().forEach(cell -> {
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
            draftSimulationStep.getCells().add(draftCell);
        });

        lastSimulationStep.getCells().forEach(cell -> {
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

        draftSimulationStep.getCells().forEach(cell -> {
            cell.setTwin(null);
        });

        lastSimulationStep.getCells().forEach(previousCell -> {
            Cell cell = previousCell.getTwin();
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
                    draftSimulationStep.getCells().forEach(otherCell -> {
                        int distanceX = otherCell.getCoordinates().getX() - neighbor.getCoordinates().getX();
                        int distanceY = otherCell.getCoordinates().getY() - neighbor.getCoordinates().getY();
                        if (Math.abs(distanceX) <= 1 && Math.abs(distanceY) <= 1) {
                            neighbor.setNeighbor(distanceX, distanceY, otherCell);
                            otherCell.setNeighbor(-distanceX, -distanceY, neighbor);
                        }
                    });

                    draftSimulationStep.getCells().add(neighbor);
                }
            }
        });

        selectAlgorithm(scenario).refine(draftSimulationStep, scenario.getConditions());

        scenario.getSimulation().getSteps().add(draftSimulationStep);
    }

    // TODO: Remove.
    private Algorithm selectAlgorithm(Scenario scenario) {
        if (scenario.getAlgorithm().equals(Scenario.Algorithm.PROBABILISTIC)) {
            return new ProbabilisticAlgorithm();
        }
        return algorithm;
    }

    private ScenarioConditions determineConditions(CellCoordinates startCoordinates) {
        return new ScenarioConditions(
                terrainService.getIgnitionTemperature(startCoordinates),
                terrainService.getActivationEnergy(startCoordinates)
        );
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
