package com.example.cellfire.api.params;


import com.example.cellfire.models.CellCoordinates;

import java.time.Instant;

public final class ScenarioCreationParams {
    private final String algorithm;
    private final CellCoordinates startCoordinates;
    private final long startTs;

    public ScenarioCreationParams(String algorithm, CellCoordinates startCoordinates, long startTs){
        this.algorithm = algorithm;
        this.startCoordinates = startCoordinates;
        this.startTs = startTs;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public CellCoordinates getStartCoordinates() {
        return startCoordinates;
    }

    public Instant getStartDate() {
        return Instant.ofEpochMilli(startTs);
    }
}
