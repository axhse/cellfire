package ru.cellularwildfire.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.cellularwildfire.api.params.SimulationCreationParams;
import ru.cellularwildfire.api.params.SimulationIdParams;
import ru.cellularwildfire.api.params.SimulationProgressParams;
import ru.cellularwildfire.models.Simulation;
import ru.cellularwildfire.services.SimulationManager;
import ru.cellularwildfire.services.Simulator;

@RestController
public final class SimulationController {
  private final SimulationManager simulationManager;
  private final Simulator simulator;

  @Autowired
  public SimulationController(SimulationManager simulationManager, Simulator simulator) {
    this.simulationManager = simulationManager;
    this.simulator = simulator;
  }

  @PostMapping("/simulation/create")
  public Map<String, Object> createSimulation(@RequestBody SimulationCreationParams params) {
    Map<String, Object> response = new HashMap<>();

    Simulation simulation =
        simulator.createSimulation(params.getStartPoint());

    if (simulator.tryStartSimulation(simulation)) {
      response.put("success", true);
      response.put("simulation", simulation);
      simulationManager.addSimulation(simulation);
    } else {
      response.put("success", false);
    }

    return response;
  }

  @PostMapping("/simulation/remove")
  public void removeSimulation(@RequestBody SimulationIdParams params) {
    simulationManager.removeSimulation(params.getSimulationId());
  }

  @PostMapping("/simulation/progress")
  public Map<String, Object> progressSimulation(@RequestBody SimulationProgressParams params) {
    Map<String, Object> response = new HashMap<>();

    Optional<Simulation> simulation = simulationManager.findSimulation(params.getSimulationId());
    if (simulation.isEmpty()) {
      response.put("success", false);
      return response;
    }

    if (simulator.tryProgressSimulation(simulation.get(), params.getEndTick())) {
      response.put("success", true);
      List<Simulation.Step> steps = simulation.get().getSteps();
      int toIndex = Math.min(params.getEndTick() + 1, steps.size());
      List<Simulation.Step> lastSteps =
          steps.subList(params.getStartTick(), toIndex).stream().toList();
      response.put("steps", lastSteps);
    } else {
      response.put("success", false);
    }
    return response;
  }
}
