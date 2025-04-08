from os import path

from map_fragment import ELEVATION_MAP_NAME, FullMap
from resource_manager import ResourceManager
from transformation import downscale_data, stack_tiles, to_map_data

ELEVATION_INPUT_FOLDER_NAME = "Elevation"
ELEVATION_TILE_SCALE = 120
ELEVATION_TILE_SIZE = 90


def produce_elevation_map(
    resource_manager: ResourceManager, scale=ELEVATION_TILE_SCALE
):
    tiles = list()
    for y in [2, 1]:
        for x in "ABCD":
            input_file_path = path.join(
                ELEVATION_INPUT_FOLDER_NAME, f"gebco_08_rev_elev_{x}{y}_grey_geo.tif"
            )
            data = resource_manager.load_tiff(input_file_path)

            initial_scale = data.shape[0] // ELEVATION_TILE_SIZE
            if initial_scale % scale != 0:
                raise Exception("Invalid scale.")
            data = downscale_data(data, initial_scale // scale)
            data = to_map_data(data)
            tiles.append(data)

    data = stack_tiles(tiles, 4)
    return FullMap(data, ELEVATION_MAP_NAME)


def load_elevation_map(
    resource_manager: ResourceManager, scale=ELEVATION_TILE_SCALE, compressed=False
):
    return resource_manager.load_full_map(ELEVATION_MAP_NAME, scale, compressed)
