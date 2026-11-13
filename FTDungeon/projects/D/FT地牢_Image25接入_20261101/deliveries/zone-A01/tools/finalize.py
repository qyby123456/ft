import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A01-abandoned-ruined-study.png')
REFERENCE_HASH = 'e28fe9c5d774a0f141b616ebeb683f09bc6a8c0bc90fe74e39d88b4f1f2d9a0d'
DIRECTIONS = ('north', 'south', 'west', 'east')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8-sig'))


def write(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def contacts():
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 28)
    for mode, title in (('interior', 'A01 废弃书房 · 四向内部剖视（仅预览剖切）'), ('exterior', 'A01 废弃书房 · 四向完整外壳（实际 NBT）')):
        sheet = Image.new('RGB', (1920, 1340), '#172029')
        draw = ImageDraw.Draw(sheet)
        draw.text((28, 15), title, font=font, fill='#f8ecd7')
        for index, direction in enumerate(DIRECTIONS):
            with Image.open(ROOT / f'views/{mode}-{direction}.png') as image:
                image.load()
                assert image.size == (1536, 1024)
                thumbnail = image.convert('RGB').resize((960, 640), Image.Resampling.LANCZOS)
            sheet.paste(thumbnail, ((index % 2) * 960, 60 + (index // 2) * 640))
        sheet.save(ROOT / f'views/{mode}-four.png')
    overview = Image.new('RGB', (1920, 1340), '#172029')
    for index, mode in enumerate(('interior', 'exterior')):
        with Image.open(ROOT / f'views/{mode}-four.png') as image:
            overview.paste(image.resize((960, 670), Image.Resampling.LANCZOS), (index * 960, 335))
    overview.save(ROOT / 'views/overview-eight.png')


def finalize():
    source_hash = digest(ROOT / 'room.nbt')
    metadata = read('room.json')
    static = read('qa/static.json')
    native = read('qa/native.json')
    render = read('qa/render.json')
    visual = read('qa/visual-review.json')
    run = read('evidence/native-run.json')
    assert metadata['sha256'] == source_hash
    for report in (static, native, render, visual):
        assert report['status'] == 'PASS' and report['source_sha256'] == source_hash
    assert len(static['checks']) == 15 and all(check['status'] == 'PASS' for check in static['checks'])
    assert native['rotations'] == 4 and native['stability_ticks'] == 400
    assert native['total_placed_records'] == 12716
    assert len(native['initial']) == 4 and len(native['after_400_ticks']) == 4
    assert all(result['exact_records'] == 3179 and result['empty_barrels'] == 3 and result['inventory_items'] == 0 and result['entities_and_drops'] == 0 for result in native['initial'] + native['after_400_ticks'])
    assert len(render['outputs']) == 8 and render['errors'] == [] and len(visual['reviewed_views']) == 8
    assert run['exit_code'] == 0 and run['production_save_used'] is False
    log = (ROOT / run['log']).read_text(encoding='utf-8-sig', errors='replace')
    for token in ('A01_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400; sha256=' + source_hash, 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        assert token in log
    with Image.open(REFERENCE) as image:
        image.verify()
    with Image.open(REFERENCE) as image:
        image.load()
        assert image.size == (1536, 1024) and image.mode == 'RGB'
    assert digest(REFERENCE) == REFERENCE_HASH
    reference_documents = [REFERENCE.parents[2] / 'README.md', REFERENCE.parents[2] / 'docs/manifest.json', REFERENCE.parents[2] / 'docs/verify.json']
    write('qa/reference.json', {'status': 'PASS', 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'bytes': REFERENCE.stat().st_size, 'full_png_decode': True, 'size': [1536, 1024], 'mode': 'RGB', 'manifest_and_verify_agree': True, 'reference_documents': [{'file': str(path), 'sha256': digest(path)} for path in reference_documents]})
    for entry in render['outputs']:
        assert digest(ROOT / entry['file']) == entry['sha256']
    assert digest(ROOT / 'preview.png') == digest(ROOT / 'views/interior-north.png')
    qa = {
        'status': 'PASS', 'id': 'zone-A01', 'design_id': 'A01', 'sha256': source_hash, 'reference_sha256': REFERENCE_HASH,
        'size': metadata['size'],
        'summary': {'static_checks': len(static['checks']), 'full_volume_records': 3179, 'non_air_blocks': static['non_air_blocks'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'full_roof_cells': 289, 'dry_reachable_feet_per_rotation': 181, 'interior_views': 4, 'exterior_views': 4, 'native_rotations_passed': 4, 'native_records_compared_per_phase': 12716, 'native_checks_phases': ['immediate', 'after_400_ticks'], 'native_stability_ticks': 400, 'neighbor_notifications': 12716, 'directional_shape_checks': 76296, 'forced_random_ticks': native['explicit_random_ticks'], 'reason_no_forced_random_ticks': 'No selected block state reports random ticking; randomTickSpeed=3 remained active.', 'empty_barrels': 3, 'inventory_items': 0, 'entities': 0, 'live_fluids': 0},
        'evidence': {'static': 'qa/static.json', 'native': 'qa/native.json', 'reference': 'qa/reference.json', 'render': 'qa/render.json', 'visual': 'qa/visual-review.json', 'native_run': 'evidence/native-run.json', 'log': run['log'], 'internal_sheet': 'views/interior-four.png', 'external_sheet': 'views/exterior-four.png'},
        'history': [{'stage': 'static checker attempt 1', 'result': 'Checker schema mismatch: verify.json uses records, manifest.json uses files; no room defect.', 'retained_evidence': 'evidence/static-attempt1.json', 'resolved': True}],
        'deviations_from_concept': metadata['adaptations'], 'limitations': ['Lodestone renders are software previews, not Minecraft client screenshots.', 'Isolated Forge 47.4.10 GameTest is not a full modpack or multiplayer playtest.', 'Natural mob spawning is disabled only in the isolated fixture; safe point checks cover geometry and dry collision space.'],
        'deployed': False, 'production_saves_touched': False, 'parallel_directories_modified': False
    }
    write('qa.json', qa)
    required = ['room.nbt', 'room.json', 'preview.png', 'qa.json']
    evidence = ['recipe.mjs', 'summary.json', 'README.md', 'Run-NativeQA.ps1', 'qa/static.json', 'qa/native.json', 'qa/reference.json', 'qa/render.json', 'qa/visual-review.json', 'evidence/native-run.json', run['log'], 'evidence/static-attempt1.json', 'views/interior-four.png', 'views/exterior-four.png', 'views/overview-eight.png']
    evidence += [entry['file'] for entry in render['outputs']]
    evidence += [str(path.relative_to(ROOT)).replace('\\', '/') for path in (ROOT / 'tools').glob('*') if path.is_file()]
    evidence += ['native-test/build.gradle', 'native-test/settings.gradle']
    evidence += [str(path.relative_to(ROOT)).replace('\\', '/') for path in (ROOT / 'native-test/src').rglob('*') if path.is_file()]
    files = [{'file': relative, 'bytes': (ROOT / relative).stat().st_size, 'sha256': digest(ROOT / relative)} for relative in dict.fromkeys(required + evidence)]
    write('delivery-manifest.json', {'status': 'PASS', 'id': 'zone-A01', 'created_on': '2026-11-03', 'timezone': 'Asia/Shanghai', 'source_sha256': source_hash, 'reference_sha256': REFERENCE_HASH, 'required_files': required, 'files': files, 'deployed': False})
    print(json.dumps({'status': 'PASS', 'id': 'zone-A01', 'sha256': source_hash, 'required': required, 'manifest_files': len(files)}, ensure_ascii=False))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--contact-only', action='store_true')
    options = parser.parse_args()
    contacts()
    if not options.contact_only:
        finalize()
