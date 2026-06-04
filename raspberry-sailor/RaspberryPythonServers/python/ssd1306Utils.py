import adafruit_ssd1306
import math

#
# All drawing function have to be followed by
# display.show()
#


# Draw empty rectangle
def fill_rectangle(oled: adafruit_ssd1306.SSD1306_SPI, x: int, y: int, w: int, h: int) -> None:
    """
    Draw and fill a rectangle

    :param SSD1306_SPI oled: The screen object
    :param int x: Top-Left corner abscissa
    :param int y: Top-left corner ordinate
    :param int w: Width of the rectangle
    :param int h: Height of the rectangle

    :return: None
    """
    for i in range(w):
        for j in range(h):
            oled.pixel(x + i, y + j, 1)


def draw_rectangle(oled: adafruit_ssd1306.SSD1306_SPI, x: int, y: int, w: int, h: int) -> None:
    """
    Draw an empty rectangle

    :param SSD1306_SPI oled: The screen object
    :param int x: Top-Left corner abscissa
    :param int y: Top-left corner ordinate
    :param int w: Width of the rectangle
    :param int h: Height of the rectangle

    :return: None
    """
    for i in range(w): # Top
        oled.pixel(x + i, y, 1)
    for i in range(w): # Bottom
        oled.pixel(x + i, y + h, 1)

    for j in range(h): # left
        oled.pixel(x, y + j, 1)
    for j in range(h): # right
        oled.pixel(x + w, y + j, 1)


def draw_line(oled: adafruit_ssd1306.SSD1306_SPI, from_x: int, from_y: int, to_x: int, to_y: int) -> None:
    """
    Draw a line

    :param SSD1306_SPI oled: The screen object
    :param int from_x: First extremity abscissa
    :param int from_y: First extremity ordinate
    :param int to_x: Second extremity abscissa
    :param int to_y: Second extremity ordinate

    :return: None
    """
    oled.pixel(from_x, from_y, 1)  # first

    # Find interval
    width: int = to_x - from_x
    height: int = to_y - from_y
    the_range: int = max(abs(width), abs(height))
    step_x: float = width / the_range
    step_y: float = height / the_range
    # Let's loop
    # print(f"StepX:{step_x}, StepY:{step_y}")
    for i in range(the_range):
        x: float = from_x + (step_x * i)
        y: float = from_y + (step_y * i)
        oled.pixel(round(x), round(y), 1)
    oled.pixel(to_x, to_y, 1)            # last


def draw_circle(oled: adafruit_ssd1306.SSD1306_SPI, center_x: int, center_y: int, radius: int) -> None:
    """
    Draw a circle

    :param SSD1306_SPI oled: The screen object
    :param int center_x: Center abscissa
    :param int center_y: Center ordinate
    :param int radius: radius

    :return: None
    """
    for r in range(360):
        x: float = center_x + (radius * math.cos(math.radians(r)))
        y: float = center_y - (radius * math.sin(math.radians(r)))
        oled.pixel(round(x), round(y), 1)