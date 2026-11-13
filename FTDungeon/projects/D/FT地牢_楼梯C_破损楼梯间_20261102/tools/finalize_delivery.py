import json
from datetime import datetime, timedelta, timezone

import build_room
from build_room import ROOT, fingerprint, save_json
from nbtio import State, Structure, plain


ROTATIONS = {'none': 0, 'clockwise_90': 1, 'clockwise_180': 2, 'counterclockwise_90': 3}
DIRECTIONS = ['north', 'east', 'south', 'west']


def rotate_state(block, turns):
    properties = dict(block.properties)
    rotated = {}
    for key, value in properties.items():
        target = DIRECTIONS[(DIRECTIONS.index(key) + turns) % 4] if key in DIRECTIONS else key
        if key == 'facing' and value in DIRECTIONS:
            value = DIRECTIONS[(DIRECTIONS.index(value) + turns) % 4]
        if key == 'axis' and value in ('x', 'z') and turns % 2:
            value = 'z' if value == 'x' else 'x'
        rotated[target] = value
    return State(block.name, tuple(sorted(rotated.items())))


def capture_position(position, turns):
    pos_x, pos_y, pos_z = position
    return ((pos_x, pos_y, pos_z), (24 - pos_z, pos_y, pos_x), (22 - pos_x, pos_y, 24 - pos_z), (pos_z, pos_y, 22 - pos_x))[turns]


def run():
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    rendered = json.loads((ROOT / 'qa/render.json').read_text(encoding='utf-8'))
    source_hash = fingerprint(ROOT / 'out/room.nbt')
    assert native['status'] == static['status'] == visual['status'] == 'PASS'
    assert native['room_sha256'] == static['room_sha256'] == visual['room_sha256'] == rendered['source_nbt_sha256'] == source_hash
    assert native['records_checked_after_ticks'] == 41400 and native['negative_probes'] == 24
    assert len(native['rotations']) == 4
    for name, digest in rendered['files'].items():
        assert fingerprint(ROOT / name) == digest
    for name, digest in visual['reviewed_files'].items():
        assert fingerprint(ROOT / name) == digest
    source = Structure.read(ROOT / 'out/room.nbt')
    captures = []
    for name, turns in ROTATIONS.items():
        path = ROOT / f'qa/native-placed-{name}.nbt'
        capture = Structure.read(path)
        expected_size = (25, 18, 23) if turns % 2 else (23, 18, 25)
        assert capture.size == expected_size and len(capture.blocks) == 10350 and not capture.entities
        for position, block in source.blocks.items():
            target = capture_position(position, turns)
            assert capture.get(target) == rotate_state(block, turns), (name, position, capture.get(target), rotate_state(block, turns))
        assert len(capture.block_entities) == 4
        for payload in capture.block_entities.values():
            data = plain(payload)
            assert data.get('Items', []) == [] and 'LootTable' not in data
        captures.append({'rotation': name, 'records_identical_after_rotation': 10350, 'empty_containers': 4, 'sha256': fingerprint(path)})
    rebuilt_names = ['room.nbt', 'room.json', 'blueprint.json', 'materials.json']
    before = {name: fingerprint(ROOT / 'out' / name) for name in rebuilt_names}
    build_room.build()
    after = {name: fingerprint(ROOT / 'out' / name) for name in rebuilt_names}
    assert before == after
    reproducibility = {'status': 'PASS', 'room_sha256': source_hash, 'byte_identical_rebuild': before, 'native_roundtrip_comparison': captures, 'native_blocks_compared': 41400}
    save_json(ROOT / 'qa/reproducibility.json', reproducibility)
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert all(marker in log for marker in ('STAIR_C_NATIVE_GAME_TEST_PASS: rotations=4/4', 'All 1 required tests passed', 'BUILD SUCCESSFUL'))
    report = {'status': 'PASS', 'id': metadata['id'], 'room_sha256': source_hash, 'reference_sha256': metadata['reference_sha256'], 'size': metadata['size'],
              'verified_at': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'static': {'status': 'PASS', 'checks': static['total'], 'report': 'qa/static.json'},
              'native': {'status': 'PASS', 'rotations': 4, 'stabilization_ticks': 100, 'records_after_ticks': 41400, 'negative_probes': 24, 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log'},
              'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json'}, 'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'software_render_only': True},
              'entrance': metadata['entrance'], 'interfaces': metadata['interfaces'], 'deployed': False, 'ft_core_modified': False, 'player_saves_opened': False, 'assigned_slot': None,
              'limitations': ['Independent candidate only; no core catalog, fixed slot, connecting roads or native FT lifecycle tests.', 'Local main entrance Y=6, upper exit Y=12, lower exit Y=1: receiving installer must respect these offsets.', 'No full-modpack client screenshot, live player input, multiplayer or FPS acceptance.', 'Source upper break is translated into two missing columns plus a preserved three-wide side lane; blue breach is sealed glass.', 'Sky fixtures use shape-safe native placement and then normal neighbor updates; earlier ground fixtures saw unrelated mineshaft/terrain drops, archived locally rather than presented as passing evidence.']}
    save_json(ROOT / 'out/qa.json', report)
    manifest_names = ['out/room.nbt', 'out/room.json', 'out/preview.png', 'out/qa.json', 'out/blueprint.json', 'out/materials.json', 'qa/static.json', 'qa/native.json', 'qa/reproducibility.json', 'qa/render.json', 'qa/visual-review.json', 'qa/forge-gametest.log']
    manifest = {'id': metadata['id'], 'files': {name: {'bytes': (ROOT / name).stat().st_size, 'sha256': fingerprint(ROOT / name)} for name in manifest_names}}
    save_json(ROOT / 'out/manifest.json', manifest)
    print(json.dumps({'status': 'PASS', 'room_sha256': source_hash, 'static_checks': static['total'], 'native_rotations': 4, 'compared_capture_records': 41400}, ensure_ascii=False))


if __name__ == '__main__':
    run()
