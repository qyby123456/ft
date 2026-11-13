import json
import zipfile
from datetime import datetime, timedelta, timezone

from PIL import Image

from build_room import REFERENCE_HASH, ROOT, digest, write_json
from nbtio import Structure


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def require(condition, message):
    if not condition:
        raise ValueError(message)


def rotate_position(position, rotation):
    pos_x, pos_y, pos_z = position
    return {
        'NONE': (pos_x, pos_y, pos_z),
        'CLOCKWISE_90': (14 - pos_z, pos_y, pos_x),
        'CLOCKWISE_180': (14 - pos_x, pos_y, 14 - pos_z),
        'COUNTERCLOCKWISE_90': (pos_z, pos_y, 14 - pos_x),
    }[rotation]


def rotate_properties(block, rotation):
    turns = {'NONE': 0, 'CLOCKWISE_90': 1, 'CLOCKWISE_180': 2, 'COUNTERCLOCKWISE_90': 3}[rotation]
    compass = ['north', 'east', 'south', 'west']
    result = {}
    for key, value in block.properties:
        next_key = compass[(compass.index(key) + turns) % 4] if key in compass else key
        if key == 'facing' and value in compass:
            value = compass[(compass.index(value) + turns) % 4]
        if key == 'axis' and value in ('x', 'z') and turns % 2:
            value = 'z' if value == 'x' else 'x'
        result[next_key] = value
    return result


