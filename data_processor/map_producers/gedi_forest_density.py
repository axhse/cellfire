from enum import Enum
from typing import Optional, Tuple

from definitions import FOREST_DENSITY_MAP_NAME, XY
from map_fragment import MapFragment
from resource_manager import ResourceManager
from transformation import downscale_data, stack_data_tiles, stretch_vector, to_map_data
from visual import Progress

GEDI_CANOPY_HEIGHT_DATASET_NAME: str = "GediCanopyHeight"
GEDI_CANOPY_HEIGHT_REGION_SCALE: int = 4000


class Region(Enum):
    """Describes input TIFF region."""

    AUSTRALIA = "AUS"
    NORTH_AMERICA = "NAM"
    NORTH_AFRICA = "NAFR"
    NORTH_ASIA = "NASIA"
    SOUTH_AMERICA = "SAM"
    SOUTH_AFRICA = "SAFR"
    SOUTH_ASIA = "SASIA"

    @property
    def position(self) -> XY:
        return self.__box[0]

    @property
    def x(self) -> int:
        return self.position[0]

    @property
    def y(self) -> int:
        return self.position[1]

    @property
    def size(self) -> XY:
        return self.__box[1]

    @property
    def width(self) -> int:
        return self.size[0]

    @property
    def height(self) -> int:
        return self.size[1]

    @property
    def __box(self) -> Tuple[XY, XY]:
        if self == Region.AUSTRALIA:
            return (112, -48), (68, 37)
        if self == Region.NORTH_AMERICA:
            return (-161, 13), (109, 39)
        if self == Region.NORTH_AFRICA:
            return (-26, 10), (89, 42)
        if self == Region.NORTH_ASIA:
            return (63, 25), (96, 27)
        if self == Region.SOUTH_AMERICA:
            return (-88, -56), (54, 69)
        if self == Region.SOUTH_AFRICA:
            return (-15, -35), (66, 45)
        if self == Region.SOUTH_ASIA:
            return (66, -11), (102, 36)
        return (0, 0), (0, 0)


