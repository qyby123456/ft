import json
from collections import deque
from pathlib import Path

from PIL import Image

from build_room import REFERENCE, REFERENCE_HASH, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'sand', 'gravel'}


def empty_payload(value):
    if isinstance(value, dict):
        return all(key not in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'}
                   and (key not in {'Items', 'Item'} or content == []) and empty_payload(content)
                   for key, content in value.items())
    if isinstance(value, list):
        return all(empty_payload(content) for content in value)
    return True


def audit():
    room = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'qa/blueprint.json').read_text(encoding='utf-8'))
    _, root = read_nbt(ROOT / 'room.nbt')
    assert room.size == SIZE == tuple(metadata['size'])
    assert room.data_version == 3465 and 'palette' in root.value and 'palettes' not in root.value
    assert len(room.blocks) == 3179 and len(room.entities) == 0
    assert len({tuple(plain(record.value['pos'])) for record in root.value['blocks'].value}) == 3179
    assert all(0 <= coordinate < limit for position in room.blocks for coordinate, limit in zip(position, SIZE))
    assert all(block.name.startswith('minecraft:') and block.name[10:] not in FORBIDDEN
               and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values())
    assert digest(ROOT / 'room.nbt') == metadata['sha256']
    assert digest(REFERENCE) == REFERENCE_HASH == digest(ROOT / 'reference/R5-stonemason-workshop.png')
    assert Image.open(REFERENCE).size == (1536, 1024)
    for pos_x in range(17):
        for pos_z in range(17):
            assert room.get((pos_x, 0, pos_z)) != AIR
            assert room.get((pos_x, 10, pos_z)).name in {'minecraft:stone_bricks', 'minecraft:dark_oak_planks'}
    for value in blueprint['main_aisle']:
        assert room.get(tuple(value)) == AIR, value
    assert metadata['entrance'] == [8, 1, 0] and metadata['interior'] == [8, 1, 4] and metadata['facing'] == 'north'

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return (room.get(position) == AIR and room.get((pos_x, pos_y + 1, pos_z)) == AIR
                and room.get((pos_x, pos_y - 1, pos_z)) != AIR)

    reached = {tuple(metadata['entrance'])}
    pending = deque(reached)
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (pos_x + offset_x, pos_y, pos_z + offset_z)
            if 0 <= neighbor[0] < 17 and 0 <= neighbor[2] < 17 and neighbor not in reached and walkable(neighbor):
                reached.add(neighbor)
                pending.append(neighbor)
    assert all(tuple(value) in reached for value in blueprint['required_walkable'])
    assert tuple(metadata['interior']) in reached
    for pos_x, pos_z in blueprint['table_legs']:
        assert room.get((pos_x, 1, pos_z)).name == 'minecraft:stone_bricks'
        assert room.get((pos_x, 2, pos_z)).name == 'minecraft:polished_andesite'
    for pos_x, pos_z in blueprint['scaffold_posts']:
        assert all(room.get((pos_x, pos_y, pos_z)) != AIR for pos_y in range(1, 6))
    for ladder in blueprint['ladders']:
        assert room.get(tuple(ladder['position'])).name == 'minecraft:ladder'
        assert room.get(tuple(ladder['backing'])).name == 'minecraft:spruce_planks'
    for lamp in blueprint['lamps']:
        assert room.get(tuple(lamp['lantern'])).name == 'minecraft:lantern'
        assert all(room.get(tuple(position)).name == 'minecraft:chain' for position in lamp['chains'])
        assert room.get(tuple(lamp['anchor'])).name == 'minecraft:dark_oak_planks'
    solid = {position for position, block in room.blocks.items() if block != AIR}
    grounded = {position for position in solid if position[1] == 0}
    pending = deque(grounded)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor in solid and neighbor not in grounded:
                grounded.add(neighbor)
                pending.append(neighbor)
    assert solid == grounded, sorted(solid - grounded)
    assert len(room.block_entities) == 2
    assert all(empty_payload(plain(value)) for value in room.block_entities.values())
    assert not empty_payload({'Items': [{'id': 'minecraft:diamond', 'Count': 1}]})
    assert not empty_payload({'LootTable': 'minecraft:chests/simple_dungeon'})
    room.write(ROOT / 'qa/rebuilt-room.nbt')
    assert digest(ROOT / 'qa/rebuilt-room.nbt') == digest(ROOT / 'room.nbt')
    return {
        'status': 'PASS', 'structure_sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH,
        'size': SIZE, 'explicit_records': len(room.blocks), 'palette_states': len(root.value['palette'].value),
        'non_air_blocks': len(solid), 'static_floor_reachable_feet': len(reached), 'entities': 0, 'empty_block_entities': 2,
        'checks': {key: 'PASS' for key in ['gzip_nbt_single_palette_3465', 'coordinate_uniqueness_bounds', 'metadata_hash',
                   'reference_image_dimensions_hash', 'full_floor_and_roof_289_each', 'north_3x3_opening', 'central_3x3_aisle_16_long',
                   'safe_point_and_workstation_access', 'table_legs_scaffold_ladders_lamp_supports', 'all_nonair_ground_connected',
                   'empty_inventories_no_loot', 'zero_entities_no_forbidden_blocks_free_fluid', 'deterministic_roundtrip']},
        'limits': ['Static walkability uses air cells only; actual collision shapes and survival require qa/native.json.',
                   'Ground connectivity is an adjacency audit, not a structural engineering solver.'],
    }


def main():
    try:
        report = audit()
    except Exception as exception:
        write_json(ROOT / 'qa/static.json', {'status': 'FAIL', 'error': repr(exception)})
        raise
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
