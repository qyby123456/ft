import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parent
references = json.loads((root / 'references.json').read_text(encoding='utf-8-sig'))
output = root / 'qa'
output.mkdir(exist_ok=True)
for reference in references['references']:
    identifier = reference['id']
    sheet = Image.new('RGB', (2000, 1000), '#16333b')
    drawing = ImageDraw.Draw(sheet)
    for offset, candidate in enumerate([Path(reference['image_path']), root / identifier / 'cutaway.png']):
        image = Image.open(candidate).convert('RGB')
        image.thumbnail((1000, 940))
        sheet.paste(image, (offset * 1000 + (1000-image.width)//2, 40 + (940-image.height)//2))
    drawing.text((20, 15), identifier + ' reference', fill='white')
    drawing.text((1020, 15), identifier + ' actual Lodestone candidate', fill='white')
    sheet.save(output / (identifier + '-comparison.jpg'), quality=94)
