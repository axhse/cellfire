package com.example.cellfire.api.params;


import com.example.cellfire.models.CellCoordinates;

import java.time.Instant;

public final class ScenarioCreationParams {
    private final CellCoordinates startCoordinates;
    private final long startTs;

    public ScenarioCreationParams(CellCoordinates startCoordinates, long startTs){
        this.startCoordinates = startCoordinates;
        this.startTs = startTs;
    }

    public CellCoordinates getStartCoordinates() {
        return startCoordinates;
    }

    public Instant getStartDate() {
        return Instant.ofEpochMilli(startTs);
    }
}
