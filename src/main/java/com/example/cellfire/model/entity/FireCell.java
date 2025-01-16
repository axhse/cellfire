package com.example.cellfire.model.entity;

public class FireCell {
    private final double heat;
    private final double power;

    public FireCell(double heat, double power) {
        this.heat = heat;
        this.power = power;
    }

    public double getHeat() {
        return heat;
    }

    public double getPower() {
        return power;
    }
}