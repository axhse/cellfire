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


def compress_gradient_map_data(data, scale):
    if scale == 1:
        return data
    compressed_data = data.reshape(
        data.shape[0] // scale, scale, data.shape[1] // scale, scale
    )
    return np.rint(compressed_data.mean(axis=(1, 3))).astype(np.uint8)


def compress_category_map_data(data, scale):
    new_shape = (data.shape[0] // scale, data.shape[1] // scale)
    compressed_data = np.zeros(new_shape, dtype=np.uint8)
    for i in range(new_shape[0]):
        for j in range(new_shape[1]):
            block = data[i * scale : (i + 1) * scale, j * scale : (j + 1) * scale]
            non_zero_values = block[block != 0]
            if non_zero_values.size > 0:
                compressed_data[i, j] = mode(non_zero_values, axis=None).mode
            else:
                compressed_data[i, j] = 0

    return compressed_data
