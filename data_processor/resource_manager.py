import os.path

import numpy as np
import rasterio
from PIL import Image

from converters import image_to_rgb_array
from map_fragment import MapFragment


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

    def save_map_fragment(self, fragment):
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
        )
        with open(file_path, "wb") as file:
            data_bytes = fragment.data.astype(np.uint8).tobytes()
            file.write(data_bytes)

    def load_map_fragment(self, map_name, scale, x, y, width, height):
        file_path = self.__build_map_fragment_file_path(
            map_name, scale, x, y, width, height
        )
        with open(file_path, "rb") as file:
            data_bytes = file.read()
            data = np.frombuffer(data_bytes, dtype=np.uint8).reshape(
                (width * scale, height * scale)
            )
            return MapFragment(data, map_name, scale, x, y)

    def load_map_full_fragment(self, map_name, scale):
        return self.load_map_fragment(map_name, scale, -180, -90, 360, 180)

    def __build_map_dir_path(self, map_name):
        return os.path.join(self.__output_dir_path, map_name)

    def __build_map_fragment_file_path(self, map_name, scale, x, y, width, height):
        file_name = f"{'West' if x < 0 else 'East'}{abs(x)}{'South' if y < 0 else 'North'}{abs(y)}Width{width}Height{height}Scale{scale}.bin"
        return os.path.join(self.__build_map_dir_path(map_name), file_name)


def create_resource_directory(dir_path):
    if not os.path.isdir(dir_path):
        os.mkdir(dir_path)
