import argparse
from collections import Counter
import gzip
import hashlib
import json
from pathlib import Path
import sys
import zlib

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
import verify_saved_world as base


class NativeWorld(base.World):
    def __init__(self, reader, world):
        super().__init__(reader, world)
        self.native = {}

    def chunk(self, coordinate_x, coordinate_z):
        key = (coordinate_x, coordinate_z)
        if key in self.chunks:
            return self.chunks[key]
        path = self.world / 'region' / f'r.{coordinate_x // 32}.{coordinate_z // 32}.mca'
        data = path.read_bytes()
        self.files[str(path)] = hashlib.sha256(data).hexdigest()
        header = 4 * (coordinate_x % 32 + (coordinate_z % 32) * 32)
        sector = int.from_bytes(data[header:header + 3], 'big')
        if sector == 0:
            raise integrate.GateError('Saved BE chunk absent')
        start = sector * 4096
        length = int.from_bytes(data[start:start + 4], 'big')
        payload = data[start + 5:start + 4 + length]
        mode = data[start + 4]
        payload = zlib.decompress(payload) if mode == 2 else gzip.decompress(payload) if mode == 1 else payload
        root = base.decode(self.reader, payload)
        self.native[key] = {(entry['x'], entry['y'], entry['z']): entry for entry in root.get('block_entities', [])}
        self.chunks[key] = {section['Y']: section for section in root['sections']}
        return self.chunks[key]

    def entity(self, position):
        key = (position[0] // 16, position[2] // 16)
        self.chunk(*key)
        return self.native[key].get(position)


def verify(snapshot, world, mode):
    reader = base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    native = NativeWorld(reader, world)
    journal_path = next((world / 'ft_biome_dungeon_generator_v2/minecraft/overworld').glob('*.nbt'), None)
    if journal_path is None:
        candidates = list(world.rglob('ftd-*.nbt'))
        if len(candidates) != 1:
            raise integrate.GateError('Expected precisely one BE restart fixture journal')
        journal_path = candidates[0]
    framed = journal_path.read_bytes()
    if framed[:4] != b'FTD2' or hashlib.sha256(framed[72:]).hexdigest().encode() != framed[8:72]:
        raise integrate.GateError('BE restart journal hash/frame invalid')
    record = base.decode(reader, gzip.decompress(framed[72:]))
    plan = json.loads(record['plan'])
    if (mode == 'complete' and record['phase'] != 'COMMITTED') or (mode == 'partial' and record['phase'] != 'BUILDING'):
        raise integrate.GateError('Unexpected persisted BE restart phase')
    found = Counter()
    absent = 0
    field_count = 0
    for room in plan['rooms']:
        path = snapshot / 'runtime/src/gametest/resources/data/ftbd_payloadtest/structures' / (room['templateId'] + '.nbt')
        structure = reader.Structure.read(path)
        for local, state in structure.blocks.items():
            if state.name not in ('minecraft:chest', 'minecraft:barrel') and 'banner' not in state.name:
                continue
            position = base.block_position(local, room)
            entity = native.entity(position)
            if entity is None and mode == 'partial':
                absent += 1
                continue
            if entity is None:
                raise integrate.GateError('Final native BE absent: ' + str(position))
            original_tag = structure.block_entities.get(local)
            original = reader.plain(original_tag) if original_tag is not None else {'id': 'minecraft:chest' if state.name == 'minecraft:chest' else 'minecraft:barrel' if state.name == 'minecraft:barrel' else 'minecraft:banner'}
            for key, value in original.items():
                if key not in ('x', 'y', 'z') and entity.get(key) != value:
                    raise integrate.GateError('Saved native field differs from raw source: ' + key + ' at ' + str(position))
                field_count += 1
            if tuple(entity[axis] for axis in ('x', 'y', 'z')) != position:
                raise integrate.GateError('Native BE relocated coordinates mismatch')
            if original['id'] in ('minecraft:chest', 'minecraft:barrel') and not original.get('Items') and entity.get('Items', []):
                raise integrate.GateError('Original empty container acquired unrequested items')
            expected_name, expected_properties = base.rotated_state(state, room['quarterTurns'])
            expected_properties = dict(expected_properties)
            if 'rotation' in expected_properties:
                expected_properties['rotation'] = str((int(expected_properties['rotation']) + room['quarterTurns'] * 4) % 16)
            if native.state(position) != (expected_name, tuple(sorted(expected_properties.items()))):
                raise integrate.GateError('Saved BE block rotation mismatch')
            found[original['id']] += 1
    if not sum(found.values()):
        raise integrate.GateError('No original BEs found in saved restart world')
    return dict(status='PASS', mode=mode, scope='Independent direct Anvil native BE tags vs original frozen structure bytes; current originals have empty Items/Patterns, not evidence of rich nonempty-original payload', phase=record['phase'], cursor=record['cursor'], operationHash=record['operationHash'], roomIds=[room['templateId'] for room in plan['rooms']], nativeTypes=dict(found), rawFieldsCompared=field_count, notYetPlacedBE=absent, regionHashes=native.files, journalSha256=integrate.digest(journal_path))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', required=True, type=Path)
    parser.add_argument('--world', required=True, type=Path)
    parser.add_argument('--mode', choices=('partial', 'complete'), required=True)
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    try:
        result = verify(args.snapshot.resolve(), args.world.resolve(), args.mode)
    except Exception as error:
        result = dict(status='FAIL', reason=str(error))
    integrate.save(args.output, result)
    print(result)


if __name__ == '__main__':
    main()
