package com.example.cellfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Iterator;

public final class Cell {
    @JsonIgnore
    private final Cell[] vicinity = new Cell[9];
    private final Coordinates coordinates;
    private final CellState state;
    private final CellFactors factors;

    public Cell(Coordinates coordinates, CellState state, CellFactors factors) {
        this.coordinates = coordinates;
        this.state = state;
        this.factors = factors;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public CellState getState() {
        return state;
    }

    public CellFactors getFactors() {
        return factors;
    }

    @JsonIgnore
    public Cell getTwin() {
        return vicinity[4];
    }

    public void setTwin(Cell cell) {
        vicinity[4] = cell;
    }

    @JsonIgnore
    public Cell getNeighbor(int offsetX, int offsetY) {
        return vicinity[3 * (offsetX + 1) + offsetY + 1];
    }

    public void setNeighbor(int offsetX, int offsetY, Cell cell) {
        vicinity[3 * (offsetX + 1) + offsetY + 1] = cell;
    }

    public Iterable<Cell> iterateNeighbors() {
        return () -> new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                while (index < 9 && (index == 4 || vicinity[index] == null)) {
                    index++;
                }
                return index < 9;
            }

            @Override
            public Cell next() {
                return vicinity[index++];
            }
        };
    }
}
