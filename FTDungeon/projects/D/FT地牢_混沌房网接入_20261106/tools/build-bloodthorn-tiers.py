import argparse
import colorsys
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


def recolor(pixel, hue_degrees):
    red, green, blue, alpha = pixel
    source_hue, lightness, saturation = colorsys.rgb_to_hls(red / 255, green / 255, blue / 255)
    output = colorsys.hls_to_rgb(hue_degrees / 360, lightness, saturation)
    return tuple(round(channel * 255) for channel in output) + (alpha,)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    project = Path(__file__).resolve().parent.parent
    textures = project / 'src/main/resources/assets/ftdungeonmobs/textures/block'
    original = textures / 'bloodthorn_brazier_fire.png'
    metadata = original.with_suffix('.png.mcmeta').read_bytes()
    source = Image.open(original).convert('RGBA')
    source_pixels = list(source.get_flattened_data())
    palette = sorted(set(source_pixels), key=lambda pixel: sum(pixel[:3]))
    report = {'geometryChanged': False, 'sourceSha256': hashlib.sha256(original.read_bytes()).hexdigest(),
              'frameSize': [16, 16], 'frames': 16, 'ticksPerFrame': 2, 'tiers': []}
    tier_images = []
    for tier, hue, label in [(1, 199, 'AZURE'), (2, 107, 'EMERALD'), (3, 40, 'GOLD'),
                             (4, 276, 'VIOLET'), (5, None, 'CRIMSON')]:
        converted = {pixel: recolor(pixel, hue) if hue is not None else pixel for pixel in palette}
        pixels = [converted[pixel] for pixel in source_pixels]
        image = Image.new('RGBA', source.size)
        image.putdata(pixels)
        if tier < 5:
            target = textures / f'bloodthorn_brazier_fire_t{tier}.png'
            image.save(target)
            target.with_suffix('.png.mcmeta').write_bytes(metadata)
        else:
            target = original
        assert len(set(pixels)) == len(palette)
        for frame in range(16):
            assert len(set(pixels[frame * 256:(frame + 1) * 256])) > 1
        tier_images.append(image)
        report['tiers'].append({'tier': tier, 'color': label, 'texture': target.name,
                                'palette': [list(converted[pixel]) for pixel in palette],
                                'sha256': hashlib.sha256(target.read_bytes()).hexdigest()})
    arguments.output.mkdir(parents=True, exist_ok=True)
    sheet = Image.new('RGB', (740, 186), '#15151c')
    drawing = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 16)
    for index, tier_image in enumerate(tier_images):
        offset = 16 + index * 146
        tile = tier_image.crop((0, 0, 16, 16)).resize((112, 112), Image.Resampling.NEAREST)
        sheet.paste(tile, (offset, 18))
        drawing.text((offset, 144), f'{index + 1}  {report["tiers"][index]["color"]}', fill='#f3e8d6', font=font)
    sheet.save(arguments.output / 'tier-flame-palette.png')
    frames = []
    for frame in range(16):
        strip = Image.new('RGB', (740, 152), '#15151c')
        for index, tier_image in enumerate(tier_images):
            tile = tier_image.crop((0, frame * 16, 16, (frame + 1) * 16))
            strip.paste(tile.resize((112, 112), Image.Resampling.NEAREST), (16 + index * 146, 18))
        frames.append(strip)
    frames[0].save(arguments.output / 'tier-flames-16-frames.gif', save_all=True, append_images=frames[1:],
                   duration=100, loop=0, disposal=2)
    (arguments.output / 'tier-textures.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print('GENERATED: four hue-only palettes + original crimson; 5 x 16 frames; cuboid/body assets untouched.')


if __name__ == '__main__':
    main()
