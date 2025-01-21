package com.example.cellfire.entity;

import java.util.ArrayList;
import java.util.List;

public final class InstantForecast {
    private final List<Cell> cells = new ArrayList<>();

    public List<Cell> getCells() {
        return cells;
    }
}
