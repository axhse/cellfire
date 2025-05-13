package ru.cellularwildfire.services;

import java.util.*;
import ru.cellularwildfire.models.Simulation;

public final class SimulationManager {
  private final int capacity;
  private final List<Simulation> simulations = new ArrayList<>();
  private final Map<String, Long> accessMoments = new HashMap<>();
  private long accessIndex = 0;

  public SimulationManager(int capacity) {
    this.capacity = capacity;
  }

  public Optional<Simulation> findSimulation(String id) {
    synchronized (simulations) {
      for (Simulation simulation : simulations) {
        if (simulation.getId().equals(id)) {
          accessMoments.put(simulation.getId(), accessIndex++);
          return Optional.of(simulation);
        }
      }
    }
    return Optional.empty();
  }

  public void addSimulation(Simulation simulation) {
    synchronized (simulations) {
      if (capacity <= simulations.size()) {
        simulations.sort(Comparator.comparing(s -> accessMoments.get(s.getId())));
        simulations.remove(0);
      }
      simulations.add(simulation);
      accessMoments.put(simulation.getId(), accessIndex++);
    }
  }

  public void removeSimulation(String id) {
    synchronized (simulations) {
      simulations.removeIf(simulation -> simulation.getId().equals(id));
    }
  }
}
