package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.Cell;
import com.example.cellfire.entity.FireCell;
import com.example.cellfire.entity.FuelCell;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class Algorithm {
    public FlameOutcome flame(Cell cell, List<Cell> influence) {
        double heat = cell.getFireCell().getHeat();
        double resource = cell.getFuelCell().getResource();
        double flammability = cell.getFuelCell().getFlammability();
        return new FlameOutcome(new FireCell(heat), new FuelCell(resource, flammability));
    }
}
