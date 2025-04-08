from map_producers.elevation import *
from map_producers.forest_type import *
from map_producers.gedi_forest_density import *
from map_producers.langnico_forest_density import *
from resource_manager import ResourceManager
from visual import *


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
    fragment = produce_forest_density_tile(resource_manager, 200, 1000, (0, 0))
    resource_manager.save_map_fragment(fragment)
    return fragment


def produce_and_save_forest_density_fragment(resource_manager: ResourceManager):
    fragment = produce_forest_density_fragment(
        resource_manager, Region.NORTH_AFRICA, 200, 1000, (8, 38), (2, 4)
    )
    resource_manager.save_map_fragment(fragment)
    return fragment


def load_and_draw_forest_type_map(resource_manager: ResourceManager):
    draw_forest_type(load_forest_type_map(resource_manager))


if __name__ == "__main__":
    manager = ResourceManager("input", "output")
    produce_and_save_forest_density_fragment(manager)
