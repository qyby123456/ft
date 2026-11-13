import hashlib
import json
from pathlib import Path

from nbtio import State, Structure, plain


ROOT = Path(__file__).resolve().parents[1]
FACING = ['north', 'east', 'south', 'west']


def transform(position, rotation):
    pos_x, pos_y, pos_z = position
    return ((pos_x, pos_y, pos_z), (32 - pos_z, pos_y, pos_x), (28 - pos_x, pos_y, 32 - pos_z), (pos_z, pos_y, 28 - pos_x))[rotation]


def rotate(block, rotation):
    properties = dict(block.properties)
    if 'facing' in properties:
        properties['facing'] = FACING[(FACING.index(properties['facing']) + rotation) % 4]
    return State(block.name, tuple(sorted(properties.items())))


def main():
    source = Structure.read(ROOT / 'out/room.nbt')
    report = []
    for rotation, name in enumerate(('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90')):
        path = ROOT / f'qa/native-placed-{name}.nbt'
        captured = Structure.read(path)
        expected_size = (29, 23, 33) if rotation % 2 == 0 else (33, 23, 29)
        assert captured.size == expected_size and len(captured.blocks) == len(source.blocks)
        expected = {transform(position, rotation): rotate(block, rotation) for position, block in source.blocks.items()}
        assert expected == captured.blocks, name + ' differs from rotated source'
        assert not captured.entities and len(captured.block_entities) == len(source.block_entities) == 1
        for position, payload in source.block_entities.items():
            native = plain(captured.block_entities[transform(position, rotation)])
            assert plain(payload)['id'] == native['id']
            assert not any(key in native for key in ('Items', 'LootTable', 'Item'))
        report.append({'rotation': name, 'status': 'PASS', 'size': list(captured.size), 'records': len(captured.blocks),
                       'states_equal_rotated_source': True, 'capture_sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    result = {'status': 'PASS', 'structure_sha256': hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest(),
              'scope': 'Independent Python reparse and full state comparison of Forge 200-tick world captures', 'rotations': report}
    (ROOT / 'qa/capture-verification.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'capture_comparison': 'PASS', 'records': sum(entry['records'] for entry in report)}))


if __name__ == '__main__':
    main()
