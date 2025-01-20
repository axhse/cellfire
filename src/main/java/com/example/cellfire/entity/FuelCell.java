package com.example.cellfire.entity;

public final class FuelCell {
    private final double flammability;
    private final double combustibility;

    public FuelCell(double flammability, double combustibility) {
        this.flammability = flammability;
        this.combustibility = combustibility;
    }

    public double getFlammability() {
        return flammability;
    }

    public double getCombustibility() {
        return combustibility;
    }
}