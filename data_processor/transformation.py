import bz2

import numpy as np
from PIL import Image
from scipy.stats import mode


def image_to_rgb_array(image):
    return np.array(image)


def rgb_array_to_image(rgb_array):
    return Image.fromarray(rgb_array.astype(np.uint8))


def compress_bytes(byte_sequence):
    return bz2.compress(byte_sequence)


def decompress_bytes(byte_sequence):
    return bz2.decompress(byte_sequence)


def to_map_data(image_data):
    return image_data[::-1, :].transpose()


def to_image_data(map_data):
    return map_data.transpose()[::-1, :]


def stretch_vector(vector, scalar):
    return tuple(n * scalar for n in vector)


def upscale_data(data, factor):
    return np.repeat(np.repeat(data, factor, axis=0), factor, axis=1)


def downscale_data(data, factor):
    if factor == 1:
        return data
    if factor < 1 or data.shape[0] % factor != 0 or data.shape[1] % factor != 0:
        raise Exception("Invalid factor.")
    downscaled_data = data.reshape(
        data.shape[0] // factor, factor, data.shape[1] // factor, factor
    )
    return np.rint(downscaled_data.mean(axis=(1, 3))).astype(np.uint8)


def stack_tiles(tiles, width):
    if len(tiles) % width != 0:
        raise Exception("Invalid width.")
    height = len(tiles) // width
    rows = [np.vstack(tiles[i * width : (i + 1) * width]) for i in range(height)]
    return np.hstack(rows)


def collapse_prevalent_category_data(data, factor):
    new_shape = (data.shape[0] // factor, data.shape[1] // factor)
    compressed_data = np.zeros(new_shape, dtype=np.uint8)
    for i in range(new_shape[0]):
        for j in range(new_shape[1]):
            block = data[i * factor : (i + 1) * factor, j * factor : (j + 1) * factor]
            non_zero_values = block[block != 0]
            if non_zero_values.size > 0:
                compressed_data[i, j] = mode(non_zero_values, axis=None).mode
            else:
                compressed_data[i, j] = 0
    return compressed_data
