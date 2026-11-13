import argparse
import hashlib
import importlib.util
import json
import sys
from collections import Counter, deque
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S12-archive-library-damaged.png')
REFERENCE_HASH = '0aaf6c72391d8b31fd4926350422149041b8cbf10a206b2b833672af1bca39dd'
SIZE = (11, 10, 16)
VIEWS = (('north', '北向', (-0.35, -1)), ('south', '南向', (0.35, 1)), ('west', '西向', (-1, 0.35)), ('east', '东向', (1, -0.35)))


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def inspect():
    structure = Structure.read(ROOT / 'room.nbt')
    raw = plain(read_nbt(ROOT / 'room.nbt')[1])
    assert structure.size == SIZE and structure.data_version == 3465
    assert 'palette' in raw and 'palettes' not in raw
    assert len(structure.blocks) == 11 * 10 * 16
    assert not structure.entities
    assert digest(REFERENCE) == REFERENCE_HASH
    counts = Counter(block.name for block in structure.blocks.values())
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'tnt', 'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cobweb', 'powder_snow', 'sand', 'gravel', 'chest', 'barrel', 'hopper', 'dispenser', 'dropper'}
    assert all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') not in forbidden for block in structure.blocks.values())
    for position, block in structure.blocks.items():
        assert all(0 <= position[axis] < SIZE[axis] for axis in range(3))
        name = block.name.removeprefix('minecraft:')
        if name.endswith('_slab'):
            assert block.get('type') in ('bottom', 'top') and block.get('waterlogged') == 'false'
            if block.get('type') == 'bottom' and position[1] == 1:
                assert structure.blocks[(position[0], 0, position[2])].name != 'minecraft:air'
        if name == 'soul_lantern':
            support = (position[0], position[1] + (1 if block.get('hanging') == 'true' else -1), position[2])
            assert structure.blocks[support].name != 'minecraft:air'
        if name == 'chain':
            above = (position[0], position[1] + 1, position[2])
            assert structure.blocks[above].name != 'minecraft:air'
        if name.endswith('_wall_banner'):
            support_x = position[0] + (-1 if block.get('facing') == 'east' else 1)
            assert structure.blocks[(support_x, position[1], position[2])].name == 'minecraft:dark_oak_planks'
    for position, block_entity in structure.block_entities.items():
        data = plain(block_entity)
        assert data == {'id': 'minecraft:banner'} and structure.blocks[position].name == 'minecraft:white_wall_banner'
    assert len(structure.block_entities) == 2
    for blockX in range(SIZE[0]):
        for blockZ in range(SIZE[2]):
            assert structure.blocks[(blockX, 0, blockZ)].name in {'minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:cracked_stone_bricks'}
            assert structure.blocks[(blockX, 9, blockZ)].name in {'minecraft:stone_bricks', 'minecraft:deepslate_tiles'}
    for blockX in range(4, 7):
        for blockZ in range(16):
            for blockY in range(1, 5):
                assert structure.blocks[(blockX, blockY, blockZ)].name == 'minecraft:air'
    solids = {position for position, block in structure.blocks.items() if block.name != 'minecraft:air'}
    connected = {(0, 0, 0)}
    pending = deque(connected)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor in solids and neighbor not in connected:
                connected.add(neighbor)
                pending.append(neighbor)
    assert connected == solids, f'Disconnected decorations: {solids - connected}'
    metadata = {
        'id': 'corridor-S12', 'source_id': 'S12', 'name': '残损档案书库短廊', 'size': list(SIZE),
        'entrance': [5, 1, 0], 'interior': [5, 1, 3], 'exit': [5, 1, 15], 'facing': 'north',
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': '1.20.1', 'data_version': 3465, 'category': 'short_straight_corridor', 'length_blocks': 16,
        'ports': [{'side': 'north', 'feet': [5, 1, 0], 'outward': [0, 0, -1], 'opening': [3, 3]}, {'side': 'south', 'feet': [5, 1, 15], 'outward': [0, 0, 1], 'opening': [3, 3]}],
        'safe_lane': {'minimum': [4, 1, 0], 'maximum': [6, 4, 15], 'width': 3, 'clear_height': 4},
        'builder': {'skill': 'minecraft-builder-skill', 'recipe': str(ROOT / 'builder/recipe.mjs'), 'lodestone_roundtrip': True},
        'design_translation': ['石砖与苔裂石构、四道拱、深橡木框书架、白色无字卷轴壁旗、六盏灵魂灯、靠墙碎石', '参考图的尽端木门改为贯通石拱，保证双端无需交互即可通行', '破拱只缺装饰拱圈，保留与完整上层屋面相连的承重构造；白旗近似羊皮卷'],
        'restrictions': {'deployment': False, 'production_world_modified': False, 'entities': 0, 'inventory_items': 0, 'loot_tables': 0},
    }
    write_json(ROOT / 'room.json', metadata)
    report = {
        'id': metadata['id'], 'status': 'PASS', 'sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH,
        'size': list(SIZE), 'data_version': 3465, 'single_palette': True, 'full_volume_records': len(structure.blocks),
        'solid_records': len(solids), 'palette_states': len(raw['palette']), 'bounds_and_unique_positions': 'PASS',
        'ports_3x3_supported': 'PASS', 'straight_clear_lane_3x4x16': 'PASS', 'floor_cells': 176, 'roof_cells': 176,
        'all_non_air_blocks_connected_to_floor': True, 'hanging_and_standing_lantern_support': 'PASS',
        'banner_wall_support': 'PASS', 'unsafe_blocks': 0, 'fluids': 0, 'containers': 0, 'inventory_items': 0,
        'loot_tables': 0, 'entities': 0, 'block_entities': 2, 'block_counts': dict(sorted(counts.items())),
        'scope': 'Static NBT, geometry and support checks; collision/state stability is checked separately by isolated Forge GameTest.',
    }
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'static': 'PASS', 'sha256': metadata['sha256'], 'blocks': report['full_volume_records'], 'counts': report['block_counts']}, ensure_ascii=False))
    return structure, metadata


