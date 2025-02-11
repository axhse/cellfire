import React from 'react';
import { SimulationMap } from './SimulationMap';

function AppPage() {
  return (
    <div id='page-container'>
      <h2>Simulate a wildfire under real weather conditions</h2>
      <SimulationMap />
    </div>
  );
}

export default AppPage;
