import numpy as np
from matplotlib import pyplot as plt
from matplotlib.colors import BoundaryNorm, LinearSegmentedColormap, ListedColormap

from converters import compress_gradient_map_data


class PlotDrawer:
    def draw(self, fragment, scale=1):
        data = fragment.data
        if fragment.scale % scale != 0:
            raise Exception("Invalid image scale.")
        data = compress_gradient_map_data(data, fragment.scale // scale)
        data = data.transpose()[::-1, :]
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


class GradientPlotDrawer(PlotDrawer):
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
            vmin=scaled_data.min(),
            vmax=scaled_data.max(),
        )
        plt.colorbar(image, ax=axes)


class CategoryPlotDrawer(PlotDrawer):
    DEFAULT_UNDEFINED_CATEGORY_COLOR = (255, 255, 255)

    def __init__(self, palette=None, undefined_category_color=None):
        self.palette = palette
        self.undefined_category_color = undefined_category_color

    def _draw_plot(self, axes, scaled_data):
        colors = [
            self.undefined_category_color or self.DEFAULT_UNDEFINED_CATEGORY_COLOR
        ] + (self.palette or self.__generate_default_palette(scaled_data.max()))
        colors = [self._to_plt_color(color) for color in colors]
        cmap = ListedColormap(colors)
        bounds = np.arange(-0.5, len(colors))
        norm = BoundaryNorm(bounds, cmap.N)
        axes.imshow(scaled_data, cmap=cmap, norm=norm, interpolation="nearest")

    @staticmethod
    def __generate_default_palette(number):
        return list(plt.get_cmap("tab20", number - 1).colors)
