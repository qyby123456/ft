import collections
import hashlib
import json
import sys
import zipfile
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'out/S7'
GAME = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2')
sys.path.insert(0, str(GAME / 'output/dungeon-expansion-20261101/tools'))
from nbtio import AIR, Structure, Tag, compound, integer, plain, read_nbt, state, string

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/S7-cataclysm-dark-iron-cell-hero.png')
EXPECTED_IMAGE = '6b5857d5b06a07ab67343b8faf1fc353802d56e14a24801681ab14ba94c8015e'
SIZE = (9, 9, 9)
ENTRANCE = (4, 3, 0)
INTERIOR = (4, 3, 2)
STATIONS = ((2, 3, 4), (6, 3, 4), (4, 3, 5))
STEPS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
ADAPTATIONS = [
    '七乘七净内室，加一格围墙成为九乘九占地；五格净高不变。底座、封液层与玻璃地面使模板总高为九格。',
    '暗铁材质用磨制黑石砖、錾制磨制黑石、黑曜石及深板岩原创组合；金铆钉用镶金黑石，不复制灾变资产。',
    '封闭铁栅囚笼位于南端，仅为陈设；北接口没有关闭门扇，主通路从刑台两侧绕行。',
    '真实熔岩仅位于地下封闭槽；下有底、四周为石材，上方以完整橙色玻璃隔离，非裸露熔岩路。',
    '红符文用红石块与红色玻璃表达；不添加自定义发光纹理、红石机关、奖励或战斗触发。',
    '锁链上端固定到封顶锚块，可自由悬垂；头骨落在石台或骨块上，黑蜡烛有石座，红旗固定到西墙。',
    '概念图的无门剖切只用于预览；实际 NBT 保留完整墙顶。无骨堆实体、活体囚犯或可领取库存。',
]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def construct():
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (8, 8, 8), AIR)

    def put(position, name, entity=None, **properties):
        structure.set(position, state(name, **properties), entity)

    for block_z in range(9):
        for block_x in range(9):
            put((block_x, 0, block_z), 'polished_blackstone_bricks')
            put((block_x, 1, block_z), 'blackstone')
            floor = 'cracked_polished_blackstone_bricks' if (block_x * 7 + block_z * 13) % 9 < 2 else 'polished_blackstone_bricks'
            put((block_x, 2, block_z), floor)
            put((block_x, 8, block_z), 'deepslate_tiles')
            if block_x in (0, 8) or block_z in (0, 8):
                for block_y in range(3, 8):
                    wall = 'cracked_polished_blackstone_bricks' if (block_x * 3 + block_z + block_y * 5) % 11 == 0 else 'polished_blackstone_bricks'
                    put((block_x, block_y, block_z), wall)

    for block_x, block_z in ((0, 0), (8, 0), (0, 8), (8, 8)):
        for block_y in range(2, 9):
            put((block_x, block_y, block_z), 'gilded_blackstone' if block_y in (2, 5, 8) else 'obsidian')

    lava = {(2, 1, block_z) for block_z in range(2, 6)}
    lava |= {(6, 1, block_z) for block_z in range(2, 6)}
    lava |= {(block_x, 1, 5) for block_x in range(2, 7)}
    for position in sorted(lava):
        put(position, 'lava', level=0)
        put((position[0], 2, position[2]), 'orange_stained_glass')

    for block_x in (2, 6):
        for block_y in range(3, 8):
            put((block_x, block_y, 6), 'gilded_blackstone' if block_y in (3, 6) else 'obsidian')
    for block_x in range(2, 7):
        put((block_x, 7, 6), 'chiseled_polished_blackstone')
    for block_x in range(3, 6):
        for block_y in range(3, 7):
            put((block_x, block_y, 6), 'iron_bars', north=False, south=False, east=True, west=True, waterlogged=False)
    put((4, 5, 8), 'redstone_block')
    put((4, 5, 7), 'red_stained_glass')
    put((4, 3, 7), 'bone_block', axis='y')
    put((4, 4, 7), 'skeleton_skull', compound(id=string('minecraft:skull')), rotation=8)

    for block_x in range(3, 6):
        for block_z in (3, 4):
            put((block_x, 3, block_z), 'gilded_blackstone' if block_x in (3, 5) else 'redstone_block')
            put((block_x, 4, block_z), 'polished_blackstone_slab', type='bottom', waterlogged=False)

    chain_positions = []
    anchors = ((3, 3), (5, 3), (3, 4), (5, 4), (1, 7), (7, 7))
    for block_x, block_z in anchors:
        put((block_x, 8, block_z), 'gilded_blackstone')
        for block_y in range(5, 8):
            position = (block_x, block_y, block_z)
            put(position, 'chain', axis='y', waterlogged=False)
            chain_positions.append(position)

    skull_positions = [(4, 4, 7)]
    for block_x in (1, 7):
        put((block_x, 3, 6), 'chiseled_polished_blackstone')
        put((block_x, 4, 6), 'skeleton_skull', compound(id=string('minecraft:skull')), rotation=6 if block_x == 1 else 10)
        skull_positions.append((block_x, 4, 6))
        put((block_x, 3, 2), 'chiseled_polished_blackstone')
        put((block_x, 4, 2), 'black_candle', candles=2, lit=True, waterlogged=False)
    patterns = Tag(9, [compound(Color=integer(15), Pattern=string('mr')), compound(Color=integer(14), Pattern=string('flo'))], 10)
    put((1, 6, 4), 'red_wall_banner', compound(id=string('minecraft:banner'), Patterns=patterns), facing='east')

    for block_x in range(3, 6):
        for block_y in range(3, 6):
            structure.set((block_x, block_y, 0), AIR)
    for block_x in range(2, 7):
        put((block_x, 6, 0), 'chiseled_polished_blackstone')

    for position, block in list(structure.blocks.items()):
        if block.name != 'minecraft:iron_bars':
            continue
        properties = dict(block.properties)
        for direction, offset in {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0)}.items():
            neighbor = structure.get(tuple(position[index] + offset[index] for index in range(3)))
            properties[direction] = str(neighbor.name.removeprefix('minecraft:') in {'iron_bars', 'obsidian', 'gilded_blackstone', 'red_stained_glass', 'bone_block'}).lower()
        structure.set(position, state('iron_bars', **properties))
    features = {
        'lava_sources': [list(position) for position in sorted(lava)],
        'chains': [list(position) for position in chain_positions],
        'chain_anchors': [[block_x, 8, block_z] for block_x, block_z in anchors],
        'skulls': [list(position) for position in skull_positions],
        'candles': [[1, 4, 2], [7, 4, 2]],
        'banner': [1, 6, 4],
        'cage_bars': [list(position) for position, block in structure.blocks.items() if block.name == 'minecraft:iron_bars'],
        'rack': [[block_x, 3, block_z] for block_x in range(3, 6) for block_z in (3, 4)],
        'red_runes': [[4, 3, 3], [4, 3, 4], [4, 5, 8]],
    }
    return structure, features


