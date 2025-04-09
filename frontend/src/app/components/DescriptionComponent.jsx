import React from "react";

import { InlineMath } from "react-katex";

export default function Description() {
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
        A fraction <InlineMath math={"f = \\min(1, k e^{\\beta_a a} \\tau)"} />{" "}
        of the fuel is consumed, where <InlineMath math={"\\tau"} /> is the step
        duration.
      </p>
      <p>
        The reaction emits energy amount proportional to the volume of consumed
        fuel.
      </p>
      <p>
        Then, the emitted energy is distributed between the burning cell itself
        and its neighbors, proportional to the proximity measure.
      </p>
      <p>
        For the cell itself, the proximity is defined as the scale effect
        divided by the grid scale: <InlineMath math={"\\frac{\\beta_s}{S}"} />.
      </p>
      <p>
        For neighboring cells, proximity is calculated as the inverse of the
        distance <InlineMath math={"d"} /> between the cells, multiplied by the
        effects of wind speed projection and slope angle:{" "}
        <InlineMath
          math={"\\frac{1}{d} e^{\\beta_w w} e^{\\beta_\\phi \\phi}"}
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
