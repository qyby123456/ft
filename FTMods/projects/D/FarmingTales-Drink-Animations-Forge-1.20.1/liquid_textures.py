"""Isolate liquid pixels from bottle atlases and close unpainted surface texels."""
from PIL import Image


def patch(image, uv):
    x0, y0, x1, y1 = [round(v * (image.width if i % 2 == 0 else image.height) / 16)
                      for i, v in enumerate(uv)]
    tile = image.crop((min(x0, x1), min(y0, y1), max(x0, x1), max(y0, y1))).convert('RGBA')
    if x1 < x0: tile = tile.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if y1 < y0: tile = tile.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    return tile


def complete(tile, fallback):
    painted = [(x, y, tile.getpixel((x, y))[:3]) for y in range(tile.height)
               for x in range(tile.width) if tile.getpixel((x, y))[3] >= 128]
    if not painted:
        return fallback.copy()
    result = tile.copy()
    for y in range(tile.height):
        for x in range(tile.width):
            rgb = tile.getpixel((x, y))[:3]
            if tile.getpixel((x, y))[3] < 128:
                rgb = min(painted, key=lambda p: (p[0]-x)**2+(p[1]-y)**2)[2]
            result.putpixel((x, y), rgb+(255,))
    return result


def liquid_atlas(side_image, side_uv, surface_image, surface_uv):
    side = patch(side_image, side_uv)
    assert any(a >= 128 for a in side.getchannel('A').tobytes()), 'Liquid needs a painted color source'
    side = complete(side, side)
    surface = complete(patch(surface_image, surface_uv), side)
    atlas = Image.new('RGBA', (32, 16))
    atlas.paste(side.resize((16, 16), Image.Resampling.NEAREST), (0, 0))
    atlas.paste(surface.resize((16, 16), Image.Resampling.NEAREST), (16, 0))
    assert atlas.getchannel('A').getextrema() == (255, 255)
    # Half-texel insets keep both faces inside their own tile under filtering.
    return atlas, [.25, .5, 7.75, 15.5], [8.25, .5, 15.75, 15.5]
