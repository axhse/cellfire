# Backend Module

The backend of the application is responsible for simulation performing and data processing.

## Implementation

The backend module is implemented in Java using the Spring framework.

- **Simulation-related models** are located in the `models` package.
- **The simulation algorithm**, based on cellular automata, is implemented in the `ThermalAlgorithm`
  class.
- **`SimulationManager` service** maintains up to `SIMULATION_LIMIT_QUANTITY` active simulations.
- **`Simulator` service** is used for creating draft states of the automaton and refining them using
  the algorithm.
- **`WeatherService`** retrieves real-time weather data and has two implementations:
    - `WeatherApiService` fetches weather data from [WeatherAPI](https://www.weatherapi.com/).
    - `StandaloneWeatherService` serves as a mocked implementation for debugging.
- **`TerrainService`** determines ground elevation, forest type, and density using predefined static
  datasets. It utilizes data models from the `data` package, with data stored in the `maps` folder
  within application resources.


## Data
Map fragments should be copied from output directory of `data_processor` module to  `maps` folder of resources.  
These fragments then need to be loaded in `ServiceConfig` with `MapLoader` to initialize a `TerrainService` instance.


## Model parameter adjustment

`tuner` module with `TunerApplication` can be used to tune model parameters.  
It variates through model instances with parameters from specified ranges to estimate model accuracy with determined tuning cases.  
`Tasks` module specifies tuning tasks `TuneTask` which describe a set validation cases and parameter restrictions for each tuning scenario.
