package com.example.cellfire.entity;

public final class FireCell {
    private final double heat;
    private final double resource;

    public FireCell(double heat, double resource) {
        this.heat = heat;
        this.resource = resource;
    }

    public double getHeat() {
        return heat;
    }

    public double getResource() {
        return resource;
    }
}