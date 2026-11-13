import collections
import hashlib
import json
import sys
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
from nbtio import Structure, plain, read_nbt


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    reference = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S13-blood-ritual-aged.png')
    reference_sha = '91a5ac1e968b27eaab5641524e3923b5dff855fd885d1cf84237b5c46bc0e8c4'
    assert digest(reference) == reference_sha
    with Image.open(reference) as image:
        image.load()
        assert image.size == (1536, 1024)
    room = Structure.read(ROOT / 'room.nbt')
    raw = plain(read_nbt(ROOT / 'room.nbt')[1])
    metadata = {
        'id': 'corridor-S13', 'name': '陈旧血誓祭仪短廊', 'size': list(room.size),
        'entrance': [7, 1, 0], 'interior': [7, 1, 3], 'exit': [7, 1, 15],
        'exit_interior': [7, 1, 12], 'facing': 'north', 'exit_facing': 'south',
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(reference),
        'reference_sha256': reference_sha, 'minecraft': '1.20.1', 'data_version': 3465,
        'type': 'straight_corridor', 'length_blocks': 16, 'length_chunks': 1,
        'main_aisle': {'x': [6, 8], 'feet_y': 1, 'clear_height': 3, 'z': [0, 15]},
        'template_candidate': 'ftdungeonmobs:image25/corridor_s13',
        'source_recipe': 'source/corridor-s13.mjs', 'builder': 'minecraft-builder-skill / Lodestone 0.6.0',
        'deployment_status': 'not_deployed', 'integration_status': 'producer_delivery_only',
        'design_adaptations': [
            '概念图封闭栅门改为升起的门栅，北南均保留至少三宽三高开口。',
            '血痕与血槽以实心红色地砖表现；无流体、伤害、红石或刷怪机制。',
            '仅原版1.20.1：末地烛光为原生白光，不伪造青色动态光照。',
            '四座吊笼为空装饰，铁盖中央承重点、铁链与木梁相接；无实体或容器。',
        ],
    }
    save(ROOT / 'room.json', metadata)
    checks = []

    def require(condition, label):
        if not condition:
            raise AssertionError(label)
        checks.append(label)

    air = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
    forbidden = {'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light'}
    fluids = {'water', 'lava', 'bubble_column'}
    hazards = {'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'sweet_berry_bush', 'wither_rose'}
    names = collections.Counter(block.name for block in room.blocks.values())
    require(room.size == (15, 11, 16), 'short_16_block_long_axis')
    require(room.data_version == 3465 and 'palette' in raw and 'palettes' not in raw, 'single_palette_java_1_20_1')
    require(not room.entities, 'no_template_entities')
    require(len(room.blocks) == 15 * 11 * 16, 'entire_volume_explicit_unique_in_bounds')
    require(all(all(0 <= coordinate < room.size[axis] for axis, coordinate in enumerate(position)) for position in room.blocks), 'bounds')
    require(all(name.startswith('minecraft:') for name in names), 'vanilla_namespace')
    require(not any(name.split(':')[1] in forbidden | fluids | hazards for name in names), 'no_executable_blocks_free_fluids_or_damage_blocks')
    for pos_x in range(15):
        for pos_z in range(16):
            require(room.get((pos_x, 0, pos_z)).name not in air, 'continuous_floor_240_blocks')
            require(room.get((pos_x, 10, pos_z)).name == 'minecraft:deepslate_tiles', 'complete_roof_240_blocks')
    for pos_x in range(6, 9):
        for pos_z in range(16):
            for pos_y in range(1, 4):
                require(room.get((pos_x, pos_y, pos_z)).name in air, 'three_wide_three_high_level_through_route')
    for endpoint in (metadata['entrance'], metadata['interior'], metadata['exit'], metadata['exit_interior']):
        require(room.get(tuple(endpoint)).name in air and room.get((endpoint[0], endpoint[1] + 1, endpoint[2])).name in air, 'safe_points_clear')
    require(names['minecraft:red_wall_banner'] == 8 and len(room.block_entities) == 8, 'eight_banner_block_entities_only')
    for position, tag in room.block_entities.items():
        value = plain(tag)
        require(value.get('id') == 'minecraft:banner' and len(value.get('Patterns', [])) == 2, 'banner_patterns_present')
        require(not any(key in value for key in ('Items', 'Item', 'LootTable', 'LootTableSeed')), 'no_loot_or_inventory')
        require(position[0] in (1, 13), 'wall_attached_banner_locations')
    cages = []
    for center_x in (3, 11):
        for center_z in (4, 11):
            cages.append({'chain': [center_x, 8, center_z], 'anchor': [center_x, 9, center_z], 'cap': [center_x, 7, center_z], 'base_center': [center_x, 4, center_z]})
            require(room.get((center_x, 8, center_z)).name == 'minecraft:chain', 'four_cage_chains')
            require(room.get((center_x, 9, center_z)).name == 'minecraft:dark_oak_log', 'cage_chain_to_continuous_beam')
            require(room.get((center_x, 7, center_z)).name == 'minecraft:iron_block', 'cage_cap_to_chain')
    candles = [list(position) for position, block in room.blocks.items() if block.name == 'minecraft:candle']
    rods = [list(position) for position, block in room.blocks.items() if block.name == 'minecraft:end_rod']
    for position in candles:
        require(room.get((position[0], position[1] - 1, position[2])).name not in air, 'all_candles_supported')
    for position in rods:
        require(room.get((position[0], position[1] - 1, position[2])).name == 'minecraft:chiseled_stone_bricks', 'all_rods_have_masonry_brackets')
    require(names['minecraft:cobweb'] == 10, 'cobwebs_confined_to_side_and_ceiling_corners')
    blueprint = {'cages': cages, 'candles': candles, 'end_rods': rods, 'banners': [list(position) for position in room.block_entities], 'pillar_z': [0, 4, 8, 11, 15], 'main_aisle': [[pos_x, 1, pos_z] for pos_x in range(6, 9) for pos_z in range(16)]}
    save(ROOT / 'qa/blueprint.json', blueprint)
    unique_checks = list(dict.fromkeys(checks))
    report = {'status': 'PASS', 'id': metadata['id'], 'nbt_sha256': metadata['sha256'], 'reference_sha256': reference_sha, 'size': list(room.size), 'records': len(room.blocks), 'non_air': sum(count for name, count in names.items() if name not in air), 'palette_states': len(raw['palette']), 'block_entities': len(room.block_entities), 'inventory_containers': 0, 'free_fluids': 0, 'check_count': len(unique_checks), 'checks': unique_checks, 'materials': dict(sorted(names.items())), 'scope': 'Static topology, bounds, fingerprints and declared physical anchors; not a substitute for native Forge validation.'}
    save(ROOT / 'qa/static.json', report)
    print(json.dumps({'static': 'PASS', 'nbt_sha256': metadata['sha256'], 'checks': len(unique_checks), 'records': len(room.blocks)}))


if __name__ == '__main__':
    main()
