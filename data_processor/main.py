from map_producers.elevation import *
from map_producers.forest_type import *
from map_producers.gedi_forest_density import *
from map_producers.langnico_forest_density import *
from resource_manager import ResourceManager
from visual import *

if __name__ == "__main__":
    resource_manager = ResourceManager("input", "output")

    fragment = produce_forest_type_map(resource_manager)
    draw_forest_type(fragment)
