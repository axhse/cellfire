package com.example.cellfire.model.entity;

public class WeatherCell {
    private final double temperature;
    private final double humidity;
    private final double windX;
    private final double windY;

    public WeatherCell(double temperature, double humidity, double windX, double windY) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windX = windX;
        this.windY = windY;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindX() {
        return windX;
    }

    public double getWindY() {
        return windY;
    }
}
