import json
from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parent
references = json.loads((root / 'references.json').read_text(encoding='utf-8'))['references']
rooms = sorted(folder for folder in root.iterdir() if folder.is_dir() and folder.name[:2].isdigit())
output = root / 'qa' / 'visual-comparisons'
output.mkdir(exist_ok=True)
for offset in range(0, 10, 2):
    canvas = Image.new('RGB', (1800, 1700), '#171a22')
    draw = ImageDraw.Draw(canvas)
    for row in range(2):
        index = offset + row
        room = rooms[index]
        base = row * 850
        draw.text((12, base + 8), f"S6-{11 + index} REFERENCE | FINAL LODESTONE CUTAWAY | EXTERIOR BELOW", fill='white')
        for column, filename in enumerate([Path(references[index]['image_path']), room / 'cutaway.png']):
            picture = Image.open(filename).convert('RGB')
            picture.thumbnail((888, 575))
            canvas.paste(picture, (column * 900 + (900 - picture.width) // 2, base + 35))
        exterior = Image.open(room / 'complete.png').convert('RGB')
        exterior.thumbnail((670, 230))
        canvas.paste(exterior, (930, base + 610))
        draw.text((15, base + 650), 'Exterior is complete; cutaway alters display copy only.', fill='white')
        draw.text((15, base + 680), 'Static self-check; independent acceptance pending.', fill='white')
    canvas.save(output / f'comparison-{11 + offset}-{12 + offset}.jpg', quality=92)
print('Wrote five final reference / interior / exterior comparison sheets')
