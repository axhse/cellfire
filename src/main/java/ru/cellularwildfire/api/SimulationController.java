package ru.cellularwildfire.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.cellularwildfire.api.params.SimulationCreationParams;
import ru.cellularwildfire.api.params.SimulationIdParams;
import ru.cellularwildfire.api.params.SimulationProgressParams;
import ru.cellularwildfire.models.LatLng;
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

  private static boolean isValidPoint(LatLng point) {
    return -90 <= point.lat && point.lat <= 90 && -180 <= point.lng && point.lng <= 180;
  }

  private static boolean isValidSimulationId(String simulationId) {
    return !simulationId.isEmpty() && simulationId.length() <= 100;
  }

  private static boolean isValidTick(Integer tick) {
    return 0 <= tick;
  }

  @PostMapping("/simulation/create")
  public Map<String, Object> createSimulation(@RequestBody SimulationCreationParams params) {
    if (params.getStartPoint().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start point not specified");
    }
    LatLng startPoint = params.getStartPoint().get();
    if (!isValidPoint(startPoint)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid start point");
    }

    Simulation simulation = simulator.createSimulation(startPoint);
    if (!simulator.tryStartSimulation(simulation)) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Simulation start failed");
    }

    Map<String, Object> response = new HashMap<>();
    response.put("simulation", simulation);
    simulationManager.addSimulation(simulation);

    return response;
  }

  @PostMapping("/simulation/remove")
  public void removeSimulation(@RequestBody SimulationIdParams params) {
    if (params.getSimulationId().isPresent()
        && isValidSimulationId(params.getSimulationId().get())) {
      simulationManager.removeSimulation(params.getSimulationId().get());
    }
  }

  @PostMapping("/simulation/progress")
  public Map<String, Object> progressSimulation(@RequestBody SimulationProgressParams params) {
    if (params.getSimulationId().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulation id not specified");
    }
    String simulationId = params.getSimulationId().get();
    if (!isValidSimulationId(simulationId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid simulation id");
    }
    if (params.getStartTick().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start tick not specified");
    }
    Integer startTick = params.getStartTick().get();
    if (!isValidTick(startTick)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid start tick");
    }
    if (params.getEndTick().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End tick not specified");
    }
    Integer endTick = params.getEndTick().get();
    if (!isValidTick(endTick)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid end tick");
    }

    Optional<Simulation> simulation = simulationManager.findSimulation(simulationId);
    if (simulation.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Simulation not found");
    }

    simulator.progressSimulation(simulation.get(), endTick);

    List<Simulation.Step> steps = simulation.get().getSteps();
    int toIndex = Math.min(endTick + 1, steps.size());
    List<Simulation.Step> lastSteps = steps.subList(startTick, toIndex).stream().toList();

    Map<String, Object> response = new HashMap<>();
    response.put("steps", lastSteps);

    return response;
  }
}
