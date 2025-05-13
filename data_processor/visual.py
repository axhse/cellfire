import abc
from datetime import datetime
from enum import IntEnum
from sys import stdout
from typing import Any, Dict, List, Optional, Tuple

import numpy as np
from matplotlib import pyplot as plt
from matplotlib.colors import BoundaryNorm, LinearSegmentedColormap, ListedColormap
from matplotlib.patches import Patch

from definitions import FOREST_TYPE_COLORS, Color, Data
from map_fragment import MapFragment
from transformation import downscale_data, squeeze_category_data, to_image_data

PltColor = Tuple[float, float, float]


class Colors:
    @staticmethod
    def plt(color: Color) -> PltColor:
        return color[0] / 255, color[1] / 255, color[2] / 255

    @staticmethod
    def category_palette() -> List[PltColor]:
        colors = list()
        np.random.seed(256)
        while len(colors) < 256:
            channels = tuple(np.random.rand(3))
            if max(channels) - min(channels) < 0.5:
                continue
            if colors and sum(abs(channels[i] - colors[-1][i])
                              for i in range(3)) < 1:
                continue
            if (
                2 <= len(colors)
                and sum(abs(channels[i] - colors[-2][i]) for i in range(3)) < 1
            ):
                continue
            colors.append(channels)
        return colors


