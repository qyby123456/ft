from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[1]
font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 28)
small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 20)
sheet = Image.new('RGB', (1536, 2240), '#151a21')
draw = ImageDraw.Draw(sheet)
draw.text((28, 15), 'A02 塌毁卧室  |  21 × 12 × 21  |  四向内外视图', font=font, fill='#f3e7ce')
for mode_index, mode in enumerate(['interior', 'exterior']):
    for index, direction in enumerate(['north', 'east', 'south', 'west']):
        image = Image.open(root / 'views' / f'{mode}-{direction}.png').convert('RGB')
        image.thumbnail((758, 505))
        column = index % 2
        row = index // 2 + mode_index * 2
        origin = (10 + column * 768, 67 + row * 542)
        sheet.paste(image, origin)
        draw.text((origin[0] + 8, origin[1] + 507), f'{mode} · {direction}  |  viewer-only cutaway' if mode == 'interior' else f'{mode} · {direction}  |  complete NBT', font=small, fill='#b7c9d6')
sheet.save(root / 'views' / 'eight-view-sheet.png')
preview = Image.open(root / 'views' / 'interior-south.png').convert('RGB')
preview.save(root / 'preview.png')
print(root / 'views' / 'eight-view-sheet.png')

