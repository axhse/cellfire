package com.example.cellfire.entity;

import java.util.ArrayList;
import java.util.List;

public final class InstantFireForecast {
    private final List<Cell> cells = new ArrayList<>();

    public List<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        cells.add(cell);
    }
}
