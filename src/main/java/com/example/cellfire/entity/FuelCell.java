package com.example.cellfire.entity;

public final class FuelCell {
    private final double resource;
    private final double flammability;

    public FuelCell(double resource, double flammability) {
        this.resource = resource;
        this.flammability = flammability;
    }

    public double getResource() {
        return resource;
    }

    public double getFlammability() {
        return flammability;
    }
}