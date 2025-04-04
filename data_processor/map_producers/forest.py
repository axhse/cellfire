import numpy as np

from map_fragment import FullMap, MapFragment
from resource_manager import ResourceManager
from transformation import (
    collapse_category_map_data,
    compress_map_data,
    transform_from_image_coordinates_to_map_coordinates,
    transform_from_tiff_coordinates_to_map_coordinates,
)
from visual import CategoryMapDrawer, GradientMapDrawer

FOREST_TYPE_MAP_INPUT_SCALE = 10
FOREST_TYPE_MAP_NAME = "ForestType"
FOREST_TYPE_CLUSTER_MAP_NAME = "ForestTypeCluster"
FOREST_DENSITY_MAP_NAME = "ForestDensity"
CANOPY_HEIGHT_SECTOR_SIZE = 3
CANOPY_HEIGHT_SECTOR_INPUT_SCALE = 12000
CANOPY_HEIGHT_MAP_NAME = "CanopyHeight"

TOLERATED_TYPE_FOREST_COLOR_INACCURACY = 40
"""
Evergreen Needleleaf Forest
Evergreen Broadleaf Forest
Deciduous Needleleaf Forest
Deciduous Broadleaf Forest
Mixed Forest
"""
FOREST_TYPE_COLORS = [
    [0, 128, 0],
    [0, 255, 0],
    [153, 204, 0],
    [153, 255, 153],
    [51, 153, 102],
]
FOREST_TYPE_MAP_DRAWER = CategoryMapDrawer(FOREST_TYPE_COLORS)
FOREST_DENSITY_MAP_DRAWER = GradientMapDrawer(top_color=(0, 128, 0))


def produce_forest_type_map(resource_manager: ResourceManager):
    pixels = resource_manager.load_image_as_rgb_array("Land_cover_IGBP.png")
    forest_types = np.zeros(pixels.shape[:2], dtype=np.uint8)
    for forest_type_index, forest_color in enumerate(np.array(FOREST_TYPE_COLORS)):
        mask = np.zeros(pixels.shape[:2], dtype=bool)
        inaccuracy = np.sum(np.abs(pixels[:, :, :3] - forest_color), axis=-1)
        mask |= inaccuracy <= TOLERATED_TYPE_FOREST_COLOR_INACCURACY
        forest_types[mask] = forest_type_index + 1
    forest_types = transform_from_image_coordinates_to_map_coordinates(forest_types)
    return FullMap(forest_types, FOREST_TYPE_MAP_NAME)


def produce_forest_type_cluster_map(resource_manager: ResourceManager):
    data = produce_forest_type_map(resource_manager).data
    compression = {1: data}
    scales = [
        (2, 2),
        (3, 3),
        (5, 5),
        (10, 2),
        (20, 2),
        (30, 3),
        (50, 5),
        (100, 10),
        (200, 20),
        (300, 30),
        (600, 60),
        (900, 90),
        (1800, 180),
    ]
    for absolute_scale, relative_scale in scales:
        initial_data = compression[absolute_scale // relative_scale]
        compressed_data = collapse_category_map_data(initial_data, relative_scale)
        compression[absolute_scale] = compressed_data
        stretched_data = np.repeat(compressed_data, absolute_scale, axis=0)
        stretched_data = np.repeat(stretched_data, absolute_scale, axis=1)
        mask = (data == 0) & (stretched_data != 0)
        data[mask] = stretched_data[mask]
    return FullMap(data, FOREST_TYPE_CLUSTER_MAP_NAME)


def produce_forest_density_sector(
    resource_manager: ResourceManager, x, y, scale, transitional_scale
):
    data = read_canopy_height_sector_data(resource_manager, x, y, scale)
    data = compress_map_data(
        data, CANOPY_HEIGHT_SECTOR_INPUT_SCALE // transitional_scale
    )

    # 15 < canopy height
    data[75 < data] = 255
    # 5 < canopy height <= 15
    mask = (25 < data) & (data <= 75)
    data[mask] = 255 * data[mask].astype(int) // 75
    # canopy height <= 5
    mask = data <= 25
    data[mask] = 255 * data[mask].astype(int) * data[mask] // 25 // 25 // 3

    data = compress_map_data(data, transitional_scale // scale)
    transformed_data = transform_from_tiff_coordinates_to_map_coordinates(data)
    return MapFragment(transformed_data, FOREST_DENSITY_MAP_NAME, scale, x, y)


def produce_canopy_height_sector(resource_manager: ResourceManager, x, y, scale):
    data = read_canopy_height_sector_data(resource_manager, x, y, scale)
    data = compress_map_data(data, CANOPY_HEIGHT_SECTOR_INPUT_SCALE // scale)
    transformed_data = transform_from_tiff_coordinates_to_map_coordinates(data)
    return MapFragment(transformed_data, CANOPY_HEIGHT_MAP_NAME, scale, x, y)


def read_canopy_height_sector_data(resource_manager: ResourceManager, x, y, scale):
    input_file_path = f"CanopyHeight\\ETH_GlobalCanopyHeight_10m_2020_{'S' if y < 0 else 'N'}{abs(y):02d}{'W' if x < 0 else 'E'}{abs(x):03d}_Map.tif"
    data = resource_manager.load_tiff(input_file_path)
    if CANOPY_HEIGHT_SECTOR_INPUT_SCALE % scale != 0:
        raise Exception("Invalid scale.")
    data[data == 255] = 0
    return data


def load_forest_type_map(resource_manager: ResourceManager, compressed=False):
    return resource_manager.load_full_map(
        FOREST_TYPE_MAP_NAME, FOREST_TYPE_MAP_INPUT_SCALE, compressed
    )


def load_forest_type_cluster_map(resource_manager: ResourceManager, compressed=False):
    return resource_manager.load_full_map(
        FOREST_TYPE_CLUSTER_MAP_NAME, FOREST_TYPE_MAP_INPUT_SCALE, compressed
    )


def load_forest_density_sector(
    resource_manager: ResourceManager, scale, x, y, compressed=False
):
    return resource_manager.load_map_fragment(
        FOREST_DENSITY_MAP_NAME,
        scale,
        x,
        y,
        CANOPY_HEIGHT_SECTOR_SIZE,
        CANOPY_HEIGHT_SECTOR_SIZE,
        compressed,
    )


def load_canopy_height_sector(
    resource_manager: ResourceManager, scale, x, y, compressed=False
):
    return resource_manager.load_map_fragment(
        CANOPY_HEIGHT_MAP_NAME,
        scale,
        x,
        y,
        CANOPY_HEIGHT_SECTOR_SIZE,
        CANOPY_HEIGHT_SECTOR_SIZE,
        compressed,
    )
