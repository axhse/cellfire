from datetime import datetime

from map_producers.elevation import *
from map_producers.forest_type import *
from map_producers.gedi_forest_density import *
from map_producers.langnico_forest_density import *
from resource_manager import ResourceManager
from visual import *

SCALE = 200
TRANSITIONAL_SCALE = 1000


def produce_and_save_elevation_map(resource_manager: ResourceManager):
    fragment = produce_elevation_map(resource_manager)
    fragment = fragment.cut(-180, -56, 360, 70 - -56)
    resource_manager.save_map_fragment(fragment)
    return fragment


def produce_and_save_forest_type_map(resource_manager: ResourceManager):
    fragment = produce_forest_type_map(resource_manager)
    resource_manager.save_map_fragment(fragment)
    return fragment


def produce_and_save_forest_density_tile(resource_manager: ResourceManager):
    fragment = produce_forest_density_tile(
        resource_manager, SCALE, TRANSITIONAL_SCALE, (0, 0)
    )
    resource_manager.save_map_fragment(fragment)
    return fragment


def produce_and_save_forest_density_fragment(resource_manager: ResourceManager):
    fragment = produce_forest_density_fragment(
        resource_manager,
        Region.NORTH_AFRICA,
        SCALE,
        TRANSITIONAL_SCALE,
        (8, 38),
        (2, 4),
    )
    resource_manager.save_map_fragment(fragment)
    return fragment


def produce_and_save_forest_density_region_tiles(
    resource_manager: ResourceManager, region: Region, start_tile=(0, 0)
):
    x_range = range(region.x, region.x + region.width, 10)
    y_range = range(region.y, region.y + region.height, 10)
    tile_quantity = len(x_range) * len(y_range)
    tile_index = 0
    for xi, x in enumerate(x_range):
        for yi, y in enumerate(y_range):
            tile_index += 1
            if xi < start_tile[0] or xi == start_tile[0] and yi < start_tile[1]:
                continue
            print(
                f"{datetime.now().strftime('%H:%M:%S')}    In progress:  {(xi, yi)}  {tile_index}/{tile_quantity}"
            )
            width = min(10, region.x + region.width - x)
            height = min(10, region.y + region.height - y)
            fragment = produce_forest_density_fragment(
                resource_manager,
                region,
                SCALE,
                TRANSITIONAL_SCALE,
                (x, y),
                (width, height),
            )
            resource_manager.save_map_fragment(fragment)


def combine_and_save_forest_density_region_tiles(
    resource_manager: ResourceManager, region: Region
):
    x_range = range(region.x, region.x + region.width, 10)
    y_range = range(region.y, region.y + region.height, 10)
    tiles = list()
    for y in y_range:
        for x in x_range:
            width = min(10, region.x + region.width - x)
            height = min(10, region.y + region.height - y)
            fragment = load_forest_density_fragment(
                resource_manager, SCALE, x, y, width, height
            )
            tiles.append(fragment.data)
    data = stack_tiles(tiles, len(x_range))
    region_fragment = MapFragment(
        data, FOREST_DENSITY_MAP_NAME, SCALE, region.x, region.y
    )
    resource_manager.save_map_fragment(region_fragment)
    return region_fragment


def load_and_draw_forest_type_map(resource_manager: ResourceManager):
    draw_forest_type(load_forest_type_map(resource_manager))


def produce_and_save_forest_density_tiles(resource_manager: ResourceManager, coordinates, size):
    x_range = range(coordinates[0], coordinates[0] + size[0], 3)
    y_range = range(coordinates[1], coordinates[1] + size[1], 3)
    file_quantity = len(x_range) * len(y_range)
    file_index = 0
    for y in y_range:
        for x in x_range:
            file_index += 1
            print(
                f"{datetime.now().strftime('%H:%M:%S')}    In progress:  {(x, y)}  {file_index}/{file_quantity}"
            )
            try:
                fragment = produce_forest_density_tile(resource_manager, 200, 1000, (x, y))
                resource_manager.save_map_fragment(fragment)
            except Exception as exception:
                print(
                    f"Failed:  {(x, y)}  {exception}"
                )


def download_forest_density_input_files(resource_manager: ResourceManager, coordinates, size):
    x_range = range(coordinates[0], coordinates[0] + size[0], 3)
    y_range = range(coordinates[1], coordinates[1] + size[1], 3)
    file_quantity = len(x_range) * len(y_range)
    file_index = 0
    for y in y_range:
        for x in x_range:
            file_index += 1
            print(
                f"{datetime.now().strftime('%H:%M:%S')}    In progress:  {(x, y)}  {file_index}/{file_quantity}"
            )
            if not download_forest_density_input_file(resource_manager, x, y):
                print(
                    f"Not found:  {(x, y)}"
                )


def produce_and_save_zero_forest_density_tiles(resource_manager: ResourceManager, coordinates, size):
    x_range = range(coordinates[0], coordinates[0] + size[0], 3)
    y_range = range(coordinates[1], coordinates[1] + size[1], 3)
    for y in y_range:
        for x in x_range:
            try:
                load_forest_density_tile(resource_manager, 200, x, y)
            except Exception:
                data = np.zeros((200 * 3, 200 * 3), dtype=np.uint8)
                fragment = MapFragment(data, FOREST_DENSITY_MAP_NAME, 200, x, y)
                resource_manager.save_map_fragment(fragment)


def combine_and_save_forest_density_tiles(resource_manager: ResourceManager, coordinates, size):
    x_range = range(coordinates[0], coordinates[0] + size[0], 3)
    y_range = range(coordinates[1], coordinates[1] + size[1], 3)
    tiles = list()
    for y in y_range:
        for x in x_range:
            fragment = load_forest_density_tile(resource_manager, 200, x, y)
            tiles.append(fragment.data)
    data = stack_tiles(tiles, len(x_range))
    combined_fragment = MapFragment(
        data, FOREST_DENSITY_MAP_NAME, 200, coordinates[0], coordinates[1]
    )
    # draw_forest_density(combined_fragment)
    resource_manager.save_map_fragment(combined_fragment)


if __name__ == "__main__":
    manager = ResourceManager("input", "output")
    # produce_and_save_forest_density_region_tiles(manager, Region.SOUTH_AMERICA, (4, 4))
    # map_fragment = combine_and_save_forest_density_region_tiles(manager, Region.SOUTH_AMERICA)
    # download_forest_density_input_files(manager, (-180, 69), (360, 3))
    # produce_and_save_forest_density_tiles(manager, (-180, 69), (360, 3))
    # produce_and_save_zero_forest_density_tiles(manager, (-180, 51), (360, 72 - 51))
    # combine_and_save_forest_density_tiles(manager, (-180, 51), (360, 72 - 51))
    manager.save_map_fragment(produce_elevation_map(manager).cut(-180, -56, 360, 72 + 56))
    draw_elevation(produce_elevation_map(manager).cut(-180, -56, 360, 72 + 56))
    # draw_forest_type(produce_and_save_forest_type_map(manager))
