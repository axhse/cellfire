import os.path

import numpy as np
import rasterio
from PIL import Image
from rasterio.windows import Window

from map_fragment import MapFragment
from transformation import (
    compress_bytes,
    decompress_bytes,
    downscale_data,
    image_to_rgb_array,
    stack_tiles,
)


class ResourceManager:
    def __init__(self, input_dir_path, output_dir_path):
        self.__input_dir_path = input_dir_path
        self.__output_dir_path = output_dir_path

    def load_rgb_image(self, file_path):
        image = Image.open(self.__get_full_input_path(file_path))
        return image_to_rgb_array(image)

    def save_tiff(self, file_path, content):
        with open(self.__get_full_input_path(file_path), 'wb') as src:
            src.write(content)

    def load_tiff(self, file_path):
        with rasterio.open(self.__get_full_input_path(file_path)) as src:
            return src.read(1)

    def load_tiff_fragment(self, file_path, x, y, width, height):
        with rasterio.open(self.__get_full_input_path(file_path)) as src:
            window = Window(x, y, width, height)
            return src.read(1, window=window)

    def load_downscaled_tiff(
        self, input_file_path, downscale_factor, tile_size, offset=None, size=None
    ):
        with rasterio.open(self.__get_full_input_path(input_file_path)) as src:
            offset = offset or (0, 0)
            if (
                min(offset) < 0
                or offset[0] + tile_size > src.width
                or offset[1] + tile_size > src.height
            ):
                raise Exception("Invalid offset.")
            size = size or (src.width - offset[0], src.height - offset[1])
            if size[0] > src.width - offset[0] or size[1] > src.height - offset[1]:
                raise Exception("Invalid size.")
            tiles = list()

            i_range = range(offset[0] + tile_size, offset[0] + size[0] + 1, tile_size)
            j_range = range(offset[1] + tile_size, offset[1] + size[1] + 1, tile_size)
            for i in i_range:
                for j in j_range:
                    window = Window(i - tile_size, j - tile_size, tile_size, tile_size)
                    tile = src.read(1, window=window)
                    tile = downscale_data(tile, downscale_factor)
                    tiles.append(tile)

            return stack_tiles(tiles, len(j_range))

    def save_map_fragment(self, fragment, compressed=False):
        if np.any(fragment.data < 0) or np.any(255 < fragment.data):
            raise Exception("Fragment can not be stored in byte format.")

        dir_path = self.__build_map_dir_path(fragment.map_name)
        if not os.path.isdir(dir_path):
            os.mkdir(dir_path)

        file_path = self.__build_map_fragment_file_path(
            fragment.map_name,
            fragment.scale,
            fragment.x,
            fragment.y,
            fragment.width,
            fragment.height,
            compressed,
        )

        with open(file_path, "wb") as file:
            data_bytes = fragment.data.astype(np.uint8).tobytes()
            if compressed:
                data_bytes = compress_bytes(data_bytes)
            file.write(data_bytes)

    def load_map_fragment(self, map_name, scale, x, y, width, height, compressed=False):
        file_path = self.__build_map_fragment_file_path(
            map_name, scale, x, y, width, height, compressed
        )

        with open(file_path, "rb") as file:
            data_bytes = file.read()
            if compressed:
                data_bytes = decompress_bytes(data_bytes)
            data = np.frombuffer(data_bytes, dtype=np.uint8).reshape(
                (width * scale, height * scale)
            )
            return MapFragment(data, map_name, scale, x, y)

    def load_full_map(self, map_name, scale, compressed=False):
        return self.load_map_fragment(map_name, scale, -180, -90, 360, 180, compressed)

    def __get_full_input_path(self, input_file_path):
        return os.path.join(self.__input_dir_path, input_file_path)

    def __build_map_dir_path(self, map_name):
        return os.path.join(self.__output_dir_path, map_name)

    def __build_map_fragment_file_path(
        self, map_name, scale, x, y, width, height, compressed
    ):
        file_name = f"x{scale}"
        if width < 360:
            file_name += f"_lon{x}+{width}"
        if height < 180:
            file_name += f"_lat{y}+{height}"
        file_name += ".bin"
        if compressed:
            file_name += ".z"

        return os.path.join(self.__build_map_dir_path(map_name), file_name)


def make_sure_directory_exists(dir_path):
    if not os.path.isdir(dir_path):
        os.mkdir(dir_path)
