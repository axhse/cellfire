package com.example.cellfire.models;

public final class Fire {
    private final float initialResource;
    private float resource;
    private float heat;

    public Fire(float heat, float initialResource, float resource) {
        this.initialResource = initialResource;
        this.resource = resource;
        this.heat = heat;
    }

    public Fire(float heat, float initialResource) {
        this(heat, initialResource, initialResource);
    }

    public float getInitialResource() {
        return initialResource;
    }

    public float getResource() {
        return resource;
    }

    public void setResource(float resource) {
        this.resource = resource;
    }

    public float getHeat() {
        return heat;
    }

    public void setHeat(float heat) {
        this.heat = heat;
    }
}