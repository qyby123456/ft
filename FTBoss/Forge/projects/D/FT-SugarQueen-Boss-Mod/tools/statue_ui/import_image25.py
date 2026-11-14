import hashlib
import json
from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw
from generate_themes import ASSETS, ROOT, nine_slice, palettes, preview, rgba

SOURCE = ROOT / 'output/imagegen/statue-workshop-sprites-20261026.png'
OUTPUT = ROOT / '_work/statue-ui-image25-20261026/art'


def cutout(source):
    image = source.convert('RGBA')
    pixels = image.load()
    pending = deque()
    seen = set()
    for horizontal in range(image.width):
        pending.extend(((horizontal, 0), (horizontal, image.height - 1)))
    for vertical in range(image.height):
        pending.extend(((0, vertical), (image.width - 1, vertical)))
    while pending:
        horizontal, vertical = pending.popleft()
        if not (0 <= horizontal < image.width and 0 <= vertical < image.height) or (horizontal, vertical) in seen:
            continue
        seen.add((horizontal, vertical))
        red, green, blue, alpha = pixels[horizontal, vertical]
        if red < 170 or blue < 165 or green > 100 or min(red, blue) - green < 90:
            continue
        pixels[horizontal, vertical] = (0, 0, 0, 0)
        pending.extend(((horizontal - 1, vertical), (horizontal + 1, vertical),
                        (horizontal, vertical - 1), (horizontal, vertical + 1)))
    bounds = image.getbbox()
    if bounds is None:
        raise ValueError('Empty sprite')
    return image.crop(bounds)


def colored(source, size, palette, selected=False):
    image = source.resize(size, Image.Resampling.NEAREST)
    stops = ((0, palette['background']), (65, palette['panel']), (105, palette['border']),
             (175, palette['accent'] if selected else palette['muted']), (255, palette['bright']))
    output = Image.new('RGBA', size)
    for vertical in range(size[1]):
        for horizontal in range(size[0]):
            red, green, blue, alpha = image.getpixel((horizontal, vertical))
            if alpha == 0:
                continue
            lightness = round(0.299 * red + 0.587 * green + 0.114 * blue)
            for lower, upper in zip(stops, stops[1:]):
                if lightness <= upper[0]:
                    weight = max(0, min(1, (lightness - lower[0]) / (upper[0] - lower[0])))
                    channels = tuple(round(first * (1 - weight) + second * weight)
                                     for first, second in zip(rgba(lower[1])[:3], rgba(upper[1])[:3]))
                    output.putpixel((horizontal, vertical), (*channels, 255))
                    break
    return output


def make_assets():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    source = Image.open(SOURCE).convert('RGBA')
    tiles = []
    for index in range(16):
        column, row = index % 4, index // 4
        box = (round(column * source.width / 4), round(row * source.height / 4),
               round((column + 1) * source.width / 4), round((row + 1) * source.height / 4))
        tile = cutout(source.crop(box))
        tile.save(OUTPUT / f'sprite_{index:02}.png')
        tiles.append(tile)
    sheet = Image.new('RGB', (972, 640), '#0A0D12')
    records = []
    for index, (identity, palette) in enumerate(palettes()):
        atlas = Image.new('RGBA', (256, 256))
        frame = colored(tiles[0], (32, 32), palette)
        ImageDraw.Draw(frame).rectangle((6, 6, 25, 25), fill=rgba(palette['panel']))
        atlas.alpha_composite(frame)
        for selected in (False, True):
            card = colored(tiles[1], (16, 16), palette, selected)
            draw = ImageDraw.Draw(card)
            draw.rectangle((3, 3, 12, 12), fill=rgba(palette['card']))
            if selected:
                draw.line(((1, 2), (1, 13)), fill=rgba(palette['accent']))
            atlas.alpha_composite(card, (60 if selected else 40, 0))
        for state in range(4):
            button = colored(tiles[(2, 3, 4, 2)[state]], (64, 20), palette, state in (1, 2))
            draw = ImageDraw.Draw(button)
            draw.rectangle((3, 3, 60, 16), fill=rgba(palette['card'] if state != 3 else palette['panel']))
            if state == 2:
                for horizontal in range(4, 60, 4):
                    draw.point((horizontal, 1), fill=rgba(palette['bright']))
                    draw.point((horizontal, 18), fill=rgba(palette['bright']))
            if state == 3:
                for vertical in range(button.height):
                    for horizontal in range(button.width):
                        color = button.getpixel((horizontal, vertical))
                        if color[3]:
                            button.putpixel((horizontal, vertical), tuple(round(color[channel] * 0.55 + rgba(palette['background'])[channel] * 0.45)
                                                                         for channel in range(3)) + (255,))
            atlas.alpha_composite(button, (0, 40 + state * 20))
        emblem = Image.new('RGBA', (32, 32))
        emblem.alpha_composite(tiles[index + 5].resize((30, 30), Image.Resampling.NEAREST), (1, 1))
        directory = ASSETS / identity
        directory.mkdir(parents=True, exist_ok=True)
        atlas.save(directory / 'panels.png', optimize=True)
        emblem.save(directory / 'crest.png', optimize=True)
        sample = preview(identity, palette, atlas, emblem)
        sample.save(OUTPUT / (identity + '_preview.png'))
        sheet.paste(sample.convert('RGB'), (index % 3 * 324, index // 3 * 160))
        records.append(dict(theme=identity, source_cell=index + 5, atlas_sha256=hashlib.sha256(atlas.tobytes()).hexdigest(),
                            crest_sha256=hashlib.sha256(emblem.tobytes()).hexdigest()))
    sheet.resize((1944, 1280), Image.Resampling.NEAREST).save(OUTPUT / 'image25_theme_contact_sheet.png')
    manifest = dict(model='gpt-image-2.5-flare', source=str(SOURCE.relative_to(ROOT)), source_dimensions=list(source.size),
                    source_sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(), themes=records,
                    processing='Border-connected chroma removal; nearest-neighbor crop/downscale; theme tint; quiet reading fields; binary alpha')
    (OUTPUT / 'import_manifest.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    print('Imported 11 image2.5 emblems and 11 compatible panel atlases')
    print(OUTPUT / 'image25_theme_contact_sheet.png')


if __name__ == '__main__':
    make_assets()
