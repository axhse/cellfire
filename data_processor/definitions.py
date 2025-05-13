from typing import List, Tuple

import numpy as np

RgbImageData = np.ndarray
Data = np.ndarray
XY = Tuple[int, int]
Color = Tuple[int, int, int]

ELEVATION_MAP_NAME: str = "Elevation"
FOREST_TYPE_MAP_NAME: str = "ForestType"
FOREST_DENSITY_MAP_NAME: str = "ForestDensity"

FOREST_TYPE_COLORS: List[Color] = [
    (0, 128, 0),
    (0, 255, 0),
    (153, 204, 0),
    (153, 255, 153),
    (51, 153, 102),
]
