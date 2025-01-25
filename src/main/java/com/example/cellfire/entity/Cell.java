package com.example.cellfire.entity;

public final class Cell {
    private final int x;
    private final int y;
    private final Fire fire;
    private final Environment environment;

    public Cell(int x, int y, Fire fire, Environment environment) {
        this.x = x;
        this.y = y;
        this.fire = fire;
        this.environment = environment;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Fire getFire() {
        return fire;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
