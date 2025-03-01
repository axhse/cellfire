package com.example.cellfire.models;

public final class CellState {
    private final boolean isDamaged;
    private float fuel;
    private float heat;

    public CellState(float heat, float fuel, boolean isDamaged) {
        this.fuel = fuel;
        this.heat = heat;
        this.isDamaged = isDamaged;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public float getFuel() {
        return fuel;
    }

    public void setFuel(float fuel) {
        this.fuel = fuel;
    }

    public float getHeat() {
        return heat;
    }

    public void setHeat(float heat) {
        this.heat = heat;
    }
}