def produce_forest_density_regional_fragment(
    resource_manager: ResourceManager,
    region: Region,
    position: XY,
    size: XY,
    scale: int,
    transitional_scale: int,
) -> MapFragment:
    """
    Produces a forest density map fragment from input TIFF canopy height data for a specified region.
    Args:
        resource_manager (ResourceManager): The resource manager.
        region (Region): The region.
        position (XY): The geographic coordinates of the left bottom corner of the fragment.
        size (XY): The width and height of the fragment in geographic coordinates.
        scale (int): Desired map scale.
        transitional_scale (int): The scale to which input data is downscaled before converting to forest density.
    Returns:
        MapFragment: The forest density map fragment.
    Raises:
        ValueError: If specified transition scale is incompatible with desired scale or input data scale.
    """
    if (
        GEDI_CANOPY_HEIGHT_REGION_SCALE % transitional_scale != 0
        or transitional_scale % scale != 0
    ):
        raise ValueError("Invalid transitional scale.")

    offset = (
        position[0] - region.x,
        region.y + region.height - position[1] - size[1],
    )
    offset = stretch_vector(offset, GEDI_CANOPY_HEIGHT_REGION_SCALE)
    offset = (offset[0] + 2, offset[1] + 2)
    size = stretch_vector(size, GEDI_CANOPY_HEIGHT_REGION_SCALE)

    data = resource_manager.load_downscaled_tiff_fragment(
        GEDI_CANOPY_HEIGHT_DATASET_NAME,
        f"Forest_height_2019_{region.value}.tif",
        GEDI_CANOPY_HEIGHT_REGION_SCALE // transitional_scale,
        GEDI_CANOPY_HEIGHT_REGION_SCALE,
        offset,
        size,
    )

    data[60 < data] = 0

    # 15m <= canopy height
    data[15 <= data] = 255
    # 5m <= canopy height < 15m
    mask = (5 <= data) & (data < 15)
    data[mask] = 255 * data[mask].astype(int) // 15
    # canopy height < 5m
    mask = data < 5
    data[mask] = 255 * data[mask].astype(int) * data[mask] // 5 // 5 // 3

    data = downscale_data(data, transitional_scale // scale)
    data = to_map_data(data)

    return MapFragment(data, FOREST_DENSITY_MAP_NAME, scale, position)


def save_produced_forest_density_regional_tiles(
    resource_manager: ResourceManager,
    region: Region,
    scale: int,
    transitional_scale: int,
    start_position: Optional[XY] = None,
    tile_size: int = 5,
) -> None:
    """
    Produce and save forest density map fragment for a specified region by tiles.
    Args:
        resource_manager (ResourceManager): The resource manager.
        region (Region): The region.
        scale (int): Desired map scale.
        transitional_scale (int): The scale to which input data is downscaled before converting to forest density.
        start_position (XY): The geographic coordinates of left bottom corner of the starting tile.
        tile_size (int): The width and height of each tile in geographic coordinates.
    """
    start_position = start_position or region.position
    start_x, start_y = start_position
    x_range = range(start_x, region.x + region.width, tile_size)
    y_range = range(start_y, region.y + region.height, tile_size)

    progress = Progress(
        "Save produced forest density regional tiles", len(
            x_range) * len(y_range)
    )
    progress.start()

    for x in x_range:
        for y in y_range:
            position = (x, y)
            width = min(tile_size, region.x + region.width - x)
            height = min(tile_size, region.y + region.height - y)
            progress.set_current_task(f"{position}{[width, height]}")
            fragment = produce_forest_density_regional_fragment(
                resource_manager,
                region,
                position,
                (width, height),
                scale,
                transitional_scale,
            )
            resource_manager.save_fragment(fragment)
            progress.count_performed_task()

    progress.end()


def save_forest_density_regional_tile_combination(
    resource_manager: ResourceManager, region: Region, scale: int, tile_size: int = 5
) -> MapFragment:
    """
    Combines already saved map fragment tiles for a specified region and saves the produced map fragment.
    Args:
        resource_manager (ResourceManager): The resource manager.
        region (Region): The region.
        scale (int): The scale of saved map fragments.
        tile_size (int): The width and height of each tile in geographic coordinates.
    Returns:
        MapFragment: The forest density map fragment.
    """
    x_range = range(region.x, region.x + region.width, tile_size)
    y_range = range(region.y, region.y + region.height, tile_size)
    tiles = list()
    for y in y_range:
        for x in x_range:
            width = min(tile_size, region.x + region.width - x)
            height = min(tile_size, region.y + region.height - y)
            fragment = load_forest_density_fragment(
                resource_manager, (x, y), (width, height), scale
            )
            tiles.append(fragment.data)
    data = stack_data_tiles(tiles, len(x_range))
    region_fragment = MapFragment(
        data,
        FOREST_DENSITY_MAP_NAME,
        scale,
        region.position)
    resource_manager.save_fragment(region_fragment)
    return region_fragment


def load_forest_density_fragment(
    resource_manager: ResourceManager,
    position: XY,
    size: XY,
    scale: int,
    compressed: bool = True,
) -> MapFragment:
    """
    Loads a forest density map fragment with specified boundaries.
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
        FOREST_DENSITY_MAP_NAME,
        scale,
        position,
        size,
        compressed,
    )


def load_forest_density_region(
    resource_manager: ResourceManager,
    region: Region,
    scale: int,
    compressed: bool = True,
) -> MapFragment:
    """
    Loads a forest density map fragment for specified region.
    Args:
        resource_manager (ResourceManager): The resource manager.
        region (Region): The region.
        scale (int): The scale of saved map fragment.
        compressed (bool): Whether to load the fragment in a compressed format.
    Returns:
        MapFragment: The map fragment.
    """
    return resource_manager.load_fragment(
        FOREST_DENSITY_MAP_NAME,
        scale,
        region.position,
        region.size,
        compressed,
    )
