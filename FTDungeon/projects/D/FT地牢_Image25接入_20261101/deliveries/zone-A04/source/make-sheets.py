from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[1]
font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 23)
small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 18)
directions = [('north', '北'), ('south', '南'), ('west', '西'), ('east', '东')]
for mode, title in [('interior', '内部剖视 · 仅移除屋顶与近侧外墙'), ('exterior', '完整外壳 · 最终 NBT 保留屋顶')]:
    sheet = Image.new('RGB', (1440, 1100), '#1c252b')
    canvas = ImageDraw.Draw(sheet)
    canvas.text((20, 12), 'A04 砸毁工坊 | ' + title, font=font, fill='#e4e9ec')
    for index, (direction, label) in enumerate(directions):
        tile = Image.open(root / 'views' / f'{mode}-{direction}.png').convert('RGB').resize((720, 500), Image.Resampling.LANCZOS)
        left = index % 2 * 720
        top = 52 + index // 2 * 520
        sheet.paste(tile, (left, top))
        canvas.text((left + 18, top + 12), label + '面 / ' + direction, font=small, fill='#ffffff', stroke_width=2, stroke_fill='#1c252b')
    sheet.save(root / 'views' / f'{mode}-four-views.png')
sheet = Image.new('RGB', (1920, 760), '#1c252b')
canvas = ImageDraw.Draw(sheet)
canvas.text((20, 12), 'A04 砸毁工坊 | 北 / 南 / 西 / 东 · 上排内部剖视 / 下排完整外壳', font=font, fill='#e4e9ec')
for column, (direction, label) in enumerate(directions):
    for row, mode in enumerate(['interior', 'exterior']):
        tile = Image.open(root / 'views' / f'{mode}-{direction}.png').convert('RGB').resize((480, 333), Image.Resampling.LANCZOS)
        sheet.paste(tile, (column * 480, 52 + row * 350))
        canvas.text((column * 480 + 12, 60 + row * 350), label + '面', font=small, fill='#ffffff', stroke_width=2, stroke_fill='#1c252b')
sheet.save(root / 'views' / 'four-direction-review.png')
print('Created three review sheets from actual Lodestone NBT renders.')
