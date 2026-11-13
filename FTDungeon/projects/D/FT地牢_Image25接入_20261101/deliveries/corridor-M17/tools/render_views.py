from __future__ import annotations

import importlib.util
import functools
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageChops

from static_qa import ROOT, digest, dump

SHARED = ROOT.parents[1] / 'tools/review_four_views.py'
specification = importlib.util.spec_from_file_location('shared_review', SHARED)
review = importlib.util.module_from_spec(specification)
sys.modules[specification.name] = review
specification.loader.exec_module(review)
original_texture = review.renderer.texture


@functools.lru_cache(maxsize=None)
def tinted_texture(identifier, uv, rotation, brightness):
    image = original_texture(identifier, uv, rotation, brightness)
    if identifier in ('block/water_still', 'block/water_flow'):
        alpha = image.getchannel('A')
        image = ImageChops.multiply(image.convert('RGB'), Image.new('RGB', image.size, '#3f76e4')).convert('RGBA')
        image.putalpha(alpha)
    return image


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, state in structure.blocks.items():
        if state.name.removeprefix('minecraft:') in review.INVISIBLE:
            continue
        block_x, height, block_z = position
        if not exterior:
            close_x = block_x <= 2 if camera[0] < 0 else block_x >= 14
            close_z = block_z <= 1 if camera[1] < 0 else block_z >= 39
            if height >= 11 or height >= 8 and block_z not in (2, 9, 16, 23, 30, 38) or height >= 3 and (close_x or close_z):
                continue
        result[position] = state
    return result


def main():
    review.filtered = filtered
    review.renderer.texture = tinted_texture
    structure = review.Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    records = []
    output = ROOT / 'views'
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2160), '#101a20')
        for view_index, (direction, title, camera) in enumerate(review.VIEWS):
            image = review.render(structure, metadata, camera, title, exterior)
            draw = ImageDraw.Draw(image)
            draw.rectangle((28, 63, 1390, 111), fill='#101a20')
            subtitle = '完整外壳；最终 NBT 未剖切' if exterior else '检查剖视：隐藏近侧三层墙、端墙和拱间屋面；六道内部拱券保留'
            draw.text((35, 70), subtitle, font=review.font(19), fill='#8ebbb9')
            path = output / f'{mode}-{direction}.png'
            image.save(path)
            sheet.paste(image, ((view_index % 2) * 1400, (view_index // 2) * 1080))
            records.append({'mode': mode, 'direction': direction, 'path': str(path), 'sha256': digest(path), 'dimensions': list(image.size)})
            print(f'RENDERED {mode}-{direction}', flush=True)
        path = output / f'{mode}-four-views.png'
        sheet.save(path)
        records.append({'mode': mode, 'direction': 'four-view-sheet', 'path': str(path), 'sha256': digest(path), 'dimensions': list(sheet.size)})
    preview = review.render(structure, metadata, (-0.55, -1), '北端轴向剖视 / 实际方块数据', False, size=(1800, 1360))
    preview.save(ROOT / 'views/cutaway-overview.png')
    preview.save(ROOT / 'preview.png')
    dump(ROOT / 'qa/render-manifest.json', {'status': 'RENDERED_PENDING_VISUAL_REVIEW', 'sha256': digest(ROOT / 'room.nbt'), 'renderer': str(SHARED), 'textures': 'Minecraft 1.20.1 client assets; software orthographic renderer', 'preview': str(ROOT / 'preview.png'), 'source_is_final_nbt': True, 'is_gameplay_screenshot': False, 'exterior_modified': False, 'views': records})


if __name__ == '__main__':
    main()
