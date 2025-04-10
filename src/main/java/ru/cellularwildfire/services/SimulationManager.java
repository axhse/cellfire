package ru.cellularwildfire.services;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import ru.cellularwildfire.models.Simulation;

@Service
public final class SimulationManager {
  private static final int CAPACITY = 50;

  private final List<Simulation> simulations = new ArrayList<>();
  private final Map<String, Instant> accessDates = new HashMap<>();

  public Optional<Simulation> findSimulation(String id) {
    synchronized (simulations) {
      for (Simulation simulation : simulations) {
        if (simulation.getId().equals(id)) {
          accessDates.put(simulation.getId(), Instant.now());
          return Optional.of(simulation);
        }
      }
    }
    return Optional.empty();
  }

  public void addSimulation(Simulation simulation) {
    synchronized (simulations) {
      if (simulations.size() == CAPACITY) {
        simulations.sort(Comparator.comparing(s -> accessDates.get(s.getId())));
        simulations.remove(0);
      }
      simulations.add(simulation);
      accessDates.put(simulation.getId(), Instant.now());
    }
  }

  public void removeSimulation(String id) {
    synchronized (simulations) {
      simulations.removeIf(simulation -> simulation.getId().equals(id));
    }
  }
}
