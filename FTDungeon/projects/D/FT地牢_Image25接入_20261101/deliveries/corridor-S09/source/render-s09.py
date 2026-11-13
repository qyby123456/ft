import hashlib
import importlib.util
import json
from pathlib import Path

from PIL import Image

from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]
RENDERER = ROOT.parents[1] / 'tools/review_four_views.py'
specification = importlib.util.spec_from_file_location('s09_four_views', RENDERER)
review = importlib.util.module_from_spec(specification)
specification.loader.exec_module(review)


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        if block.name.removeprefix('minecraft:') in review.INVISIBLE:
            continue
        block_x, block_y, block_z = position
        if not exterior:
            if block_y == 8:
                continue
            near_wall = block_x <= 1 if camera[0] < 0 else block_x >= 9
            near_end = block_z == 0 if camera[1] < 0 else block_z == 15
            if block_y >= 1 and (near_wall or near_end):
                continue
        result[position] = block
    return result


def main():
    review.filtered = filtered
    structure = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    fingerprint = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    views = []
    for exterior in [False, True]:
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2160), '#101a20')
        for view_index, (direction, title, camera) in enumerate(review.VIEWS):
            image = review.render(structure, metadata, camera, title, exterior)
            target = ROOT / 'views' / f'{mode}-{direction}.png'
            image.save(target)
            sheet.paste(image, ((view_index % 2) * 1400, (view_index // 2) * 1080))
            views.append({'direction': direction, 'mode': mode, 'path': str(target.relative_to(ROOT)).replace('\\', '/'),
                          'image_sha256': hashlib.sha256(target.read_bytes()).hexdigest()})
            print(f'RENDERED {mode}-{direction}', flush=True)
            if not exterior and direction == 'north':
                image.save(ROOT / 'preview.png')
        sheet.save(ROOT / 'views' / f'{mode}-four-views.png')
    manifest = {'id': 'corridor-S09', 'sha256': fingerprint, 'render_source': 'room.nbt',
                'renderer': 'Minecraft 1.20.1 blockstate/model/texture software renderer',
                'not_game_screenshots': True,
                'cutaway_rule': 'Interior: hide y=8 roof, near 2-block-thick outer wall and near end arch; retain inner piers, crossbeams, chain anchors and all decoration. NBT unchanged.',
                'approximation': 'Skeleton skulls use synthetic skull geometry; lanterns/cages/chains use vanilla block models. No in-game dynamic lighting simulation.',
                'views': views}
    (ROOT / 'views/manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
