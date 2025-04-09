import React from "react";

export default function Instruction() {
  return (
    <div className="section">
      <h3>Quick Start</h3>
      <p>
        To start a simulation, activate the lighter button 🔥 and select a point
        on the map where the fire should originate.
      </p>

      <h3>Timeline</h3>
      <p>
        Use the timeline navigation buttons to advance or rewind the simulation.
      </p>

      <h3>Information</h3>
      <p>
        The information section displays details about current simulation step,
        including fire influencing factors.
      </p>
      <p>
        Indicators use a gradient color scheme: from red for factors that
        promote fire to green for those that restrain it.
      </p>

      <h3>Layers</h3>
      <p>Use the layer toggles to switch between layers.</p>
      <p>Each layer represents data with color gradients.</p>
      <p>
        The fire status layer colors: red for actively burning cells, gray for
        cooled burned cells and green for cold igniting cells.
      </p>
      <p>
        The fuel density layer colors: from gray to purple, indicating fuel
        density from 0 to 1.
      </p>
      <p>
        The elevation layer colors: green for areas at sea level or below,
        yellow at 2km, red at 4km, and black above 6.4km.
      </p>
    </div>
  );
}
