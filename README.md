# Cellular Wildfire

![demo](./img/demo.png)

Cellular Wildfire is a web application designed for wildfire simulation using a model based on cellular automata.

It utilizes predefined landscape and forest data along with real-time weather data to provide accurate simulations.

The model replicates the fire spread process by incorporating physical principles and insights from experimental observations.

## Implementation

The project is structured into three main modules:

1. [src](./src/) - Backend module responsible for core simulation logic and data processing.
2. [frontend](./frontend/) - Frontend module providing the web interface for visualization and interaction.
3. [data_processor](./data_processor/) - Auxiliary module for processing forest and landscape data from open datasets.

Each module contains its own README file with development-specific details.
These files are located in the root directory of each module.

## Development

Build the application (including the frontend build):

```sh
mvn clean package
```

Start the development build with a mocked weather service:

```sh
mvn spring-boot:run
```

Start the production build with real weather data, specifying the WEATHER_API_KEY environment
variable with an actual API key with sufficient heap space:

```sh
WEATHER_API_KEY=0123456789abcdef0123456789abcde mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx3g -Xms2g"
```

