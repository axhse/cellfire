package com.example.cellfire.entity;

public final class Cell {
    private final int x;
    private final int y;
    private final FuelCell fuelCell;
    private final WeatherCell weatherCell;
    private final FireCell fireCell;

    public Cell(int x, int y, FuelCell fuelCell, WeatherCell weatherCell, FireCell fireCell) {
        this.x = x;
        this.y = y;
        this.fuelCell = fuelCell;
        this.weatherCell = weatherCell;
        this.fireCell = fireCell;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public FuelCell getFuelCell() {
        return fuelCell;
    }

    public WeatherCell getWeatherCell() {
        return weatherCell;
    }

    public FireCell getFireCell() {
        return fireCell;
    }
}
