import os.path

import numpy as np
import rasterio
from PIL import Image

from map_fragment import MapFragment
from transformation import compress_bytes, decompress_bytes, image_to_rgb_array


class ResourceManager:
    def __init__(self, input_dir_path, output_dir_path):
        self.__input_dir_path = input_dir_path
        self.__output_dir_path = output_dir_path

    def __get_full_input_path(self, input_file_path):
        return os.path.join(self.__input_dir_path, input_file_path)

    def load_image(self, file_path):
        return Image.open(self.__get_full_input_path(file_path))

    def load_tiff(self, file_path):
        with rasterio.open(self.__get_full_input_path(file_path)) as src:
            return src.read(1)

    def load_image_as_rgb_array(self, file_path):
        return image_to_rgb_array(self.load_image(file_path))

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
