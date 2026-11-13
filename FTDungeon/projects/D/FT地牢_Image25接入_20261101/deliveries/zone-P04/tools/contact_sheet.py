import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[1]
font_path = Path('C:/Windows/Fonts/msyh.ttc')
font = ImageFont.truetype(str(font_path), 25)
small = ImageFont.truetype(str(font_path), 17)
sha = hashlib.sha256((root / 'room.nbt').read_bytes()).hexdigest()
canvas = Image.new('RGB', (1440, 2300), '#10171c')
draw = ImageDraw.Draw(canvas)
draw.text((28, 18), 'P04 崩裂岩沿 · 最终 NBT 四向内外视图', fill='#f0e0bb', font=font)
draw.text((28, 58), '左：内部剖视（仅渲染移除屋顶/近墙）  右：完整外壳', fill='#c9bda8', font=small)
draw.text((28, 88), 'SHA-256 ' + sha, fill='#95b9b0', font=small)
for index, direction in enumerate(['north', 'south', 'west', 'east']):
    for column, kind in enumerate(['interior', 'exterior']):
        image_path = root / 'views' / (kind + '-' + direction + '.png')
        with Image.open(image_path) as image:
            image.verify()
        with Image.open(image_path) as image:
            thumbnail = image.convert('RGB').resize((700, 496), Image.Resampling.LANCZOS)
        left = 15 + column * 715
        top = 148 + index * 535
        draw.text((left + 8, top), direction.upper() + ' / ' + kind.upper(), fill='#f0e0bb', font=small)
        canvas.paste(thumbnail, (left, top + 28))
canvas.save(root / 'views/four-direction-review.png')
print(json.dumps({'status': 'PASS', 'room_sha256': sha, 'views': 8, 'sheet': 'views/four-direction-review.png'}))
