import hashlib
import json
import subprocess
import sys
from collections import Counter, deque
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import AIR, Structure, plain, read_nbt, state


REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M20-copper-machinery-intact.png')
REFERENCE_HASH = 'ba38b1a755aed26654acd83aa1cfc4fdeb3b722e989f7f80ce963b3096acbae5'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def check(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    check(ROOT.name == 'corridor-M20', 'Refusing an unexpected output directory')
    check(digest(REFERENCE) == REFERENCE_HASH, 'Reference SHA-256 mismatch')
    for directory in ('preview', 'qa', 'reference'):
        (ROOT / directory).mkdir(parents=True, exist_ok=True)
    command = ['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'tools/recipe.mjs'),
               '--out-dir', str(ROOT), '--name', 'room', '--data-version', '3465', '--json']
    execution = subprocess.run(command, cwd=SKILL, capture_output=True, text=True, encoding='utf-8', check=True)
    (ROOT / 'qa/build.log').write_text(execution.stdout + execution.stderr, encoding='utf-8')
    summary = json.loads(execution.stdout)
    save(ROOT / 'qa/builder-summary.json', summary)
    metadata = {
        'id': 'corridor-M20', 'name': '铜械机廊·完好型', 'category': 'medium_straight_corridor',
        'size': [13, 11, 40], 'entrance': [6, 2, 0], 'interior': [6, 2, 3], 'exit': [6, 2, 39],
        'facing': 'north', 'axis': 'z', 'length_blocks': 40, 'length_chunks': 2.5,
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': '1.20.1', 'data_version': 3465,
        'ports': [{'face': 'north', 'feet': [6, 2, 0], 'clear_width': 5, 'clear_height': 5},
                  {'face': 'south', 'feet': [6, 2, 39], 'clear_width': 5, 'clear_height': 5}],
        'straight_route': {'x_range': [4, 8], 'y_feet': 2, 'z_range': [0, 39], 'minimum_headroom': 3},
        'theme_features': {'hanging_cages': 4, 'lanterns': 4, 'wall_light_panels': 10,
                           'gauge_stations': 12, 'waxed_copper': True},
        'substitutions': [
            '1.20.1 has no copper grates or copper bulbs; closed upper iron trapdoors over oxidized copper form the grates.',
            'Lava-like lighting uses fully solid shroomlight and orange glass, without lava, fire or fluids.',
            'Brass gauges use gold/copper frames, quartz dials and supported blackstone buttons; pipes use copper slabs.'
        ],
        'integration': {'canonical_id': 'corridor-M20', 'candidate_template': 'ftdungeonmobs:image25/corridor_m20',
                        'reserved_intake_slot': 60, 'deployed': False, 'production_save_modified': False,
                        'integration_acceptance_owner': 'DSH 地牢交接与四视图续接',
                        'ready_for_unified_integration': False,
                        'reason': 'Producer evidence only; receiver must freeze and independently repeat visual/native review.'}
    }
    save(ROOT / 'room.json', metadata)
    room = Structure.read(ROOT / 'room.nbt')
    _, document = read_nbt(ROOT / 'room.nbt')
    values = plain(document)
    check(room.size == (13, 11, 40) and room.data_version == 3465, 'Wrong dimensions or data version')
    check('palette' in values and 'palettes' not in values, 'Not a single-palette structure')
    check(len(room.blocks) == 5720 and len(values['blocks']) == 5720, 'Explicit full-volume record count mismatch')
    check(not room.entities and not room.block_entities, 'Unexpected entities or block entities')
    prohibited = {'lava', 'water', 'fire', 'soul_fire', 'magma_block', 'powder_snow', 'cobweb', 'tnt',
                  'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block',
                  'structure_void', 'jigsaw', 'spawner', 'chest', 'barrel', 'dispenser', 'dropper'}
    materials = Counter(block.name for block in room.blocks.values())
    check(all(name.startswith('minecraft:') and name.split(':')[1] not in prohibited for name in materials), 'Forbidden material')
    for position, block in room.blocks.items():
        check(all(0 <= value < limit for value, limit in zip(position, room.size)), 'Block outside bounds')
        check(not ('copper' in block.name and not block.name.startswith('minecraft:waxed_')), 'Unwaxed copper')
    for horizontal in range(13):
        for depth in range(40):
            check(room.get((horizontal, 0, depth)) != AIR, 'Incomplete foundation')
            check(room.get((horizontal, 10, depth)) != AIR, 'Incomplete roof')
    for depth in range(40):
        for horizontal in range(4, 9):
            floor = room.get((horizontal, 1, depth))
            check(floor != AIR, 'Missing straight-route floor')
            if floor.name.endswith('trapdoor'):
                check(floor.get('half') == 'top' and floor.get('open') == 'false', 'Unsafe grate')
            for vertical in range(2, 5):
                check(room.get((horizontal, vertical, depth)) == AIR, 'Obstructed three-high five-wide straight route')
    for depth in (0, 39):
        for horizontal in range(4, 9):
            check(not room.get((horizontal, 1, depth)).name.endswith('trapdoor'), 'Endpoint needs full-cube floor')
            for vertical in range(2, 7):
                check(room.get((horizontal, vertical, depth)) == AIR, 'Endpoint aperture obstructed')
    for position, block in room.blocks.items():
        horizontal, vertical, depth = position
        if block.name == 'minecraft:lantern':
            check(block.get('hanging') == 'true' and room.get((horizontal, vertical + 1, depth)).name == 'minecraft:iron_block', 'Floating lantern')
        if block.name == 'minecraft:chain':
            check(room.get((horizontal, vertical + 1, depth)).name.startswith('minecraft:waxed_'), 'Chain lacks roof anchor')
            check(room.get((horizontal, vertical - 1, depth)).name == 'minecraft:iron_block', 'Chain misses cage cap')
        if block.name == 'minecraft:polished_blackstone_button':
            support_x = horizontal - 1 if block.get('facing') == 'east' else horizontal + 1
            check(room.get((support_x, vertical, depth)).name == 'minecraft:smooth_quartz', 'Unsupported gauge button')
    reachable = {(6, 0)}
    pending = deque(reachable)
    while pending:
        horizontal, depth = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = (horizontal + offset_x, depth + offset_z)
            if 4 <= candidate[0] <= 8 and 0 <= candidate[1] < 40 and candidate not in reachable:
                reachable.add(candidate)
                pending.append(candidate)
    check(len(reachable) == 200 and (6, 39) in reachable and (6, 3) in reachable, 'Route connectivity failure')
    check(materials['minecraft:lantern'] == 4 and materials['minecraft:chain'] == 4, 'Cage lighting count mismatch')
    for center, depth in ((6, 7), (5, 15), (7, 23), (6, 31)):
        for horizontal in range(center - 1, center + 2):
            for longitudinal in range(depth - 1, depth + 2):
                if (horizontal, longitudinal) != (center, depth):
                    check(room.get((horizontal, 7, longitudinal)).get('half') == 'bottom', 'Cage lid must touch bars at y=7')
                    check(room.get((horizontal, 6, longitudinal)).name == 'minecraft:iron_bars', 'Incomplete cage wall')
                check(room.get((horizontal, 5, longitudinal)).get('half') == 'top', 'Cage base must touch bars at y=6')
    save(ROOT / 'qa/materials.json', dict(sorted(materials.items())))
    save(ROOT / 'qa/static.json', {
        'status': 'PASS', 'id': 'corridor-M20', 'sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH,
        'size': list(room.size), 'data_version': room.data_version, 'records': len(room.blocks),
        'non_air_records': len(room.blocks) - materials['minecraft:air'], 'palette_states': len(values['palette']),
        'full_volume_explicit': True, 'roof_cells': 520, 'foundation_cells': 520,
        'main_route_feet_cells': 200, 'main_route_width': 5, 'main_route_headroom': 3,
        'north_south_ports': '5 wide × 5 high; full-cube thresholds; both at y=2',
        'entities': 0, 'block_entities': 0, 'inventory_items': 0, 'fluids': 0, 'fire': 0,
        'chain_and_gauge_anchors': 'PASS', 'copper_oxidation': 'all copper waxed',
        'cage_geometry': 'Bottom cage plates end at y=6; bars span y=6..7; bottom-half lids begin at y=7 and touch central iron caps.',
        'forbidden_materials': 0, 'bounds_and_duplicate_positions': 'PASS',
        'scope': 'Static template checks only, not native block-state/collision or Forge stability proof.'
    })
    fixture = Structure((1, 1, 1))
    fixture.set((0, 0, 0), state('air'))
    fixture.write(ROOT / 'src/main/resources/data/ftcorridorm20qa/structures/empty.nbt')
    print(json.dumps({'status': 'PASS', 'size': list(room.size), 'sha256': metadata['sha256'], 'records': len(room.blocks)}))


if __name__ == '__main__':
    main()
