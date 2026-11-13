import json
from collections import Counter, deque

from build_r2 import ENTRANCE, REFERENCE, REFERENCE_SHA256, ROOT, SIZE, TARGETS, digest, write_json
from nbtio import AIR, Structure, plain


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    results = {}

    def check(name, condition):
        results[name] = {'status': 'PASS' if condition else 'FAIL'}
        if not condition:
            raise AssertionError(name)

    check('reference_fingerprint', digest(REFERENCE) == REFERENCE_SHA256)
    check('dense_unique_in_bounds', len(room.blocks) == 2025 and room.size == SIZE)
    check('vanilla_only_no_entities', not room.entities and all(block.name.startswith('minecraft:') for block in room.blocks.values()))
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'spawner', 'tnt', 'fire', 'soul_fire', 'lava', 'water'}
    check('no_forbidden_blocks_or_free_fluids', not any(block.name.split(':')[1] in forbidden for block in room.blocks.values()))
    check('complete_floor_and_roof', all(room.get((block_x, block_y, block_z)) != AIR for block_x in range(15) for block_z in range(15) for block_y in (0, 8)))
    check('north_3x3_opening', all(room.get((block_x, block_y, 0)) == AIR for block_x in (6, 7, 8) for block_y in (1, 2, 3)))
    check('enclosed_nonentrance_boundaries', all(room.get((block_x, block_y, block_z)) != AIR for block_y in range(1, 8) for block_x in range(15) for block_z in range(15) if (block_x in (0, 14) or block_z in (0, 14)) and not (block_z == 0 and 6 <= block_x <= 8 and 1 <= block_y <= 3)))
    reachable = {ENTRANCE}
    pending = deque([ENTRANCE])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for step_x, step_z in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            target = block_x + step_x, block_y, block_z + step_z
            if target in reachable or not (0 <= target[0] < 15 and 0 <= target[2] < 15):
                continue
            if room.get(target) == AIR and room.get((target[0], 2, target[2])) == AIR and room.get((target[0], 0, target[2])) != AIR:
                reachable.add(target)
                pending.append(target)
    check('safe_point_and_workspaces_reachable', all(tuple(target) in reachable for target in TARGETS))
    check('three_wide_three_high_central_aisle', all(room.get((block_x, block_y, block_z)) == AIR for block_x in (6, 7, 8) for block_y in (1, 2, 3) for block_z in range(12)))
    supported = []
    for position, block in room.blocks.items():
        block_x, block_y, block_z = position
        name = block.name.split(':')[1]
        below = room.get((block_x, block_y - 1, block_z))
        if name == 'chain':
            anchor_y = block_y + 1
            while room.get((block_x, anchor_y, block_z)).name == 'minecraft:chain':
                anchor_y += 1
            check('chain_anchor_' + str(position), room.get((block_x, anchor_y, block_z)) != AIR)
        if name in ('lantern', 'hanging_roots'):
            check('overhead_support_' + str(position), room.get((block_x, block_y + 1, block_z)) != AIR)
        if name == 'candle' or name == 'flower_pot' or name.startswith('potted_'):
            check('surface_support_' + str(position), below != AIR and (not below.name.endswith('_slab') or below.get('type') == 'top'))
        if name == 'campfire':
            check('campfire_stone_bed_' + str(position), below.name == 'minecraft:polished_andesite')
            check('campfire_no_adjacent_wood_' + str(position), all(not any(word in room.get((block_x + step_x, block_y, block_z + step_z)).name for word in ('oak', 'spruce', 'birch', 'wool', 'leaves')) for step_x, step_z in [(1, 0), (-1, 0), (0, 1), (0, -1)]))
        if name.endswith('_trapdoor') and block.get('open') == 'false':
            check('shelf_wall_anchor_' + str(position), room.get((block_x, block_y, block_z + 1)) != AIR)
        if position in map(tuple, metadata['furniture']['table_leg_blocks'] + metadata['furniture']['bench_blocks']):
            supported.append(position)
            check('furniture_floor_' + str(position), below != AIR)
    check('eight_table_corner_supports_and_four_grounded_benches', len(metadata['furniture']['table_leg_blocks']) == 8 and len(supported) == 28)
    check('tabletops_bridge_supported_ends', all(room.get((block_x, 1, 5)).name == 'minecraft:spruce_stairs' and room.get((block_x, 1, 9)).name == 'minecraft:spruce_stairs' and all(room.get((block_x, 1, block_z)).get('type') == 'top' for block_z in (6, 7, 8)) for block_x in (3, 4, 10, 11)))
    check('hanging_cauldron_supported', room.get((7, 3, 13)).name == 'minecraft:chain' and room.get((7, 4, 13)).name == 'minecraft:stone_bricks')
    entities = {str(position): plain(tag) for position, tag in room.block_entities.items()}
    check('empty_containers_no_loot', all(entity.get('Items') == [] and 'LootTable' not in entity for entity in entities.values()))
    counts = Counter(block.name for block in room.blocks.values())
    write_json(ROOT / 'qa/reachable.json', sorted(reachable))
    write_json(ROOT / 'qa/materials.json', dict(sorted(counts.items())))
    evidence = {'id': 'R2', 'status': 'PASS', 'sha256': digest(ROOT / 'out/room.nbt'), 'checks': results, 'checks_passed': len(results), 'reachable_dry_feet': len(reachable), 'walk_targets': TARGETS, 'block_entities': entities, 'inventory_items': 0, 'loot_tables': 0, 'normal_blocks': 2025 - counts['minecraft:air'], 'air_blocks': counts['minecraft:air'], 'palette_states': len(set(room.blocks.values())), 'minecraft_client_tested': False}
    write_json(ROOT / 'qa/static.json', evidence)
    print(json.dumps({'status': 'PASS', 'checks': len(results), 'reachable': len(reachable)}))


if __name__ == '__main__':
    main()
