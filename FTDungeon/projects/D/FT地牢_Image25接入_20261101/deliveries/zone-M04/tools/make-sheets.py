from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parent.parent
font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 24)
for mode, title in [('interior', 'zone-M04 熔岩矿脉 · 四向内部剖切'), ('exterior', 'zone-M04 熔岩矿脉 · 四向完整外观')]:
    sheet = Image.new('RGB', (1600, 1160), '#151923')
    drawing = ImageDraw.Draw(sheet)
    drawing.text((28, 12), title, font=font, fill='#f2dfc6')
    for index, direction in enumerate(['north', 'south', 'west', 'east']):
        image = Image.open(root / 'views' / f'{mode}-{direction}.png').convert('RGB')
        image.thumbnail((800, 550), Image.Resampling.LANCZOS)
        sheet.paste(image, ((index % 2) * 800, 60 + (index // 2) * 550))
    sheet.save(root / 'views' / f'{mode}-four-views.png')
print('Created two four-view contact sheets from actual NBT renders.')
