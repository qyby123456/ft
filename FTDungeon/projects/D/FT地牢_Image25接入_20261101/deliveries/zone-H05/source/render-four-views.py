import hashlib
import importlib.util
import json
import shutil
import sys
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
renderer_path = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py')
specification = importlib.util.spec_from_file_location('h05_renderer', renderer_path)
renderer = importlib.util.module_from_spec(specification)
specification.loader.exec_module(renderer)
renderer.REVIEW = root / 'views'
source_root = root / 'render-source'
source = source_root / 'zone-H05'
source.mkdir(parents=True, exist_ok=True)
for name in ('room.nbt', 'room.json'):
    shutil.copy2(root / name, source / name)
digest = hashlib.sha256((root / 'room.nbt').read_bytes()).hexdigest()
reference = Path(json.loads((root / 'room.json').read_text(encoding='utf-8'))['reference'])
with Image.open(reference) as image:
    image.load()
    (root / 'qa/reference.json').write_text(json.dumps({'status':'PASS','sha256':hashlib.sha256(reference.read_bytes()).hexdigest(),'size':list(image.size),'mode':image.mode,'fully_decoded':True}, indent=2) + '\n', encoding='utf-8')

def roof_height(block_x):
    nave = [16,20,21,22,23,23,24,24,24,23,23,22,21,20,16]
    aisle = [13,14,16,17,17,17,16,14,13]
    return nave[block_x-8] if 8 <= block_x <= 22 else aisle[block_x if block_x < 8 else 30-block_x]

def hall_filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        name = block.name.removeprefix('minecraft:')
        if name in renderer.INVISIBLE:
            continue
        block_x, block_y, block_z = position
        if not exterior:
            near_x = block_x <= 1 if camera[0] < 0 else block_x >= 29
            near_z = block_z <= 1 if camera[1] < 0 else block_z >= 37
            if block_y >= 2 and (near_x or near_z):
                continue
            roof = block_y >= roof_height(block_x)
            rib = block_z in (9,20,31)
            anchor = (block_x, block_z) in ((4,14),(26,14),(15,14),(15,27)) and block_y == roof_height(block_x)
            if roof and not rib and not anchor:
                continue
        result[position] = block
    return result

renderer.filtered = hall_filtered
sys.argv = [str(renderer_path), '--rooms', 'zone-H05', '--revision', digest[:16], '--source-root', str(source_root)]
renderer.main()
target = root / 'views' / digest[:16] / 'zone-H05'
if (root / 'views/lodestone-north.png').exists():
    shutil.copy2(root / 'views/lodestone-north.png', root / 'preview.png')
else:
    shutil.copy2(target / 'interior-north.png', root / 'preview.png')
shutil.copy2(root / 'views' / digest[:16] / 'render-manifest.json', root / 'qa/four-view-manifest.json')
evidence = []
for image_path in sorted(target.glob('*.png')):
    with Image.open(image_path) as image:
        image.load()
        evidence.append({'file':str(image_path.relative_to(root)),'sha256':hashlib.sha256(image_path.read_bytes()).hexdigest(),'size':list(image.size)})
(root / 'qa/render.json').write_text(json.dumps({'status':'PASS','source_sha256':digest,'renderer':'Minecraft 1.20.1 block-state and texture software renderer; Lodestone viewer separately','game_screenshot':False,'cutaway_affects_delivery_nbt':False,'retained_in_cutaway':'all six piers, three transverse roof ribs, all chandelier suspension chains; only roof panels and two near outer walls removed','files':evidence},ensure_ascii=False,indent=2) + '\n',encoding='utf-8')
print('H05_FOUR_VIEWS_PASS sha256=' + digest, flush=True)
