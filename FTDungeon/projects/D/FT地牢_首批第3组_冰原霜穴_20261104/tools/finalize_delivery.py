import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def rotate(position, size, turns):
    across, elevation, along = position
    width, height, depth = size
    for _ in range(turns):
        across, along = depth - 1 - along, across
        width, depth = depth, width
    return [across, elevation, along], [width, height, depth]

def main():
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    evidence = json.loads((ROOT / 'evidence/reference-evidence.json').read_text(encoding='utf-8'))
    for entry in evidence['entries']:
        assert hashlib.sha256(Path(entry['source_path']).read_bytes()).hexdigest() == entry['source_sha256']
    images = Path(manifest['entries'][0]['reference']).parent
    references = [dict(path=str(path), sha256=hashlib.sha256(path.read_bytes()).hexdigest()) for path in sorted(images.glob('S3-0*.png')) + [images / 'S3-10-overview.png']]
    manifest['reference_images'] = references
    manifest['group_fingerprint'] = hashlib.sha256('\n'.join(item['path'] + ':' + item['sha256'] for item in references).encode('utf-8')).hexdigest()
    manifest['status'] = 'CANDIDATE_READY; native world placement, ticks and integration pending'
    manifest['unpacked_authority'] = str(ROOT / 'evidence/reference-evidence.json')
    directions = ['north', 'east', 'south', 'west']
    rotations = []
    for entry in manifest['entries']:
        folder = ROOT / 'incoming' / entry['id']
        assert hashlib.sha256((folder / 'room.nbt').read_bytes()).hexdigest() == entry['sha256']
        for quarter in range(4):
            dimensions = rotate(entry['entrance'], entry['size'], quarter)[1]
            connectors = []
            for connection in entry['connections']:
                position, _ = rotate(connection['position'], entry['size'], quarter)
                connectors.append(dict(connection, position=position, facing=directions[(directions.index(connection['facing']) + quarter) % 4]))
            rotations.append(dict(id=entry['id'], clockwise_degrees=quarter * 90, size=dimensions, connectors=connectors,
                coordinate_basis='Nonnegative rotated bounding box origin; engine pivot placement requires corresponding translation. No world placement claimed.'))
        qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
        for report_name, key in [('lodestone.json', 'lodestone_exterior'), ('lodestone-interior.json', 'lodestone_interior')]:
            reports = json.loads((ROOT / 'qa' / report_name).read_text(encoding='utf-8'))
            qa[key] = next(report for report in reports if report['id'] == entry['id'])
            assert qa[key]['nbt_sha256'] == entry['sha256']
        save(folder / 'qa.json', qa)
    save(ROOT / 'rotated-connectors.json', rotations)
    save(ROOT / 'manifest.json', manifest)
    sheet = Image.new('RGB', (1800, 1374), '#101a20')
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
    for index, entry in enumerate(manifest['entries']):
        folder = ROOT / 'incoming' / entry['id']
        image = Image.open(folder / 'lodestone-interior.png').convert('RGB').resize((600, 417))
        across, elevation = index % 3 * 600, index // 3 * 458
        sheet.paste(image, (across, elevation))
        ImageDraw.Draw(sheet).text((across + 12, elevation + 421), entry['id'] + ' ' + entry['name'], font=font, fill='#dfedf5')
    sheet.save(ROOT / 'lodestone-overview.png')
    hashes = {}
    for file in sorted(ROOT.rglob('*')):
        if file.is_file() and file.suffix in {'.nbt', '.png', '.json', '.mjs', '.java', '.py', '.ps1', '.md'} and 'native-classes' not in file.parts and file.name != 'SHA256.json':
            hashes[str(file.relative_to(ROOT))] = hashlib.sha256(file.read_bytes()).hexdigest()
    save(ROOT / 'SHA256.json', hashes)
    print('FINALIZED 9 modules, 18 Lodestone previews, 72 directional software views, reference hashes unchanged')

if __name__ == '__main__':
    main()
