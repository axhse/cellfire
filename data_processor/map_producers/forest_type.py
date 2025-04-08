import numpy as np

from map_fragment import FOREST_TYPE_MAP_NAME, FullMap
from resource_manager import ResourceManager
from transformation import collapse_prevalent_category_data, to_map_data, upscale_data

FOREST_TYPE_MAP_SCALE = 10

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
TOLERATED_FOREST_TYPE_COLOR_INACCURACY = 40


def produce_forest_type_map(resource_manager: ResourceManager):
    pixels = resource_manager.load_rgb_image("Land_cover_IGBP.png")
    data = np.zeros(pixels.shape[:2], dtype=np.uint8)
    for forest_type_index, forest_color in enumerate(np.array(FOREST_TYPE_COLORS)):
        mask = np.zeros(pixels.shape[:2], dtype=bool)
        inaccuracy = np.sum(np.abs(pixels[:, :, :3] - forest_color), axis=-1)
        mask |= inaccuracy <= TOLERATED_FOREST_TYPE_COLOR_INACCURACY
        data[mask] = forest_type_index + 1
    data = to_map_data(data)

    collapse_steps = [
        (1, 2),
        (1, 3),
        (1, 5),
        (2, 10),
        (10, 20),
        (10, 30),
        (10, 50),
        (10, 100),
        (10, 200),
        (10, 300),
        (10, 600),
        (10, 900),
        (10, 1800),
    ]

    collapsed_datasets = {1: data}
    for base_factor, target_factor in collapse_steps:
        base_data = collapsed_datasets[base_factor]
        collapsed_data = collapse_prevalent_category_data(
            base_data, target_factor // base_factor
        )
        stretched_data = upscale_data(collapsed_data, target_factor)

        mask = (data == 0) & (stretched_data != 0)
        data[mask] = stretched_data[mask]

        if target_factor in [step[0] for step in collapse_steps]:
            collapsed_datasets[target_factor] = collapsed_data
    return FullMap(data, FOREST_TYPE_MAP_NAME)


def load_forest_type_map(resource_manager: ResourceManager, compressed=False):
    return resource_manager.load_full_map(
        FOREST_TYPE_MAP_NAME, FOREST_TYPE_MAP_SCALE, compressed
    )
