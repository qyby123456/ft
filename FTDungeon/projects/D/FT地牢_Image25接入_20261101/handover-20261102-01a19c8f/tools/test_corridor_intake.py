import json
import sys
from datetime import datetime, timedelta, timezone

from PIL import Image

from intake_corridors import INSTANCE, ROOT, digest, load, save, static_check

sys.path.insert(0, str(INSTANCE / 'output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, Tag, state


def fixture(directory, length, reference, identifier, mutation=None):
    structure = Structure((9, 7, length))
    stone = state('minecraft:stone_bricks')
    for across in range(9):
        for depth in range(length):
            structure.set((across, 0, depth), stone)
            structure.set((across, 5, depth), stone)
    for across in (0, 8):
        for elevation in range(1, 5):
            for depth in range(length):
                structure.set((across, elevation, depth), stone)
    if mutation is not None:
        mutation(structure)
    structure.write(directory / 'room.nbt')
    metadata = {'id': identifier, 'size': list(structure.size), 'entrance': [4, 1, 0],
                'interior': [4, 1, 2], 'exit': [4, 1, length - 1],
                'reference': reference['source'], 'sha256': digest(directory / 'room.nbt')}
    save(directory / 'room.json', metadata)
    save(directory / 'qa.json', {'status': 'SYNTHETIC_TEST_FIXTURE_NOT_A_DELIVERY'})
    Image.new('RGB', (4, 4), '#555555').save(directory / 'preview.png')
    return directory


def main():
    registry = load(load(ROOT / 'latest.json')['registry']['path'])
    medium, short = registry['corridors'][0], registry['corridors'][20]
    output = ROOT / 'test-fixtures' / datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')
    cases = [
        ('short_16', short, 16, None, True, None),
        ('medium_32', medium, 32, None, True, None),
        ('medium_48', medium, 48, None, True, None),
        ('wrong_medium_length', medium, 31, None, False, 'length'),
        ('blocked_far_exit', short, 16, lambda value: value.set((4, 1, 15), state('minecraft:stone')), False, 'main lane'),
        ('lava_on_main_lane', short, 16, lambda value: value.set((4, 1, 8), state('minecraft:lava')), False, 'main lane'),
        ('stair_on_main_lane', short, 16, lambda value: value.set((4, 0, 8), state('minecraft:stone_brick_stairs')), False, 'floor'),
        ('missing_roof', short, 16, lambda value: value.set((4, 5, 8), state('minecraft:air')), False, 'roof'),
        ('spawner', short, 16, lambda value: value.set((1, 1, 8), state('minecraft:spawner')), False, 'forbidden'),
        ('non_vanilla', short, 16, lambda value: value.set((1, 1, 8), state('ftdungeonmobs:bloodstone_bricks')), False, 'Non-vanilla'),
        ('entity', short, 16, lambda value: value.entities.append(Tag(10, {})), False, 'Entities'),
    ]
    results = []
    for name, entry, length, mutation, expected, message in cases:
        directory = fixture(output / name, length, entry['reference'], entry['id'], mutation)
        try:
            result = static_check(directory, entry, entry['reference'])
        except ValueError as failure:
            if expected or message not in str(failure):
                raise
            results.append({'case': name, 'result': 'PASS_EXPECTED_REJECTION', 'reason': str(failure)})
        else:
            if not expected or result['status'] != 'PASS_INTAKE_ONLY':
                raise ValueError('Unexpected static result: ' + name)
            results.append({'case': name, 'result': 'PASS_INTAKE_ONLY'})
    summary = {'status': 'PASS', 'synthetic_test_data_only': True, 'not_producer_deliveries': True,
               'native_runtime_executed': False, 'cases': results}
    save(output / 'results.json', summary)
    print(json.dumps({'report': str(output / 'results.json'), 'passed': len(results), 'status': 'PASS'}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
