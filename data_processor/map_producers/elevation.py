from definitions import ELEVATION_MAP_NAME, XY
from map_fragment import FullMap, MapFragment
from resource_manager import ResourceManager
from transformation import downscale_data, stack_data_tiles, to_map_data
from visual import Progress

ELEVATION_DATASET_NAME: str = "Elevation"
ELEVATION_TILE_SCALE: int = 120
ELEVATION_TILE_SIZE: int = 90


def produce_elevation_map(
    resource_manager: ResourceManager, scale: int = ELEVATION_TILE_SCALE
) -> MapFragment:
    """
    Produces an elevation map from 8 input elevation TIFF tiles.
    Args:
        resource_manager (ResourceManager): The resource manager.
        scale (int): Desired map scale.
    Returns:
        MapFragment: The elevation map.
    Raises:
        ValueError: If the desired scale is incompatible with input data scale.
    """
    x_range, y_range = list("ABCD"), [2, 1]

    progress = Progress(
        "Produce elevation map",
        len(x_range) * len(y_range) + 1)
    progress.start()

    tiles = list()
    for y in y_range:
        for x in x_range:
            progress.set_current_task(f"process tile {x}{y}")
            data = resource_manager.load_tiff(
                ELEVATION_DATASET_NAME, f"gebco_08_rev_elev_{x}{y}_grey_geo.tif"
            )

            initial_scale = data.shape[0] // ELEVATION_TILE_SIZE
            if initial_scale % scale != 0:
                raise ValueError("Invalid scale.")
            data = downscale_data(data, initial_scale // scale)
            data = to_map_data(data)
            tiles.append(data)
            progress.count_performed_task()

    progress.set_current_task(f"combine tiles")
    data = stack_data_tiles(tiles, 4)
    progress.count_performed_task()

    progress.end()
    return FullMap(data, ELEVATION_MAP_NAME)


def load_elevation_fragment(
    resource_manager: ResourceManager,
    position: XY,
    size: XY,
    scale: int = ELEVATION_TILE_SCALE,
    compressed: bool = True,
) -> MapFragment:
    """
    Loads an elevation map fragment with specified boundaries.
    Args:
        resource_manager (ResourceManager): The resource manager.
        position (XY): The geographic coordinates of the left bottom corner of the fragment.
        size (XY): The width and height of the fragment in geographic coordinates.
        scale (int): The scale of saved map fragment.
        compressed (bool): Whether to load the fragment in a compressed format.
    Returns:
        MapFragment: The map fragment.
    """
    return resource_manager.load_fragment(
        ELEVATION_MAP_NAME, scale, position, size, compressed
    )


def save_produced_elevation_map(
        resource_manager: ResourceManager) -> MapFragment:
    """
    Produces and saves an elevation map.
    Args:
        resource_manager (ResourceManager): The resource manager.
    Returns:
        MapFragment: The map.
    """
    fragment = produce_elevation_map(resource_manager)
    resource_manager.save_fragment(fragment)
    return fragment


def load_elevation_map(
    resource_manager: ResourceManager,
    scale: int = ELEVATION_TILE_SCALE,
    compressed: bool = True,
) -> MapFragment:
    """
    Loads an elevation map.
    Args:
        resource_manager (ResourceManager): The resource manager.
        scale (int): The scale of saved map fragment.
        compressed (bool): Whether to load the map in a compressed format.
    Returns:
        MapFragment: The map.
    """
    return resource_manager.load_map(ELEVATION_MAP_NAME, scale, compressed)
