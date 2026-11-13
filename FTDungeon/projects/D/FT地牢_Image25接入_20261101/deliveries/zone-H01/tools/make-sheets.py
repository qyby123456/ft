import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
FONT = 'C:/Windows/Fonts/msyh.ttc'
directions = (('north', '北'), ('south', '南'), ('west', '西'), ('east', '东'))
records = []
for image_path in sorted((ROOT / 'views').glob('*.png')):
    if 'sheet' in image_path.stem:
        continue
    with Image.open(image_path) as source:
        source.load()
        if source.size != (1600, 1100):
            raise ValueError(f'Unexpected image size: {image_path}')
        records.append({'file': str(image_path.relative_to(ROOT)), 'size': list(source.size), 'sha256': hashlib.sha256(image_path.read_bytes()).hexdigest()})

for mode, title in (('interior', '内部四向剖视'), ('exterior', '完整外壳四向')):
    sheet = Image.new('RGB', (2400, 1830), '#161a22')
    draw = ImageDraw.Draw(sheet)
    draw.text((40, 24), f'H01 残金王座大厅 | {title}', font=ImageFont.truetype(FONT, 42), fill='#f1dfbd')
    draw.text((40, 88), '29 × 18 × 33 · 实际 NBT / Lodestone 渲染 · 非游戏截图', font=ImageFont.truetype(FONT, 27), fill='#b9c3d0')
    for index, (direction, label) in enumerate(directions):
        left = 24 + (index % 2) * 1188
        top = 145 + (index // 2) * 830
        with Image.open(ROOT / 'views' / f'{mode}-{direction}.png') as image:
            thumbnail = ImageOps.contain(image, (1160, 760), Image.Resampling.LANCZOS)
            sheet.paste(thumbnail, (left + (1160 - thumbnail.width) // 2, top))
        draw.text((left + 16, top + 770), f'{label}面 / {direction.upper()}', font=ImageFont.truetype(FONT, 28), fill='#edcf9c')
    sheet.save(ROOT / 'views' / f'{mode}-sheet.png')

if len(records) != 10:
    raise ValueError('Expected ten original renders')
report = {'status': 'PASS', 'original_image_count': len(records), 'all_decode': True, 'images': records, 'sheets': ['views/interior-sheet.png', 'views/exterior-sheet.png']}
(ROOT / 'qa/image-decode.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('PASS: 10 originals decode; 2 sheets written')
