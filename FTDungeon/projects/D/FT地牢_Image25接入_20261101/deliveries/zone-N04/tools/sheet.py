import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / 'views/manifest.json').read_text(encoding='utf-8'))
font = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 23)
thumb_width = 576
thumb_height = 384
sheet = Image.new('RGB', (thumb_width * 4, (thumb_height + 40) * 3), '#10121b')
draw = ImageDraw.Draw(sheet)
for row, mode in enumerate(['interior', 'cutaway', 'exterior']):
    for column, direction in enumerate(['north', 'east', 'south', 'west']):
        image_path = ROOT / 'views' / (direction + '-' + mode + '.png')
        with Image.open(image_path) as image:
            assert image.size == (1536, 1024)
            thumbnail = image.convert('RGB').resize((thumb_width, thumb_height), Image.Resampling.LANCZOS)
        sheet.paste(thumbnail, (column * thumb_width, row * (thumb_height + 40) + 40))
        draw.text((column * thumb_width + 12, row * (thumb_height + 40) + 7), direction.upper() + ' / ' + mode, fill='#d8d5e6', font=font)
sheet.save(ROOT / 'views/contact-sheet.png')
print(ROOT / 'views/contact-sheet.png')
