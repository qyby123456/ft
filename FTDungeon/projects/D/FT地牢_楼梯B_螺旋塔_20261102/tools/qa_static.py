import json
import math
from collections import Counter, deque

from build_room import FULL, REFERENCE, REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


FORBIDDEN = {'lava', 'water', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    report = {'status': 'RUNNING', 'checks': {}}
    try:
        room = Structure.read(ROOT / 'out/room.nbt')
        metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
        blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
        _, source = read_nbt(ROOT / 'out/room.nbt')
        require('palette' in source.value and 'palettes' not in source.value, 'Require single palette')
        require(room.size == SIZE and room.data_version == 3465, 'Wrong Java 1.20.1 size/version')
        require(len(room.blocks) == math.prod(SIZE) and not room.entities, 'Missing volume or embedded entities')
        require(digest(REFERENCE) == REFERENCE_HASH and metadata['sha256'] == digest(ROOT / 'out/room.nbt'), 'Fingerprint mismatch')
        require(all(block.name.startswith('minecraft:') and block.name.split(':')[1] not in FORBIDDEN for block in room.blocks.values()), 'Unsafe/custom block')
        report['checks']['format_reference_hash_dense_volume_no_entities_vanilla_no_hazards'] = 'PASS'
        require(len(room.block_entities) == 2, 'Expected two empty barrels')
        for position, block_data in room.block_entities.items():
            payload = plain(block_data)
            require(room.get(position).name == 'minecraft:barrel' and payload['id'] == 'minecraft:barrel' and payload['Items'] == [], 'Nonempty container')
            require(not any(key in payload for key in ('LootTable', 'LootTableSeed')), 'Unexpected loot')
        report['checks']['empty_containers_no_rewards'] = 'PASS'
        openings = set(map(tuple, blueprint['openings']))
        checked_shell = 0
        for pos_y in range(27):
            for pos_z in range(27):
                for pos_x in range(27):
                    position = (pos_x, pos_y, pos_z)
                    radius = math.hypot(pos_x - 13, pos_z - 13)
                    if radius <= 13.25 and (pos_y in (0, 26) or radius >= 11.5) and position not in openings:
                        require(room.get(position).name.split(':')[1] in FULL, f'Shell hole: {position}')
                        checked_shell += 1
        report['checks']['closed_cylindrical_shell_roof_foundation_except_three_ports'] = checked_shell
        for port in metadata['ports']:
            center_x, feet_y, pos_z = port['feet']
            for pos_x in range(center_x - 1, center_x + 2):
                require(room.get((pos_x, feet_y - 1, pos_z)).name.split(':')[1] in FULL, 'Port floor missing')
                for pos_y in range(feet_y, feet_y + 3):
                    require(room.get((pos_x, pos_y, pos_z)) == AIR, 'Port not 3x3 clear')
        require(metadata['entrance'][2] == 0 and metadata['entrance'][0] == SIZE[0] // 2, 'Entrance not north centered')
        report['checks']['three_ports_full_width_height_and_solid_floors'] = 'PASS'
        counts = Counter()
        for stair in blueprint['stairs']:
            position, below = tuple(stair['pos']), tuple(stair['support'])
            block = room.get(position)
            require(block.name == 'minecraft:stone_brick_stairs' and block.get('facing') == stair['facing'] and block.get('shape') == 'straight', 'Stair mismatch')
            require(room.get(below).name.split(':')[1] in FULL, f'Unsupported tread: {position}')
            counts[stair['kind']] += 1
        require(counts == {'spiral_up': 36, 'straight_down': 21}, 'Wrong stair runs')
        for position in blueprint['core_blocks']:
            require(room.get(tuple(position)).name.split(':')[1] in FULL, 'Central pier damaged')
        full_blocks = {position for position, block in room.blocks.items() if block.name.split(':')[1] in FULL}
        anchored = {position for position in full_blocks if position[1] == 0}
        pending = deque(anchored)
        while pending:
            pos_x, pos_y, pos_z = pending.popleft()
            for delta_x, delta_y, delta_z in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
                neighbor = (pos_x + delta_x, pos_y + delta_y, pos_z + delta_z)
                if neighbor in full_blocks and neighbor not in anchored:
                    anchored.add(neighbor)
                    pending.append(neighbor)
        for position in blueprint['structural_supports']:
            require(tuple(position) in anchored, f'Disconnected support: {position}')
        for position in blueprint['guardrails']:
            pos_x, pos_y, pos_z = position
            require(room.get(tuple(position)).name == 'minecraft:iron_bars' and (pos_x, pos_y - 1, pos_z) in anchored, f'Floating guardrail: {position}')
        require(len(blueprint['inner_corner_guards']) == 4, 'Missing inner corner guards')
        for rib in blueprint['wall_bearing_ribs']:
            require(len(rib) == 7 and all(tuple(position) in anchored for position in rib), 'Wall bearing rib not connected')
            require(any(math.hypot(position[0] - 13, position[2] - 13) >= 11.5 for position in rib), 'Bearing rib fails to reach wall')
        report['checks']['four_wall_bearing_ribs_and_inner_corner_guards'] = 'PASS'
        report['checks']['stairs_width_three_supported_and_all_masonry_load_paths_to_foundation'] = {'stairs': dict(counts), 'anchored_full_blocks': len(anchored), 'guardrails': len(blueprint['guardrails']), 'core_blocks': len(blueprint['core_blocks'])}
        for lamp in blueprint['lanterns']:
            require(room.get(tuple(lamp['lantern'])).name == 'minecraft:lantern', 'Lantern missing')
            require(all(room.get(tuple(position)).name == 'minecraft:chain' for position in lamp['chains']), 'Chain broken')
            require(tuple(lamp['anchor']) in anchored, 'Lamp anchor floating')
        report['checks']['hanging_lamps_continuous_chains_anchored_to_roof'] = 'PASS'
        report.update(status='PASS', structure_sha256=metadata['sha256'], records=len(room.blocks), native_collision_routes='See qa/native.json; not inferred from air-only flood fill')
    except Exception as exception:
        report.update(status='FAIL', error=str(exception))
        write_json(ROOT / 'qa/static.json', report)
        raise
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
