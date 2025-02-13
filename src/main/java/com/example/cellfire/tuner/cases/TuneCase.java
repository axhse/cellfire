package com.example.cellfire.tuner.cases;

import com.example.cellfire.algorithms.Algorithm;
import com.example.cellfire.algorithms.ProbabilisticAlgorithm;
import com.example.cellfire.algorithms.ThermalAlgorithm;
import com.example.cellfire.models.CellCoordinates;
import com.example.cellfire.models.Scenario;
import com.example.cellfire.services.ForecastService;
import com.example.cellfire.services.TerrainService;
import com.example.cellfire.services.WeatherService;

import java.time.Instant;

public abstract class TuneCase<TAlgorithm extends Algorithm> {
    protected final ForecastService forecastService;
    protected final Scenario scenario;
    protected final TAlgorithm algorithm;
    private final double weight;
    private final boolean isObligatory;
    protected double score = -2;

    public TuneCase(TAlgorithm algorithm, double weight, boolean isObligatory) {
        this.algorithm = algorithm;
        this.weight = weight;
        this.isObligatory = isObligatory;
        this.forecastService = this.createForecastService();
        this.scenario = this.createScenario();
    }

    public TuneCase(TAlgorithm algorithm, double weight) {
        this(algorithm, weight, true);
    }

    public TuneCase(TAlgorithm algorithm) {
        this(algorithm, 1);
    }

    public boolean isObligatory() {
        return this.isObligatory;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getWeightedScore() {
        return this.weight * this.score;
    }

    public double getScore() {
        return this.score;
    }

    public abstract void evaluate();

    protected abstract TerrainService createTerrainService();

    protected abstract WeatherService createWeatherService();

    protected CellCoordinates getStartCoordinates() {
        return new CellCoordinates(0, 0);
    }

    private Scenario createScenario() {
        String algorithm = this.algorithm instanceof ThermalAlgorithm
                ? Scenario.Algorithm.THERMAL : Scenario.Algorithm.PROBABILISTIC;
        // FIXME: round to step duration.
        Instant startTime = Instant.now();
        return forecastService.startScenario(algorithm, getStartCoordinates(), startTime);
    }

    private ForecastService createForecastService() {
        return new ForecastService(createTerrainService(), createWeatherService()
                , algorithm instanceof ThermalAlgorithm ? (ThermalAlgorithm)algorithm : null,
                algorithm instanceof ProbabilisticAlgorithm ? (ProbabilisticAlgorithm) algorithm : null);
    }
}
