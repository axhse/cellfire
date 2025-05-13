from typing import List

import numpy as np
import zstandard
from PIL import Image
from scipy.stats import mode

from definitions import XY, Data, RgbImageData


def zeros(shape: XY) -> Data:
    return np.zeros(shape, dtype=np.uint8)


def image_to_rgb_array(image: Image.Image) -> RgbImageData:
    return np.array(image)


def rgb_array_to_image(rgb_array: RgbImageData) -> Image.Image:
    return Image.fromarray(rgb_array.astype(np.uint8))


def compress_bytes(byte_sequence: bytes) -> bytes:
    return zstandard.ZstdCompressor().compress(byte_sequence)


def decompress_bytes(byte_sequence: bytes) -> bytes:
    return zstandard.ZstdDecompressor().decompress(byte_sequence)


def to_map_data(image_data: Data) -> Data:
    return image_data[::-1, :].transpose()


def to_image_data(map_data: Data) -> Data:
    return map_data.transpose()[::-1, :]


def stretch_vector(vector: XY, scalar: int) -> XY:
    return vector[0] * scalar, vector[1] * scalar


def upscale_data(data: Data, factor: int) -> Data:
    return np.repeat(np.repeat(data, factor, axis=0), factor, axis=1)


def downscale_data(data: Data, factor: int) -> Data:
    if factor == 1:
        return data
    if factor < 1 or data.shape[0] % factor != 0 or data.shape[1] % factor != 0:
        raise Exception("Invalid factor.")
    downscaled_data = data.reshape(
        data.shape[0] // factor, factor, data.shape[1] // factor, factor
    )
    return np.rint(downscaled_data.mean(axis=(1, 3))).astype(np.uint8)


def stack_data_tiles(tiles: List[Data], width: int) -> Data:
    if len(tiles) % width != 0:
        raise Exception("Invalid width.")
    height = len(tiles) // width
    rows = [np.vstack(tiles[i * width: (i + 1) * width])
            for i in range(height)]
    return np.hstack(rows)


def squeeze_category_data(data: Data, factor: int) -> Data:
    if factor == 1:
        return data
    new_shape = (data.shape[0] // factor, data.shape[1] // factor)
    squeezed_data = np.zeros(new_shape, dtype=np.uint8)
    for i in range(new_shape[0]):
        for j in range(new_shape[1]):
            block = data[i * factor: (i + 1) * factor,
                         j * factor: (j + 1) * factor]
            non_zero_values = block[block != 0]
            if non_zero_values.size > 0:
                squeezed_data[i, j] = mode(non_zero_values, axis=None).mode
            else:
                squeezed_data[i, j] = 0
    return squeezed_data
