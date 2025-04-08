from enum import Enum
from os import path

from map_fragment import FOREST_DENSITY_MAP_NAME, MapFragment
from resource_manager import ResourceManager
from transformation import downscale_data, stretch_vector, to_map_data

CANOPY_HEIGHT_INPUT_FOLDER_NAME = "GediCanopyHeight"
CANOPY_HEIGHT_REGION_SCALE = 4000


class Region(Enum):
    AUSTRALIA = "AUS"
    NORTH_AMERICA = "NAM"
    NORTH_AFRICA = "NAFR"
    NORTH_ASIA = "NASIA"
    SOUTH_AMERICA = "SAM"
    SOUTH_AFRICA = "SAFR"
    SOUTH_ASIA = "SASIA"

    @property
    def x(self):
        return self.__box[0]

    @property
    def y(self):
        return self.__box[1]

    @property
    def width(self):
        return self.__box[2]

    @property
    def height(self):
        return self.__box[3]

    @property
    def coordinates(self):
        return self.x, self.y

    @property
    def size(self):
        return self.width, self.height

    @property
    def __box(self):
        if self == Region.AUSTRALIA:
            return 112, -48, 68, 37
        if self == Region.NORTH_AMERICA:
            return -161, 13, 109, 39
        if self == Region.NORTH_AFRICA:
            return -26, 10, 89, 42
        if self == Region.NORTH_ASIA:
            return 63, 25, 96, 27
        if self == Region.SOUTH_AMERICA:
            return -88, -56, 54, 69
        if self == Region.SOUTH_AFRICA:
            return -15, -35, 66, 45
        if self == Region.SOUTH_ASIA:
            return 66, -11, 102, 36
        return 0, 0, 0, 0


def produce_forest_density_fragment(
    resource_manager: ResourceManager,
    region: Region,
    scale,
    transitional_scale,
    coordinates,
    size,
) -> MapFragment:
    if (
        CANOPY_HEIGHT_REGION_SCALE % transitional_scale != 0
        or transitional_scale % scale != 0
    ):
        raise Exception("Invalid transitional scale.")

    offset = (
        coordinates[0] - region.x,
        region.y + region.height - coordinates[1] - size[1],
    )
    offset = stretch_vector(offset, CANOPY_HEIGHT_REGION_SCALE)
    offset = tuple(2 + n for n in offset)
    size = stretch_vector(size, CANOPY_HEIGHT_REGION_SCALE)

    input_file_path = path.join(
        CANOPY_HEIGHT_INPUT_FOLDER_NAME, f"Forest_height_2019_{region.value}.tif"
    )
    data = resource_manager.load_downscaled_tiff(
        input_file_path,
        CANOPY_HEIGHT_REGION_SCALE // transitional_scale,
        CANOPY_HEIGHT_REGION_SCALE,
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

    return MapFragment(
        data, FOREST_DENSITY_MAP_NAME, scale, coordinates[0], coordinates[1]
    )


def produce_forest_density_region(
    resource_manager: ResourceManager, region: Region, scale, transitional_scale
) -> MapFragment:
    return produce_forest_density_fragment(
        resource_manager, region, scale, transitional_scale, (0, 0), region.size
    )


def load_forest_density_fragment(
    resource_manager: ResourceManager, scale, x, y, width, height, compressed=False
):
    return resource_manager.load_map_fragment(
        FOREST_DENSITY_MAP_NAME,
        scale,
        x,
        y,
        width,
        height,
        compressed,
    )
