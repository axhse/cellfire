import os.path
from io import BytesIO
from typing import Optional

import numpy as np
import rasterio
from PIL import Image
from rasterio.windows import Window

from definitions import XY, Data, RgbImageData
from map_fragment import MapFragment
from transformation import (
    compress_bytes,
    decompress_bytes,
    downscale_data,
    image_to_rgb_array,
    stack_data_tiles,
)


class ResourceManager:
    def __init__(self, input_dir_path: str, output_dir_path: str) -> None:
        self.__input_dir_path: str = input_dir_path
        self.__output_dir_path: str = output_dir_path

    @staticmethod
    def read_tiff(content: bytes) -> Data:
        with BytesIO(content) as stream:
            with rasterio.open(stream) as src:
                return src.read(1)

    def load_rgb_image(self, dataset_name: str,
                       file_name: str) -> RgbImageData:
        image = Image.open(self.__input_path(dataset_name, file_name))
        return image_to_rgb_array(image)

    def save_tiff(self, dataset_name: str, file_name: str,
                  content: bytes) -> None:
        input_folder = self.__input_folder(dataset_name)
        ResourceManager.__make_dir_if_not_exists(input_folder)
        with open(self.__input_path(dataset_name, file_name), "wb") as src:
            src.write(content)

    def load_tiff(self, dataset_name: str, file_name: str) -> Data:
        with rasterio.open(self.__input_path(dataset_name, file_name)) as src:
            return src.read(1)

    def load_tiff_fragment(
        self, dataset_name: str, file_name: str, offset: XY, size: XY
    ) -> Data:
        with rasterio.open(self.__input_path(dataset_name, file_name)) as src:
            window = Window(*offset, *size)
            return src.read(1, window=window)

    def load_downscaled_tiff_fragment(
        self,
        dataset_name: str,
        file_name: str,
        downscale_factor: int,
        window_size: int,
        offset: Optional[XY] = None,
        size: Optional[XY] = None,
    ) -> Data:
        with rasterio.open(self.__input_path(dataset_name, file_name)) as src:
            offset = offset or (0, 0)
            if (
                min(offset) < 0
                or offset[0] + window_size > src.width
                or offset[1] + window_size > src.height
            ):
                raise Exception("Invalid offset.")
            size = size or (src.width - offset[0], src.height - offset[1])
            if size[0] > src.width - \
                    offset[0] or size[1] > src.height - offset[1]:
                raise Exception("Invalid size.")

            tiles = list()
            i_range = range(
                offset[0] + window_size, offset[0] + size[0] + 1, window_size
            )
            j_range = range(
                offset[1] + window_size, offset[1] + size[1] + 1, window_size
            )
            for i in i_range:
                for j in j_range:
                    window = Window(
                        i - window_size, j - window_size, window_size, window_size
                    )
                    tile = src.read(1, window=window)
                    tile = downscale_data(tile, downscale_factor)
                    tiles.append(tile)

            return stack_data_tiles(tiles, len(j_range))

    def save_fragment(self, fragment: MapFragment,
                      compressed: bool = True) -> None:
        if np.any(fragment.data < 0) or np.any(255 < fragment.data):
            raise Exception("Fragment can not be stored in byte format.")

        output_folder = self.__output_folder(fragment.map_name)
        self.__make_dir_if_not_exists(output_folder)

        file_name = ResourceManager.__fragment_file_name(
            fragment.scale,
            fragment.position,
            fragment.size,
            compressed,
        )

        output_path = self.__output_path(fragment.map_name, file_name)
        with open(output_path, "wb") as src:
            data_bytes = fragment.data.astype(np.uint8).tobytes()
            if compressed:
                data_bytes = compress_bytes(data_bytes)
            src.write(data_bytes)

    def load_fragment(
        self,
        map_name: str,
        scale: int,
        position: XY,
        size: XY,
        compressed: bool = True,
    ) -> MapFragment:
        file_name = ResourceManager.__fragment_file_name(
            scale,
            position,
            size,
            compressed,
        )

        with open(self.__output_path(map_name, file_name), "rb") as src:
            data_bytes = src.read()
            if compressed:
                data_bytes = decompress_bytes(data_bytes)
            shape = (size[0] * scale, size[1] * scale)
            data = np.frombuffer(data_bytes, dtype=np.uint8).reshape(shape)
            return MapFragment(data, map_name, scale, position)

    def load_map(
        self, map_name: str, scale: int, compressed: bool = True
    ) -> MapFragment:
        return self.load_fragment(
            map_name, scale, (-180, -90), (360, 180), compressed)

    @staticmethod
    def __make_dir_if_not_exists(dir_path: str) -> None:
        if not os.path.isdir(dir_path):
            os.mkdir(dir_path)

    @staticmethod
    def __fragment_file_name(
        scale: int,
        position: XY,
        size: XY,
        compressed: bool,
    ) -> str:
        file_name = f"x{scale}"
        if size[0] < 360:
            file_name += f"_lon{position[0]}+{size[0]}"
        if size[1] < 180:
            file_name += f"_lat{position[1]}+{size[1]}"
        file_name += ".bin"
        if compressed:
            file_name += ".z"
        return file_name

    def __input_folder(self, dataset_name: str) -> str:
        return os.path.join(self.__input_dir_path, dataset_name)

    def __input_path(self, dataset_name: str, file_name: str) -> str:
        return os.path.join(self.__input_dir_path, dataset_name, file_name)

    def __output_folder(self, map_name: str) -> str:
        return os.path.join(self.__output_dir_path, map_name)

    def __output_path(self, map_name: str, file_name: str) -> str:
        return os.path.join(self.__output_dir_path, map_name, file_name)
