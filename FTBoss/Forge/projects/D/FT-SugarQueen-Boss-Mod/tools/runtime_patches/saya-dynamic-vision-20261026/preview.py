import io
import math
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/saya-dynamic-vision-20261026'
OUTPUT = WORK / 'previews'
OUTPUT.mkdir(exist_ok=True)


def composite(background, flesh, veins, frame, pieces):
    ticks = frame * 3
    phase = ticks / 36
    heartbeat = min(1, math.exp(-((phase - 0.23) / 0.075) ** 2)
                    + 0.42 * math.exp(-((phase - 0.43) / 0.09) ** 2))
    intensity = pieces / 4
    result = background.convert('RGBA')
    result = Image.alpha_composite(result, Image.new('RGBA', result.size, (104, 22, 38, round(12 * intensity))))
    origin_x = frame % 4 * 512
    origin_y = frame // 4 * 288
    for atlas, opacity in [(flesh, 0.14 + 0.79 * intensity + 0.05 * intensity * heartbeat),
                           (veins, 0.06 + 0.52 * intensity + 0.18 * intensity * heartbeat)]:
        layer = atlas.crop((origin_x, origin_y, origin_x + 512, origin_y + 288)).resize(
            result.size, Image.Resampling.NEAREST)
        layer.putalpha(layer.getchannel('A').point(lambda value: round(value * opacity)))
        result = Image.alpha_composite(result, layer)
    return result.convert('RGB')


def main():
    with zipfile.ZipFile(WORK / 'qys-bosses.jar') as archive:
        images = [Image.open(io.BytesIO(archive.read('assets/ftboss/textures/gui/overlays/' + name))).convert('RGBA')
                  for name in ['saya_flesh_dynamic.png', 'saya_veins_dynamic.png']]
    background = Image.new('RGB', (768, 432), '#a7afa5')
    drawing = ImageDraw.Draw(background)
    for pixel_y in range(0, 432, 24):
        for pixel_x in range(0, 768, 24):
            color = '#adb6ac' if (pixel_x // 24 + pixel_y // 24) % 2 else '#909e90'
            drawing.rectangle((pixel_x, pixel_y, pixel_x + 23, pixel_y + 23), fill=color)
    drawing.line((374, 216, 394, 216), fill='white', width=2)
    drawing.line((384, 206, 384, 226), fill='white', width=2)
    animation = [composite(background, *images, frame, 4) for frame in range(12)]
    animation[0].save(OUTPUT / 'saya-dynamic.gif', save_all=True, append_images=animation[1:],
                      duration=150, loop=0, optimize=False)
    sheet = Image.new('RGB', (1536, 760), '#18221e')
    drawing = ImageDraw.Draw(sheet)
    drawing.text((12, 12), 'OFFLINE PREVIEW FROM FINAL JAR | 12 frames / 1.8 s | 512 x 288 per frame', fill='white')
    for frame, image in enumerate(animation):
        origin_x = frame % 4 * 384
        origin_y = 42 + frame // 4 * 238
        sheet.paste(image.resize((384, 216)), (origin_x, origin_y))
        drawing.text((origin_x + 8, origin_y + 220), f'{frame:02d}  {frame * 150} ms', fill='white')
    sheet.save(OUTPUT / 'all-frames.png')
    comparison = Image.new('RGB', (1536, 240), '#18221e')
    drawing = ImageDraw.Draw(comparison)
    for pieces in range(1, 5):
        comparison.paste(composite(background, *images, 3, pieces).resize((384, 216)), ((pieces - 1) * 384, 24))
        drawing.text(((pieces - 1) * 384 + 8, 6), f'{pieces} armor piece(s)', fill='white')
    comparison.save(OUTPUT / 'armor-strength.png')
    if len(sys.argv) > 1:
        screenshot = Image.open(sys.argv[1]).convert('RGB')
        for width, height, name in [(960, 540, '16x9'), (960, 720, '4x3'), (1260, 540, '21x9')]:
            result = composite(screenshot.resize((width, height)), *images, 3, 4)
            ImageDraw.Draw(result).text((width // 4, 8), 'OFFLINE COMPOSITE - NOT IN-GAME CAPTURE', fill='white')
            result.save(OUTPUT / f'screenshot-{name}.png')
    print('Final JAR animation previews: ' + str(OUTPUT))


if __name__ == '__main__':
    main()
