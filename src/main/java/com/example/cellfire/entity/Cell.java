package com.example.cellfire.entity;

public final class Cell {
    private final int x;
    private final int y;
    private final FireCell fireCell;
    private final FuelCell fuelCell;
    private final WeatherCell weatherCell;

    public Cell(int x, int y, FireCell fireCell, FuelCell fuelCell, WeatherCell weatherCell) {
        this.x = x;
        this.y = y;
        this.fireCell = fireCell;
        this.fuelCell = fuelCell;
        this.weatherCell = weatherCell;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public FireCell getFireCell() {
        return fireCell;
    }

    public FuelCell getFuelCell() {
        return fuelCell;
    }

    public WeatherCell getWeatherCell() {
        return weatherCell;
    }
}