class MapDrawer(abc.ABC):
    def draw(self, fragment: MapFragment) -> None:
        size = max(fragment.width, fragment.height)
        scale = 1
        for possible_scale in range(fragment.scale, 0, -1):
            if fragment.scale % possible_scale == 0 and possible_scale * size <= 3600:
                scale = possible_scale
                break

        data = fragment.data
        data = self._downscale_data(data, fragment.scale // scale)
        data = to_image_data(data)

        figure, axes = plt.subplots(figsize=(12, 6))
        self._draw_plot(axes, data)
        axes.set_title(fragment.map_name)
        axes.set_xticks(list())
        axes.set_yticks(list())
        plt.show()

    @abc.abstractmethod
    def _downscale_data(self, data: Data, factor: int) -> Data:
        pass

    @abc.abstractmethod
    def _draw_plot(self, axes: Any, scaled_data: Data) -> None:
        pass


class GradientMapDrawer(MapDrawer):
    DEFAULT_MIN_COLOR: PltColor = Colors.plt((255, 255, 255))
    DEFAULT_MAX_COLOR: PltColor = Colors.plt((0, 0, 128))

    def __init__(
        self,
        min_color: PltColor = DEFAULT_MIN_COLOR,
        max_color: PltColor = DEFAULT_MAX_COLOR,
        boundaries: Tuple[float, float] = (0, 255),
    ):
        self.__min_color: PltColor = min_color
        self.__max_color: PltColor = max_color
        self.__boundaries: Tuple[float, float] = boundaries

    def _downscale_data(self, data: Data, factor: int) -> Data:
        return downscale_data(data, factor)

    def _draw_plot(self, axes: Any, scaled_data: Data) -> None:
        custom_cmap = LinearSegmentedColormap.from_list(
            "", np.array([self.__min_color, self.__max_color])
        )
        image = axes.imshow(
            scaled_data,
            cmap=custom_cmap,
            vmin=0,
            vmax=255,
        )
        cbar = plt.colorbar(image, ax=axes)

        cbar.set_ticks(np.linspace(0, 255, num=5))
        cbar.set_ticklabels(
            np.linspace(self.__boundaries[0], self.__boundaries[1], num=5)
        )


class CategoryMapDrawer(MapDrawer):
    DEFAULT_COLORS: List[PltColor] = Colors.category_palette()

    def __init__(
        self,
        colors: Optional[List[PltColor]] = None,
        categories: Optional[Dict[int, str]] = None,
    ):
        self.__colors: List[PltColor] = colors or self.DEFAULT_COLORS
        self.__categories: Dict[int, str] = categories or dict()

    def _downscale_data(self, data: Data, factor: int) -> Data:
        return squeeze_category_data(data, factor)

    def _draw_plot(self, axes: Any, scaled_data: Data) -> None:
        cmap = ListedColormap(np.array(self.__colors))
        bounds = np.arange(-0.5, len(self.__colors))
        norm = BoundaryNorm(bounds, cmap.N)
        axes.imshow(scaled_data, cmap=cmap, norm=norm, interpolation="nearest")

        legend_elements = [
            Patch(
                facecolor=self.__colors[category],
                label=self.__categories.get(category) or str(category),
            )
            for category in np.unique(scaled_data)
        ]
        axes.legend(handles=legend_elements, loc="lower left")


class Progress:
    class TextStyle(IntEnum):
        BOLD = 1
        RED = 31
        GREEN = 32
        YELLOW = 33
        BLUE = 34
        PURPLE = 35
        CYAN = 36
        GRAY = 90

        def to(self, text: str) -> str:
            return f"\033[{self.value}m{text}\033[00m"

        def to_bold(self, text: str) -> str:
            return self.to(Progress.TextStyle.BOLD.to(text))

    def __init__(
        self,
        challenge_name: str = "",
        task_number: int = 0,
        failure_description: str = "",
        silent=False,
    ):
        self.__challenge_name: str = challenge_name or "?"
        self.__task_number: int = task_number
        self.__failure_description: str = failure_description
        self.__silent = silent
        self.__progress: int = 0
        self.__current_task: Optional[Any] = None
        self.__failed_tasks: List[Any] = list()
        self.__error: str = ""
        self.__start_dt: datetime = datetime.now()

    def start(self) -> None:
        self.__start_dt = datetime.now()
        self.__report()

    def end(self) -> None:
        self.__current_task = None
        self.__report(is_final=True)

    def error(self, error: str) -> None:
        self.__error = error
        self.__report(is_final=True)

    def set_current_task(self, task: Any) -> None:
        self.__current_task = task
        self.__report()

    def count_performed_task(self) -> None:
        self.__progress += 1
        self.__report()

    def add_failure(self, task: Any) -> None:
        self.__failed_tasks.append(task)
        self.__report()

    def __report(self, is_final=False) -> None:
        if self.__silent:
            return
        start_dt = Progress.__format_dt(self.__start_dt)
        current_dt = Progress.__format_dt(datetime.now())
        dt = f"{start_dt} - {current_dt}"
        progress = f"{self.__progress}/{self.__task_number}"
        current_task = str(self.__current_task)
        failure_counter = str(len(self.__failed_tasks))
        failures = " ".join(map(str, self.__failed_tasks))

        style = Progress.TextStyle
        separator = "    "
        stdout.write("\r")
        stdout.write(style.PURPLE.to_bold(self.__challenge_name))
        stdout.write(separator + Progress.TextStyle.GRAY.to_bold(dt))
        if self.__task_number > 0:
            stdout.write(separator + style.GREEN.to_bold(progress))
        if self.__current_task:
            stdout.write(separator + style.BLUE.to("in progress: "))
            stdout.write(style.BLUE.to_bold(current_task))
        if self.__failed_tasks:
            stdout.write(separator + style.YELLOW.to_bold(failure_counter))
            if self.__failure_description:
                stdout.write(" " + style.YELLOW.to(self.__failure_description))

        if is_final:
            stdout.write("\n")
            if self.__error:
                stdout.write(style.RED.to_bold("Error: "))
                stdout.write(style.RED.to(self.__error))
                stdout.write("\n")
            if self.__failed_tasks:
                stdout.write(style.YELLOW.to_bold("Failures:   "))
                stdout.write(style.YELLOW.to(failures))
                stdout.write("\n")

        stdout.flush()

    @staticmethod
    def __format_dt(dt: datetime) -> str:
        return dt.strftime("%H:%M:%S")


def draw_elevation(fragment: MapFragment) -> None:
    drawer = GradientMapDrawer(
        Colors.plt((240, 240, 255)), Colors.plt((64, 32, 0)), (0, 6400)
    )
    drawer.draw(fragment)


def draw_forest_type(fragment: MapFragment) -> None:
    colors = [(255, 255, 255)] + FOREST_TYPE_COLORS
    colors = [Colors.plt(color) for color in colors]
    titles = {
        0: "No forest",
        1: "Evergreen Needleleaf Forest",
        2: "Evergreen Broadleaf Forest",
        3: "Deciduous Needleleaf Forest",
        4: "Deciduous Broadleaf Forest",
        5: "Mixed Forest",
    }
    drawer = CategoryMapDrawer(colors, titles)
    drawer.draw(fragment)


def draw_forest_density(fragment: MapFragment) -> None:
    drawer = GradientMapDrawer(
        max_color=Colors.plt(
            (0, 128, 0)), boundaries=(
            0, 1))
    drawer.draw(fragment)
