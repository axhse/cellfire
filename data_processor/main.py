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


if __name__ == "__main__":
    manager = ResourceManager("input", "output")
    produce_and_save_forest_density_region_tiles(manager, Region.NORTH_AMERICA, (6, 3))
    # map_fragment = combine_and_save_forest_density_region_tiles(manager, Region.NORTH_AMERICA)
    # draw_forest_density(map_fragment)
