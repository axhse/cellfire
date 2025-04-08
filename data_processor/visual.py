import numpy as np
from matplotlib import pyplot as plt
from matplotlib.colors import BoundaryNorm, LinearSegmentedColormap, ListedColormap

from map_fragment import MapFragment
from map_producers.forest_type import FOREST_TYPE_COLORS
from transformation import downscale_data, to_image_data


class MapDrawer:
    def draw(self, fragment: MapFragment):
        size = max(fragment.width, fragment.height)
        scale = 1
        for possible_scale in range(fragment.scale, 0, -1):
            if fragment.scale % possible_scale == 0 and possible_scale * size <= 1800:
                scale = possible_scale
                break

        data = fragment.data
        data = downscale_data(data, fragment.scale // scale)
        data = to_image_data(data)

        figure, axes = plt.subplots(figsize=(12, 6))
        self._draw_plot(axes, data)
        axes.set_title(fragment.map_name)
        axes.set_xticks([])
        axes.set_yticks([])
        plt.show()

    @staticmethod
    def _to_plt_color(rgb_color):
        return [channel / 255 for channel in rgb_color]

    def _draw_plot(self, axes, scaled_data):
        pass


class GradientMapDrawer(MapDrawer):
    DEFAULT_BOTTOM_COLOR = (255, 255, 255)
    DEFAULT_TOP_COLOR = (0, 0, 128)

    def __init__(self, bottom_color=DEFAULT_BOTTOM_COLOR, top_color=DEFAULT_TOP_COLOR):
        self.__bottom_color = self._to_plt_color(bottom_color)
        self.__top_color = self._to_plt_color(top_color)

    def _draw_plot(self, axes, scaled_data):
        custom_cmap = LinearSegmentedColormap.from_list(
            "", [self.__bottom_color, self.__top_color]
        )
        image = axes.imshow(
            scaled_data,
            cmap=custom_cmap,
            vmin=0,
            vmax=255,
        )
        plt.colorbar(image, ax=axes)


class CategoryMapDrawer(MapDrawer):
    DEFAULT_UNDEFINED_CATEGORY_COLOR = (255, 255, 255)

    def __init__(self, palette=None, undefined_category_color=None):
        self.palette = palette
        self.undefined_category_color = undefined_category_color

    def _draw_plot(self, axes, scaled_data):
        colors = [
            self.undefined_category_color
            and self._to_plt_color(self.undefined_category_color)
            or self._to_plt_color(self.DEFAULT_UNDEFINED_CATEGORY_COLOR)
        ] + (
            [self._to_plt_color(color) for color in self.palette or []]
            or self.__generate_default_palette(scaled_data.max())
        )
        cmap = ListedColormap(colors)
        bounds = np.arange(-0.5, len(colors))
        norm = BoundaryNorm(bounds, cmap.N)
        axes.imshow(scaled_data, cmap=cmap, norm=norm, interpolation="nearest")

    @staticmethod
    def __generate_default_palette(number):
        return list(plt.get_cmap("tab20", number - 1).colors)


def draw_elevation(fragment):
    drawer = GradientMapDrawer((240, 240, 255), (64, 32, 0))
    drawer.draw(fragment)


def draw_forest_type(fragment):
    drawer = CategoryMapDrawer(FOREST_TYPE_COLORS)
    drawer.draw(fragment)


def draw_forest_density(fragment):
    drawer = GradientMapDrawer(top_color=(0, 128, 0))
    drawer.draw(fragment)
