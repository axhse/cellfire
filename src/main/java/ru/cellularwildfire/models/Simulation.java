package ru.cellularwildfire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Simulation {
  private final String id = UUID.randomUUID().toString();
  private final List<Step> steps = new ArrayList<>();
  private final MarkedGrid grid;
  private final Timeline timeline;
  private final Conditions conditions;
  private final String algorithm;

  public Simulation(MarkedGrid grid, Timeline timeline, Conditions conditions, String algorithm) {
    this.grid = grid;
    this.timeline = timeline;
    this.conditions = conditions;
    this.algorithm = algorithm;
  }

  public String getId() {
    return id;
  }

  public boolean hasStep(int step) {
    return step < steps.size();
  }

  public List<Step> getSteps() {
    return steps;
  }

  public MarkedGrid getGrid() {
    return grid;
  }

  public Timeline getTimeline() {
    return timeline;
  }

  public Conditions getConditions() {
    return conditions;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public boolean isBurning(Cell cell) {
    return conditions.getIgnitionTemperature() <= cell.getState().getHeat();
  }

  public static final class MarkedGrid extends Grid {
    private final Coordinates startCoordinates;

    public MarkedGrid(int scale, LatLng startPoint) {
      super(scale);
      startCoordinates = coordinatesOf(startPoint);
    }

    public Coordinates getStartCoordinates() {
      return startCoordinates;
    }
  }

  public static final class Timeline {
    @JsonIgnore private final Instant startDate;
    @JsonIgnore private final Duration stepDuration;
    private final int limitTicks;

    public Timeline(Instant startDate, Duration stepDuration, Duration limitDuration) {
      this.startDate = roundStartDate(startDate, stepDuration);
      this.stepDuration = stepDuration;
      this.limitTicks = (int) (limitDuration.toSeconds() / stepDuration.toSeconds());
    }

    private static Instant roundStartDate(Instant startDate, Duration stepDuration) {
      long duration = stepDuration.toSeconds();
      return Instant.ofEpochSecond(startDate.getEpochSecond() / duration * duration);
    }

    @JsonIgnore
    public Instant getStartDate() {
      return startDate;
    }

    public long getStartDateMs() {
      return startDate.toEpochMilli();
    }

    @JsonIgnore
    public Duration getStepDuration() {
      return stepDuration;
    }

    public long getStepDurationMs() {
      return stepDuration.toMillis();
    }

    public int getLimitTicks() {
      return limitTicks;
    }
  }

  public static final class Step {
    private final List<Cell> cells = new ArrayList<>();
    private boolean isFinal = false;

    public void markAsFinal() {
      isFinal = true;
    }

    public boolean isFinal() {
      return isFinal;
    }

    public List<Cell> getCells() {
      return cells;
    }
  }

  public static final class Conditions {
    private static final double IGNITION_TEMPERATURE = 500;

    private final double activationEnergy;

    public Conditions(double activationEnergy) {
      this.activationEnergy = activationEnergy;
    }

    public double getIgnitionTemperature() {
      return IGNITION_TEMPERATURE;
    }

    public double getActivationEnergy() {
      return activationEnergy;
    }
  }

  public static final class Algorithm {
    public static final String THERMAL = "thermal";
    public static final String PROBABILISTIC = "probabilistic";
  }
}
