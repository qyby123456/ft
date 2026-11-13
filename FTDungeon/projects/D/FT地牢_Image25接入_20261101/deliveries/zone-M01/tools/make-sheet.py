import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageStat

ROOT = Path(__file__).resolve().parents[1]
VIEWS = ROOT / 'views'
FONT = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 23)
SMALL = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 17)
source_hash = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
records = []
for image_path in sorted(VIEWS.glob('*.png')):
    if image_path.stem.endswith('sheet'):
        continue
    with Image.open(image_path) as image:
        image.load()
        deviation = max(ImageStat.Stat(image.convert('RGB')).stddev)
        if image.size != (1440, 1000) or deviation < 15:
            raise RuntimeError('Invalid or blank image: ' + str(image_path))
        records.append({'file': image_path.relative_to(ROOT).as_posix(), 'size': list(image.size), 'sha256': hashlib.sha256(image_path.read_bytes()).hexdigest(), 'stddev_max': deviation})
for mode in ['interior', 'exterior']:
    sheet = Image.new('RGB', (1600, 1260), '#151e26')
    draw = ImageDraw.Draw(sheet)
    draw.text((30, 20), 'ZONE-M01 | ACTIVE MINE TUNNEL | ' + mode.upper(), font=FONT, fill='#f1e9dc')
    draw.text((30, 56), 'Actual NBT software render | ' + source_hash, font=SMALL, fill='#b3c4cc')
    for index, direction in enumerate(['north', 'east', 'south', 'west']):
        left = 25 + index % 2 * 790
        top = 100 + index // 2 * 565
        with Image.open(VIEWS / (mode + '-' + direction + '.png')) as image:
            image = image.convert('RGB')
            image.thumbnail((765, 531), Image.Resampling.LANCZOS)
            sheet.paste(image, (left, top + 27))
        draw.text((left + 5, top), direction.upper(), font=SMALL, fill='#f1e9dc')
    sheet.save(VIEWS / (mode + '-sheet.png'))
report = {'status': 'PASS', 'source_sha256': source_hash, 'decoded_views': len(records), 'views': records, 'contact_sheets': ['views/interior-sheet.png', 'views/exterior-sheet.png']}
(ROOT / 'qa/image-decode.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'status': 'PASS', 'decoded_views': len(records)}))
