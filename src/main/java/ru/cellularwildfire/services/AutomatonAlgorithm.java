package ru.cellularwildfire.services;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.cellularwildfire.data.ForestTypeFactors;
import ru.cellularwildfire.models.Cell;
import ru.cellularwildfire.models.Coordinates;
import ru.cellularwildfire.models.Grid;
import ru.cellularwildfire.models.Simulation;

@Service
public final class AutomatonAlgorithm {
  public static final double DEFAULT_COMBUSTION_INTENSITY = 47_000_000;
  public static final double DEFAULT_ENERGY_EMISSION = 19500;
  public static final double DEFAULT_PROPAGATION_INTENSITY = 0.43;
  public static final double DEFAULT_CONVECTION_INTENSITY = 0.3;
  public static final double DEFAULT_RADIATION_INTENSITY = 4 * Math.pow(10, -11);

  /** 3.5 in some research model. */
  public static final double DEFAULT_HUMIDITY_EFFECT = 5.7;

  public static final double DEFAULT_SLOPE_EFFECT = 3;

  /** 0.13 in some research model. */
  public static final double DEFAULT_WIND_EFFECT = 0.13;

  private static final double UNIVERSAL_GAS_CONSTANT = 8.3;
  private static final double CELSIUS_ZERO_TEMPERATURE = 273;

  private static final double HEAT_LIMIT = 2000;
  private static final double HEAT_CHANGE_LIMIT = 0.15;

  private final double combustionIntensity;
  private final double energyEmission;
  private final double propagationIntensity;
  private final double convectionIntensity;
  private final double radiationIntensity;
  private final double humidityEffect;
  private final double slopeEffect;
  private final double windEffect;

  public AutomatonAlgorithm(
      double combustionIntensity,
      double energyEmission,
      double propagationIntensity,
      double convectionIntensity,
      double radiationIntensity,
      double humidityEffect,
      double slopeEffect,
      double windEffect) {
    this.combustionIntensity = combustionIntensity;
    this.energyEmission = energyEmission;
    this.humidityEffect = humidityEffect;
    this.slopeEffect = slopeEffect;
    this.windEffect = windEffect;
    this.propagationIntensity = propagationIntensity;
    this.convectionIntensity = convectionIntensity;
    this.radiationIntensity = radiationIntensity;
  }

  public AutomatonAlgorithm(double... parameters) {
    this(
        parameters[0],
        parameters[1],
        parameters[2],
        parameters[3],
        parameters[4],
        parameters[5],
        parameters[6],
        parameters[7]);
  }

  public AutomatonAlgorithm() {
    this(
        DEFAULT_COMBUSTION_INTENSITY,
        DEFAULT_ENERGY_EMISSION,
        DEFAULT_PROPAGATION_INTENSITY,
        DEFAULT_CONVECTION_INTENSITY,
        DEFAULT_RADIATION_INTENSITY,
        DEFAULT_HUMIDITY_EFFECT,
        DEFAULT_SLOPE_EFFECT,
        DEFAULT_WIND_EFFECT);
  }

  private static double saturate(double linearValue) {
    return 2 / (1 + Math.exp(-linearValue)) - 1;
  }

  private static double calculateLinearValue(double saturatedValue) {
    return -Math.log((1 - saturatedValue) / (saturatedValue + 1));
  }

  private static double estimateDistance(Grid grid, Coordinates base, Coordinates neighbor) {
    double localCos = Math.cos(Math.toRadians(grid.pointOf(base).lat));
    // Cells neighboring through the poles are not expected.
    double distanceX = Math.abs(base.getX() - neighbor.getX()) * localCos;
    double distanceY = Math.abs(base.getY() - neighbor.getY());
    return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
  }

  private static double toKelvin(double celsiusTemperature) {
    return celsiusTemperature + CELSIUS_ZERO_TEMPERATURE;
  }

  private static double toCelsius(double kelvinTemperature) {
    return kelvinTemperature - CELSIUS_ZERO_TEMPERATURE;
  }

  private static void setEmittedEnergy(Cell cell, double energy) {
    cell.setTwin(new Cell(null, new Cell.State(energy, 0), null));
  }

  private static double getEmittedEnergy(Cell cell) {
    return cell.getTwin().getState().getHeat();
  }

  public void refineDraftStep(Simulation.Step draftStep, Simulation simulation) {
    List<Cell> burningCells = draftStep.getCells().stream().filter(Cell::isBurning).toList();
    burningCells.forEach(this::burnFuel);
    burningCells.forEach((cell) -> transferEnergy(cell, simulation.getGrid()));
    draftStep.getCells().forEach(this::regulateHeat);
  }

  private void burnFuel(Cell cell) {
    double burnedVolume = calculateBurnedVolume(cell);
    cell.getState().setFuel(cell.getState().getFuel() - burnedVolume);
    setEmittedEnergy(cell, energyEmission * burnedVolume);
  }

