import argparse
import collections
import gzip
import hashlib
import itertools
import json
import zipfile
from pathlib import Path

import build_vault as base
import render_vault as renderer


ROOT = base.ROOT
OUT = ROOT / 'out/m2'
REFERENCE = 'D:/ft工程文件/image2.5/dungeon-room-copper-ember-vault-20261101/out/dungeon-room-copper-ember-vault-hero.png'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--textures-jar', type=Path, required=True)
    arguments = parser.parse_args()
    base.generate()
    base.box((11, 3, 0), (21, 8, 1), 'waxed_cut_copper')
    for side_x in (11, 12, 20, 21):
        base.box((side_x, 3, 0), (side_x, 8, 5), 'deepslate_bricks')
    base.box((11, 9, 0), (21, 9, 5), 'deepslate_tiles')
    base.ROOF.update(itertools.product(range(11, 22), (9,), range(6)))
    base.box((15, 2, 0), (17, 2, 6), 'deepslate_tiles')
    base.box((15, 3, 0), (17, 5, 6), 'air')
    base.box((15, 6, 4), (17, 6, 5), 'waxed_exposed_cut_copper')
    for position, state in list(base.BLOCKS.items()):
        if state['Name'] == 'minecraft:iron_door':
            base.put(*position, 'air')
        if state['Name'] == 'minecraft:light_weighted_pressure_plate' and position[2] <= 6:
            base.put(*position, 'air')
    for payload in base.ENTITIES.values():
        payload.pop('LootTable', None)
        payload.pop('LootTableSeed', None)
        if payload['id'] in ('minecraft:chest', 'minecraft:barrel'):
            payload['Items'] = []
    palette, palette_indexes, entries = [], {}, []
    for position in itertools.product(*(range(length) for length in base.SIZE)):
        state = base.BLOCKS.get(position, {'Name': 'minecraft:air'})
        serialized = json.dumps(state, sort_keys=True)
        if serialized not in palette_indexes:
            palette_indexes[serialized] = len(palette)
            palette.append(state)
        entry = {'pos': list(position), 'state': palette_indexes[serialized]}
        if position in base.ENTITIES:
            entry['nbt'] = base.ENTITIES[position]
        entries.append(entry)
    template = {'DataVersion': 3465, 'size': list(base.SIZE), 'palette': palette, 'blocks': entries, 'entities': []}
    OUT.mkdir(parents=True, exist_ok=True)
    template_path = OUT / 'room.nbt'
    template_path.write_bytes(gzip.compress(b'\x0a\x00\x00' + base.nbt(template)[1], mtime=0))
    assert not any(state['Name'] == 'minecraft:iron_door' for state in palette)
    for block_x, block_y in itertools.product(range(15, 18), range(3, 6)):
        assert base.BLOCKS[block_x, block_y, 0]['Name'] == 'minecraft:air'
    for side_x, block_y in itertools.product((14, 18), range(3, 6)):
        assert base.BLOCKS[side_x, block_y, 0]['Name'] != 'minecraft:air'
    for block_z in range(13):
        assert base.BLOCKS.get((16, 2, block_z), {}).get('Name') not in (None, 'minecraft:air')
        for block_y in (3, 4, 5):
            assert base.BLOCKS.get((16, block_y, block_z), {}).get('Name', 'minecraft:air') == 'minecraft:air'
    assert all('LootTable' not in entry and not entry.get('Items') for entry in base.ENTITIES.values())
    opening = set(itertools.product(range(15, 18), range(3, 6), (0,)))
    permeable_parts = ('air', 'bars', 'chain', 'button', 'plate', 'bell', 'campfire', 'rod')

    def blocked(position):
        if position in opening:
            return True
        name = base.BLOCKS.get(position, {}).get('Name', 'minecraft:air')
        return not any(part in name for part in permeable_parts)

    pending, exterior = collections.deque([(-1, -1, -1)]), {(-1, -1, -1)}
    while pending:
        current = pending.popleft()
        for delta in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            candidate = tuple(current[axis] + delta[axis] for axis in range(3))
            if candidate not in exterior and all(-1 <= candidate[axis] <= base.SIZE[axis] for axis in range(3)) and not blocked(candidate):
                exterior.add(candidate)
                pending.append(candidate)
    assert (16, 3, 10) not in exterior, 'Envelope has a second opening'
    sha256 = hashlib.sha256(template_path.read_bytes()).hexdigest()
    base.write_json(OUT / 'room.json', {'id': 'M2', 'name': '锈铜宝库', 'size': list(base.SIZE), 'entrance': [16, 3, 0],
                    'interior': [16, 3, 10], 'facing': 'north', 'sha256': sha256, 'reference': REFERENCE})
    base.write_json(OUT / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'id': 'M2', 'sha256': sha256,
                    'minecraft': '1.20.1', 'DataVersion': 3465, 'size_xyz': list(base.SIZE), 'dense_volume': len(entries),
                    'north_opening': {'width': 3, 'height': 3, 'feet_center': [16, 3, 0], 'closed_doors': 0, 'floor_inside_template': True},
                    'safe_interior': [16, 3, 10], 'straight_three_high_route_to_altar_side': [[16, 3, 0], [16, 3, 12]],
                    'walls_and_roof_voxel_enclosure_except_north_opening': 'PASS', 'configured_rewards': 0,
                    'loot_tables': 0, 'filled_containers': 0, 'ordinary_entities': 0, 'tick_functions': 0,
                    'only_vanilla_1_20_1_blocks': True, 'core_modified': False, 'deployed': False, 'player_worlds_modified': False,
                    'preview': 'Same exported block data; roof and near wall cut away; bell/chest entity models simplified; not an in-game screenshot.'})
    with zipfile.ZipFile(arguments.textures_jar) as renderer.ARCHIVE:
        renderer.render({'roof': sorted(base.ROOF), **template}, 'hero', 1900, 1450).save(OUT / 'preview.png')
    print(json.dumps({'status': 'STATIC_PASS', 'staging': str(OUT), 'sha256': sha256}, ensure_ascii=False))


if __name__ == '__main__':
    main()
