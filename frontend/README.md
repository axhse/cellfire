# Frontend Module

The frontend of the application consists of a single page that includes the simulation map, instructions, and a model description.

## Implementation

The frontend module is built using JavaScript and the React framework.

The main interface component is an interactive map that provides all the necessary controls for running simulations with custom scenarios.

Simulation-related models are partially implemented in the frontend module for encapsulation purposes.

The `Simulator` service handles communication with the backend API to perform fire simulations.
This service also includes a mocked standalone implementation for debugging purposes.

## Development

Format the source code with Prettier:
```sh
npm run format
```

Build the frontend module:
```sh
npm run build
```

Start the standalone configuration for debugging:
```sh
npm run start:standalone
```

