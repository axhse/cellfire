package com.example.cellfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Cell {
    @JsonIgnore
    private final Cell[] vicinity = new Cell[9];
    private final CellCoordinates coordinates;
    private final Environment environment;
    private final Fire fire;
    public boolean isArtificial = false;

    public Cell(CellCoordinates coordinates, Environment environment, Fire fire) {
        if (coordinates == null) {
            var x = 0;
        }
        this.coordinates = coordinates;
        this.environment = environment;
        this.fire = fire;
    }

    public Cell(CellCoordinates coordinates, Environment environment, Fire fire, boolean isArtificial) {
        if (coordinates == null) {
            var x = 0;
        }
        this.coordinates = coordinates;
        this.environment = environment;
        this.fire = fire;
        this.isArtificial = isArtificial;
    }

    public CellCoordinates getCoordinates() {
        return coordinates;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Fire getFire() {
        return fire;
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
        var index = 3 * (offsetX + 1) + offsetY + 1;
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
