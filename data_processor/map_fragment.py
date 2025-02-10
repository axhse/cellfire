from converters import compress_map_data, stretch_map_data


class MapFragment:
    def __init__(self, data, map_name, scale, x, y):
        self.__data = data
        self.__map_name = map_name
        self.__scale = scale
        self.__x = x
        self.__y = y
        if data.shape[0] % scale != 0 or data.shape[1] % scale:
            raise Exception("Invalid fragment shape.")
        self.__width = data.shape[0] // scale
        self.__height = data.shape[1] // scale
        if self.__width < 1 or self.__height < 1:
            raise Exception("Invalid fragment shape.")

    def compress(self, factor):
        if self.__scale * self.__width % factor != 0:
            raise Exception("Invalid factor.")
        data = compress_map_data(self.data, factor)
        scale = self.__scale // factor
        if data.shape[0] < self.__width:
            data = stretch_map_data(data, self.__width // data.shape[0])
            scale = 1
        return MapFragment(data, self.__map_name, scale, self.__x, self.__y)

    def cut(self, x, y, width, height):
        if (
            x < self.__x
            or y < self.__y
            or x + width > self.__width
            or y + height > self.__height
        ):
            raise Exception("Invalid fragment shape.")
        offset_x = (x - self.__x) * self.__scale
        offset_y = (y - self.__y) * self.__scale
        data = self.data[
            offset_x : offset_x + width * self.__scale,
            offset_y : offset_y + height * self.__scale,
        ]
        return MapFragment(data, self.__map_name, self.__scale, x, y)

    @property
    def data(self):
        return self.__data

    @property
    def map_name(self):
        return self.__map_name

    @property
    def scale(self):
        return self.__scale

    @property
    def x(self):
        return self.__x

    @property
    def y(self):
        return self.__y

    @property
    def width(self):
        return self.__width

    @property
    def height(self):
        return self.__height


class MapFullFragment(MapFragment):
    def __init__(self, data, map_name):
        if data.shape[0] < 360:
            if 360 % data.shape[0] != 0:
                raise Exception("Invalid fragment shape.")
            data = stretch_map_data(data, 360 // data.shape[0])
        print(data.shape)
        if data.shape[0] % 360 != 0 or data.shape[0] != data.shape[1] * 2:
            raise Exception("Invalid fragment shape.")
        scale = data.shape[0] // 360
        super().__init__(data, map_name, scale, -180, -90)
