import React from "react";

import { InlineMath } from "react-katex";

export default function Instruction() {
  return (
    <div>
      <h2>Simulate a wildfire under real weather conditions</h2>
      <h2>Instruction</h2>
      <GuideContent />
      <h2>Methods and algorithms</h2>
      <AboutContent />
      <div id="instruction-footer"> </div>
    </div>
  );
}

function GuideContent() {
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

function AboutContent() {
  return (
    <div className="section">
      <h3>Simulation</h3>
      <p>
        The simulation is performed with a model based on cellular automaton.
      </p>
      <p>
        It runs with respect for landscape and forest density features and goes
        under real weather conditions.
      </p>

      <h3>Cellular Automaton Model</h3>
      <p>
        The automaton operates on a square geographical grid with cell having
        width of 1/200th of a degree (grid scale: <InlineMath math={"S=200"} />
        ).
      </p>
      <p>The time interval between simulation steps is 30 minutes.</p>

      <h3>Cellular Automaton State</h3>
      <p>Each cell holds information about its heat and fuel amounts.</p>
      <p>
        A cell is burning if its heat exceeds a specific threshold. These cells
        emit and transfer energy to their neighboring cells.
      </p>
      <p>
        Burning cells can ignite other neighboring cells and after some time
        they burn out.
      </p>
      <p>The simulation starts with a single burning cell, set by the user.</p>

      <h3>Cellular Automaton Transition Rule</h3>
      <p>The automaton state transition occurs in 3 steps.</p>
      <p>
        Firstly, burning cells combust at a rate calculated using the Arrhenius
        equation: <InlineMath math={"k = A e^{-\\frac{E_a}{RT}}"} />
      </p>
      <p>
        The air humidity <InlineMath math={"a"} /> affects combustion rate with
        the effect <InlineMath math={"e^{\\beta_a a}"} />.
      </p>
      <p>
        A fraction <InlineMath math={"f = \\min(1, k e^{\\beta_a a})"} /> of the
        fuel is consumed.
      </p>
      <p>
        The reaction emits energy amount proportional to the volume of consumed
        fuel.
      </p>
      <p>
        Then, the emitted energy is distributed between the burning cell itself
        and its neighbors, proportional to the proximity measure.
      </p>
      <p>For the cell itself, the proximity is equal to 1.</p>
      <p>
        For neighboring cells, proximity is calculated as the inverse of the
        distance <InlineMath math={"d"} /> between the cells, multiplied by
        propagation intensity parameter <InlineMath math={"\\beta_p"} /> and the
        effects of wind speed projection and slope angle:{" "}
        <InlineMath
          math={"\\frac{\\beta_p}{d} e^{\\beta_w w} e^{\\beta_\\phi \\phi}"}
        />
      </p>
      <p>
        Finally, cells cool down according to the Stefan-Boltzmann law:{" "}
        <InlineMath math={"dT = -\\beta_r T^4"} /> and Newton&apos;s law of
        cooling: <InlineMath math={"dT = -\\beta_c (T - T_{env})"} />.
      </p>

      <h3>Input Data</h3>
      <p>
        The model incorporates forest type, forest density, and ground elevation
        maps, while weather information is requested in real-time.
      </p>
      <p>These maps are derived from open datasets.</p>
      <p>
        The elevation map uses the{" "}
        <a href="https://visibleearth.nasa.gov/images/73934/topography/83040l">
          Topography dataset
        </a>
        .
      </p>
      <p>
        The forest type map is derived from the{" "}
        <a href="https://no.m.wikipedia.org/wiki/Fil:Land_cover_IGBP.png">
          Land cover IGBP map
        </a>
        . For each point on the globe, one of five forest types is assigned
        based on the most common forest type in the surrounding area. Forests of
        different types have varying activation energies for burning.
      </p>
      <p>
        The forest density map is based on the{" "}
        <a href="https://glad.umd.edu/dataset/gedi">
          Canopy height GEDI dataset
        </a>{" "}
        and{" "}
        <a href="https://www.research-collection.ethz.ch/handle/20.500.11850/609802">
          Canopy height langnico dataset
        </a>
        . Forest density at each point is calculated from canopy height using
        the following formula:
      </p>
      <p>
        <InlineMath
          math={
            "d(h) = \\begin{cases} 1, & h > 15 \\\\ \\frac{h}{15}, & 5 < h \\leq 15 \\\\ \\frac{h^2}{75}, & h \\leq 5 \\end{cases}"
          }
        />
      </p>
      <p>
        The forest density of a cell is calculated as the average density of the
        points within it.
      </p>

      <h3>Parameter Adjustment</h3>
      <p>
        The model has numerous parameters that can be tuned: combustion
        intensity, energy emission, convection intensity, radiation intensity,
        scope effect, air humidity effect, slope effect and wind effect.
      </p>
      <p>
        These parameters are adjusted with a tuner module that specifies a set
        of verification cases describing desired behavior of the model.
      </p>
      <p>
        The tuner iterates over parameter combinations to find the optimal set
        that satisfies all cases.
      </p>
    </div>
  );
}
