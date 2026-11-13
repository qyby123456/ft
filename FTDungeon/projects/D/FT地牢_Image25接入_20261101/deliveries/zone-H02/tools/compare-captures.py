import hashlib
import json
from pathlib import Path

from nbtio import State, plain, read_nbt

ROOT = Path(__file__).resolve().parents[1]
DIRECTIONS = ['north', 'east', 'south', 'west']


def decode(path):
    document = read_nbt(path)[1].value
    palette = [State.from_tag(entry) for entry in document['palette'].value]
    blocks = {tuple(plain(entry.value['pos'])): palette[entry.value['state'].value] for entry in document['blocks'].value}
    return tuple(plain(document['size'])), blocks


def rotate_state(block, turns):
    properties = dict(block.properties)
    if 'facing' in properties and properties['facing'] in DIRECTIONS:
        properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing']) + turns) % 4]
    if turns % 2 and properties.get('axis') in ['x', 'z']:
        properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
    return State(block.name, tuple(sorted(properties.items())))


def rotated_position(position, turns):
    horizontal, vertical, depth = position
    return [(horizontal, vertical, depth), (32 - depth, vertical, horizontal),
            (28 - horizontal, vertical, 32 - depth), (depth, vertical, 28 - horizontal)][turns]


def main():
    size, blocks = decode(ROOT / 'room.nbt')
    roundtrip_size, roundtrip_blocks = decode(ROOT / 'qa/native-roundtrip.nbt')
    assert (size, blocks) == (roundtrip_size, roundtrip_blocks), 'Native serialization roundtrip mismatch'
    comparisons = []
    for name, turns in [('none', 0), ('clockwise_90', 1), ('clockwise_180', 2), ('counterclockwise_90', 3)]:
        path = ROOT / f'qa/capture-{name}.nbt'
        captured_size, actual = decode(path)
        expected = {rotated_position(position, turns): rotate_state(block, turns) for position, block in blocks.items()}
        assert captured_size == ((33, 16, 29) if turns % 2 else size)
        differences = [(position, str(block), str(actual.get(position))) for position, block in expected.items() if actual.get(position) != block]
        assert actual == expected, (name, len(differences), differences[:3])
        comparisons.append({'rotation': name, 'records': len(actual), 'state_differences': 0,
                            'capture_sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    report = {'status': 'PASS', 'source_sha256': hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest(),
              'native_roundtrip': 'all coordinates and complete block states identical',
              'method': 'independent Python NBT decode; transform coordinates and all directional/axis properties; compare every air and solid record',
              'comparisons': comparisons}
    (ROOT / 'qa/capture-comparison.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'capture_comparison': 'PASS', 'rotations': 4, 'records': 61248, 'differences': 0}))


if __name__ == '__main__':
    main()
