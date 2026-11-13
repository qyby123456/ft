from __future__ import annotations

import importlib.util
import json
import sys
from pathlib import Path
from PIL import Image, ImageDraw

from static_qa import ROOT, digest, dump

SHARED = ROOT.parents[1] / 'tools/review_four_views.py'
specification = importlib.util.spec_from_file_location('r05_shared_review', SHARED)
review = importlib.util.module_from_spec(specification)
sys.modules[specification.name] = review
specification.loader.exec_module(review)


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, state in structure.blocks.items():
        if state.name.removeprefix('minecraft:') in review.INVISIBLE:
            continue
        block_x, height, block_z = position
        if not exterior:
            close_x = block_x <= 1 if camera[0] < 0 else block_x >= 25
            close_z = block_z <= 1 if camera[1] < 0 else block_z >= 27
            if height >= 13 or height >= 2 and (close_x or close_z):
                continue
        result[position] = state
    return result


def main():
    review.filtered = filtered
    structure = review.Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    output = ROOT / 'views'
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2160), '#101a20')
        for view_index, (direction, title, camera) in enumerate(review.VIEWS):
            image = review.render(structure, metadata, camera, title, exterior)
            draw = ImageDraw.Draw(image)
            draw.rectangle((28, 63, 1390, 111), fill='#101a20')
            subtitle = '完整外壳：双层屋顶、入口与围护墙；最终 NBT 未剖切' if exterior else '检查剖视：仅隐藏近侧双层墙、顶层梁与屋面；四柱、残廊与家具保留'
            draw.text((35, 70), subtitle, font=review.font(18), fill='#8ebbb9')
            path = output / f'{mode}-{direction}.png'
            image.save(path)
            sheet.paste(image, ((view_index % 2) * 1400, (view_index // 2) * 1080))
            records.append({'mode': mode, 'direction': direction, 'path': str(path), 'sha256': digest(path), 'dimensions': list(image.size)})
            print(f'RENDERED {mode}-{direction}', flush=True)
        path = output / f'{mode}-four-views.png'
        sheet.save(path)
        records.append({'mode': mode, 'direction': 'four-view-sheet', 'path': str(path), 'sha256': digest(path), 'dimensions': list(sheet.size)})
    overview = review.render(structure, metadata, (-0.8, -1), '破碎档案厅 / 实际 NBT 剖视', False, size=(1800, 1360))
    overview.save(ROOT / 'preview.png')
    overview.save(output / 'cutaway-overview.png')
    dump(ROOT / 'qa/render-manifest.json', {'status': 'RENDERED_PENDING_VISUAL_REVIEW', 'sha256': digest(ROOT / 'room.nbt'), 'renderer': str(SHARED), 'textures': 'Minecraft 1.20.1 client assets; software orthographic renderer', 'source_is_final_nbt': True, 'is_gameplay_screenshot': False, 'exterior_modified': False, 'views': records})


if __name__ == '__main__':
    main()
