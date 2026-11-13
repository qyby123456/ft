import argparse
import collections
import hashlib
import json
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S09-webbed-spider-burnt.png')
REFERENCE_HASH = 'eeb80b6a2cf8e64e3ec92740ed43119b714b7b9e72d671c339484cfd886d6c26'
AIR_NAMES = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
FULL_BLOCKS = {'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'andesite',
               'polished_andesite', 'chiseled_stone_bricks', 'coal_block', 'cracked_deepslate_bricks',
               'deepslate_tiles', 'cracked_deepslate_tiles', 'polished_basalt',
               'polished_blackstone_bricks', 'shroomlight', 'bone_block', 'mushroom_stem', 'iron_block'}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def prepare():
    metadata = {
        'id': 'corridor-S09',
        'concept_id': 'S09',
        'name': '焚痕蛛巢短廊',
        'size': [11, 9, 16],
        'entrance': [5, 1, 0],
        'interior': [5, 1, 2],
        'exit': [5, 1, 15],
        'facing': 'north',
        'axis': 'z',
        'ports': [
            {'facing': 'north', 'feet': [5, 1, 0], 'width': 3, 'height': 3},
            {'facing': 'south', 'feet': [5, 1, 15], 'width': 3, 'height': 3},
        ],
        'protected_route': {'minimum': [4, 1, 0], 'maximum': [6, 3, 15], 'clearance': [3, 3, 16]},
        'sha256': digest(ROOT / 'room.nbt'),
        'reference': str(REFERENCE),
        'reference_sha256': REFERENCE_HASH,
        'data_version': 3465,
        'minecraft': 'Java 1.20.1',
        'format': 'gzip single-palette vanilla structure NBT; explicit air records',
        'builder': 'minecraft-builder-skill / Lodestone run-build.mjs',
        'recipe': 'source/build-s09.mjs',
        'rebuild_options': ['--name', 'room', '--data-version', '3465', '--no-block-state-updates'],
        'block_state_note': 'Iron-bar connections are explicitly authored against full solid neighbours; automatic Lodestone connections must remain disabled for this recipe.',
        'length_class': 'short / one chunk (16 blocks)',
        'features': ['4 suspended iron cages', '3 soul lanterns', '4 ivory egg-sac representations',
                     '6 skeleton skulls', 'off-route cobwebs', 'soot patches', '4 non-damaging ember lights'],
        'design_adaptations': [
            'Reference closed end door is an open 3x3 arch for north-south through travel.',
            'Egg sacs use mushroom stems and sandstone caps; no custom blocks or entities.',
            'Charred beams use nonflammable basalt. Ember niches use shroomlights, not fire or magma.',
            'Cages are static decoration; no spawner, scripted trap or loot.',
        ],
        'placement': {'world_slot': None, 'origin_is_floor_y': 0, 'player_feet_y': 1,
                      'validate_entire_volume_before_placement': True,
                      'do_not_overwrite_occupied_volume': True,
                      'integration_owner_assigns_slot': True},
        'deployed': False,
        'player_saves_modified': False,
        'paid_image_generation_invoked': False,
    }
    write_json(ROOT / 'room.json', metadata)


