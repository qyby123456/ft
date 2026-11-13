import hashlib
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))
from nbtio import Structure, plain


def normalized(block, turns=0):
    properties = dict(block.properties)
    directions = ('north', 'east', 'south', 'west')
    if 'facing' in properties and properties['facing'] in directions:
        properties['facing'] = directions[(directions.index(properties['facing']) + turns) % 4]
    return block.name, properties


def transformed(position, rotation):
    across, elevation, depth = position
    if rotation == 'NONE':
        return across, elevation, depth
    if rotation == 'CLOCKWISE_90':
        return 40 - depth, elevation, across
    if rotation == 'CLOCKWISE_180':
        return 12 - across, elevation, 40 - depth
    if rotation == 'COUNTERCLOCKWISE_90':
        return depth, elevation, 12 - across
    raise ValueError(rotation)


def main():
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    source = Structure.read(ROOT / 'room.nbt')
    source_hash = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    assert native['status'] == 'PASS' and native['structure_sha256'] == source_hash
    results = []
    turns = {'NONE': 0, 'CLOCKWISE_90': 1, 'CLOCKWISE_180': 2, 'COUNTERCLOCKWISE_90': 3}
    for entry in native['rotations']:
        relative = entry['capture'].replace(chr(92), '/')
        capture_path = ROOT / relative
        capture_hash = hashlib.sha256(capture_path.read_bytes()).hexdigest()
        assert capture_hash == entry['capture_sha256']
        capture = Structure.read(capture_path)
        assert capture.size == tuple(entry['capture_size']) and len(capture.blocks) == len(source.blocks)
        differences = []
        for position, block in source.blocks.items():
            target = transformed(position, entry['rotation'])
            if normalized(capture.blocks[target]) != normalized(block, turns[entry['rotation']]):
                differences.append({'source': position, 'target': target, 'source_state': normalized(block, turns[entry['rotation']]), 'actual': normalized(capture.blocks[target])})
            if position in source.block_entities:
                expected_patterns = plain(source.block_entities[position])['Patterns']
                assert plain(capture.block_entities[target])['Patterns'] == expected_patterns
        assert not differences, json.dumps(differences[:5])
        results.append({'rotation': entry['rotation'], 'capture': relative, 'sha256': capture_hash, 'size': capture.size, 'checked_records': len(source.blocks), 'mismatches': len(differences), 'banner_patterns_preserved': len(source.block_entities)})
    report = {'status': 'PASS', 'structure_sha256': source_hash, 'rotations': results, 'records_compared': sum(entry['checked_records'] for entry in results), 'source_and_world_semantic_match': True}
    (ROOT / 'qa/capture-review.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
