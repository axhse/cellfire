import React from "react";

import { InlineMath } from "react-katex";

export default function Instruction() {
  return (
    <div id="instruction-content">
      <h2>Simulate a wildfire under real weather conditions</h2>
      <h2>Instruction</h2>
      <GuideContent />
      <h2>Methods and algorithms</h2>
      <AboutContent />
    </div>
  );
}

function GuideContent() {
  return (
    <div className="section">
      <h3>Quick Start</h3>
      <p>
        To start a simulation, click the fire button 🔥 and select a point on
        the map where the fire should begin.
      </p>

      <h3>Timeline</h3>
      <p>
        Use the timeline buttons to move the simulation forward or backward.
      </p>

      <h3>Information</h3>
      <p>
        The information section provides a summary of the current simulation
        step, including key fire-influencing factors.
      </p>
      <p>
        These factors are shown with gradient backgrounds: red for those that
        promote fire, and green for those that suppress it.
      </p>

      <h3>Layers</h3>
      <p>
        Different layers display distinct aspects of the simulation step state,
        using color gradients to represent varying values.
      </p>
      <p>
        Fire status layer: red for active fires, gray for burned-out areas, and
        green for areas flaming up.
      </p>
      <p>
        Fuel density layer: colors range from gray to purple, representing fuel
        density from 0 to 1.
      </p>
      <p>
        Elevation layer: green for sea level or lower, yellow at 2 km, red at 4
        km, and black above 6.4 km.
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
        It happens with respect for landscape and forest density features and
        goes under real weather conditions.
      </p>

      <h3>Cellular Automaton Model</h3>
      <p>
        The automaton operates on a square geographical grid with cell having
        width and height of 1/200th of a degree (grid scale{" "}
        <InlineMath math={"S=200"} />
        ).
      </p>
      <p>The time interval between simulation steps is 30 minutes.</p>

      <h3>Cellular Automaton State</h3>
      <p>Each cell holds information about its heat and fuel amounts.</p>
      <p>A cell is burning if its heat exceeds a specific threshold.</p>
      <p>
        Burning cells emit energy and distribute it between neighboring cells.
      </p>
      <p>
        The simulation starts with a single burning cell, picked by the user.
      </p>

      <h3>Cellular Automaton Transition Rule</h3>
      <p>The automaton state transition occurs in 3 steps.</p>
      <p>
        Burning cells combust at a rate <InlineMath math={"k"} />, based on the
        Arrhenius equation. The rate formula includes a linear coefficient{" "}
        <InlineMath math={"\\beta_k"} /> and accounts for the effect of air
        humidity <InlineMath math={"0 \\leq a \\leq 1"} />, modulated by the
        parameter <InlineMath math={"\\beta_a"} />:{" "}
        <InlineMath
          math={"k = \\beta_k e^{-\\frac{E_a}{RT}} (1 - a)^{\\beta_a}"}
        />
        .
      </p>
      <p>
        Suppose that at step <InlineMath math={"i"} /> the combustion rate is{" "}
        <InlineMath math={"k_i"} />. Then, at each step{" "}
        <InlineMath math={"j"} /> fuel amount equal to{" "}
        <InlineMath
          math={
            "v = f\\left(\\sum_{n=1}^{j-1} k_n\\right) - f\\left(\\sum_{n=1}^{j} k_n\\right)"
          }
        />{" "}
        is consumed, where{" "}
        <InlineMath
          math={"f(K) = v_0 \\cdot \\left(2 - \\frac{2}{1 + e^{-K}}\\right)"}
        />{" "}
        and <InlineMath math={"v_0"} /> is the initial amount of fuel.
      </p>
      <p>
        The combustion emits energy <InlineMath math={"E"} /> depending on the
        factor <InlineMath math={"\\beta_e"} />:{" "}
        <InlineMath math={"E = \\beta_e \\cdot v"} />
      </p>
      <p>
        Then, the emitted energy is distributed between the burning cell and its
        8 neighbors in a proportion based on specific weights{" "}
        <InlineMath math={"w"} />.
      </p>
      <p>The weight for the burning cell itself is set to 1.</p>
      <p>
        For neighboring cells, the weight depends on the propagation intensity
        parameter <InlineMath math={"\\beta_p"} />, the distance{" "}
        <InlineMath math={"d"} /> between cells, and the effects of wind speed
        projection <InlineMath math={"w"} /> and slope angle{" "}
        <InlineMath math={"\\phi"} />, with their respective exponential
        parameters:{" "}
        <InlineMath
          math={"w = \\frac{\\beta_p}{d} e^{\\beta_w w} e^{\\beta_\\phi \\phi}"}
        />
        .
      </p>
      <p>
        Finally, cells cool down according to the Stefan-Boltzmann law:{" "}
        <InlineMath math={"dT = -\\beta_r T^4"} /> and Newton's law of cooling:{" "}
        <InlineMath math={"dT = -\\beta_c (T - T_{air})"} />, with respect to
        the corresponding linear parameters.
      </p>

      <h3>Parameter Adjustment</h3>
      <p>
        The model has numerous parameters that can be tuned: combustion
        intensity, energy emission, propagation intensity, convection intensity,
        radiation intensity, air humidity effect, slope effect and wind effect.
      </p>
      <p>
        These parameters are adjusted with a tuner module that specifies a set
        of verification cases describing desired behavior of the model.
      </p>
      <p>
        The tuner iterates over parameter combinations to find the optimal set
        that satisfies all cases.
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
        different types are distinguished by their activation energy{" "}
        <InlineMath math={"E_a"} />.
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
        . Forest density at each point is calculated from canopy height{" "}
        <InlineMath math={"h"} /> using the following function:
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
    </div>
  );
}
