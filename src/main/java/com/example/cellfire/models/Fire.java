package com.example.cellfire.models;

public final class Fire {
    private final double initialResource;
    private double resource;
    private double heat;

    public Fire(double heat, double initialResource, double resource) {
        this.initialResource = initialResource;
        this.resource = resource;
        this.heat = heat;
    }

    public Fire(double heat, double initialResource) {
        this(heat, initialResource, initialResource);
    }

    public double getInitialResource() {
        return initialResource;
    }

    public double getResource() {
        return resource;
    }

    public void setResource(double resource) {
        this.resource = resource;
    }

    public double getHeat() {
        return heat;
    }

    public void setHeat(double heat) {
        this.heat = heat;
    }
}