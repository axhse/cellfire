# Data Processor Module

The Data Processor module is responsible for generating landscape and forest maps from open datasets.

## Implementation

- The module defines a uniform `MapFragment` model, which stores all landscape and forest data in a byte format.
- The `ResourceManager` handles input and output operations, loading input files and saving generated map fragments.
- The `MapDrawer` provides visualization capabilities for the generated maps.
- All functions for processing input data into map fragments are implemented in the `map_producers` module.
- Additional functionality is available for cutting or rescaling maps.

## Development

### Usage

To use the data processor, first download the necessary input data, then run map producer functions using the `ResourceManager`.

Create a Resource Manager:
```python
resource_manager = ResourceManager("input", "output")
```

Produce an Elevation Map:
```python
elevation_map = produce_elevation_map(resource_manager)
```

Save the Elevation Map:
```python
resource_manager.save_map_fragment(elevation_map)
```

Display the Elevation Map (optional):
```python
ELEVATION_MAP_DRAWER.draw(elevation_map)
```

