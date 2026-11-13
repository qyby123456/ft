import argparse
import json
import math
from pathlib import Path
import audit

def records(path):
    template = audit.decode(path)
    assert template['DataVersion'] == 3465
    size = template['size']
    assert len(size) == 3 and all(isinstance(length, int) and length > 0 for length in size)
    assert not template.get('entities'), 'Entity relocation requires a separate verifier'
    result = {}
    for block in template['blocks']:
        position = tuple(block['pos'])
        assert len(position) == 3 and all(0 <= value < size[axis] for axis, value in enumerate(position))
        assert position not in result
        state = template['palette'][block['state']]
        assert state['Name'].startswith('minecraft:')
        result[position] = {'state': state, 'nbt': block.get('nbt')}
    assert len(result) == math.prod(size), 'Every cell including air must be explicit'
    return size, result

def verify(candidate, assembly_path):
    assembly = json.loads(assembly_path.read_text(encoding='utf-8'))
    size, complete = records(candidate)
    expected = audit.digest(candidate)
    assert assembly['source_sha256'] == expected and assembly['size'] == size
    parts = assembly['parts']
    assert len(parts) == assembly['part_count'] and len(parts) > 1
    assert [part['step'] for part in parts] == list(range(1, len(parts) + 1))
    combined = {}
    findings = []
    for part in parts:
        path = Path(part['path'])
        if not path.is_absolute():
            path = assembly_path.parent / path
        assert audit.digest(path) == part['sha256']
        part_size, local = records(path)
        assert part_size == part['size'] and max(part_size) <= 48
        origin = part['relative_origin']
        assert len(origin) == 3 and all(isinstance(value, int) and value >= 0 for value in origin)
        air = 0
        for position, record in local.items():
            relocated = tuple(value + origin[axis] for axis, value in enumerate(position))
            assert relocated not in combined, ('overlap', relocated)
            assert relocated in complete, ('out of bounds', relocated)
            assert record == complete[relocated], ('state or block entity mismatch', relocated)
            combined[relocated] = record
            air += record['state']['Name'] == 'minecraft:air'
        if 'blocks' in part:
            assert part['blocks'] == len(local)
        if 'air_blocks' in part:
            assert part['air_blocks'] == air
        findings.append({'path': str(path), 'size': part_size, 'origin': origin, 'records': len(local), 'explicit_air': air})
    assert combined == complete
    return {'status': 'PASS_EXACT_DENSE_REASSEMBLY', 'source_sha256': expected, 'size': size, 'records': len(complete), 'block_entities': sum(record['nbt'] is not None for record in complete.values()), 'parts': findings, 'scope': 'Independent decoded NBT value equality for states and entity payloads at every source position. NBT numeric tag-width identity is not checked. Rotation zero only; not native template placement, world updates, conflict handling or deployment certification.'}

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('candidate', type=Path)
    parser.add_argument('assembly', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    result = verify(arguments.candidate, arguments.assembly)
    arguments.output.write_text(json.dumps(result, indent=2), encoding='utf-8')
    print(json.dumps(result, indent=2))
