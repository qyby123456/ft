import argparse
from collections import Counter
import gzip
import hashlib
import importlib.util
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
import verify_saved_world as base


def inspect(snapshot, world):
    lock = integrate.load(snapshot / 'snapshot-lock.json')
    for name, component in lock['components'].items():
        for relative, expected in component['files'].items():
            if integrate.digest(integrate.contained(snapshot / name, relative)) != expected:
                raise integrate.GateError('Frozen input changed')
    reader = base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    spec = importlib.util.spec_from_file_location('sealed_native', integrate.HOME / 'v2/read_native_be.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    native = module.NativeWorld(reader, world)
    paths = list((world / 'ft_biome_dungeon_candidate_v4').rglob('*.nbt'))
    if len(paths) != 1:
        raise integrate.GateError('Expected exactly one actual candidate command job')
    framed = paths[0].read_bytes()
    if framed[:4] != b'FTD4' or int.from_bytes(framed[4:8], 'big') != len(framed)-72 or hashlib.sha256(framed[72:]).hexdigest().encode() != framed[8:72]:
        raise integrate.GateError('Candidate journal framing/checksum invalid')
    record = base.decode(reader, gzip.decompress(framed[72:]))
    plan = json.loads(record['plan'])
    bounds = json.loads(record['bounds'])
    counts = Counter()
    mismatches, native_differences, fluids, escaped, shell_errors = [], [], [], [], []
    fluid_room = None
    for room in plan['rooms']:
        source = snapshot / 'catalog/candidate-resources/data/biomedungeon/structures' / (room['templateId'] + '.nbt')
        structure = reader.Structure.read(source)
        if room['templateId'] == 's8_08':
            fluid_room = room['bounds']
        for local, state in structure.blocks.items():
            position = base.block_position(local, room)
            actual = native.state(position)
            expected_name, expected_props = base.rotated_state(state, room['quarterTurns'])
            expected_props = dict(expected_props)
            if 'rotation' in expected_props:
                expected_props['rotation'] = str((int(expected_props['rotation']) + room['quarterTurns']*4) % 16)
            expected = (expected_name, tuple(sorted(expected_props.items())))
            counts['sourceCells'] += 1
            if actual != expected:
                mismatches.append(dict(room=room['templateId'], position=position, expected=expected, actual=actual))
            raw = structure.block_entities.get(local)
            native_type = 'minecraft:banner' if 'banner' in state.name else state.name if state.name in ('minecraft:chest', 'minecraft:barrel') else None
            if raw is not None or native_type is not None:
                counts['nativeBlockEntities'] += 1
                entity = native.entity(position)
                original = reader.plain(raw) if raw is not None else {'id': native_type, 'Patterns' if native_type == 'minecraft:banner' else 'Items': []}
                for key, value in original.items():
                    if key in ('x', 'y', 'z'):
                        continue
                    counts['rawNativeFieldsCompared' if raw is not None else 'defaultNativeFieldsCompared'] += 1
                    if entity is None or entity.get(key) != value:
                        native_differences.append(dict(position=position, field=key, original=value, actual=None if entity is None else entity.get(key)))
    lower, upper = bounds['min'], bounds['maxExclusive']
    for coordinate_y in range(lower['y']-1, upper['y']+1):
        for coordinate_z in range(lower['z']-1, upper['z']+1):
            for coordinate_x in range(lower['x']-1, upper['x']+1):
                position = (coordinate_x, coordinate_y, coordinate_z)
                actual = native.state(position)
                counts['envelopeAndOutsideCells'] += 1
                interior = all(lower[key] <= value < upper[key] for key, value in zip(('x', 'y', 'z'), position))
                shell = interior and any(value in (lower[key], upper[key]-1) for key, value in zip(('x', 'y', 'z'), position))
                if shell:
                    counts['shellCells'] += 1
                    if actual[0] != 'minecraft:barrier':
                        shell_errors.append(dict(position=position, actual=actual))
                if actual[0] in ('minecraft:lava', 'minecraft:water', 'minecraft:fire', 'minecraft:soul_fire'):
                    fluids.append(dict(position=position, state=actual))
                    allowed = fluid_room is not None and all(fluid_room['min'][key] <= value < fluid_room['maxExclusive'][key] for key, value in zip(('x', 'y', 'z'), position))
                    if not allowed:
                        escaped.append(dict(position=position, state=actual))
    return dict(status='OBSERVATIONS_REQUIRE_DYNAMIC_AND_INTERACTION_REVIEW', world=str(world), journalSha256=integrate.digest(paths[0]), phase=record['phase'], cursor=record['cursor'], rooms=[dict(id=room['templateId'], rotation=room['quarterTurns']) for room in plan['rooms']], counts=dict(counts), originalStateDifferences=mismatches, originalNativeFieldDifferences=native_differences, fluidFireStates=fluids, escapedFluidOrFire=escaped, enclosureErrors=shell_errors, regionHashes=native.files, completeGeneratorReady=False, limitations=['Raw differences are reported, never silently accepted as dynamic evolution', 'Observed saved fluid/enclosure states do not prove native tick scheduling without separate live logs', 'Offline readback is not physical routes, door interactions or UI evidence'])


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', type=Path, required=True)
    parser.add_argument('--world', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    integrate.save(arguments.output, inspect(arguments.snapshot, arguments.world))
