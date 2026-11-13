import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from PIL import Image, ImageDraw, ImageFont, ImageStat


ROOT = Path(__file__).resolve().parents[1]


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()


def write_json(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def contact_sheet(paths, destination, title, columns=2):
    tile_width, tile_height = 768, 512
    header_height, label_height = 70, 42
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new('RGB', (tile_width * columns, header_height + (tile_height + label_height) * rows), '#17242c')
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 23)
    draw.text((24, 20), title, fill='#e5ebed', font=font)
    for index, relative in enumerate(paths):
        source = Image.open(ROOT / relative).convert('RGB')
        source.thumbnail((tile_width, tile_height), Image.Resampling.LANCZOS)
        column, row = index % columns, index // columns
        left = column * tile_width
        top = header_height + row * (tile_height + label_height)
        sheet.paste(source, (left + (tile_width - source.width) // 2, top))
        draw.text((left + 18, top + tile_height + 8), Path(relative).stem, fill='#d8e6e8', font=font)
    sheet.save(ROOT / destination)


def main():
    metadata = read_json('room.json')
    static = read_json('qa/static.json')
    native = read_json('qa/forge-native.json')
    render = read_json('qa/render-manifest.json')
    fingerprint = digest(ROOT / 'room.nbt')
    assert static['status'] == native['status'] == 'PASS'
    assert metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == render['source_sha256'] == fingerprint
    assert len(native['stable_placements']) == len(native['immediate_placements']) == 4
    assert native['stabilization_ticks'] == 200 and native['explicit_scheduled_fluid_ticks'] == 3604
    assert len(render['views']) == 9 and not render['browser_errors']
    evidence = []
    for view in render['views']:
        file = ROOT / view['path']
        image = Image.open(file).convert('RGB')
        deviation = ImageStat.Stat(image).stddev
        assert image.size == (1536, 1024) and max(deviation) > 8
        assert digest(file) == view['sha256']
        evidence.append({'file': view['path'], 'sha256': view['sha256'], 'dimensions': list(image.size), 'rgb_stddev': deviation})
    for placement in native['stable_placements']:
        assert digest(ROOT / placement['capture']) == placement['capture_sha256']
        assert placement['source_fluid_cells'] == 901 and placement['waterlogged_rails'] == 15
        assert placement['leaks'] == placement['block_drift'] == placement['entities'] == placement['support_failures'] == 0
    assert digest(ROOT / 'qa/native-roundtrip.nbt') == native['native_roundtrip_sha256']
    exterior = [view['path'] for view in render['views'] if view['name'].startswith('exterior-')]
    interior = [view['path'] for view in render['views'] if view['name'].startswith('interior-')]
    contact_sheet(exterior, 'views/exterior-four-views.png', 'zone-M02 | Actual NBT | Exterior N / E / S / W')
    contact_sheet(interior, 'views/interior-four-views.png', 'zone-M02 | Actual NBT | Interior N / E / S / W (render cutaways)')
    contact_sheet(exterior + interior, 'views/eight-view-review.png', 'zone-M02 | Flooded Mine | Actual NBT review', columns=4)
    protections = []
    for item in read_json('qa/read-only-input-baseline.json'):
        current = digest(Path(item['file']))
        protections.append({**item, 'final_sha256': current, 'unchanged': current == item['sha256']})
    assert all(item['unchanged'] for item in protections)
    write_json('qa/protected-inputs-final.json', {'status': 'PASS', 'files': protections})
    material_lines = ['# zone-M02 淹水矿井 · 方块统计', '', '| 原版方块 | 数量 |', '| --- | ---: |']
    material_lines.extend(f'| {identity} | {count} |' for identity, count in sorted(static['counts'].items()) if identity != 'minecraft:air')
    (ROOT / 'MATERIALS.md').write_text('\n'.join(material_lines) + '\n', encoding='utf-8')
    review_path = ROOT / 'qa/visual-review.json'
    review = read_json('qa/visual-review.json') if review_path.exists() else {'status': 'PENDING'}
    assert review['status'] == 'PASS' and review['structure_sha256'] == fingerprint
    qa = {
        'id': 'zone-M02', 'name': '淹水矿井', 'status': 'PASS',
        'generated_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'],
        'static': {'status': 'PASS', 'tests': len(static['tests']), 'report': 'qa/static.json'},
        'native': {
            'status': 'PASS', 'runtime': native['runtime'], 'rotations': 4, 'ticks': 200,
            'records_compared_per_phase': 37500, 'total_immediate_and_stable_comparisons': 75000,
            'fluid_cells_each_rotation': 901, 'water_blocks': 886, 'waterlogged_rails': 15,
            'explicit_scheduled_fluid_ticks': 3604, 'water_leaks': 0, 'block_drift': 0, 'dropped_entities': 0,
            'negative_probes': native['negative_probes'], 'report': 'qa/forge-native.json', 'log': 'qa/forge-zone-m02-final-02.log'
        },
        'visual': {
            'status': 'PASS', 'evidence_type': render['evidence_type'], 'manifest': 'qa/render-manifest.json',
            'review': 'qa/visual-review.json', 'exterior_views': exterior, 'interior_views': interior,
            'additional_plan': 'views/plan.png', 'image_checks': evidence, 'cutaway_render_only': True,
            'room_nbt_roof_intact': True
        },
        'safety': {'entrance': metadata['entrance'], 'interior': metadata['interior'], 'full_3x3_north_interface': True, 'sealed_water': True, 'grounded_headframe': True, 'guarded_dry_path': True, 'full_double_roof': True, 'entities': 0, 'inventories': 0},
        'protection': {'report': 'qa/protected-inputs-final.json', 'corridor_m02_unchanged': True, 'source_batch_unchanged': True},
        'deployment': {'deployed': False, 'production_save_opened': False, 'formal_mods_modified': False, 'ft_core_modified': False},
        'limitations': native['limitations'] + metadata['adaptations']
    }
    write_json('qa.json', qa)
    files = [ROOT / filename for filename in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'MATERIALS.md', 'summary.json']]
    for directory in ['tools', 'views']:
        files.extend(file for file in (ROOT / directory).glob('*') if file.is_file())
    files.extend(file for file in (ROOT / 'qa').glob('*') if file.is_file())
    files.extend(file for file in (ROOT / 'qa/revisions').rglob('*') if file.is_file())
    files.extend(file for file in (ROOT / 'native-test/src').rglob('*') if file.is_file())
    files.extend(ROOT / 'native-test' / filename for filename in ['settings.gradle', 'build.gradle', 'gradle.properties'])
    records = [{'path': str(file.relative_to(ROOT)), 'bytes': file.stat().st_size, 'sha256': digest(file)} for file in sorted(set(files))]
    write_json('delivery-manifest.json', {'id': 'zone-M02', 'status': 'PASS', 'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'], 'files': records, 'excluded_generated_runtime': ['native-test/.gradle', 'native-test/build', 'qa/forge-runs'], 'note': 'Manifest does not hash itself; original revision remains historical, not current evidence.'})
    print(json.dumps({'status': 'PASS', 'id': 'zone-M02', 'sha256': fingerprint, 'evidence_files': len(records), 'protected_inputs_unchanged': True}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
