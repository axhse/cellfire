import numpy as np

from converters import (
    collapse_category_map_data,
    compress_map_data,
    transform_from_image_coordinates_to_map_coordinates,
    transform_from_tiff_coordinates_to_map_coordinates,
)
from map_fragment import MapFragment, MapFullFragment
from resource_manager import ResourceManager
from visual import CategoryMapDrawer, GradientMapDrawer

FOREST_TYPE_MAP_NAME = "ForestType"
FOREST_TYPE_CLUSTER_MAP_NAME = "ForestTypeCluster"
FOREST_TYPE_MAP_SCALE = 10
CANOPY_HEIGHT_MAP_NAME = "CanopyHeight"
CANOPY_HEIGHT_MAP_FRAGMENT_SIZE = 3

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
CANOPY_HEIGHT_MAP_DRAWER = GradientMapDrawer(top_color=(0, 128, 0))


def produce_forest_type_map(resource_manager: ResourceManager):
    pixels = resource_manager.load_image_as_rgb_array("Land_cover_IGBP.png")
    forest_types = np.zeros(pixels.shape[:2], dtype=np.uint8)
    for forest_type_index, forest_color in enumerate(np.array(FOREST_TYPE_COLORS)):
        mask = np.zeros(pixels.shape[:2], dtype=bool)
        inaccuracy = np.sum(np.abs(pixels[:, :, :3] - forest_color), axis=-1)
        mask |= inaccuracy <= TOLERATED_TYPE_FOREST_COLOR_INACCURACY
        forest_types[mask] = forest_type_index + 1
    forest_types = transform_from_image_coordinates_to_map_coordinates(forest_types)
    return MapFullFragment(forest_types, FOREST_TYPE_MAP_NAME)


def load_forest_type_map(resource_manager: ResourceManager):
    return resource_manager.load_map_full_fragment(
        FOREST_TYPE_MAP_NAME, FOREST_TYPE_MAP_SCALE
    )


def compress_cluster_map_data(resource_manager: ResourceManager, scale):
    initial_data = load_forest_type_map(resource_manager, FOREST_TYPE_MAP_NAME).data
    data = compress_cluster_map_data(initial_data, scale)
    return MapFullFragment(data, "CompressedForestType")


def produce_forest_type_cluster_map(resource_manager: ResourceManager):
    data = load_forest_type_map(resource_manager, FOREST_TYPE_MAP_NAME).data.copy()
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
    return MapFullFragment(data, FOREST_TYPE_CLUSTER_MAP_NAME)


def load_forest_type_cluster_map(resource_manager: ResourceManager):
    return resource_manager.load_map_full_fragment(
        FOREST_TYPE_CLUSTER_MAP_NAME, FOREST_TYPE_MAP_SCALE
    )


def produce_canopy_height_map(resource_manager: ResourceManager, x, y, scale):
    input_file_path = f"CanopyHeight\\ETH_GlobalCanopyHeight_10m_2020_{'S' if y < 0 else 'N'}{abs(y):02d}{'W' if x < 0 else 'E'}{abs(x):03d}_Map.tif"
    data = resource_manager.load_tiff(input_file_path)
    initial_scale = data.shape[0] // CANOPY_HEIGHT_MAP_FRAGMENT_SIZE
    if initial_scale % scale != 0:
        raise Exception("Invalid scale.")
    data[data == 255] = 0
    data = compress_map_data(data, initial_scale // scale)
    canopy_heights = transform_from_tiff_coordinates_to_map_coordinates(data)
    return MapFragment(canopy_heights, CANOPY_HEIGHT_MAP_NAME, scale, x, y)


def load_canopy_height_map(resource_manager: ResourceManager, x, y, scale):
    return resource_manager.load_map_fragment(
        CANOPY_HEIGHT_MAP_NAME,
        scale,
        x,
        y,
        CANOPY_HEIGHT_MAP_FRAGMENT_SIZE,
        CANOPY_HEIGHT_MAP_FRAGMENT_SIZE,
    )
