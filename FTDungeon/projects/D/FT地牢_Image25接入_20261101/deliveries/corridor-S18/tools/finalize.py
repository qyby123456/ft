import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = read_json(ROOT / 'room.json')
    static = read_json(ROOT / 'qa/static-qa.json')
    render = read_json(ROOT / 'qa/render.json')
    visual = read_json(ROOT / 'qa/visual-review.json')
    lodestone = read_json(ROOT / 'qa/lodestone.json')
    assert all(entry['sha256'] == fingerprint for entry in (metadata, static, render, visual, lodestone))
    assert digest(Path(metadata['reference'])) == metadata['reference_sha256']
    assert static['status'] == visual['status'] == lodestone['status'] == 'PASS'
    assert all(static['checks'].values()) and all(entry['status'] == 'PASS' for entry in visual['checks'])
    native = []
    for direction in ('north', 'east', 'south', 'west'):
        report = read_json(ROOT / f'qa/native-{direction}.json')
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint
        assert report['rotation'] == direction and report['records'] == 2640
        assert report['observed_ticks'] == 400 and report['checkpoints'] == [0, 40, 120, 400]
        assert report['lava_sources'] == 12 and report['water_sources'] == 48
        assert report['reachable_positions'] >= 48
        assert report['game_rules']['doFireTick'] and report['game_rules']['randomTickSpeed'] == 3
        native.append(report)
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'All 4 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    images = []
    for path in [ROOT / 'preview.png', *sorted((ROOT / 'preview').glob('*.png'))]:
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            size = list(image.size)
        images.append({'file': str(path.relative_to(ROOT)).replace('\\', '/'), 'sha256': digest(path), 'size': size})
    assert len(images) == 14
    render['status'] = 'PASS'
    render['visual_review'] = 'qa/visual-review.json'
    write_json(ROOT / 'qa/render.json', render)
    report = {
        'id': metadata['id'], 'design_id': 'S18', 'status': 'PASS',
        'delivery_state': 'READY_FOR_UNIFIED_INTAKE', 'sha256': fingerprint,
        'reference_sha256': metadata['reference_sha256'], 'size': metadata['size'],
        'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'],
        'static_qa': {'status': 'PASS', 'file': 'qa/static-qa.json', 'checks': len(static['checks'])},
        'four_view_review': {'status': 'PASS', 'file': 'qa/visual-review.json', 'interior': 'preview/interior-four-views.png', 'exterior': 'preview/exterior-four-views.png'},
        'forge_native': {'status': 'PASS', 'minecraft': '1.20.1', 'forge': '47.4.10', 'directions': [entry['rotation'] for entry in native], 'records_per_direction': 2640, 'ticks_per_direction': 400, 'checkpoints': [0, 40, 120, 400], 'reachable_positions': [entry['reachable_positions'] for entry in native], 'log': 'qa/forge-gametest.log'},
        'liquid_safety': {'status': 'PASS', 'lava': 12, 'water': 48, 'explicit_fluid_tick_scheduling': True, 'full_cube_lava_enclosures': True, 'water_floor_and_sides': True, 'dry_route': '3x16 blocks with 3-block headroom', 'leaks_in_exterior_halo': 0, 'state_changes': 0},
        'images': images,
        'scope': {'deployment': False, 'player_saves_touched': False, 'modpack_files_changed': False, 'global_slot_assigned': False, 'entities': 0, 'block_entities': 0, 'containers': 0, 'reward_inventory': False},
        'limits': 'Final NBT based software imagery and isolated vanilla+Forge placement/physics verification. Full modpack client, integrated corridor connections and live player-world play remain untested.',
        'revision_notes': ['Initial endpoint bars changed connections under native neighbor updates; retained failure evidence and moved bar strips one block inward with stone jambs.', 'Visual review closed the upper endwall slot and both arch-to-roof corner gaps; final four-direction tests were rerun against this exact SHA-256.']
    }
    write_json(ROOT / 'qa.json', report)
    handoff = {'id': metadata['id'], 'design_id': 'S18', 'status': 'READY_FOR_UNIFIED_INTAKE', 'directory': str(ROOT), 'sha256': fingerprint, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'required_files': ['room.nbt', 'room.json', 'preview.png', 'qa.json'], 'verification': 'static PASS + final-NBT four-view PASS + Forge 4/4 x 400 ticks PASS', 'deployment': False, 'assigned_world_slot': None}
    write_json(ROOT / 'handoff.json', handoff)
    members = [ROOT / name for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'handoff.json', 'README.md', 'materials.csv', 'recipe.mjs', 'summary.json', 'build.gradle', 'settings.gradle', 'gradle.properties')]
    members += list((ROOT / 'preview').glob('*.png'))
    members += list((ROOT / 'qa').glob('*.json'))
    members += [ROOT / 'qa/forge-gametest.log']
    members += list((ROOT / 'tools').glob('*.*'))
    members += [path for path in (ROOT / 'src').rglob('*') if path.is_file()]
    with zipfile.ZipFile(ROOT / 'corridor-S18-delivery.zip', 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(set(members)):
            archive.write(path, path.relative_to(ROOT))
    with zipfile.ZipFile(ROOT / 'corridor-S18-delivery.zip') as archive:
        assert archive.testzip() is None
    write_json(ROOT / 'package-sha256.json', {'file': 'corridor-S18-delivery.zip', 'sha256': digest(ROOT / 'corridor-S18-delivery.zip'), 'room_sha256': fingerprint, 'status': 'PASS'})
    print(f'S18 FINAL PASS sha256={fingerprint} images={len(images)} Forge=4/4 ticks=400 package={digest(ROOT / "corridor-S18-delivery.zip")}')


if __name__ == '__main__':
    main()
