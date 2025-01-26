package com.example.cellfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public final class Cell {
    @JsonIgnore
    private final List<Cell> neighbours = new ArrayList<>();
    private final CellCoordinates coordinates;
    private final Fire fire;
    private final Environment environment;

    public Cell(CellCoordinates coordinates, Fire fire, Environment environment) {
        this.coordinates = coordinates;
        this.fire = fire;
        this.environment = environment;
    }

    public CellCoordinates getCoordinates() {
        return coordinates;
    }

    public Fire getFire() {
        return fire;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public List<Cell> getNeighbours() {
        return neighbours;
    }
}
