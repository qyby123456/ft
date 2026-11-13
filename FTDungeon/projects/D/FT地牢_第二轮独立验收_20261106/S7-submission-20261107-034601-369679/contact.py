from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parent
for numbers in [(11,12),(13,14),(15,16)]:
    canvas = Image.new('RGB', (1200,900), '#17212b')
    draw = ImageDraw.Draw(canvas)
    for row, number in enumerate(numbers):
        for column, filename in enumerate(['cutaway.png','feature-detail.png']):
            image = Image.open(root / f'S7-{number}' / filename).convert('RGB')
            image.thumbnail((600,420))
            canvas.paste(image,(column*600,row*450+25))
            draw.text((column*600+10,row*450+6),f'S7-{number} {filename}', fill='white')
    canvas.save(root / f'takeover-views-{numbers[0]}-{numbers[1]}.jpg')
