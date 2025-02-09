package com.example.cellfire.models;

public final class Fire {
    private final boolean isDamaged;
    private float fuel;
    private float heat;

    public Fire(float heat, float fuel, boolean isDamaged) {
        this.fuel = fuel;
        this.heat = heat;
        this.isDamaged = isDamaged;
    }

    public Fire(float heat, float fuel) {
        this(heat, fuel, false);
    }

    public boolean getIsDamaged() {
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
