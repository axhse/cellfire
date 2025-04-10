from os import path

from map_fragment import FOREST_DENSITY_MAP_NAME, MapFragment
from resource_manager import ResourceManager
from transformation import downscale_data, to_map_data

CANOPY_HEIGHT_INPUT_FOLDER_NAME = "LangnicoCanopyHeight"
CANOPY_HEIGHT_TILE_SCALE = 12000
CANOPY_HEIGHT_TILE_SIZE = 3


def produce_forest_density_tile(
    resource_manager: ResourceManager, scale, transitional_scale, coordinates
):
    if (
        CANOPY_HEIGHT_TILE_SCALE % transitional_scale != 0
        or transitional_scale % scale != 0
    ):
        raise Exception("Invalid transitional scale.")

    x, y = coordinates

    input_file_path = path.join(
        CANOPY_HEIGHT_INPUT_FOLDER_NAME,
        f"ETH_GlobalCanopyHeight_10m_2020_{'S' if y < 0 else 'N'}{abs(y):02d}{'W' if x < 0 else 'E'}{abs(x):03d}_Map.tif",
    )
    data = resource_manager.load_tiff(input_file_path)
    data = downscale_data(data, CANOPY_HEIGHT_TILE_SCALE // transitional_scale)

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
    return MapFragment(data, FOREST_DENSITY_MAP_NAME, scale, x, y)


def load_forest_density_tile(
    resource_manager: ResourceManager, scale, x, y, compressed=False
):
    return resource_manager.load_map_fragment(
        FOREST_DENSITY_MAP_NAME,
        scale,
        x,
        y,
        CANOPY_HEIGHT_TILE_SIZE,
        CANOPY_HEIGHT_TILE_SIZE,
        compressed,
    )
