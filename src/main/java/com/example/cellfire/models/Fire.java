package com.example.cellfire.models;

public final class Fire {
    private final float initialFuel;
    private float fuel;
    private float heat;

    public Fire(float heat, float initialFuel, float fuel) {
        this.initialFuel = initialFuel;
        this.fuel = fuel;
        this.heat = heat;
    }

    public Fire(float heat, float initialFuel) {
        this(heat, initialFuel, initialFuel);
    }

    public float getInitialFuel() {
        return initialFuel;
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