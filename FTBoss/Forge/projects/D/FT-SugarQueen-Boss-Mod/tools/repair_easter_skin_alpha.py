import argparse
import json
from pathlib import Path

from PIL import Image


SKINS = ("youzi", "duola", "xueli")
BACKGROUND_LIMIT = 16
OVERLAY_PARTS = (
    (32, 0, 8, 8, 8),
    (16, 32, 8, 12, 4),
    (40, 32, 3, 12, 4),
    (48, 48, 3, 12, 4),
    (0, 32, 4, 12, 4),
    (0, 48, 4, 12, 4),
)


def overlay_pixels():
    result = set()
    for origin_x, origin_y, width, height, depth in OVERLAY_PARTS:
        rectangles = (
            (origin_x + depth, origin_y, 2 * width, depth),
            (origin_x, origin_y + depth, 2 * (width + depth), height),
        )
        for left, top, rectangle_width, rectangle_height in rectangles:
            result.update((column, row)
                          for row in range(top, top + rectangle_height)
                          for column in range(left, left + rectangle_width))
    return result


def restore_alpha(image):
    restored = image.convert("RGBA")
    if restored.size != (64, 64):
        raise ValueError("Expected the current 64x64 Alex skin layout")
    changed = 0
    for point in overlay_pixels():
        red, green, blue, alpha = restored.getpixel(point)
        if max(red, green, blue) <= BACKGROUND_LIMIT and alpha != 0:
            restored.putpixel(point, (red, green, blue, 0))
            changed += 1
    return restored, changed


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("original_directory", type=Path)
    parser.add_argument("output_directory", type=Path)
    arguments = parser.parse_args()
    arguments.output_directory.mkdir(parents=True, exist_ok=True)
    for name in SKINS:
        original = Image.open(arguments.original_directory / (name + ".png"))
        restored, changed = restore_alpha(original)
        restored.save(arguments.output_directory / (name + ".png"), format="PNG")
        print(json.dumps({"skin": name, "originalFormat": original.format,
                          "restoredOverlayPixels": changed, "rgbChanged": False}))


if __name__ == "__main__":
    main()
