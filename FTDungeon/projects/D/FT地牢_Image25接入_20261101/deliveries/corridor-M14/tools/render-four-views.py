import importlib.util
import json
import shutil
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source_root = root / 'render-source'
source = source_root / 'corridor-M14'
source.mkdir(parents=True, exist_ok=True)
shutil.copy2(root / 'room.nbt', source / 'room.nbt')
shutil.copy2(root / 'room.json', source / 'room.json')
renderer_path = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py')
spec = importlib.util.spec_from_file_location('corridor_renderer', renderer_path)
renderer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(renderer)
renderer.ROOM_IDS = ('corridor-M14',)
renderer.REVIEW = root / 'preview' / 'four-views'


def corridor_filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        name = block.name.removeprefix('minecraft:')
        if name in renderer.INVISIBLE:
            continue
        block_x, block_y, block_z = position
        if not exterior:
            below = structure.blocks.get((block_x, block_y - 1, block_z))
            anchor = below and below.name.removeprefix('minecraft:') in ('hanging_roots', 'spore_blossom')
            if block_y >= 10 and not (block_y == 10 and (name == 'polished_basalt' or anchor)):
                continue
            near_x = block_x <= 1 if camera[0] < 0 else block_x >= 13
            near_z = block_z == 0 if camera[1] < 0 else block_z == 40
            if block_y >= 2 and ((near_x and name != 'polished_basalt') or near_z):
                continue
        result[position] = block
    return result


renderer.filtered = corridor_filtered
snapshot = renderer.REVIEW / 'm14-final' / 'corridor-M14'
snapshot.mkdir(parents=True, exist_ok=True)
shutil.copy2(root / 'room.nbt', snapshot / 'room.nbt')
shutil.copy2(root / 'room.json', snapshot / 'room.json')
sys.argv = [str(renderer_path), '--rooms', 'corridor-M14', '--revision', 'm14-final', '--source-root', str(source_root)]
renderer.main()
manifest = renderer.REVIEW / 'm14-final' / 'render-manifest.json'
if manifest.exists():
    data = json.loads(manifest.read_text(encoding='utf-8'))
    (root / 'qa' / 'four-view-manifest.json').write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