def render(structure, metadata):
    source = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py')
    specification = importlib.util.spec_from_file_location('shared_fourview', source)
    views = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(views)

    def filtered(current, current_metadata, camera, exterior):
        visible = {}
        for position, block in current.blocks.items():
            if block.name.removeprefix('minecraft:') in views.INVISIBLE:
                continue
            blockX, blockY, blockZ = position
            if not exterior and blockY >= 1:
                if blockY >= 8:
                    continue
                if (blockX <= 1 if camera[0] < 0 else blockX >= 9):
                    continue
                if (blockZ == 0 if camera[1] < 0 else blockZ == 15):
                    continue
            visible[position] = block
        return visible

    views.filtered = filtered
    output = ROOT / 'views'
    output.mkdir(parents=True, exist_ok=True)
    files = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2160), '#101a20')
        for index, (direction, title, camera) in enumerate(VIEWS):
            image = views.render(structure, metadata, camera, title, exterior)
            target = output / f'{mode}-{direction}.png'
            image.save(target)
            files.append(str(target))
            sheet.paste(image, ((index % 2) * 1400, (index // 2) * 1080))
        target = output / f'{mode}-four-views.png'
        sheet.save(target)
        files.append(str(target))
    hero = views.render(structure, metadata, (-0.45, -1), '北口内部剖视 / 16 格短直廊', False, size=(1680, 1260))
    hero.save(ROOT / 'preview.png')
    write_json(ROOT / 'qa/render-manifest.json', {
        'status': 'PASS', 'sha256': metadata['sha256'], 'source': str(ROOT / 'room.nbt'), 'views': files,
        'preview': str(ROOT / 'preview.png'), 'renderer': 'NBT-driven 1.20.1 client model/texture software render',
        'cutaway_only_in_preview': True, 'source_nbt_unchanged': digest(ROOT / 'room.nbt') == metadata['sha256'],
        'limitations': ['软件视图而非游戏截图；壁旗为平面近似，不模拟布料。', '日间技术照明，不等价于整合包实机的魂灯光照效果。'],
    })
    print('Rendered eight views and two sheets from final room.nbt', flush=True)


def finalize():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    renders = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    reports = (static, native, visual, renders)
    assert all(report['status'] == 'PASS' and report['sha256'] == metadata['sha256'] for report in reports)
    assert digest(ROOT / 'room.nbt') == metadata['sha256']
    write_json(ROOT / 'qa.json', {
        'id': metadata['id'], 'status': 'PASS', 'sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH,
        'static': 'qa/static.json', 'four_direction_interior_exterior': 'qa/render-manifest.json',
        'visual_review': 'qa/visual-review.json', 'forge_placement_and_stability': 'qa/forge-native.json',
        'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations': 4, 'ticks_observed': 240,
        'ready_for_coordinator_review': True, 'deployed': False, 'production_world_modified': False,
        'client_gameplay_tested': False, 'limits': ['尚未接入大厅或正式实例；只有独立临时 Forge GameTest 世界。', '最终统一接入前仍需交接线程按相同哈希独立复验。'],
    })
    print('Delivery QA finalized: PASS ' + metadata['sha256'])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--render', action='store_true')
    parser.add_argument('--finalize', action='store_true')
    args = parser.parse_args()
    if args.finalize:
        finalize()
        return
    structure, metadata = inspect()
    if args.render:
        render(structure, metadata)


if __name__ == '__main__':
    main()