  private void transferEnergy(Cell cell, Grid grid) {
    double[] proximity = new double[9];
    proximity[8] = 1;
    int neighborIndex = 0;
    for (Cell neighbor : cell.iterateNeighbors()) {
      double distance = estimateDistance(grid, cell.getCoordinates(), neighbor.getCoordinates());
      double environmentalEffect = calculateEnvironmentalEffect(cell, neighbor, grid);
      proximity[neighborIndex++] = propagationIntensity * environmentalEffect / distance;
    }
    double totalProximity = Arrays.stream(proximity).sum();

    double emittedEnergy = getEmittedEnergy(cell);
    double heat = cell.getState().getHeat() + emittedEnergy * proximity[8] / totalProximity;
    cell.getState().setHeat(heat);
    neighborIndex = 0;
    for (Cell neighbor : cell.iterateNeighbors()) {
      heat =
          neighbor.getState().getHeat() + emittedEnergy * proximity[neighborIndex] / totalProximity;
      neighbor.getState().setHeat(heat);
      neighborIndex++;
    }
  }

  private void regulateHeat(Cell cell) {
    double heat = toKelvin(Math.min(cell.getState().getHeat(), HEAT_LIMIT));
    double airTemperature = toKelvin(cell.getFactors().getAirTemperature());
    double phase = 0;
    while (phase < 0.999) {
      double convectionRate = -convectionIntensity * (heat - airTemperature);
      double radiationRate = -radiationIntensity * Math.pow(heat, 4);
      double heatChangeRate = convectionRate + radiationRate;
      double phaseFraction = 1;
      double iterationDuration = phaseFraction;
      double heatChange = heatChangeRate * iterationDuration;
      if (Math.abs(heatChange) > heat * HEAT_CHANGE_LIMIT) {
        heatChange = heat * HEAT_CHANGE_LIMIT * (heatChange < 0 ? -1 : 1);
        iterationDuration = heatChange / heatChangeRate;
        phaseFraction = iterationDuration;
      }
      if (phase + phaseFraction > 1) {
        phaseFraction = 1 - phase;
        iterationDuration = phaseFraction;
        heatChange = heatChangeRate * iterationDuration;
      }
      phase += phaseFraction;
      heat += heatChange;
      if (heat < 0) {
        heat = 0;
      }
    }
    cell.getState().setHeat(toCelsius(heat));
  }

  private double calculateBurnedVolume(Cell cell) {
    double combustionRate = calculateCombustionRate(cell);
    double saturatedVolume = 1 - cell.getState().getFuel() / cell.getState().getInitialFuel();
    double linearVolume = calculateLinearValue(saturatedVolume);
    double burnedPortion = saturate(linearVolume + combustionRate) - saturate(linearVolume);
    return burnedPortion * cell.getState().getInitialFuel();
  }

  private double calculateCombustionRate(Cell cell) {
    double activationEnergy =
        ForestTypeFactors.determineActivationEnergy(cell.getFactors().getForestType());
    double temperature = toKelvin(cell.getState().getHeat());
    double firePower = -activationEnergy / UNIVERSAL_GAS_CONSTANT / temperature;
    double airHumidityInfluence = Math.exp(-humidityEffect * cell.getFactors().getAirHumidity());
    return airHumidityInfluence * combustionIntensity * Math.exp(firePower);
  }

  private double calculateEnvironmentalEffect(Cell cell, Cell otherCell, Grid grid) {
    return calculateSlopeEffect(cell, otherCell, grid) * calculateWindEffect(cell, otherCell);
  }

  private double calculateSlopeEffect(Cell cell, Cell otherCell, Grid grid) {
    double elevation = otherCell.getFactors().getElevation() - cell.getFactors().getElevation();
    if (elevation == 0) {
      return 1;
    }
    double localCos = Math.cos(Math.toRadians(grid.pointOf(cell.getCoordinates()).lat));
    double distanceX =
        Math.abs(cell.getCoordinates().getX() - otherCell.getCoordinates().getX()) * localCos;
    double distanceY = Math.abs(cell.getCoordinates().getY() - otherCell.getCoordinates().getY());
    double distance =
        grid.getCellHeight() * Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    double slope = elevation / distance;
    return Math.exp(slopeEffect * slope);
  }

  private double calculateWindEffect(Cell cell, Cell otherCell) {
    double vectorX = otherCell.getCoordinates().getX() - cell.getCoordinates().getX();
    double vectorY = otherCell.getCoordinates().getY() - cell.getCoordinates().getY();
    double windX = cell.getFactors().getWindX();
    double windY = cell.getFactors().getWindY();
    double windSpeed =
        (windX * vectorX + windY * vectorY) / Math.sqrt(vectorX * vectorX + vectorY * vectorY);
    return Math.exp(windEffect * windSpeed);
  }
}
