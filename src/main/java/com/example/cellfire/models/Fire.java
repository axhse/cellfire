package com.example.cellfire.models;

public final class Fire {
    private final double heat;
    private final double resource;

    public Fire(double heat, double resource) {
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