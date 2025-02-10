import numpy as np

from map_fragment import MapFragment, MapFullFragment
from resource_manager import ResourceManager
from transformation import (
    compress_map_data,
    transform_from_tiff_coordinates_to_map_coordinates,
)
from visual import GradientMapDrawer

ELEVATION_MAP_NAME = "Elevation"
ELEVATION_MAP_FRAGMENT_SIZE = 90
ELEVATION_MAP_SCALE = 120
ELEVATION_MAP_DRAWER = GradientMapDrawer((240, 240, 255), (64, 32, 0))


def get_sector_coordinates(sector):
    x = ("ABCD".index(sector[0]) - 2) * ELEVATION_MAP_FRAGMENT_SIZE
    y = (1 - int(sector[1])) * ELEVATION_MAP_FRAGMENT_SIZE
    return x, y


def produce_elevation_sector_map(
    resource_manager: ResourceManager, sector, scale=ELEVATION_MAP_SCALE
):
    input_file_path = f"Elevation\\gebco_08_rev_elev_{sector}_grey_geo.tif"
    data = resource_manager.load_tiff(input_file_path)
    initial_scale = data.shape[0] // ELEVATION_MAP_FRAGMENT_SIZE
    if initial_scale % scale != 0:
        raise Exception("Invalid scale.")
    data = compress_map_data(data, initial_scale // scale)
    elevations = transform_from_tiff_coordinates_to_map_coordinates(data)
    x, y = get_sector_coordinates(sector)
    return MapFragment(elevations, ELEVATION_MAP_NAME, scale, x, y)


def produce_elevation_full_map(
    resource_manager: ResourceManager, scale=ELEVATION_MAP_SCALE
):
    sectors = [f"{x}{y}" for x in "ABCD" for y in [1, 2]]
    fragments = [
        produce_elevation_sector_map(resource_manager, sector, scale)
        for sector in sectors
    ]
    fragments.sort(key=lambda fragment: (fragment.x, fragment.y))
    rows = [
        np.hstack([fragments[2 * x].data, fragments[2 * x + 1].data]) for x in range(4)
    ]
    elevations = np.vstack(rows)
    return MapFullFragment(elevations, ELEVATION_MAP_NAME)


def load_elevation_sector_map(
    resource_manager: ResourceManager, sector, scale=ELEVATION_MAP_SCALE
):
    x, y = get_sector_coordinates(sector)
    return resource_manager.load_map_fragment(
        ELEVATION_MAP_NAME,
        scale,
        x,
        y,
        ELEVATION_MAP_FRAGMENT_SIZE,
        ELEVATION_MAP_FRAGMENT_SIZE,
    )


def load_elevation_full_map(
    resource_manager: ResourceManager, scale=ELEVATION_MAP_SCALE
):
    return resource_manager.load_map_full_fragment(ELEVATION_MAP_NAME, scale)
