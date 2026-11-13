from __future__ import annotations

import hashlib
import json
from collections import Counter, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
from nbtio import Structure, plain, read_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S01-bone-crypt-overgrown.png')
SOURCE_HASH = '31b3e9c04d8f1e6c562edea2bbd7c9b99c02d1c7280fd89e922f5ec214cfded0'
SIZE = (13, 11, 16)


def digest(filename):
    return hashlib.sha256(Path(filename).read_bytes()).hexdigest()


def write_json(filename, value):
    Path(filename).parent.mkdir(parents=True, exist_ok=True)
    Path(filename).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    assert digest(REFERENCE) == SOURCE_HASH
    source = ROOT / 'room.nbt'
    assert source.read_bytes()[:2] == b'\x1f\x8b'
    name, root = read_nbt(source)
    raw = plain(root)
    assert raw['DataVersion'] == 3465 and 'palettes' not in raw
    assert tuple(raw['size']) == SIZE and not raw['entities']
    assert len(raw['blocks']) == 2288
    assert len({tuple(entry['pos']) for entry in raw['blocks']}) == 2288
    structure = Structure.read(source)
    assert len(structure.blocks) == 2288
    materials = Counter(block.name for block in structure.blocks.values())
    forbidden = {'water', 'lava', 'fire', 'soul_fire', 'cobweb', 'powder_snow', 'cactus', 'spawner', 'chest', 'barrel', 'hopper', 'dispenser', 'dropper', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void'}
    assert not {block.removeprefix('minecraft:') for block in materials}.intersection(forbidden)
    assert all(block.startswith('minecraft:') for block in materials)
    def block(pos_x, pos_y, pos_z):
        return structure.blocks[(pos_x, pos_y, pos_z)]
    for pos_x in range(13):
        for pos_z in range(16):
            assert block(pos_x, 10, pos_z).name in {'minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks'}
    for pos_x in range(5, 8):
        for pos_z in range(16):
            assert block(pos_x, 1, pos_z).name != 'minecraft:air'
            assert all(block(pos_x, pos_y, pos_z).name == 'minecraft:air' for pos_y in range(2, 5))
    assert all(block(pos_x, pos_y, pos_z).name != 'minecraft:air' for pos_x in (0, 12) for pos_y in range(11) for pos_z in range(16))
    for position, state in structure.blocks.items():
        pos_x, pos_y, pos_z = position
        assert all(0 <= coordinate < size for coordinate, size in zip(position, SIZE))
        if state.name in ('minecraft:candle', 'minecraft:skeleton_skull', 'minecraft:moss_carpet'):
            support = block(pos_x, pos_y - 1, pos_z)
            assert support.name not in ('minecraft:air', 'minecraft:candle') and not support.name.endswith(('_stairs', '_slab', '_trapdoor'))
        if state.name == 'minecraft:chain':
            assert block(pos_x, pos_y + 1, pos_z).name.endswith('stone_bricks')
            assert block(pos_x, pos_y - 1, pos_z).name == 'minecraft:iron_block'
        if state.name == 'minecraft:azalea_leaves':
            assert state.get('persistent') == 'true'
        if state.name == 'minecraft:candle':
            assert state.get('lit') == 'true'
        if state.name == 'minecraft:glow_lichen':
            offsets = {'up': (0, 1, 0), 'down': (0, -1, 0), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'north': (0, 0, -1), 'south': (0, 0, 1)}
            for face, delta in offsets.items():
                if state.get(face) == 'true':
                    assert block(pos_x + delta[0], pos_y + delta[1], pos_z + delta[2]).name != 'minecraft:air'
    assert len(structure.block_entities) == materials['minecraft:skeleton_skull'] == 10
    for entity in structure.block_entities.values():
        value = plain(entity)
        assert value['id'] == 'minecraft:skull'
        assert not {'Items', 'LootTable', 'Book'}.intersection(value)
    assert materials['minecraft:chain'] == 4
    assert materials['minecraft:candle'] == 8
    assert materials['minecraft:end_rod'] == 8
    assert materials['minecraft:iron_bars'] == 32
    assert materials['minecraft:azalea_leaves'] == 12
    traversable = {(pos_x, 2, pos_z) for pos_x in range(3, 10) for pos_z in range(16) if block(pos_x, 2, pos_z).name == 'minecraft:air' and block(pos_x, 3, pos_z).name == 'minecraft:air'}
    entrance, interior, exit_pos = (6, 2, 0), (6, 2, 2), (6, 2, 15)
    reachable, queue = {entrance}, deque([entrance])
    while queue:
        current = queue.popleft()
        for delta in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            candidate = tuple(current[index] + delta[index] for index in range(3))
            if candidate in traversable and candidate not in reachable:
                reachable.add(candidate)
                queue.append(candidate)
    assert interior in reachable and exit_pos in reachable
    assert all((pos_x, 2, pos_z) in reachable for pos_x in range(5, 8) for pos_z in range(16))
    metadata = {'id': 'S01', 'name': '蔓生白骨吊笼短廊', 'size': list(SIZE), 'entrance': list(entrance), 'interior': list(interior), 'exit': list(exit_pos), 'facing': 'north', 'exit_facing': 'south', 'sha256': digest(source), 'reference': str(REFERENCE), 'reference_sha256': SOURCE_HASH, 'minecraft': '1.20.1', 'data_version': 3465, 'axis': 'z', 'length_blocks': 16, 'length_chunks': 1, 'portal_width': 3, 'portal_clear_height': 3, 'floor_y': 1, 'explicit_air': True, 'rotation_supported': ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'], 'ports': [{'name': 'north', 'feet': list(entrance), 'outward': 'north'}, {'name': 'south', 'feet': list(exit_pos), 'outward': 'south'}], 'theme': {'theme': 'bone-crypt', 'condition': 'overgrown', 'light': 'end-rods', 'feature': 'hanging-cages'}, 'adaptations': ['South arch intentionally open rather than concept closed door; straight-through takes priority.', 'Side timber panels are sealed burial fronts, not doors or branches.', 'Persistent azalea leaves and supported glow lichen stand in for uncontrolled growing vines.', 'Cage bones use supported vanilla skulls and bone blocks; no entities.'], 'deployment': 'not deployed; isolated delivery only', 'placement_policy': 'Validate all 2288 cells and surrounding entities before first placement. Explicit air does not authorize clearing. Never overwrite a generated or occupied room.'}
    write_json(ROOT / 'room.json', metadata)
    write_json(ROOT / 'qa/materials.json', dict(sorted(materials.items())))
    write_json(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': digest(source), 'reference_sha256': SOURCE_HASH, 'gzip': True, 'DataVersion': 3465, 'single_palette': True, 'size': list(SIZE), 'explicit_cells': len(raw['blocks']), 'non_air': 2288 - materials['minecraft:air'], 'palette_states': len(raw['palette']), 'in_bounds': True, 'unique_coordinates': True, 'native_state_validation': 'See qa/native.json; Python checks do not replace Forge.', 'roof_full_cells': 208, 'dry_straight_3x3_route_cells': 144, 'both_portals_open': True, 'turns': 0, 'branches': 0, 'floor_level_changes': 0, 'static_reachable_air_feet': len(reachable), 'skull_block_entities': len(structure.block_entities), 'hanging_cages': 4, 'lit_candle_clusters': 8, 'end_rod_cells': 8, 'persistent_leaves': 12, 'supported_glow_lichen': materials['minecraft:glow_lichen'], 'template_entities': 0, 'inventory_or_loot_payloads': 0, 'forbidden_blocks': [], 'production_save_used': False})
    print(json.dumps({'static': 'PASS', 'sha256': digest(source), 'materials': dict(materials)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
