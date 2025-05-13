from definitions import XY, Data
from transformation import downscale_data, upscale_data


class MapFragment:
    def __init__(self, data: Data, map_name: str,
                 scale: int, position: XY) -> None:
        self.__data: Data = data
        self.__map_name: str = map_name
        self.__scale: int = scale
        self.__position: XY = position

        if data.shape[0] % scale != 0 or data.shape[1] % scale:
            raise Exception("Invalid fragment shape.")

        self.__size: XY = (data.shape[0] // scale, data.shape[1] // scale)

        if self.width < 1 or self.height < 1:
            raise Exception("Invalid fragment shape.")

    def downscale(self, factor: int) -> "MapFragment":
        if self.__scale * self.width % factor != 0:
            raise Exception("Invalid factor.")

        data = downscale_data(self.data, factor)
        scale = self.__scale // factor
        if data.shape[0] < self.width:
            data = upscale_data(data, self.width // data.shape[0])
            scale = 1

        return MapFragment(data, self.__map_name, scale, self.__position)

    def cut(self, position: XY, size: XY) -> "MapFragment":
        if (
            position[0] < self.x
            or position[1] < self.y
            or position[0] + size[0] > self.x + self.width
            or position[1] + size[1] > self.y + self.height
        ):
            raise Exception("Invalid fragment shape.")

        offset_x = (position[0] - self.x) * self.__scale
        offset_y = (position[1] - self.y) * self.__scale
        data = self.data[
            offset_x: offset_x + size[0] * self.__scale,
            offset_y: offset_y + size[1] * self.__scale,
        ]

        return MapFragment(data, self.__map_name, self.__scale, position)

    @property
    def data(self) -> Data:
        return self.__data

    @property
    def map_name(self) -> str:
        return self.__map_name

    @property
    def scale(self) -> int:
        return self.__scale

    @property
    def position(self) -> XY:
        return self.__position

    @property
    def x(self) -> int:
        return self.__position[0]

    @property
    def y(self) -> int:
        return self.__position[1]

    @property
    def size(self) -> XY:
        return self.__size

    @property
    def width(self) -> int:
        return self.__size[0]

    @property
    def height(self) -> int:
        return self.__size[1]


class FullMap(MapFragment):
    def __init__(self, data: Data, map_name: str) -> None:
        if data.shape[0] < 360:
            if 360 % data.shape[0] != 0:
                raise Exception("Invalid map shape.")
            data = upscale_data(data, 360 // data.shape[0])

        if data.shape[0] % 360 != 0 or data.shape[0] != data.shape[1] * 2:
            raise Exception("Invalid map shape.")

        scale = data.shape[0] // 360
        super().__init__(data, map_name, scale, (-180, -90))
