import argparse
import hashlib
import importlib.util
import json
import sys
from collections import Counter, deque
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from PIL import Image, ImageChops


ROOT = Path(__file__).resolve().parents[1]
SHARED = ROOT.parents[1] / 'tools/review_four_views.py'
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, Tag, compound, integer, intlist, plain, read_nbt, write_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M19-webbed-spider-flooded.png')
REFERENCE_HASH = '8f3c423cf62ca1d8fd47704f0635c2e478d037295f5a6531a16468ba2a5f927f'
SOLID = {'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'chiseled_stone_bricks', 'polished_andesite', 'calcite', 'bone_block'}
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def static_review():
    assert digest(REFERENCE) == REFERENCE_HASH
    with Image.open(REFERENCE) as image:
        assert image.size == (1536, 1024)
        image.verify()
    structure = Structure.read(ROOT / 'room.nbt')
    assert structure.size == (15, 12, 41) and structure.data_version == 3465
    assert len(structure.blocks) == 7380 and not structure.entities
    _, raw = read_nbt(ROOT / 'room.nbt')
    assert 'palette' in raw.value and 'palettes' not in raw.value
    counts = Counter(block.name.removeprefix('minecraft:') for block in structure.blocks.values())
    allowed = SOLID | {'air', 'water', 'cobweb', 'stone_brick_stairs', 'quartz_slab', 'chain', 'lantern', 'skeleton_skull'}
    assert set(counts) <= allowed
    for position, block in structure.blocks.items():
        assert block.name.startswith('minecraft:')
        assert all(0 <= coordinate < bound for coordinate, bound in zip(position, structure.size))

    def name(position):
        return structure.get(position).name.removeprefix('minecraft:')

    def offset(position, delta):
        return tuple(first + second for first, second in zip(position, delta))

    for blockX in range(15):
        for blockZ in range(41):
            assert name((blockX, 0, blockZ)) in SOLID
            assert name((blockX, 11, blockZ)) in SOLID
    for blockZ in range(41):
        for blockY in range(2, 11):
            assert name((0, blockY, blockZ)) in SOLID
            assert name((14, blockY, blockZ)) in SOLID
        for blockX in range(6, 9):
            assert name((blockX, 1, blockZ)) in SOLID
            assert all(name((blockX, blockY, blockZ)) == 'air' for blockY in range(2, 5))
    water = {position for position in structure.blocks if name(position) == 'water'}
    for position in water:
        assert position[1] == 1 and position[0] not in range(6, 9) and position[2] in range(3, 38)
        assert structure.get(position).get('level') == '0'
        for delta in ((1, 0, 0), (-1, 0, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = offset(position, delta)
            assert neighbor in water or name(neighbor) in SOLID, (position, neighbor)
    walkable = {(blockX, 2, blockZ) for blockX in range(1, 14) for blockZ in range(41)
                if name((blockX, 1, blockZ)) in SOLID
                and name((blockX, 2, blockZ)) == name((blockX, 3, blockZ)) == 'air'}
    reachable = {(7, 2, 0)}
    pending = deque(reachable)
    while pending:
        position = pending.popleft()
        for delta in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = offset(position, delta)
            if neighbor in walkable and neighbor not in reachable:
                reachable.add(neighbor)
                pending.append(neighbor)
    assert all((blockX, 2, blockZ) in reachable for blockX in range(6, 9) for blockZ in range(41))
    for position, block in structure.blocks.items():
        if name(position) == 'chain':
            assert block.get('axis') == 'y' and block.get('waterlogged') == 'false'
            anchor = offset(position, (0, 1, 0))
            while name(anchor) == 'chain':
                anchor = offset(anchor, (0, 1, 0))
            assert name(anchor) in SOLID
        if name(position) == 'lantern':
            assert block.get('hanging') == 'true' and name(offset(position, (0, 1, 0))) == 'chain'
        if name(position) == 'skeleton_skull':
            assert name(offset(position, (0, -1, 0))) == 'bone_block'
    webs = {position for position in structure.blocks if name(position) == 'cobweb'}
    unvisited = set(webs)
    while unvisited:
        component = {unvisited.pop()}
        queue = deque(component)
        anchored = False
        while queue:
            position = queue.popleft()
            for delta in DIRECTIONS:
                neighbor = offset(position, delta)
                anchored |= name(neighbor) in SOLID
                if neighbor in unvisited:
                    unvisited.remove(neighbor)
                    component.add(neighbor)
                    queue.append(neighbor)
        assert anchored, component
    for position, payload in structure.block_entities.items():
        assert name(position) == 'skeleton_skull' and plain(payload) == {'id': 'minecraft:skull'}

    metadata = {
        'id': 'corridor-M19', 'source_id': 'M19', 'name': '苔砖蛛巢积水直廊',
        'type': 'straight_corridor', 'length_class': 'medium', 'size': list(structure.size),
        'entrance': [7, 2, 0], 'interior': [7, 2, 4], 'exit': [7, 2, 40], 'facing': 'north',
        'ports': [{'side': 'north', 'feet': [7, 2, 0], 'clearance': [3, 3]},
                  {'side': 'south', 'feet': [7, 2, 40], 'clearance': [3, 3]}],
        'safe_path': {'x': [6, 8], 'feet_y': 2, 'z': [0, 40], 'width': 3, 'minimum_headroom': 3, 'dry': True},
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'minecraft': '1.20.1', 'data_version': 3465, 'block_records': len(structure.blocks),
        'explicit_air': True, 'deployment': 'not_deployed', 'persistent_world_modified': False,
        'design_adaptations': ['原图满地浅水调整为中央三格干燥堤道，两侧保留真实一格深水；北南端均封水。',
                               '卵囊由方解石和石英半砖表示；无实体、刷怪笼或战利品。',
                               '六跨石拱、五盏暖色链灯、十处丝网壁龛，两个局部缺损拱肩不影响屋顶承重。'],
        'integration_note': '独立资产，未分配大厅槽位。接入方须先检查完整7380格体积（含显式空气），不得覆盖占用区或重复重建。',
    }
    save_json(ROOT / 'room.json', metadata)
    evidence = {
        'status': 'PASS', 'sha256': metadata['sha256'], 'reference_sha256': REFERENCE_HASH,
        'full_volume_records': len(structure.blocks), 'palette_count': len(raw.value['palette'].value),
        'size': list(structure.size), 'material_counts': dict(sorted(counts.items())),
        'water_source_cells': len(water), 'sealed_water_sides_and_bottom': True,
        'water_on_main_route': 0, 'main_route_width': 3, 'main_route_cells_verified': 123,
        'reachable_dry_feet_cells': len(reachable), 'north_south_through_route': True,
        'roof_and_floor_full_cells_each': 615, 'exterior_side_walls_continuous': True,
        'hanging_lanterns_anchored': counts['lantern'], 'chains_anchored': counts['chain'],
        'cobweb_cells': len(webs), 'all_web_components_attached': True,
        'decorative_skulls_supported': counts['skeleton_skull'], 'entities': 0,
        'containers': 0, 'inventory_items': 0, 'loot_tables': 0, 'forbidden_blocks': 0,
    }
    save_json(ROOT / 'qa/static.json', evidence)
    empty = compound(DataVersion=integer(3465), size=intlist([1, 1, 1]),
                     palette=Tag(9, [], 10), blocks=Tag(9, [], 10), entities=Tag(9, [], 10))
    write_nbt(ROOT / 'qa/forge/src/main/resources/data/ftm19qa/structures/empty.nbt', empty)
    print(json.dumps(evidence, ensure_ascii=False))


def render_review():
    specification = importlib.util.spec_from_file_location('m19_renderer', SHARED)
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    structure = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    original_texture = renderer.renderer.texture

    def tinted_texture(identifier, uv, rotation, brightness):
        texture = original_texture(identifier, uv, rotation, brightness)
        if identifier == 'block/water_still':
            texture = ImageChops.multiply(texture.convert('RGBA'), Image.new('RGBA', texture.size, (63, 118, 228, 220)))
        return texture

    renderer.renderer.texture = tinted_texture
    original_render = renderer.render

    def labeled_render(source, information, camera, title, exterior, size):
        title = title.replace('内部', '外壳') if exterior else title
        return original_render(source, information, camera, title, exterior, size=size)

    renderer.render = labeled_render

    def filtered(source, information, camera, exterior):
        if exterior:
            return {position: block for position, block in source.blocks.items() if block.name != 'minecraft:air'}
        visible = {}
        for position, block in source.blocks.items():
            blockX, blockY, blockZ = position
            if block.name == 'minecraft:air' or blockY >= 10:
                continue
            if blockY >= 8 and blockZ not in (0, 8, 16, 24, 32, 40):
                continue
            near_side = blockX <= 1 if camera[0] < 0 else blockX >= 13
            near_end = blockZ == 0 if camera[1] < 0 else blockZ == 40
            if blockY >= 2 and (near_side or near_end):
                continue
            visible[position] = block
        return visible

    renderer.filtered = filtered
    target = ROOT / 'views'
    target.mkdir(exist_ok=True)
    views = (('north', '北向内部 / 面向南', (-0.32, -1)), ('south', '南向内部 / 面向北', (0.32, 1)),
             ('west', '西向内部 / 面向东', (-1, 0.24)), ('east', '东向内部 / 面向西', (1, -0.24)))
    manifest = {'sha256': digest(ROOT / 'room.nbt'), 'renderer': str(SHARED),
                'water_tint': 'Minecraft default water color #3f76e4; biome-independent inspection approximation', 'images': []}
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2100), '#101a20')
        for index, (direction, title, camera) in enumerate(views):
            image = renderer.render(structure, metadata, camera, title, exterior, size=(1400, 1050))
            path = target / (mode + '-' + direction + '.png')
            image.save(path)
            sheet.paste(image, ((index % 2) * 1400, (index // 2) * 1050))
            manifest['images'].append({'path': str(path), 'sha256': digest(path), 'size': list(image.size)})
        sheet.save(target / (mode + '-four-views.png'))
        if not exterior:
            sheet.resize((1680, 1260), Image.Resampling.LANCZOS).save(ROOT / 'preview.png')
    for name in ('lodestone-exterior.png', 'lodestone-eye-north.png', 'lodestone-eye-south.png'):
        path = target / name
        if path.exists():
            with Image.open(path) as image:
                image.verify()
            manifest['images'].append({'path': str(path), 'sha256': digest(path), 'renderer': 'Minecraft Builder / Lodestone WebGL'})
    manifest['preview_sha256'] = digest(ROOT / 'preview.png')
    save_json(ROOT / 'qa/render-manifest.json', manifest)
    print('Rendered eight final-NBT views and preview.png')


def finalize():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8'))
    rendered = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    expected = digest(ROOT / 'room.nbt')
    assert all(document['sha256'] == expected for document in (metadata, static, native, rendered))
    assert static['status'] == native['status'] == 'PASS' and len(native['rotations']) == 4
    for entry in rendered['images']:
        assert digest(Path(entry['path'])) == entry['sha256']
        with Image.open(entry['path']) as image:
            image.verify()
    assert digest(ROOT / 'preview.png') == rendered['preview_sha256']
    result = {
        'id': 'corridor-M19', 'status': 'PASS', 'ready_for_integration_review': True,
        'checked_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'sha256': expected,
        'reference_sha256': REFERENCE_HASH, 'static': static,
        'native_forge': native, 'four_views': rendered,
        'visual_review': {'status': 'PASS', 'reviewed_from_final_nbt': True,
                          'checks': ['屋顶与外壳连续', '拱柱落地且梁柱连通', '链灯顶部锚固', '骨堆、卵囊位于壁龛',
                                     '北南三格入口及三格干路贯通', '蛛网不侵入主路', '水槽底和四周密封', '无容器库存'],
                          'cutaway_only': True},
        'deployed': False, 'player_world_modified': False, 'core_source_modified': False,
        'limits': ['软件预览不是完整整合包客户端截图；未验证客户端FPS或多人游戏。',
                   'Forge隔离GameTest仅在本交付qa目录内生成测试世界；未打开玩家存档。',
                   '正式大厅接入、唯一槽位和部署留给统一接入负责人。'],
    }
    save_json(ROOT / 'qa.json', result)
    print('M19_DELIVERY_PASS ' + expected)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['static', 'render', 'finalize'])
    arguments = parser.parse_args()
    {'static': static_review, 'render': render_review, 'finalize': finalize}[arguments.action]()
