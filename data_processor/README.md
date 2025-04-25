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

The module produces map fragments `MapFragment` from data in input directory and saves the fragments to output directory.  
`ResourceManager` can be used to manage input and output files.  
"input" and "output" folders of the module are ignored by git.

Create a Resource Manager:
```python
resource_manager = ResourceManager("input", "output")
```

Save a map fragment `fragment` to the output directory:
```python
resource_manager.save_map_fragment(fragment)
```

To visualize fragments use functions from `visual` module:
```python
draw_elevation(elevation_map_fragment)
```

### Elevation map

To produce an elevation map all 8 tiff sections must be downloaded from
[Topography](https://visibleearth.nasa.gov/images/73934/topography/83040l) dataset
and placed in `Elevation` folder inside the input directory.  
Then `produce_elevation_map` function of `map_producers.elevation` module may be used to transform these sections into a map:
```python
elevation_map = produce_elevation_map(resource_manager)
```

### Forest type map

Forest type map is derived from land cover classification map
[Land cover IGBP.png](https://en.wikipedia.org/wiki/File:Land_cover_IGBP.png).  
This picture must be downloaded and placed right in the input directory.  
Then `produce_forest_type_map` function of `map_producers.forest_type` may be used to generate a forest type map:
```python
forest_type_map = produce_forest_type_map(resource_manager)
```

### Gedi forest density map

[GEDI Canopy Height](https://glad.umd.edu/dataset/gedi) dataset is mainly used to produce forest density map fragments.  
It's 7 regions must be downloaded and placed in `GediCanopyHeight` folder of the input directory.  
The files are large and can not be processed at ones.  
`produce_forest_density_fragment` function of `map_producers.gedi_forest_density` may be used to produce small forest density sections.  
`produce_and_save_forest_density_region_tiles` of `main` can be used to fully process these regions tile by tile.
Then the tiles can be combined with `combine_and_save_forest_density_region_tiles` function:
```python
produce_and_save_forest_density_region_tiles(resource_manager, Region.AUSTRALIA)
combine_and_save_forest_density_region_tiles(resource_manager, Region.AUSTRALIA)
```

### Langnico forest density map

As Gedi dataset does not cover the full Earth map,
[Langnico Canopy Height](https://www.research-collection.ethz.ch/handle/20.500.11850/609802)
dataset is used to produce fragments for North region.  
It's input 3x3 tile files must be downloaded to `LangnicoCanopyHeight` folder of the input directory.  
It can be done with `download_forest_density_input_file` function of `map_producers.langnico_forest_density`:
```python
download_forest_density_input_file(resource_manager, 51, 18)
```
Then forest density fragments can be produced with `produce_forest_density_tile` function:
```python
produce_forest_density_tile(resource_manager, 200, 1000, 51, 18)
```
