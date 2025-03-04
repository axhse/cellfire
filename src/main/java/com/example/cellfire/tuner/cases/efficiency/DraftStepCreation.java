package com.example.cellfire.tuner.cases.efficiency;

import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.*;
import com.example.cellfire.tuner.experiment.TuneCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class DraftStepCreation extends TuneCase {
    private final CopyingAlgorithm copyingAlgorithm;

    public DraftStepCreation(CopyingAlgorithm copyingAlgorithm) {
        super();
        this.copyingAlgorithm = copyingAlgorithm;
    }

    @Override
    protected TuneCase.ModelScore score(ThermalAlgorithm algorithm) {
        Map<CopyingAlgorithm, Function<Simulation.Step, Simulation.Step>> creators = Map.of(
                CopyingAlgorithm.RANDOM_POINTER_NEIGHBOR_SEARCH,
                DraftStepCreation::randomPointerNeighborSearch,
                CopyingAlgorithm.RANDOM_POINTER_NEIGHBOR_HASHMAP,
                DraftStepCreation::randomPointerNeighborHashmap,
                CopyingAlgorithm.HASHMAP,
                DraftStepCreation::hashmap
        );
        Map<CopyingAlgorithm, Double> results = new HashMap<>();
        for (Map.Entry<CopyingAlgorithm, Function<Simulation.Step, Simulation.Step>> entry : creators.entrySet()) {
            Simulation.Step step = createInitialStep();
            long timeStart = System.nanoTime();
            step = entry.getValue().apply(step);
            for (int i = 0; i < 50; i++) {
                step = entry.getValue().apply(step);
            }
            long timeEnd = System.nanoTime();
            results.put(entry.getKey(), (double) timeEnd - timeStart);
        }
        double bestTime = results.values().stream().min(Double::compareTo).orElseThrow();
        results.keySet().forEach(key -> results.put(key, results.get(key) / bestTime));
        return ModelScore.success(1.0 / results.get(copyingAlgorithm));
    }

    private static Simulation.Step randomPointerNeighborSearch(Simulation.Step lastStep) {
        Simulation.Step draftStep = new Simulation.Step();
        Weather weather = new Weather(0, 0, 0, 0, 0);

        lastStep.getCells().forEach(cell -> {
            CellState lastCellState = cell.getState();
            CellState draftCellState = new CellState(lastCellState.getHeat(), lastCellState.getFuel(), true);
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

        lastStep.getCells().forEach(previousCell -> {
            Cell cell = previousCell.getTwin();
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    Coordinates neighborCoordinates = new Coordinates(
                            cell.getCoordinates().getX() + offsetX,
                            cell.getCoordinates().getY() + offsetY
                    );
                    CellState neighborState = new CellState(weather.getAirTemperature(), 1, false);
                    Cell neighbor = new Cell(neighborCoordinates, neighborState, weather);

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

    private static Simulation.Step randomPointerNeighborHashmap(Simulation.Step lastStep) {
        Simulation.Step draftStep = new Simulation.Step();
        Weather weather = new Weather(0, 0, 0, 0, 0);

        lastStep.getCells().forEach(cell -> {
            CellState lastCellState = cell.getState();
            CellState draftCellState = new CellState(lastCellState.getHeat(), lastCellState.getFuel(), true);
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

        draftCellMap.values().stream().toList().forEach(cell -> {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    Coordinates neighborCoordinates = new Coordinates(
                            cell.getCoordinates().getX() + offsetX,
                            cell.getCoordinates().getY() + offsetY
                    );
                    CellState neighborState = new CellState(weather.getAirTemperature(), 1, false);
                    Cell neighbor = new Cell(neighborCoordinates, neighborState, weather);

                    for (int dX = -1; dX <= 1; dX++) {
                        for (int dY = -1; dY <= 1; dY++) {
                            Coordinates otherCoordinates = new Coordinates(
                                    neighbor.getCoordinates().getX() + dX,
                                    neighbor.getCoordinates().getY() + dY
                            );
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

    private static Simulation.Step hashmap(Simulation.Step lastStep) {
        Simulation.Step draftStep = new Simulation.Step();
        Weather weather = new Weather(0, 0, 0, 0, 0);

        Map<Coordinates, Cell> draftCellMap = new HashMap<>(draftStep.getCells().size());

        lastStep.getCells().forEach(cell -> {
            CellState lastCellState = cell.getState();
            CellState draftCellState = new CellState(lastCellState.getHeat(), lastCellState.getFuel(), true);
            Cell draftCell = new Cell(cell.getCoordinates(), draftCellState, weather);
            draftCellMap.put(cell.getCoordinates(), draftCell);
        });
        draftStep.getCells().addAll(draftCellMap.values());

        draftCellMap.values().forEach(cell -> {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0) {
                        continue;
                    }
                    Coordinates otherCoordinates = new Coordinates(
                            cell.getCoordinates().getX() + offsetX,
                            cell.getCoordinates().getY() + offsetY
                    );
                    if (draftCellMap.containsKey(otherCoordinates)) {
                        cell.setNeighbor(offsetX, offsetY, draftCellMap.get(otherCoordinates));
                    }
                }
            }
        });

        draftCellMap.values().stream().toList().forEach(cell -> {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    if (offsetX == 0 && offsetY == 0 || cell.getNeighbor(offsetX, offsetY) != null) {
                        continue;
                    }
                    Coordinates neighborCoordinates = new Coordinates(
                            cell.getCoordinates().getX() + offsetX,
                            cell.getCoordinates().getY() + offsetY
                    );
                    CellState neighborState = new CellState(weather.getAirTemperature(), 1, false);
                    Cell neighbor = new Cell(neighborCoordinates, neighborState, weather);

                    for (int dX = -1; dX <= 1; dX++) {
                        for (int dY = -1; dY <= 1; dY++) {
                            Coordinates otherCoordinates = new Coordinates(
                                    neighbor.getCoordinates().getX() + dX,
                                    neighbor.getCoordinates().getY() + dY
                            );
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

    private static Simulation.Step createInitialStep() {
        Simulation.Step initialStep = new Simulation.Step();
        CellState cellState = new CellState(10000, 1, true);
        Weather weather = new Weather(0, 0, 0, 0, 0);
        Cell initialCell = new Cell(new Coordinates(123, 456), cellState, weather);
        initialStep.getCells().add(initialCell);
        return initialStep;
    }

    public enum CopyingAlgorithm {
        RANDOM_POINTER_NEIGHBOR_SEARCH,
        RANDOM_POINTER_NEIGHBOR_HASHMAP,
        HASHMAP,
    }
}
