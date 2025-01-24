package com.example.cellfire.model.forecast;

import com.example.cellfire.entity.FireCell;
import com.example.cellfire.entity.FuelCell;

public final class FlameOutcome {
    private final FireCell fireCell;
    private final FuelCell fuelCell;

    public FlameOutcome(FireCell fireCell, FuelCell fuelCell) {
        this.fireCell = fireCell;
        this.fuelCell = fuelCell;
    }

    public FireCell getFireCell() {
        return fireCell;
    }

    public FuelCell getFuelCell() {
        return fuelCell;
    }
}
