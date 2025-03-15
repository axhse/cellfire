import React from 'react';

import { InlineMath } from 'react-katex';

export default function Description() {
  return (
    <div className='section'>
      <h3>Simulation Model</h3>
      <p>
        The simulation is based on real weather conditions, landscape features,
        and forest density arrangements.
      </p>
      <p>It uses a cellular automaton model.</p>

      <h3>Cellular Automaton Model</h3>
      <p>
        The automaton operates on a geographical grid, with each cell measuring
        1/200th of a degree (grid scale: <InlineMath math={'S=200'} />
        ).
      </p>
      <p>The time interval between automaton states is 30 minutes.</p>

      <h3>Cellular Automaton State</h3>
      <p>Each cell holds information about fuel and heat levels.</p>
      <p>
        A cell is considered burning if its heat exceeds a specific threshold.
      </p>
      <p>
        Neighboring cells are either damaged and burned down or undamaged and
        igniting.
      </p>
      <p>Burning cells transfer energy to 8 neighboring cells.</p>
      <p>
        Cells that have never been adjacent to burning cells are considered
        intact and are excluded from the simulation.
      </p>
      <p>The simulation starts with a single burning cell, set by the user.</p>

      <h3>Cellular Automaton Rule</h3>
      <p>The automaton state transition occurs in 3 steps.</p>
      <p>
        First, burning cells combust at a rate calculated using the Arrhenius
        equation: <InlineMath math={'k = A e^{-\\frac{E_a}{RT}}'} />
      </p>
      <p>
        The air humidity <InlineMath math={'a'} /> affects combustion rate with
        the effect <InlineMath math={'e^{\\beta_a a}'} />.
      </p>
      <p>
        A fraction <InlineMath math={'f = \\min(1, k e^{\\beta_a a} \\tau)'} />{' '}
        of the fuel is consumed, where <InlineMath math={'\\tau'} /> is the step
        duration.
      </p>
      <p>
        The reaction emits energy proportional to the amount of fuel consumed.
      </p>
      <p>
        In the second step, the emitted energy is distributed between the
        burning cell and its neighbors, based on proximity.
      </p>
      <p>
        Initially, proximity is calculated as the inverse of the average
        distance between the centers of the cells.
      </p>
      <p>
        To account for the grid scale, the proximity is divided by{' '}
        <InlineMath math={'\\beta_S S'} /> — proximity to neighboring cells
        depends linearly on grid scale compared to proximity within the cell.
      </p>
      <p>
        Wind speed in the direction between cells <InlineMath math={'w'} />{' '}
        affects proximity with the effect <InlineMath math={'e^{\\beta_w w}'} />
        .
      </p>
      <p>
        The slope angle between cells <InlineMath math={'\\phi'} /> affects
        proximity with the effect <InlineMath math={'e^{\\beta_\\phi \\phi}'} />
        .
      </p>
      <p>
        Finally, cells cool down according to the Stefan-Boltzmann law:{' '}
        <InlineMath math={'dT = -\\beta_r T^4'} /> and Newton&apos;s law of
        cooling: <InlineMath math={'dT = -\\beta_c (T - T_{env})'} />.
      </p>

      <h3>Input Data</h3>
      <p>
        The model incorporates forest type, forest density, and ground elevation
        maps, while weather information is requested in real-time.
      </p>
      <p>These maps are derived from open datasets.</p>
      <p>
        The elevation map uses the{' '}
        <a href='https://visibleearth.nasa.gov/images/73934/topography/83040l'>
          Topography map
        </a>
        .
      </p>
      <p>
        The forest type map is derived from the{' '}
        <a href='https://no.m.wikipedia.org/wiki/Fil:Land_cover_IGBP.png'>
          Land cover IGBP map
        </a>
        . For each point on the globe, one of five forest types is assigned
        based on the most common forest type in the surrounding area. Forests of
        different types have varying activation energies for burning.
      </p>
      <p>
        The forest density map is based on the{' '}
        <a href='https://nlang.users.earthengine.app/view/global-canopy-height-2020'>
          Canopy height map
        </a>
        . Forest density at each point is calculated from canopy height using
        the following formula:
      </p>
      <p>
        <InlineMath
          math={
            'd(h) = \\begin{cases} 1, & h > 15 \\\\ \\frac{h}{15}, & 5 < h \\leq 15 \\\\ \\frac{h^2}{75}, & h \\leq 5 \\end{cases}'
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
        intensity, energy emission, air humidity effect, slope effect, wind
        effect, distance effect, heat regulation intensity, and radiation
        prevalence.
      </p>
      <p>
        These parameters are automatically adjusted through a set of
        verification cases, which define the desired model behavior.
      </p>
      <p>
        The model iterates over parameter combinations to find the optimal set
        that satisfies all cases.
      </p>
    </div>
  );
}
