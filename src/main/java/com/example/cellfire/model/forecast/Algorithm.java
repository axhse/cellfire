package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.FireCell;

import java.util.List;
import java.util.Random;

public final class Algorithm {
    private final Random random = new Random();

    public FireCell flame(Cell cell, List<Cell> influence) {
        double heat = cell.getFireCell().getHeat() * random.nextDouble() * 1.5;
        double capacity = cell.getFireCell().getHeat() * random.nextDouble();
        return new FireCell(heat, capacity);
    }
}
