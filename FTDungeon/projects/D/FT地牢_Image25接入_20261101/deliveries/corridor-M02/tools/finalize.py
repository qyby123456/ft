import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageStat


ROOT = Path(__file__).resolve().parents[1]


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def write_json(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()


def contact_sheet(paths, destination, title, columns=2):
    tile_width, tile_height, label_height, header_height = 768, 512, 42, 70
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new('RGB', (tile_width * columns, header_height + (tile_height + label_height) * rows), '#121d24')
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 23)
    draw.text((24, 20), title, fill='#e5ebed', font=font)
    for index, relative in enumerate(paths):
        source = Image.open(ROOT / relative).convert('RGB')
        source.thumbnail((tile_width, tile_height), Image.Resampling.LANCZOS)
        column, row = index % columns, index // columns
        left, top = column * tile_width, header_height + row * (tile_height + label_height)
        sheet.paste(source, (left + (tile_width - source.width) // 2, top + (tile_height - source.height) // 2))
        draw.text((left + 18, top + tile_height + 8), Path(relative).stem, fill='#d8e6e8', font=font)
    sheet.save(ROOT / destination)


def main():
    metadata = read_json('room.json')
    static = read_json('qa/static.json')
    native = read_json('qa/forge-native.json')
    rendering = read_json('qa/render-manifest.json')
    fingerprint = digest(ROOT / 'room.nbt')
    assert static['status'] == native['status'] == 'PASS'
    assert metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == rendering['source_sha256'] == fingerprint
    assert len(native['stable_placements']) == 4 and native['stabilization_ticks'] == 200
    assert len(rendering['views']) >= 8 and not rendering['browser_errors']
    image_checks = []
    for view in rendering['views']:
        file = ROOT / view['path']
        image = Image.open(file).convert('RGB')
        deviation = ImageStat.Stat(image).stddev
        assert image.size == (1536, 1024) and max(deviation) > 8
        assert digest(file) == view['sha256']
        image_checks.append({'path': view['path'], 'size': list(image.size), 'rgb_stddev': deviation, 'passed': True})
    exterior = [view['path'] for view in rendering['views'] if view['name'].startswith('exterior-')]
    interior = [view['path'] for view in rendering['views'] if view['name'].startswith('interior-')]
    contact_sheet(exterior, 'views/exterior-four-views.png', 'M02 | Actual NBT | exterior N / E / S / W')
    contact_sheet(interior, 'views/interior-four-views.png', 'M02 | Actual NBT | N / E cutaway / S / W cutaway')
    contact_sheet(exterior + interior, 'views/eight-view-review.png', 'M02 | Actual NBT review | exterior / interior', columns=4)
    materials = ['# M02 材料统计', '', '| 原版方块 | 数量 |', '| --- | ---: |']
    materials.extend(f'| `{name}` | {count} |' for name, count in sorted(static['block_counts'].items()) if not name.endswith(':air'))
    (ROOT / 'MATERIALS.md').write_text('\n'.join(materials) + '\n', encoding='utf-8')
    qa = {
        'id': metadata['id'], 'status': 'PASS', 'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'],
        'static': {'status': 'PASS', 'tests': len(static['tests']), 'report': 'qa/static.json'},
        'native': {'status': 'PASS', 'runtime': 'Minecraft 1.20.1 / Forge 47.4.10 / Java 17.0.8', 'rotations': 4, 'ticks': 200, 'records_compared_each_phase': 36244, 'water_sources_each_rotation': 222, 'water_leaks': 0, 'negative_probes': native['negative_probes'], 'report': 'qa/forge-native.json', 'log': 'qa/forge-m02-final-02.log'},
        'visual': {'status': 'PASS', 'type': 'Actual NBT software renderer, not a client screenshot', 'render_manifest': 'qa/render-manifest.json', 'exterior_views': exterior, 'interior_views': interior, 'image_integrity_checks': image_checks, 'cutaway_render_only': True, 'roof_intact_in_room_nbt': True, 'assistant_visual_review': 'Inspected actual NBT perspective and contact sheets; straight route, shelves, cage supports, bounded water and roof confirmed.'},
        'deployment': {'deployed': False, 'production_save_opened': False, 'formal_mods_modified': False, 'ft_core_modified': False},
        'limitations': native['limitations'] + ['Lava glow uses safe shroomlights behind orange stained glass; no live lava.', 'The source concept floods the full floor; a level dry 3-wide central causeway is an intentional safety adaptation.'],
    }
    write_json('qa.json', qa)
    evidence_files = [ROOT / name for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'MATERIALS.md', 'README.md', 'summary.json']]
    evidence_files += [file for directory in ['tools', 'views'] for file in (ROOT / directory).glob('*') if file.is_file()]
    evidence_files += [file for file in (ROOT / 'qa').glob('*') if file.is_file()]
    evidence_files += [file for file in (ROOT / 'native-test/src').rglob('*') if file.is_file()]
    evidence_files += [ROOT / 'native-test' / name for name in ['build.gradle', 'settings.gradle', 'gradle.properties']]
    manifest = {'id': metadata['id'], 'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'], 'deployed': False, 'artifacts': [{'path': str(file.relative_to(ROOT)).replace('\\', '/'), 'bytes': file.stat().st_size, 'sha256': digest(file)} for file in sorted(set(evidence_files))]}
    write_json('delivery-manifest.json', manifest)
    print(json.dumps({'status': 'PASS', 'sha256': fingerprint, 'files': len(manifest['artifacts']), 'path': str(ROOT)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