def static_qa():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    structure = Structure.read(ROOT / 'room.nbt')
    _, raw = read_nbt(ROOT / 'room.nbt')
    blocks = structure.blocks
    checks = []

    def check(name, condition, detail):
        checks.append({'name': name, 'status': 'PASS' if condition else 'FAIL', 'detail': detail})

    def short(position):
        return structure.get(position).name.removeprefix('minecraft:')

    def full(position):
        return short(position) in FULL_BLOCKS

    def walkable(position):
        block_x, block_y, block_z = position
        return (blocks.get(position, AIR).name in AIR_NAMES
                and blocks.get((block_x, block_y + 1, block_z), AIR).name in AIR_NAMES
                and full((block_x, block_y - 1, block_z)))

    def reachable(start):
        visited = {start}
        pending = collections.deque([start])
        while pending:
            block_x, block_y, block_z = pending.popleft()
            for offset_x, offset_z in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                target = (block_x + offset_x, block_y, block_z + offset_z)
                if 0 <= target[0] < 11 and 0 <= target[2] < 16 and target not in visited and walkable(target):
                    visited.add(target)
                    pending.append(target)
        return visited

    counts = collections.Counter(block.name for block in blocks.values())
    palette = {block for block in blocks.values()}
    check('identity_and_sha256', metadata['id'] == 'corridor-S09' and metadata['sha256'] == digest(ROOT / 'room.nbt'), metadata['sha256'])
    check('reference_fingerprint', digest(REFERENCE) == REFERENCE_HASH, REFERENCE_HASH)
    with Image.open(REFERENCE) as image:
        check('reference_image_readable', image.size == (1536, 1024), list(image.size))
        image.verify()
    check('short_straight_dimensions', structure.size == (11, 9, 16) and metadata['size'] == [11, 9, 16], list(structure.size))
    check('gzip_single_palette_java_1_20_1', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b'
          and structure.data_version == 3465 and 'palette' in raw.value and 'palettes' not in raw.value, {'data_version': structure.data_version})
    check('explicit_complete_volume', len(blocks) == 1584 and all(all(0 <= coordinate < limit for coordinate, limit in zip(position, structure.size)) for position in blocks), len(blocks))
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void',
                 'jigsaw', 'spawner', 'tnt', 'chest', 'trapped_chest', 'barrel', 'dropper', 'dispenser', 'hopper'}
    check('vanilla_only_no_forbidden_blocks', all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') not in forbidden for block in palette), len(palette))
    hazards = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'campfire', 'soul_campfire', 'powder_snow', 'wither_rose', 'sweet_berry_bush'}
    check('no_fire_liquids_or_damage_blocks', all(block.name.removeprefix('minecraft:') not in hazards and block.get('waterlogged', 'false') == 'false' for block in palette), 'No real fire, liquids, waterlogging, magma or powder snow')
    check('no_entities_inventory_or_loot', not structure.entities and all(plain(tag) == {'id': 'minecraft:skull'} for tag in structure.block_entities.values()), {'entities': len(structure.entities), 'skull_block_entities': len(structure.block_entities), 'containers': 0, 'loot_tables': 0})
    route = [(block_x, block_y, block_z) for block_x in range(4, 7) for block_y in range(1, 4) for block_z in range(16)]
    check('three_wide_three_high_full_length_route', all(blocks[position].name in AIR_NAMES for position in route), {'air_cells': len(route), 'feet_positions': 48})
    forward = reachable((5, 1, 0))
    reverse = reachable((5, 1, 15))
    check('bidirectional_walk_and_safety_point', (5, 1, 15) in forward and (5, 1, 2) in forward
          and forward == reverse and all((block_x, 1, block_z) in forward for block_x in range(4, 7) for block_z in range(16)), {'reachable_feet_positions': len(forward)})
    check('continuous_full_floor_and_roof', all(full((block_x, block_y, block_z)) for block_x in range(11) for block_z in range(16) for block_y in [0, 8]), {'floor': 176, 'roof': 176})
    check('no_side_branches_or_shell_gaps', all(full((block_x, block_y, block_z)) for block_x in [0, 10] for block_y in range(1, 8) for block_z in range(16)), 'Only north/south openings')
    check('anchored_chains_and_lanterns', all(block.get('axis') == 'y' and full((position[0], position[1] + 1, position[2])) for position, block in blocks.items() if block.name == 'minecraft:chain')
          and all(block.get('hanging') == 'true' and short((position[0], position[1] + 1, position[2])) == 'chain' for position, block in blocks.items() if block.name == 'minecraft:soul_lantern'), {'chains': counts['minecraft:chain'], 'lanterns': counts['minecraft:soul_lantern']})
    cage_valid = True
    for start_x in [2, 7]:
        for start_z in [3, 11]:
            anchor_x = 3 if start_x == 2 else 7
            anchor_z = 3 if start_z == 3 else 12
            cage_valid &= short((anchor_x, 6, anchor_z)) == 'chain' and full((anchor_x, 7, anchor_z))
            for block_x in range(start_x, start_x + 2):
                for block_z in range(start_z, start_z + 2):
                    state = structure.get((block_x, 2, block_z))
                    cage_valid &= state.name == 'minecraft:iron_trapdoor' and state.get('half') == 'top' and state.get('open') == 'false' and state.get('powered') == 'false'
                    cage_valid &= short((block_x, 5, block_z)) == 'iron_block'
                    for block_y in [3, 4]:
                        cage_valid &= short((block_x, block_y, block_z)) == 'iron_bars'
    check('four_complete_suspended_cages', cage_valid and counts['minecraft:iron_trapdoor'] == 16 and counts['minecraft:iron_block'] == 16 and counts['minecraft:iron_bars'] == 36, 'Four 2x2 cages; top-half base meets bars, full iron lid meets bars and chain, no fractional gaps')
    check('bone_piles_and_wall_egg_supports', all(full((position[0], position[1] - 1, position[2])) for position, block in blocks.items() if block.name == 'minecraft:skeleton_skull')
          and all(full((1 if position[0] == 2 else 9, position[1], position[2])) for position, block in blocks.items() if block.name == 'minecraft:mushroom_stem'), {'skulls': counts['minecraft:skeleton_skull'], 'egg_sacs': counts['minecraft:mushroom_stem']})
    check('reference_theme_features', counts['minecraft:cobweb'] > 40 and counts['minecraft:coal_block'] > 0
          and counts['minecraft:shroomlight'] == 4 and counts['minecraft:mushroom_stem'] == 4
          and counts['minecraft:soul_lantern'] == 3 and counts['minecraft:skeleton_skull'] == 6, dict(sorted(counts.items())))
    report = {'id': metadata['id'], 'status': 'PASS' if all(entry['status'] == 'PASS' for entry in checks) else 'FAIL',
              'sha256': digest(ROOT / 'room.nbt'), 'reference_sha256': REFERENCE_HASH,
              'checks': checks, 'palette_states': len(palette), 'block_counts': dict(sorted(counts.items())),
              'limits': ['Static walk check is conservative air/full-block geometry; native Forge report covers actual collision and state survival.']}
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'static': report['status'], 'failed': [entry['name'] for entry in checks if entry['status'] != 'PASS'],
                      'sha256': report['sha256'], 'cobwebs': counts['minecraft:cobweb'], 'reachable': len(forward)}))
    if report['status'] != 'PASS':
        raise SystemExit(1)


