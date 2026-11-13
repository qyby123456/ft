import argparse
from collections import deque
import gzip
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import struct
import sys
import zlib

import integrate


def load_reader(snapshot):
    path = snapshot / 'catalog/nbtio.py'
    lock = integrate.load(snapshot / 'snapshot-lock.json')
    expected = next(item['files']['nbtio.py'] for item in lock['components'] if item['component'] == 'catalog')
    if integrate.digest(path) != expected:
        raise integrate.GateError('Frozen NBT reader hash differs')
    specification = importlib.util.spec_from_file_location('frozen_nbtio', path)
    module = importlib.util.module_from_spec(specification)
    sys.modules[specification.name] = module
    specification.loader.exec_module(module)
    return module


def decode(reader, payload):
    stream = io.BytesIO(payload)
    kind = reader.take(stream, 1)[0]
    reader.read_string(stream)
    return reader.plain(reader.read_payload(stream, kind))


class World:
    def __init__(self, reader, world):
        self.reader = reader
        self.world = world
        self.chunks = {}
        self.files = {}

    def chunk(self, chunk_x, chunk_z):
        key = (chunk_x, chunk_z)
        if key in self.chunks:
            return self.chunks[key]
        path = self.world / 'region' / f'r.{chunk_x // 32}.{chunk_z // 32}.mca'
        raw = path.read_bytes()
        self.files[str(path)] = hashlib.sha256(raw).hexdigest()
        offset = 4 * ((chunk_x % 32) + (chunk_z % 32) * 32)
        sector = int.from_bytes(raw[offset:offset + 3], 'big')
        if not sector:
            raise integrate.GateError(f'Expected saved chunk missing: {key}')
        start = sector * 4096
        length = int.from_bytes(raw[start:start + 4], 'big')
        compression = raw[start + 4]
        payload = raw[start + 5:start + 4 + length]
        if compression == 2:
            payload = zlib.decompress(payload)
        elif compression == 1:
            payload = gzip.decompress(payload)
        elif compression != 3:
            raise integrate.GateError('Unsupported region compression/external chunk')
        chunk = decode(self.reader, payload)
        sections = {section['Y']: section for section in chunk['sections']}
        self.chunks[key] = sections
        return sections

    def state(self, position):
        coordinate_x, coordinate_y, coordinate_z = position
        sections = self.chunk(coordinate_x // 16, coordinate_z // 16)
        section = sections.get(coordinate_y // 16, {})
        storage = section.get('block_states', {})
        palette = storage.get('palette', [{'Name': 'minecraft:air'}])
        index = 0
        if len(palette) > 1:
            bits = max(4, (len(palette) - 1).bit_length())
            per_long = 64 // bits
            linear = (coordinate_y % 16) * 256 + (coordinate_z % 16) * 16 + coordinate_x % 16
            word = storage['data'][linear // per_long] & ((1 << 64) - 1)
            index = (word >> ((linear % per_long) * bits)) & ((1 << bits) - 1)
        state = palette[index]
        return state['Name'], tuple(sorted(state.get('Properties', {}).items()))


def block_position(local, room):
    coordinate_x, coordinate_y, coordinate_z = local
    turn = room['quarterTurns']
    rotated = ((coordinate_x, coordinate_z), (-coordinate_z - 1, coordinate_x), (-coordinate_x - 1, -coordinate_z - 1), (coordinate_z, -coordinate_x - 1))[turn]
    origin = room['origin']
    return origin['x'] + rotated[0], origin['y'] + coordinate_y, origin['z'] + rotated[1]


def rotated_state(state, turn):
    properties = dict(state.properties)
    directions = ['north', 'east', 'south', 'west']
    if properties.get('facing') in directions:
        properties['facing'] = directions[(directions.index(properties['facing']) + turn) % 4]
    if turn % 2 and properties.get('axis') in ('x', 'z'):
        properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
    horizontal = {key: value for key, value in properties.items() if key in directions}
    for key in horizontal:
        del properties[key]
    for key, value in horizontal.items():
        properties[directions[(directions.index(key) + turn) % 4]] = value
    return state.name, tuple(sorted(properties.items()))


def verify(snapshot, world_path, identity=None):
    reader = load_reader(snapshot)
    world = World(reader, world_path)
    journals = list((world_path / 'ft_biome_dungeon_generator/minecraft/overworld').glob('*.nbt'))
    if identity:
        journals = [path for path in journals if path.stem == identity]
    if not journals:
        raise integrate.GateError('No requested persisted runtime journal')
    results = []
    for journal in journals:
        raw = journal.read_bytes()
        if raw[:4] != b'FTD1' or len(raw) != int.from_bytes(raw[4:8], 'big') + 72 or hashlib.sha256(raw[72:]).hexdigest().encode('ascii') != raw[8:72]:
            raise integrate.GateError('Journal frame/checksum invalid')
        record = decode(reader, gzip.decompress(raw[72:]))
        if record['phase'] != 'COMMITTED':
            raise integrate.GateError('Readback expects COMMITTED journal')
        plan = json.loads(record['plan'])
        bounds = json.loads(record['bounds'])
        raw_checked = 0
        for room in plan['rooms']:
            source = snapshot / 'catalog/data/biomedungeon/structures' / (room['templateId'] + '.nbt')
            structure = reader.Structure.read(source)
            for local, state in structure.blocks.items():
                if local[0] in (0, structure.size[0] - 1) or local[2] in (0, structure.size[2] - 1):
                    continue
                position = block_position(local, room)
                actual = world.state(position)
                expected = rotated_state(state, room['quarterTurns'])
                if actual != expected:
                    raise integrate.GateError(f'Raw interior mismatch {position}: {actual} != {expected}')
                raw_checked += 1
        minimum = tuple(bounds['min'][axis] for axis in ('x', 'y', 'z'))
        maximum = tuple(bounds['maxExclusive'][axis] for axis in ('x', 'y', 'z'))
        shell_checked = 0
        for coordinate_y in range(minimum[1], maximum[1]):
            for coordinate_z in range(minimum[2], maximum[2]):
                for coordinate_x in range(minimum[0], maximum[0]):
                    if coordinate_x not in (minimum[0], maximum[0] - 1) and coordinate_y not in (minimum[1], maximum[1] - 1) and coordinate_z not in (minimum[2], maximum[2] - 1):
                        continue
                    if world.state((coordinate_x, coordinate_y, coordinate_z))[0] != 'minecraft:barrier':
                        raise integrate.GateError('Saved perimeter barrier gap')
                    shell_checked += 1
        targets = {tuple(point[axis] for axis in ('x', 'y', 'z')) for link in plan['links'] for point in link['waypoints']}
        reached = set()
        if targets:
            start = next(iter(targets))
            queue = deque([start])
            reached.add(start)
            while queue:
                current = queue.popleft()
                for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    neighbor = (current[0] + delta_x, current[1], current[2] + delta_z)
                    if neighbor in reached or not all(minimum[index] <= neighbor[index] < maximum[index] for index in range(3)):
                        continue
                    foot = world.state(neighbor)[0]
                    head = world.state((neighbor[0], neighbor[1] + 1, neighbor[2]))[0]
                    floor = world.state((neighbor[0], neighbor[1] - 1, neighbor[2]))[0]
                    if foot in ('minecraft:air', 'minecraft:cave_air') and head in ('minecraft:air', 'minecraft:cave_air') and floor in ('minecraft:stone_bricks', 'minecraft:calcite', 'minecraft:amethyst_block', 'minecraft:polished_deepslate', 'minecraft:deepslate_bricks', 'minecraft:deepslate_tiles'):
                        reached.add(neighbor)
                        queue.append(neighbor)
            if not targets.issubset(reached):
                raise integrate.GateError('Saved full-air two-block route cannot connect corridor waypoints')
        results.append(dict(instanceId=plan['instanceId'], phase=record['phase'], roomIds=[room['templateId'] for room in plan['rooms']], rotations=[room['quarterTurns'] for room in plan['rooms']], rawInteriorCells=raw_checked, perimeterCells=shell_checked, routeTargets=len(targets), reachableFullAirCells=len(reached), journalSha256=integrate.digest(journal)))
    return dict(status='PASS', scope='Independent offline saved Anvil chunks, raw interior states, barrier shell and conservative full-air/full-block floor route; native collision shapes and unused door faces excluded', results=results, regionFiles=world.files, readerSha256=integrate.digest(snapshot / 'catalog/nbtio.py'))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', required=True, type=Path)
    parser.add_argument('--world', required=True, type=Path)
    parser.add_argument('--identity')
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    if not args.output.resolve().is_relative_to(integrate.HOME):
        raise integrate.GateError('Output must remain in integration')
    try:
        result = verify(args.snapshot.resolve(), args.world.resolve(), args.identity)
    except Exception as error:
        result = dict(status='FAIL', reason=str(error))
    integrate.save(args.output, result)
    print(result)
    return 0 if result['status'] == 'PASS' else 2


if __name__ == '__main__':
    raise SystemExit(main())
