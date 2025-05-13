import numpy as np

from definitions import FOREST_TYPE_COLORS, FOREST_TYPE_MAP_NAME
from map_fragment import FullMap, MapFragment
from resource_manager import ResourceManager
from transformation import squeeze_category_data, to_map_data, upscale_data
from visual import Progress

LAND_CORER_DATASET_NAME: str = "LandCover"
FOREST_TYPE_MAP_SCALE: int = 10
TOLERATED_FOREST_TYPE_COLOR_INACCURACY: int = 40


def produce_forest_type_map(resource_manager: ResourceManager) -> MapFragment:
    """
    Produces a prevalent forest type map from input land cover classification map image.
    Args:
        resource_manager (ResourceManager): The resource manager.
    Returns:
        MapFragment: The forest type map.
    """
    squeeze_steps = [
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

    progress = Progress("Produce forest type map", len(squeeze_steps))
    progress.start()

    pixels = resource_manager.load_rgb_image(
        LAND_CORER_DATASET_NAME, "Land_cover_IGBP.png"
    )
    data = np.zeros(pixels.shape[:2], dtype=np.uint8)
    for forest_type_index, forest_color in enumerate(
            np.array(FOREST_TYPE_COLORS)):
        mask = np.zeros(pixels.shape[:2], dtype=bool)
        inaccuracy = np.sum(np.abs(pixels[:, :, :3] - forest_color), axis=-1)
        mask |= inaccuracy <= TOLERATED_FOREST_TYPE_COLOR_INACCURACY
        data[mask] = forest_type_index + 1
    data = to_map_data(data)

    squeezed_data_collection = {1: data}
    for base_factor, target_factor in squeeze_steps:
        progress.set_current_task(
            f"squeeze from factor of {base_factor} to {target_factor}"
        )
        base_data = squeezed_data_collection[base_factor]
        squeezed_data = squeeze_category_data(
            base_data, target_factor // base_factor)
        stretched_data = upscale_data(squeezed_data, target_factor)

        mask = (data == 0) & (stretched_data != 0)
        data[mask] = stretched_data[mask]

        if target_factor in [step[0] for step in squeeze_steps]:
            squeezed_data_collection[target_factor] = squeezed_data
        progress.count_performed_task()

    progress.end()
    return FullMap(data, FOREST_TYPE_MAP_NAME)


def save_produced_forest_type_map(
        resource_manager: ResourceManager) -> MapFragment:
    """
    Produces and saves a prevalent forest type map.
    Args:
        resource_manager (ResourceManager): The resource manager.
    Returns:
        MapFragment: The map.
    """
    fragment = produce_forest_type_map(resource_manager)
    resource_manager.save_fragment(fragment)
    return fragment


def load_forest_type_map(
    resource_manager: ResourceManager, compressed: bool = True
) -> MapFragment:
    """
    Loads a prevalent forest type map.
    Args:
        resource_manager (ResourceManager): The resource manager.
        compressed (bool): Whether to load the map in a compressed format.
    Returns:
        MapFragment: The map.
    """
    return resource_manager.load_map(
        FOREST_TYPE_MAP_NAME, FOREST_TYPE_MAP_SCALE, compressed
    )
