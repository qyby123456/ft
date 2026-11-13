import hashlib
import importlib.util
import json
from pathlib import Path

from PIL import Image, ImageChops

ROOT = Path(__file__).resolve().parents[1]
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py')
specification = importlib.util.spec_from_file_location('s07_four_view_renderer', SHARED)
renderer = importlib.util.module_from_spec(specification)
specification.loader.exec_module(renderer)


def cutaway(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        if block.name.removeprefix('minecraft:') in renderer.INVISIBLE:
            continue
        horizontal, vertical, depth = position
        if not exterior:
            close_wall = horizontal <= 1 if camera[0] < 0 else horizontal >= 11
            close_end = depth == 0 if camera[1] < 0 else depth == 15
            if vertical == 10 or vertical >= 2 and (close_wall or close_end):
                continue
        result[position] = block
    return result


def main():
    renderer.filtered = cutaway
    original_texture = renderer.renderer.texture

    def tinted_texture(identifier, uv, rotation, brightness):
        image = original_texture(identifier, uv, rotation, brightness)
        if identifier in ('block/water_still', 'block/water_flow'):
            tinted = ImageChops.multiply(image.convert('RGB'), Image.new('RGB', image.size, '#3979d4')).convert('RGBA')
            tinted.putalpha(image.getchannel('A').point(lambda value: round(value * 0.83)))
            return tinted
        return image

    renderer.renderer.texture = tinted_texture
    structure = renderer.Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    assert digest == metadata['sha256']
    target = ROOT / 'four-views'
    target.mkdir(exist_ok=True)
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2160), '#101a20')
        for index, (direction, title, camera) in enumerate(renderer.VIEWS):
            image = renderer.render(structure, metadata, camera, title, exterior)
            path = target / f'{mode}-{direction}.png'
            image.save(path)
            sheet.paste(image, (index % 2 * 1400, index // 2 * 1080))
            records.append({'direction': direction, 'mode': mode, 'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'source_nbt_sha256': digest})
            if not exterior and direction == 'north':
                image.save(ROOT / 'preview.png')
        sheet.save(target / f'{mode}-four-views.png')
    (target / 'manifest.json').write_text(json.dumps({'id': metadata['id'], 'source_nbt_sha256': digest, 'renderer': str(SHARED), 'scope': 'Minecraft 1.20.1 vanilla asset software renders; not client screenshots. Cutaways affect display only. Water uses a fixed temperate-blue preview tint, not a measured biome or shader.', 'cutaway': 'remove y=10 roof and camera-near two-block-thick side wall/end facade at y>=2; retain final NBT unchanged', 'views': records}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('S07 eight final-NBT views and preview rendered: ' + digest)


if __name__ == '__main__':
    main()