def finalize():
    fingerprint = digest(ROOT / 'room.nbt')
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8'))
    views = json.loads((ROOT / 'views/manifest.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    valid = all(report['status'] == 'PASS' and report['sha256'] == fingerprint for report in [static, native, visual])
    valid &= views['sha256'] == fingerprint and len(views['views']) == 8
    for evidence in views['views']:
        evidence_path = ROOT / evidence['path']
        valid &= digest(evidence_path) == evidence['image_sha256']
        with Image.open(evidence_path) as image:
            image.verify()
    valid &= visual['preview_sha256'] == digest(ROOT / 'preview.png')
    valid &= all(entry['states_preserved'] == 1584 and entry['item_drops'] == 0 for entry in native['rotations'])
    valid &= len(native['rotations']) == 4 and native['ticks_observed'] >= 240
    report = {'id': 'corridor-S09', 'status': 'PASS' if valid else 'FAIL', 'sha256': fingerprint,
              'reference_sha256': REFERENCE_HASH, 'size': [11, 9, 16],
              'static_qa': {'status': static['status'], 'path': 'qa/static.json'},
              'four_direction_interior_and_exterior_views': {'status': visual['status'], 'count': 8, 'manifest': 'views/manifest.json', 'review': 'qa/visual-review.json'},
              'forge_native_placement_stability': {'status': native['status'], 'rotations': 4, 'ticks': native['ticks_observed'], 'path': 'qa/forge-native.json', 'log': 'qa/forge-run.log'},
              'preview': {'path': 'preview.png', 'sha256': digest(ROOT / 'preview.png'), 'kind': 'software render of final NBT, not a game screenshot or AI image'},
              'route': '3 wide x 3 high x 16 long; north entrance to south exit; no cobweb/fire/liquid crossing',
              'ready_for_independent_integration_review': bool(valid), 'slot_assigned': False, 'deployed': False,
              'player_saves_modified': False, 'production_jar_modified': False,
              'full_modpack_client_tested': False,
              'limits': ['Forge evidence is an isolated 1.20.1/47.4.10 GameTest, not full-modpack client/FPS/multiplayer acceptance.',
                         'No core catalog, permanent location, hall connector or gameplay logic added.']}
    write_json(ROOT / 'qa.json', report)
    print(json.dumps({'aggregate_qa': report['status'], 'sha256': fingerprint}))
    if not valid:
        raise SystemExit(1)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--prepare', action='store_true')
    parser.add_argument('--finalize', action='store_true')
    arguments = parser.parse_args()
    if arguments.prepare:
        prepare()
    static_qa()
    if arguments.finalize:
        finalize()


if __name__ == '__main__':
    main()
