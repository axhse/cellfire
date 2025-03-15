import React from 'react';

import Description from './DescriptionComponent';
import Instruction from './InstructionComponent';
import MapComponent from './MapComponent';

function AppPage() {
  return (
    <div id='page-container'>
      <h2>Simulate a wildfire under real weather conditions</h2>
      <MapComponent />
      <h2 className='section-header'>Instruction</h2>
      <Instruction />
      <h2 className='section-header'>Simulation methods and algorithms</h2>
      <Description />
    </div>
  );
}

export default AppPage;