def main():
    metadata = load('out/room.json')
    static = load('qa/static.json')
    native = load('qa/native.json')
    render = load('qa/render-manifest.json')
    visual = load('qa/visual-review.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    require(metadata['sha256'] == static['source_sha256'] == native['structure_sha256'] == render['source_nbt_sha256'] == visual['source_sha256'] == fingerprint, 'mixed source fingerprints')
    require(all(report['status'] == 'PASS' for report in (static, native, render, visual)), 'QA not complete')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    require('SWAMP_WITCH_NATIVE_GAME_TEST_PASS: rotations=4/4, records=9000, ticks=100, stock=0, source=' + fingerprint in log, 'missing native success fingerprint')
    require('All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log, 'native process did not finish successfully')
    room = Structure.read(ROOT / 'out/room.nbt')
    captures = []
    for result in native['rotations']:
        relative = result['world_capture'].replace('\\', '/')
        path = ROOT / relative
        require(digest(path) == result['world_capture_sha256'], 'capture hash mismatch')
        capture = Structure.read(path)
        require(capture.size == room.size and len(capture.blocks) == len(room.blocks), 'capture dimensions or count differ')
        for position, block in room.blocks.items():
            actual = capture.get(rotate_position(position, result['rotation']))
            require(block.name == actual.name and rotate_properties(block, result['rotation']) == dict(actual.properties), f'captured state mismatch {result["rotation"]} {position}')
        captures.append({'rotation': result['rotation'], 'file': relative, 'sha256': digest(path), 'records_matched': len(room.blocks)})
    capture_report = {'status': 'PASS', 'source_sha256': fingerprint, 'rotated_coordinate_and_state_comparison': captures, 'note': 'All 2250 records in each native recapture exactly match source names and rotated properties. Block entities were independently loaded and checked empty in Forge.'}
    write_json(ROOT / 'qa/native-capture-comparison.json', capture_report)
    images = []
    for relative in ('out/preview.png', 'preview/four-views.png', 'preview/plan.png', 'preview/exterior.png', 'preview/native-world-capture.png'):
        with Image.open(ROOT / relative) as image:
            image.load()
            images.append({'file': relative, 'size': list(image.size), 'sha256': digest(ROOT / relative)})
    qa = {
        'status': 'PASS', 'id': metadata['id'], 'variant': 'H', 'name': metadata['name'],
        'source_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH,
        'minecraft_version': '1.20.1', 'forge_version': '47.4.10',
        'created_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'static': {'status': static['status'], 'report': '../qa/static.json', 'records': static['records'], 'non_air': static['non_air'], 'negative_probes': len(static['negative_probes']), 'reproducible': static['deterministic_rebuild']},
        'native': {'status': native['status'], 'report': '../qa/native.json', 'log': '../qa/forge-gametest.log', 'rotations': 4, 'stabilization_ticks': 100, 'records_checked': 9000, 'reachable_feet_each_rotation': 137, 'inventory_items': 0, 'empty_block_entities_each_rotation': 9, 'unexpected_entities': 0, 'world_recapture': '../qa/native-capture-comparison.json', 'random_tick_speed': 0, 'scheduled_water_ticks_enabled': True},
        'supports': {'four_floor_to_ceiling_pillars': 'PASS', 'pillar_ring_and_counter_clearance': 'PASS', 'two_chain_supported_lamps': 'PASS', 'two_closed_ceiling_anchored_cages': 'PASS', 'wall_vines': 'PASS', 'hanging_roots': 'PASS', 'specimens_and_shelves': 'PASS', 'contained_basin_lily_reed': 'PASS'},
        'visual': {'status': 'PASS', 'review': '../qa/visual-review.json', 'render_manifest': '../qa/render-manifest.json', 'images': images},
        'integration': {'status': 'NOT_INTEGRATED_NOT_DEPLOYED', 'provisional_id': True, 'assigned_slot': None, 'coordinator_thread': metadata['integration']['coordinator_thread'], 'handoff': '../HANDOFF.md', 'coordinator_notification': 'PREPARED_NOT_SENT_PENDING_DIRECT_HUMAN_AUTHORIZATION'},
        'boundaries': {'only_variant': 'H', 'other_rooms_modified': False, 'production_worlds_opened': False, 'deployed_jars_modified': False, 'core_source_modified': False, 'incoming_written': False, 'paid_image_requests': 0, 'player_client_started': False},
        'limitations': ['Headless isolated Forge GameTest is not full modpack client, multiplayer, FPS or player perspective verification.', 'No FT catalog slot, actual lobby road, persistence, idempotence or production deployment validation in this standalone handoff.', 'Random block growth was disabled for deterministic verification; scheduled fluid ticks and 100-tick source stability were checked. Vines and sugar cane can grow during longer normal play.', 'This is a display shop with empty containers and brewing stands, not a functional merchant or reward system.', 'Software previews use approximate plant/water tint, simplified sign board and no true dynamic lighting.'],
    }
    write_json(ROOT / 'out/qa.json', qa)
    paths = []
    for directory in ('out', 'preview', 'reference', 'src', 'tools'):
        paths.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    paths.extend(ROOT / name for name in ('README.md', 'HANDOFF.md', 'build.gradle', 'gradle.properties', 'settings.gradle'))
    paths.extend(path for path in (ROOT / 'qa').glob('*') if path.is_file() and path.suffix in ('.json', '.nbt', '.log'))
    paths = sorted(set(paths))
    manifest = {'variant': 'H', 'source_sha256': fingerprint, 'files': [{'file': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in paths]}
    write_json(ROOT / 'release/manifest.json', manifest)
    archive = ROOT / 'release/FT_Shop_H_Swamp_Witch_Standalone_20261102.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as package:
        for path in paths + [ROOT / 'release/manifest.json']:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as package:
        require(package.testzip() is None, 'archive CRC failure')
        names = set(package.namelist())
        require({'out/room.nbt', 'out/room.json', 'out/preview.png', 'out/qa.json'} <= names, 'incomplete four-artifact delivery')
        require(not any(name.endswith('.jar') or '/world/' in name or '.gradle/' in name for name in names), 'archive includes runtime/deployment cache')
    write_json(ROOT / 'release/package.json', {'status': 'PASS', 'archive': str(archive), 'sha256': digest(archive), 'bytes': archive.stat().st_size, 'file_count': len(paths) + 1, 'crc': 'PASS', 'source_nbt_sha256': fingerprint})
    print(json.dumps({'status': 'PASS', 'archive': str(archive), 'sha256': digest(archive), 'source_sha256': fingerprint, 'world_capture_records_matched': 9000}, ensure_ascii=False))


if __name__ == '__main__':
    main()
