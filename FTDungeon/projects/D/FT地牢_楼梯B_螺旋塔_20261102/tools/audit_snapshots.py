import json

from build_room import ROOT, digest, write_json
from nbtio import State, Structure, plain


DIRECTIONS = ['north', 'east', 'south', 'west']


def rotate_state(block, turns):
    properties = {}
    for key, value in block.properties:
        transformed = DIRECTIONS[(DIRECTIONS.index(key) + turns) % 4] if key in DIRECTIONS else key
        properties[transformed] = DIRECTIONS[(DIRECTIONS.index(value) + turns) % 4] if key == 'facing' and value in DIRECTIONS else value
    return State(block.name, tuple(sorted(properties.items())))


def main():
    source = Structure.read(ROOT / 'out/room.nbt')
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS' and native['structure_sha256'] == digest(ROOT / 'out/room.nbt')
    results = []
    for turns, suffix in enumerate(['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']):
        path = ROOT / ('qa/native-placed-' + suffix + '.nbt')
        captured = Structure.read(path)
        assert captured.size == source.size and len(captured.blocks) == len(source.blocks) == 19683
        assert not captured.entities and len(captured.block_entities) == 2
        for (pos_x, pos_y, pos_z), block in source.blocks.items():
            transformed = [(pos_x, pos_y, pos_z), (26 - pos_z, pos_y, pos_x), (26 - pos_x, pos_y, 26 - pos_z), (pos_z, pos_y, 26 - pos_x)][turns]
            assert captured.get(transformed) == rotate_state(block, turns), (suffix, transformed, block, captured.get(transformed))
        for payload in captured.block_entities.values():
            data = plain(payload)
            assert data['id'] == 'minecraft:barrel' and data.get('Items', []) == [] and 'LootTable' not in data
        results.append({'rotation': suffix, 'status': 'PASS', 'records': 19683, 'sha256': digest(path), 'bytes': path.stat().st_size})
    report = {'status': 'PASS', 'structure_sha256': digest(ROOT / 'out/room.nbt'), 'method': 'Independent final NBT vs native post-200-tick snapshot state comparison; rotate coordinates, facing and directional iron-bar properties.', 'records_checked': 78732, 'snapshots': results}
    write_json(ROOT / 'qa/captures.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
