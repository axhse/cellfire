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

public abstract class TuneCase {
    protected final ForecastService forecastService;
    protected final Algorithm algorithm;
    private final double weight;
    private final boolean isObligatory;

    public TuneCase(Algorithm algorithm, double weight, boolean isObligatory) {
        this.algorithm = algorithm;
        this.weight = weight;
        this.isObligatory = isObligatory;
        this.forecastService = this.createForecastService();
    }

    public TuneCase(Algorithm algorithm, double weight) {
        this(algorithm, weight, true);
    }

    public TuneCase(Algorithm algorithm) {
        this(algorithm, 1);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public final double evaluate() {
        double score = evaluate(createScenario());
        score = score < 0 ? (this.isObligatory ? -1 : 0) : Math.min(1, score);
        return score * this.weight;
    }

    protected abstract double evaluate(Scenario scenario);

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
