import hashlib
import importlib.util
import itertools
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
BASE = Path('D:/ft工程文件')
SPEC = importlib.util.spec_from_file_location('reader', BASE / '结构参考_20261103/analyze_structures.py')
READER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(READER)
GROUPS = [
    (2, 'FT地牢_第2组重做B_20261104/S2-01', 'incoming/G2B-01/room.nbt', 'incoming/G2B-01/preview.png'),
    (3, 'FT地牢_第3组重做B_20261104/S3-01', 'room.nbt', 'preview.png'),
    (4, 'FT地牢_第4组逐图返工_20261104/S4-01', 'incoming/S4-01/room.nbt', 'preview-reverse.png'),
    (5, 'FT地牢_第5组逐图重做_20261104/01-coral-chamber', 'nbt/s5-01-coral-chamber-redo.nbt', 'qa/lodestone-front-WATER-HIDDEN.png'),
    (6, 'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/01-large-chamber', 'room.nbt', 'preview.png'),
    (7, 'FT地牢_第7组逐图重做_20261104/01-large-chamber', 'incoming/S7R2-01/room.nbt', 'lodestone-interior.png'),
    (8, 'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01', 'incoming/S8R_IMG01/room.nbt', 'qa/takeover-lodestone-cutaway-north.png'),
]
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:water', 'minecraft:lava'}

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def decode(path):
    reader = READER.NbtReader(path.read_bytes())
    assert reader.number('>B') == 10
    reader.string()
    result = reader.value(10)
    assert reader.offset == len(reader.payload)
    return result

def occupancy(blocks, size, rotation):
    occupied = set()
    for east, up, south in blocks:
        horizontal = east / (size[0] - 1), south / (size[2] - 1)
        for unused in range(rotation):
            horizontal = 1 - horizontal[1], horizontal[0]
        occupied.add((round(horizontal[0] * 24), round(up / (size[1] - 1) * 16), round(horizontal[1] * 24)))
    return occupied

def panel(path, label):
    canvas = Image.new('RGB', (800, 560), '#20232b')
    image = Image.open(path).convert('RGB')
    image.thumbnail((790, 520))
    canvas.paste(image, ((800 - image.width) // 2, 34 + (520 - image.height) // 2))
    ImageDraw.Draw(canvas).text((12, 10), label, fill='white')
    return canvas

def main():
    records, masks = [], {}
    for group, relative, candidate, preview in GROUPS:
        directory = BASE / relative
        path = directory / candidate
        template = decode(path)
        size, palette = template['size'], template['palette']
        positions = [tuple(entry['pos']) for entry in template['blocks']]
        assert len(positions) == len(set(positions))
        assert all(all(0 <= coordinate < size[index] for index, coordinate in enumerate(position)) for position in positions)
        assert all(0 <= entry['state'] < len(palette) for entry in template['blocks'])
        assert template['DataVersion'] == 3465
        assert all(entry['Name'].startswith('minecraft:') for entry in palette)
        block_map = {tuple(entry['pos']): palette[entry['state']] for entry in template['blocks']}
        solids = {position for position, state in block_map.items() if state['Name'] not in AIR}
        interior = {position for position in solids if 1 < position[0] < size[0] - 2 and 1 < position[2] < size[2] - 2 and 1 < position[1] < size[1] - 2}
        masks[group] = [occupancy(interior, size, rotation) for rotation in range(4)]
        manifests = []
        for manifest in directory.rglob('*'):
            if not manifest.is_file() or 'checkpoint-original' in manifest.parts or 'revision-history' in manifest.parts or manifest.name.lower() not in {'sha256.json', 'sha256sums.json', 'checksums.json', 'sha256sums.txt', 'manifest.json', 'file_manifest.json'}:
                continue
            entries = []
            if manifest.suffix == '.json':
                data = json.loads(manifest.read_text(encoding='utf-8-sig'))
                if isinstance(data, dict):
                    entries = [(name, expected) for name, expected in data.items() if isinstance(expected, str) and len(expected) == 64]
                    if isinstance(data.get('files'), list):
                        entries = [(entry['path'], entry['sha256']) for entry in data['files']]
                elif isinstance(data, list):
                    entries = [(entry['path'], entry['sha256']) for entry in data]
            else:
                for line in manifest.read_text(encoding='utf-8-sig').splitlines():
                    fields = line.split(maxsplit=1)
                    if len(fields) == 2 and len(fields[0]) == 64:
                        entries.append((fields[1].lstrip('* '), fields[0]))
            errors = []
            for name, expected in entries:
                target = manifest.parent / name
                if not target.is_file():
                    target = directory / name
                if not target.is_file() or digest(target).lower() != expected.lower():
                    errors.append(name)
            manifests.append({'path': str(manifest), 'checked': len(entries), 'mismatches': errors})
        metadata_path = path.with_suffix('.json')
        metadata = json.loads(metadata_path.read_text(encoding='utf-8')) if metadata_path.is_file() else {}
        references = [Path(metadata['reference'])] if metadata.get('reference') else list((BASE / 'image2.5/nature-dungeon-styles-20261104').glob(f'S{group}-*/out/S{group}-01-large-chamber.png'))
        assert len(references) == 1
        reference = references[0]
        comparison = Image.new('RGB', (1600, 560))
        comparison.paste(panel(reference, f'{reference.stem} REFERENCE'), (0, 0))
        comparison.paste(panel(directory / preview, f'ACTUAL NBT PREVIEW {size}'), (800, 0))
        comparison.save(ROOT / f'{reference.stem}-comparison.jpg')
        records.append({'group': group, 'candidate': str(path), 'sha256': digest(path), 'size': size, 'reference': str(reference), 'reference_sha256': digest(reference), 'blocks': len(positions), 'palette_states': len(palette), 'decode_bounds_unique_palette_indices_data_version': 'PASS', 'roof_top_solid_coverage': sum((east, size[1] - 1, south) in solids for east in range(size[0]) for south in range(size[2])) / (size[0] * size[2]), 'manifests': manifests, 'status': 'UNDER_REVIEW_NO_RELEASE', 'world_ticks': 'NOT_RUN'})
    similarities = []
    for first, second in itertools.combinations(masks, 2):
        first_mask = masks[first][0]
        score = max(len(first_mask & other) / len(first_mask | other) for other in masks[second])
        similarities.append({'groups': [first, second], 'normalized_interior_jaccard_best_y_rotation': score})
    report = {'scope': 'Read-only independent file audit; occupancy is screening, not semantic certification.', 'groups': records, 'similarity_screen': sorted(similarities, key=lambda item: -item['normalized_interior_jaccard_best_y_rotation'])}
    (ROOT / 'file-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    for record in records:
        print(record['group'], record['size'], record['sha256'], record['manifests'])
    print(json.dumps(report['similarity_screen'], indent=2))

if __name__ == '__main__':
    main()
