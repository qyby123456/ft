import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from build_r2 import ROOT, digest, write_json
from nbtio import Structure, plain, state


def rotate(position, rotation):
    block_x, block_y, block_z = position
    return {'none': (block_x, block_y, block_z), 'clockwise_90': (14 - block_z, block_y, block_x),
            'clockwise_180': (14 - block_x, block_y, 14 - block_z),
            'counterclockwise_90': (block_z, block_y, 14 - block_x)}[rotation]


def runtime_state(block):
    properties = dict(block.properties)
    if block.name == 'minecraft:hanging_roots':
        properties.setdefault('waterlogged', 'false')
    return state(block.name, **properties)


def rotate_state(block, rotation):
    turns = {'none': 0, 'clockwise_90': 1, 'clockwise_180': 2, 'counterclockwise_90': 3}[rotation]
    cardinal = ['north', 'east', 'south', 'west']
    properties = {}
    for key, value in runtime_state(block).properties:
        rotated_key = cardinal[(cardinal.index(key) + turns) % 4] if key in cardinal else key
        if key == 'facing' and value in cardinal:
            value = cardinal[(cardinal.index(value) + turns) % 4]
        if key == 'axis' and value in ('x', 'z') and turns % 2:
            value = 'z' if value == 'x' else 'x'
        properties[rotated_key] = value
    return state(block.name, **properties)


def main():
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    room = Structure.read(ROOT / 'out/room.nbt')
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render.json').read_text(encoding='utf-8'))
    for report in [metadata, static, native, render]:
        assert report['sha256'] == digest(ROOT / 'out/room.nbt')
    assert static['status'] == native['status'] == render['status'] == 'PASS'
    assert len(native['rotations']) == 4 and native['ticks_observed'] == 240
    log = (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8')
    assert 'R2_FORGE_PLACEMENT_PASS' in log and 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    roundtrip = Structure.read(ROOT / 'qa/native-roundtrip.nbt')
    runtime_blocks = {position: runtime_state(block) for position, block in room.blocks.items()}
    assert roundtrip.blocks == runtime_blocks and roundtrip.size == room.size
    assert {position: plain(tag) for position, tag in roundtrip.block_entities.items()} == {position: plain(tag) for position, tag in room.block_entities.items()}
    comparisons = []
    for rotation in ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']:
        capture_path = ROOT / ('qa/native-placed-' + rotation + '.nbt')
        capture = Structure.read(capture_path)
        expected = {rotate(position, rotation): rotate_state(block, rotation) for position, block in room.blocks.items()}
        assert capture.size == room.size and capture.blocks == expected
        assert not capture.entities and len(capture.block_entities) == 7
        for tag in capture.block_entities.values():
            entity = plain(tag)
            assert not entity.get('Items') and 'LootTable' not in entity
        comparisons.append({'rotation': rotation, 'status': 'PASS', 'compared_states': len(expected), 'native_block_entities': len(capture.block_entities), 'sha256': digest(capture_path)})
    write_json(ROOT / 'qa/native-capture-comparison.json', {'status': 'PASS', 'sha256': metadata['sha256'], 'roundtrip_runtime_states_exact': True, 'roundtrip_block_entity_nbt_values_exact': True, 'default_property_resolution': {'minecraft:hanging_roots': {'waterlogged': 'false', 'positions': 4, 'reason': 'Java 1.20.1 native decoder fills omitted default properties; template and runtime state are semantically identical.'}}, 'captures': comparisons})
    summary = {
        'id': 'R2', 'name': metadata['name'], 'status': 'PASS', 'sha256': metadata['sha256'],
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'reference_sha256': metadata['reference_sha256'], 'static_checks': static['checks_passed'],
        'static_reachable_feet': static['reachable_dry_feet'], 'native_rotations': native['rotations'],
        'ticks_observed': native['ticks_observed'], 'forced_neighbor_and_shape_updates': True,
        'full_floor_and_roof': True, 'north_3x3_interface': True, 'central_3x3_aisle': True,
        'native_capture_comparison': 'PASS: four rotations, all 8100 world states, 28 empty block entities',
        'inventory_items': 0, 'loot_tables': 0, 'dropped_items': 0, 'fire_spread': False, 'free_fluids': 0,
        'client_visual_tested': False, 'deployed': False,
        'evidence': ['qa/static.json', 'qa/forge-native.json', 'qa/forge-run.log', 'qa/native-capture-comparison.json', 'qa/render.json', 'qa/visual-review.json', 'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png', 'preview/native-world.png'],
        'limitations': native['limitations'] + render['limitations'],
        'integration_required': ['Register unique R2 template and persistent generation marker.', 'Create a walkable connector from the FT hall to the north interface.', 'Reject occupied/liquid/entity-bearing destinations across the entire volume, including recorded air.', 'Do not regenerate existing rooms, clear player worlds, or hot-replace a running production JAR.'],
    }
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    assert visual['status'] == 'PASS' and visual['sha256'] == metadata['sha256']
    write_json(ROOT / 'out/qa.json', summary)
    artifact_paths = ['out/room.nbt', 'out/room.json', 'out/preview.png', 'out/qa.json', 'qa/static.json', 'qa/forge-native.json', 'qa/forge-run.log', 'qa/native-capture-comparison.json', 'qa/render.json', 'qa/visual-review.json', 'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png', 'preview/native-world.png']
    artifacts = [{'path': path, 'bytes': (ROOT / path).stat().st_size, 'sha256': digest(ROOT / path)} for path in artifact_paths]
    handoff = {'id': 'R2', 'name': metadata['name'], 'status': 'READY_FOR_INTEGRATION_NOT_DEPLOYED', 'created_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'root': str(ROOT), 'delivery_directory': str(ROOT / 'out'), 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'room_nbt_sha256': metadata['sha256'], 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'artifacts': artifacts, 'project_artifact_writes_confined_to': str(ROOT), 'tool_cache_note': 'Gradle updates its ordinary user cache and daemon logs; no other room or production project files are edited.', 'formal_saves_modified': False, 'ft_core_modified': False, 'other_room_projects_modified': False, 'image_api_called': False, 'read_only_source_reuse': ['S6/tools/nbtio.py copied into this project', 'H/tools/render_engine.py copied and adapted only locally', 'S6 Gradle 8.8 wrapper copied'], 'integration_required': summary['integration_required']}
    write_json(ROOT / 'handoff.json', handoff)
    print(json.dumps({'status': 'READY_FOR_INTEGRATION_NOT_DEPLOYED', 'sha256': metadata['sha256'], 'static_checks': static['checks_passed'], 'compared_states': 8100, 'artifacts': len(artifacts)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
