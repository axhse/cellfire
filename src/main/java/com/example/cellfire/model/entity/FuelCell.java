package com.example.cellfire.model.entity;

public class FuelCell {
    private final double flammability;
    private final double combustibility;
    private final double capacity;

    public FuelCell(double flammability, double combustibility, double capacity) {
        this.flammability = flammability;
        this.combustibility = combustibility;
        this.capacity = capacity;
    }

    public double getFlammability() {
        return flammability;
    }

    public double getCombustibility() {
        return combustibility;
    }

    public double getCapacity() {
        return capacity;
    }
}