def check_static(metadata, features):
    source = OUTPUT / 'room.nbt'
    structure = Structure.read(source)
    _, root = read_nbt(source)
    document = plain(root)
    checks = {}

    def require(name, passed):
        checks[name] = bool(passed)
        if not passed:
            raise AssertionError(name)

    def name(position):
        return structure.get(position).name.removeprefix('minecraft:')

    def clear(position):
        return name(position) == 'air'

    def solid(position):
        return name(position) in {'blackstone', 'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks', 'chiseled_polished_blackstone', 'gilded_blackstone', 'obsidian', 'deepslate_tiles', 'orange_stained_glass', 'red_stained_glass', 'redstone_block', 'bone_block'}

    def walkable(position):
        block_x, block_y, block_z = position
        return all(0 <= coordinate < maximum for coordinate, maximum in zip(position, SIZE)) and clear(position) and clear((block_x, block_y + 1, block_z)) and solid((block_x, block_y - 1, block_z))

    require('reference_sha256', digest(REFERENCE) == EXPECTED_IMAGE)
    require('structure_hash_matches_metadata', digest(source) == metadata['sha256'])
    require('minecraft_1_20_1_single_palette', document['DataVersion'] == 3465 and 'palette' in document and 'palettes' not in document)
    require('size_and_dense_coordinates', structure.size == SIZE and len(structure.blocks) == 9 ** 3)
    require('no_entities', not document['entities'])
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'end_portal', 'nether_portal', 'chest', 'barrel', 'trapped_chest'}
    require('no_forbidden_blocks_or_reward_containers', not any(block.name.removeprefix('minecraft:') in forbidden for block in structure.blocks.values()))
    records = document['blocks']
    require('no_inventory_loot_or_entity_payload', all(not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable') for record in records))
    require('complete_roof_and_bottom', all(solid((block_x, block_y, block_z)) for block_x in range(9) for block_z in range(9) for block_y in (0, 8)))
    doorway = [(block_x, block_y, 0) for block_x in range(3, 6) for block_y in range(3, 6)]
    require('north_three_by_three_no_door_or_projection', all(clear(position) for position in doorway) and all(solid((block_x, 2, 0)) for block_x in range(3, 6)))
    require('enclosed_walls_except_north_interface', all(solid((block_x, block_y, block_z)) for block_x in range(9) for block_z in range(9) if block_x in (0, 8) or block_z in (0, 8) for block_y in range(3, 8) if (block_x, block_y, block_z) not in doorway))
    visited = {ENTRANCE}
    pending = collections.deque([ENTRANCE])
    while pending:
        position = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (position[0] + delta_x, position[1], position[2] + delta_z)
            if neighbor not in visited and walkable(neighbor):
                visited.add(neighbor)
                pending.append(neighbor)
    require('entrance_and_safe_interior_walkable', walkable(ENTRANCE) and walkable(INTERIOR))
    require('bidirectional_safe_route_and_rack_bypass', {INTERIOR, *STATIONS}.issubset(visited))
    lava = {tuple(position) for position in features['lava_sources']}
    require('lava_source_count', len(lava) == 11 and all(name(position) == 'lava' for position in lava))
    require('lava_has_bottom_sides_and_sealed_glass_cover', all(solid(tuple(position[index] + offset[index] for index in range(3))) or tuple(position[index] + offset[index] for index in range(3)) in lava for position in lava for offset in STEPS))
    require('all_seams_beneath_full_glass_blocks', all(name((position[0], position[1] + 1, position[2])) == 'orange_stained_glass' for position in lava))
    require('no_exposed_fire_or_hazard_floor', not any(name(position) in {'fire', 'soul_fire', 'magma_block', 'campfire', 'soul_campfire', 'cobweb', 'powder_snow'} for position in structure.blocks))
    require('skulls_and_candles_have_full_support', all(solid((position[0], position[1] - 1, position[2])) for position in features['skulls'] + features['candles']))
    require('banner_has_wall_support', solid((0, 6, 4)) and name((1, 6, 4)) == 'red_wall_banner')
    require('chains_are_vertical_with_continuous_ceiling_anchors', all(name(tuple(position)) == 'chain' and structure.get(tuple(position)).get('axis') == 'y' and (name((position[0], position[1] + 1, position[2])) == 'chain' or solid((position[0], position[1] + 1, position[2]))) for position in features['chains']))
    require('rack_has_stone_support_and_red_cores', len(features['rack']) == 6 and all(solid(tuple(position)) and solid((position[0], 2, position[2])) for position in features['rack']) and all(name(tuple(position)) == 'redstone_block' for position in features['red_runes']))
    require('cage_is_rear_decoration_not_north_door', len(features['cage_bars']) == 12 and all(position[2] == 6 for position in features['cage_bars']) and all(position[2] < 6 for position in visited))
    client = Path.home() / '.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar'
    with zipfile.ZipFile(client) as assets:
        names = set(assets.namelist())
        require('all_blocks_have_vanilla_1_20_1_assets', all('assets/minecraft/blockstates/' + block.name.removeprefix('minecraft:') + '.json' in names for block in structure.blocks.values()))
    report = {
        'id': 'S7', 'status': 'PASS', 'sha256': metadata['sha256'], 'reference_sha256': EXPECTED_IMAGE,
        'checks': checks, 'block_records': len(records), 'palette_entries': len(document['palette']),
        'reachable_feet_positions': [list(position) for position in sorted(visited)],
        'reachable_positions': len(visited), 'required_stations': [list(position) for position in STATIONS],
        'counts': dict(sorted(collections.Counter(block.name for block in structure.blocks.values()).items())),
        'adaptations': ADAPTATIONS,
        'limits': 'Static NBT/resource and conservative air-only flat-route checks. Native collision/placement/support/fluid checks are separate evidence.',
    }
    write_json(ROOT / 'qa/static-qa.json', report)
    return report


def main():
    if digest(REFERENCE) != EXPECTED_IMAGE:
        raise ValueError('Reference fingerprint mismatch')
    structure, features = construct()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    structure.write(OUTPUT / 'room.nbt')
    metadata = {
        'id': 'S7', 'name': '暗金囚笼', 'size': list(SIZE), 'entrance': list(ENTRANCE),
        'interior': list(INTERIOR), 'facing': 'north', 'sha256': digest(OUTPUT / 'room.nbt'),
        'reference': str(REFERENCE), 'reference_sha256': EXPECTED_IMAGE,
        'template': 'ftdungeonmobs:image25/s7', 'roof': 'enclosed', 'loot': 'none',
        'entities': 0, 'interior_clear_dimensions': [7, 5, 7], 'adaptations': ADAPTATIONS,
    }
    write_json(OUTPUT / 'room.json', metadata)
    write_json(ROOT / 'qa/features.json', features)
    empty = Structure((21, 16, 21))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/s7verification/structures/empty.nbt')
    report = check_static(metadata, features)
    print(json.dumps({'id': 'S7', 'static': report['status'], 'checks': len(report['checks']), 'sha256': metadata['sha256'], 'size': SIZE}, ensure_ascii=False))


if __name__ == '__main__':
    main()
