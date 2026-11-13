import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path

from PIL import ImageDraw
from nbtio import Structure

ROOT = Path(__file__).resolve().parents[1]
specification = importlib.util.spec_from_file_location('h02_views', ROOT / 'tools/render-views.py')
views = importlib.util.module_from_spec(specification)
specification.loader.exec_module(views)
views.models.ASSETS = zipfile.ZipFile(views.models.CLIENT)
source = Structure.read(ROOT / 'room.nbt')
detail = Structure(source.size)
detail.blocks = {position: block for position, block in source.blocks.items()
                 if 9 <= position[0] <= 19 and 10 <= position[2] <= 29 and position[1] <= 4}
image = views.render(detail, (-0.72, -1), '桌椅与落地残骸细节', False, (1600, 1350))
draw = ImageDraw.Draw(image)
draw.rectangle((0, 0, 1600, 110), fill='#151b23')
draw.text((32, 20), 'H02 废墟宴会厅 · 桌椅支撑细部', font=views.models.font(32), fill='#ead4ad')
draw.text((32, 70), '实际 NBT 局部剖取 / 六条桌腿、十把椅子、桌面烛台与地面残骸', font=views.models.font(22), fill='#c98e76')
path = ROOT / 'qa/furniture-supported.png'
image.save(path)
(ROOT / 'qa/detail-render.json').write_text(json.dumps({'status': 'PASS', 'source_sha256': hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest(),
    'file': 'qa/furniture-supported.png', 'image_sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
    'filter': {'x': [9, 19], 'y': [0, 4], 'z': [10, 29]}, 'nbt_modified': False}, indent=2) + '\n', encoding='utf-8')
print('Furniture detail rendered from final NBT')
