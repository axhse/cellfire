from typing import Optional, Union

import requests

from definitions import FOREST_DENSITY_MAP_NAME, XY, Data
from map_fragment import MapFragment
from resource_manager import ResourceManager
from transformation import (
    downscale_data,
    stack_data_tiles,
    stretch_vector,
    to_map_data,
    zeros,
)
from visual import Progress

LANGNICO_CANOPY_HEIGHT_DATASET_NAME: str = "LangnicoCanopyHeight"
LANGNICO_CANOPY_HEIGHT_TILE_SCALE: int = 12000
LANGNICO_CANOPY_HEIGHT_TILE_SIZE: XY = (3, 3)


def build_tile_file_name(position: XY) -> str:
    """
    Builds a file name of the input TIFF canopy height data file for a specified tile.
    Args:
        position (XY): The geographic coordinates of the left bottom corner of the tile.
    Returns:
        str: The file name.
    """
    x, y = position
    return f"ETH_GlobalCanopyHeight_10m_2020_{'S' if y < 0 else 'N'}{abs(y):02d}{'W' if x < 0 else 'E'}{abs(x):03d}_Map.tif"


def produce_forest_density_tile(
    source: Union[Data, ResourceManager],
    position: XY,
    scale: int,
    transitional_scale: int,
    progress: Optional[Progress] = None,
) -> MapFragment:
    """
    Produces a forest density map fragment from input TIFF canopy height data for a specified tile.
    Args:
        source (Union[Data, ResourceManager]): The resource manager or raw input data.
        position (XY): The geographic coordinates of the left bottom corner of the tile.
        scale (int): Desired map scale.
        transitional_scale (int): The scale to which input data is downscaled before converting to forest density.
        progress (Optional[Progress]): The progress reporter to log process steps.
    Returns:
        MapFragment: The forest density map fragment.
    Raises:
        ValueError: If specified transition scale is incompatible with desired scale or input data scale.
    """
    if (
        LANGNICO_CANOPY_HEIGHT_TILE_SCALE % transitional_scale != 0
        or transitional_scale % scale != 0
    ):
        raise ValueError("Invalid transitional scale.")

    progress = progress or Progress("Produce forest density tile", 2)

    if isinstance(source, Data):
        data = source
    else:
        progress.set_current_task("read input data")
        file_name = build_tile_file_name(position)
        data = source.load_tiff(LANGNICO_CANOPY_HEIGHT_DATASET_NAME, file_name)

    progress.count_performed_task()
    progress.set_current_task("process data")

    data = downscale_data(
        data,
        LANGNICO_CANOPY_HEIGHT_TILE_SCALE //
        transitional_scale)

    data[data == 255] = 0

    # 15m <= canopy height
    data[75 <= data] = 255
    # 5m <= canopy height < 15m
    mask = (25 <= data) & (data < 75)
    data[mask] = 255 * data[mask].astype(int) // 75
    # canopy height < 5m
    mask = data < 25
    data[mask] = 255 * data[mask].astype(int) * data[mask] // 25 // 25 // 3

    data = downscale_data(data, transitional_scale // scale)
    data = to_map_data(data)

    progress.count_performed_task()
    progress.end()

    return MapFragment(data, FOREST_DENSITY_MAP_NAME, scale, position)


def download_forest_density_tile(
    position: XY, scale: int, transitional_scale: int, silent=False
) -> Optional[MapFragment]:
    """
    Downloads input canopy height data for a specified tile and produces a forest density map fragment from it.
    Args:
        position (XY): The geographic coordinates of the left bottom corner of the tile.
        scale (int): Desired map fragment scale.
        transitional_scale (int): The scale to which input data is downscaled before converting to forest density.
        silent (bool): True to log the progress, else False.
    Returns:
        Optional[MapFragment]: The forest density map fragment if the input tile data exists, else None.
    """
    progress = Progress("Download forest density tile", 3, silent=silent)
    progress.start()
    progress.set_current_task("download data")

    file_name = build_tile_file_name(position)
    url = f"https://libdrive.ethz.ch/index.php/s/cO8or7iOe5dT2Rt/download?path=%2F3deg_cogs&files={file_name}"
    response = requests.get(url)
    if response.status_code == 404:
        progress.error("tile does not exist")
        return None
    response.raise_for_status()
    data = ResourceManager.read_tiff(response.content)
    progress.count_performed_task()
    return produce_forest_density_tile(
        data, position, scale, transitional_scale, progress
    )


def save_downloaded_forest_density_tiles(
    resource_manager: ResourceManager,
    scale: int,
    transitional_scale: int,
    start_position: XY,
    size: XY,
    save_zero_tiles=False,
) -> None:
    """
    Downloads input canopy height data for specified tiles, produce and save forest density map fragments.
    Args:
        resource_manager (ResourceManager): The resource manager.
        scale (int): Desired map fragment scale.
        transitional_scale (int): The scale to which input data is downscaled before converting to forest density.
        start_position (XY): The geographic coordinates of left bottom corner of the starting tile.
        size (int): The width and height of the desired area in geographic coordinates.
        save_zero_tiles (bool): True to save zero map fragments for non-existent tiles, else False.
    """
    x_range = range(start_position[0], start_position[0] + size[0], 3)
    y_range = range(start_position[1], start_position[1] + size[1], 3)

    progress = Progress(
        "Save downloaded forest density tiles",
        len(x_range) * len(y_range),
        "tiles not found: " +
        ("zero tiles saved" if save_zero_tiles else "skipped"),
    )
    progress.start()

    for y in y_range:
        for x in x_range:
            position = (x, y)
            progress.set_current_task(position)
            tile = download_forest_density_tile(
                position, scale, transitional_scale, silent=True
            )
            if not tile:
                progress.add_failure(position)
                if save_zero_tiles:
                    tile = MapFragment(
                        zeros(
                            stretch_vector(
                                LANGNICO_CANOPY_HEIGHT_TILE_SIZE,
                                scale)),
                        FOREST_DENSITY_MAP_NAME,
                        scale,
                        position,
                    )
            if tile:
                resource_manager.save_fragment(tile)
            progress.count_performed_task()
    progress.end()


def save_forest_density_tile_combination(
    resource_manager: ResourceManager, scale: int, start_position: XY, size: XY
) -> MapFragment:
    """
    Combines already saved map fragment tiles and saves the produced map fragment.
    Args:
        resource_manager (ResourceManager): The resource manager.
        scale (int): The scale of saved map fragments.
        start_position (XY): The geographic coordinates of left bottom corner of the starting tile.
        size (int): The width and height of the desired area in geographic coordinates.
    Returns:
        MapFragment: The forest density map fragment.
    """
    x_range = range(start_position[0], start_position[0] + size[0], 3)
    y_range = range(start_position[1], start_position[1] + size[1], 3)

    tiles = list()
    for y in y_range:
        for x in x_range:
            fragment = load_forest_density_tile(
                resource_manager, (x, y), scale)
            tiles.append(fragment.data)
    data = stack_data_tiles(tiles, len(x_range))

    combined_fragment = MapFragment(
        data, FOREST_DENSITY_MAP_NAME, scale, start_position
    )
    resource_manager.save_fragment(combined_fragment)
    return combined_fragment


def load_forest_density_tile(
    resource_manager: ResourceManager,
    position: XY,
    scale: int,
    compressed: bool = True,
) -> MapFragment:
    """
    Loads a forest density map fragment with specified boundaries.
    Args:
        resource_manager (ResourceManager): The resource manager.
        position (XY): The geographic coordinates of the left bottom corner of the fragment.
        scale (int): The scale of saved map fragment.
        compressed (bool): Whether to load the fragment in a compressed format.
    Returns:
        MapFragment: The map fragment.
    """
    return resource_manager.load_fragment(
        FOREST_DENSITY_MAP_NAME,
        scale,
        position,
        LANGNICO_CANOPY_HEIGHT_TILE_SIZE,
        compressed,
    )
