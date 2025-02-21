import bz2

import numpy as np
from PIL import Image
from scipy.stats import mode


def image_to_rgb_array(image):
    return np.array(image)


def rgb_array_to_image(rgb_array):
    return Image.fromarray(rgb_array.astype(np.uint8))


def transform_from_image_coordinates_to_map_coordinates(matrix):
    return matrix[::-1, :].transpose()


def transform_from_tiff_coordinates_to_map_coordinates(matrix):
    return transform_from_image_coordinates_to_map_coordinates(matrix)


def compress_bytes(byte_sequence):
    return bz2.compress(byte_sequence)


def decompress_bytes(byte_sequence):
    return bz2.decompress(byte_sequence)


def compress_map_data(data, factor):
    if factor == 1:
        return data
    if data.shape[0] % factor != 0 or data.shape[1] % factor != 0:
        raise Exception("Invalid factor.")
    compressed_data = data.reshape(
        data.shape[0] // factor, factor, data.shape[1] // factor, factor
    )
    return np.rint(compressed_data.mean(axis=(1, 3))).astype(np.uint8)


def stretch_map_data(data, factor):
    data = np.repeat(data, factor, axis=0)
    return np.repeat(data, factor, axis=1)


def collapse_category_map_data(data, factor):
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
