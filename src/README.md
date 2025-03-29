# Backend Module

The backend of the application is responsible for simulation performing and data processing.

## Implementation

The backend module is implemented in Java using the Spring framework.

- **Simulation-related models** are located in the `models` package.
- **The simulation algorithm**, based on cellular automata, is implemented in the `ThermalAlgorithm` class.
- **`SimulationManager` service** maintains up to `SIMULATION_LIMIT_QUANTITY` active simulations.
- **`Simulator` service** is used for creating draft states of the automaton and refining them using the algorithm.
- **`WeatherService`** retrieves real-time weather data and has two implementations:
    - `WeatherApiService` fetches weather data from [WeatherAPI](https://www.weatherapi.com/).
    - `StandaloneWeatherService` serves as a mocked implementation for debugging.
- **`TerrainService`** determines ground elevation, forest type, and density using predefined static datasets. It utilizes data models from the `data` package, with data stored in the `maps` folder within application resources.

## Development

Build the application (including the frontend build):
```sh
mvn clean package
```

Start the development build with a mocked weather service:
```sh
mvn spring-boot:run
```

Start the production build with real weather data, specifying the WEATHER_API_KEY environment variable with an actual API key:
```sh
WEATHER_API_KEY=0123456789abcdef0123456789abcde mvn spring-boot:run
```

