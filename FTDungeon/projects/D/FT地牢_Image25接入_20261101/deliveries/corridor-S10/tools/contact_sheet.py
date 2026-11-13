import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103')
SIDES = ('north', 'south', 'west', 'east')
FONT = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 24)
SMALL = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 18)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def entry(value):
    if isinstance(value, dict):
        if value.get('id') == 'S10':
            return value
        for child in value.values():
            result = entry(child)
            if result:
                return result
    if isinstance(value, list):
        for child in value:
            result = entry(child)
            if result:
                return result
    return None


def load_image(path):
    with Image.open(path) as image:
        image.verify()
    with Image.open(path) as image:
        image.load()
        return image.convert('RGB')


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    reference = Path(metadata['reference'])
    original = load_image(reference)
    assert original.size == (1536, 1024)
    assert digest(reference) == metadata['reference_sha256']
    records = {}
    for filename in ('manifest.json', 'out/corridors_meta.json', 'out/corridor_index.json'):
        source = SOURCE / filename
        record = entry(json.loads(source.read_text(encoding='utf-8-sig')))
        assert record, filename
        if 'sha256' in record:
            assert record['sha256'] == metadata['reference_sha256']
            assert record['bytes'] == reference.stat().st_size
            assert record['size'] == [1536, 1024]
            assert (SOURCE / record['path']).resolve() == reference.resolve()
        else:
            assert record['filename'] == reference.name
            assert record['theme'] == 'copper-machinery'
            assert record['condition'] == 'damaged'
            assert record['feature'] == 'broken-arches'
        records[filename] = {'sha256': digest(source), 'entry': record}
    readme = SOURCE / 'README.md'
    specification = SOURCE / 'SPEC.md'
    assert '| S10 | S10-copper-machinery-damaged |' in readme.read_text(encoding='utf-8')
    assert 'S01–S20' in specification.read_text(encoding='utf-8')
    report = {
        'status': 'PASS', 'reference': str(reference), 'reference_sha256': digest(reference),
        'dimensions': list(original.size), 'bytes': reference.stat().st_size,
        'full_png_decode': True, 'readme_sha256': digest(readme), 'spec_sha256': digest(specification),
        'records': records,
    }
    (ROOT / 'qa/reference.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    for kind in ('interior', 'exterior'):
        sheet = Image.new('RGB', (1600, 1260), '#101b20')
        draw = ImageDraw.Draw(sheet)
        draw.text((28, 18), f'CORRIDOR S10 / {kind.upper()} FOUR VIEWS / actual final room.nbt', font=FONT, fill='#eed6ba')
        draw.text((28, 54), f'SHA-256 {metadata["sha256"]}', font=SMALL, fill='#a5c8c3')
        for index, side in enumerate(SIDES):
            image = load_image(ROOT / f'views/{kind}-{side}.png')
            assert image.size == (1200, 850)
            image.thumbnail((770, 548), Image.Resampling.LANCZOS)
            column, row = index % 2, index // 2
            offset = (15 + column * 790, 112 + row * 570)
            sheet.paste(image, offset)
        sheet.save(ROOT / f'views/{kind}-four-views.png')
    preview = load_image(ROOT / 'preview.png')
    comparison = Image.new('RGB', (1600, 660), '#101b20')
    draw = ImageDraw.Draw(comparison)
    draw.text((24, 18), 'S10 / concept reference versus actual Java 1.20.1 NBT', font=FONT, fill='#eed6ba')
    for column, (title, image) in enumerate((('Concept reference (not the delivered build)', original), ('Actual NBT / south arch intentionally open', preview))):
        draw.text((24 + 800 * column, 60), title, font=SMALL, fill='#a5c8c3')
        image.thumbnail((760, 538), Image.Resampling.LANCZOS)
        comparison.paste(image, (20 + 800 * column, 106))
    comparison.save(ROOT / 'views/reference-comparison.png')
    print('REFERENCE_DECODE_AND_CONTACT_SHEETS_PASS')


if __name__ == '__main__':
    main()
