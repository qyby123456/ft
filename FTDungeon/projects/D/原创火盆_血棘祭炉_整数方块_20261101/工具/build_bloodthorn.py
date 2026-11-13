import argparse
import base64
import copy
import hashlib
import json
import math
import random
import uuid
from collections import Counter
from dataclasses import dataclass, field
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / '运行资源' / 'assets'
TEXTURES = ASSETS / 'ftboss' / 'textures' / 'block'
IDENTIFIER = 'bloodthorn_brazier'
DIRECTIONS = ('north', 'east', 'south', 'west', 'up', 'down')
NAMESPACE = uuid.UUID('6c237d17-b6b9-4ad9-b8a3-8ccf83c3ee51')
TILES = {
    'stone': (0, 0), 'iron': (16, 0), 'blood': (32, 0), 'soot': (48, 0),
    'rim': (0, 16), 'bone': (16, 16), 'skull': (32, 16), 'back_skull': (48, 16),
    'jaw': (0, 32), 'coal': (16, 32), 'side': (32, 32), 'underside': (48, 32),
}
GROUPS = {
    'root': ('血棘祭炉_root', None, [8, 0, 8]),
    'base': ('01_重底座', 'root', [8, 0, 8]),
    'feet': ('四角短足', 'base', [8, 0, 8]),
    'steps': ('两级方台', 'base', [8, 2, 8]),
    'bowl': ('02_黑铁方盆', 'root', [8, 4, 8]),
    'walls': ('盆底与厚壁', 'bowl', [8, 4, 8]),
    'rim': ('方形压边', 'bowl', [8, 9, 8]),
    'relics': ('03_骨饰', 'root', [8, 9, 8]),
    'masks': ('前后方颅面饰', 'relics', [8, 6, 8]),
    'fangs': ('四角阶梯骨齿', 'relics', [8, 10, 8]),
    'embers': ('04_炭床', 'root', [8, 7, 8]),
    'fire': ('05_血焰_仅纹理循环', 'root', [8, 8, 8]),
    'core': ('主焰', 'fire', [8, 8, 8]),
    'forks': ('四向阶梯副焰', 'fire', [8, 9, 8]),
}


def stable_uuid(label):
    return str(uuid.uuid5(NAMESPACE, label))


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


@dataclass
class Cuboid:
    name: str
    start: list
    end: list
    group: str
    material: str
    overrides: dict = field(default_factory=dict)

    @property
    def size(self):
        return [maximum - minimum for minimum, maximum in zip(self.start, self.end)]


def geometry():
    cubes = []

    def add(name, start, end, group, material, **overrides):
        cubes.append(Cuboid(name, start, end, group, material, overrides))

    for label, position in [('NW', [0, 0]), ('NE', [12, 0]), ('SW', [0, 12]), ('SE', [12, 12])]:
        horizontal, depth = position
        add('足_' + label, [horizontal, 0, depth], [horizontal + 4, 2, depth + 4], 'feet', 'stone')
    add('宽底方台', [1, 1, 1], [15, 3, 15], 'steps', 'stone', up='iron')
    add('内收承台', [2, 3, 2], [14, 4, 14], 'steps', 'iron')
    add('厚实盆底', [2, 4, 2], [14, 7, 14], 'walls', 'stone', up='soot', down='underside')
    add('前厚壁', [1, 4, 1], [15, 9, 3], 'walls', 'iron', north='blood', south='soot')
    add('后厚壁', [1, 4, 13], [15, 9, 15], 'walls', 'iron', south='blood', north='soot')
    add('左厚壁', [1, 4, 3], [3, 9, 13], 'walls', 'iron', west='side', east='soot')
    add('右厚壁', [13, 4, 3], [15, 9, 13], 'walls', 'iron', east='side', west='soot')
    add('前压边', [0, 9, 0], [16, 10, 2], 'rim', 'rim')
    add('后压边', [0, 9, 14], [16, 10, 16], 'rim', 'rim')
    add('左压边', [0, 9, 2], [2, 10, 14], 'rim', 'rim')
    add('右压边', [14, 9, 2], [16, 10, 14], 'rim', 'rim')
    add('前颅骨', [5, 5, 0], [11, 9, 1], 'masks', 'bone', north='skull')
    add('前方颌', [6, 3, 0], [10, 5, 1], 'masks', 'bone', north='jaw')
    add('后颅骨', [5, 5, 15], [11, 9, 16], 'masks', 'bone', south='back_skull')
    add('后方颌', [6, 3, 15], [10, 5, 16], 'masks', 'bone', south='jaw')
    for label, horizontal, depth, tip_horizontal, tip_depth in [
        ('NW', 1, 1, 1, 1), ('NE', 13, 1, 14, 1),
        ('SW', 1, 13, 1, 14), ('SE', 13, 13, 14, 14),
    ]:
        add('骨齿根_' + label, [horizontal, 10, depth], [horizontal + 2, 12, depth + 2], 'fangs', 'bone')
        add('骨齿尖_' + label, [tip_horizontal, 12, tip_depth], [tip_horizontal + 1, 13, tip_depth + 1], 'fangs', 'bone')
    for index, (start, end) in enumerate([
        ([4, 7, 4], [7, 9, 7]), ([8, 7, 4], [12, 9, 6]),
        ([9, 7, 9], [12, 9, 12]), ([4, 7, 9], [7, 8, 12]),
        ([7, 7, 7], [9, 9, 9]),
    ]):
        add('方炭_' + str(index + 1), start, end, 'embers', 'coal')
    for name, start, end, group in [
        ('主焰根', [6, 8, 6], [10, 13, 10], 'core'),
        ('主焰冠', [7, 13, 7], [10, 15, 10], 'core'),
        ('主焰尖', [8, 15, 8], [9, 16, 9], 'core'),
        ('左副焰根', [4, 9, 7], [6, 12, 9], 'forks'),
        ('左副焰尖', [4, 12, 7], [5, 14, 8], 'forks'),
        ('右副焰根', [10, 9, 6], [12, 12, 8], 'forks'),
        ('右副焰尖', [11, 12, 6], [12, 13, 7], 'forks'),
        ('后副焰根', [7, 9, 10], [9, 12, 12], 'forks'),
        ('后副焰尖', [7, 12, 11], [8, 14, 12], 'forks'),
        ('前副焰根', [7, 9, 4], [9, 11, 6], 'forks'),
        ('前副焰尖', [7, 11, 4], [8, 12, 5], 'forks'),
    ]:
        add(name, start, end, group, 'fire')
    return cubes


def face_cells(cube, direction):
    axis, sign = {'north': (2, -1), 'south': (2, 1), 'west': (0, -1), 'east': (0, 1), 'up': (1, 1), 'down': (1, -1)}[direction]
    tangent_axes = [candidate for candidate in range(3) if candidate != axis]
    plane = cube.end[axis] if sign > 0 else cube.start[axis]
    for first in range(cube.start[tangent_axes[0]], cube.end[tangent_axes[0]]):
        for second in range(cube.start[tangent_axes[1]], cube.end[tangent_axes[1]]):
            point = [0, 0, 0]
            point[axis] = plane + sign * 0.001
            point[tangent_axes[0]] = first + 0.5
            point[tangent_axes[1]] = second + 0.5
            yield point


def covered(cube, point):
    return all(minimum < coordinate < maximum for minimum, maximum, coordinate in zip(cube.start, cube.end, point))


def hidden_faces(cubes):
    removed = {}
    for cube in cubes:
        removed[cube.name] = []
        for direction in DIRECTIONS:
            if all(any(other is not cube and covered(other, point) for other in cubes) for point in face_cells(cube, direction)):
                removed[cube.name].append(direction)
    return removed


def uv_for(cube, direction, grey=False):
    horizontal, vertical, depth = cube.size
    width, height = {'north': (horizontal, vertical), 'south': (horizontal, vertical), 'west': (depth, vertical), 'east': (depth, vertical), 'up': (horizontal, depth), 'down': (horizontal, depth)}[direction]
    if grey:
        return [0, 0, width, height]
    material = cube.overrides.get(direction, cube.material)
    if material == 'fire':
        if direction in ('north', 'south'):
            return [cube.start[0] * 4, (16 - cube.end[1]) * 4, cube.end[0] * 4, (16 - cube.start[1]) * 4]
        if direction in ('east', 'west'):
            return [cube.start[2] * 4, (16 - cube.end[1]) * 4, cube.end[2] * 4, (16 - cube.start[1]) * 4]
        return [cube.start[0] * 4, cube.start[2] * 4, cube.end[0] * 4, cube.end[2] * 4]
    origin_horizontal, origin_vertical = TILES[material]
    return [origin_horizontal, origin_vertical, origin_horizontal + width, origin_vertical + height]


def make_body():
    generator = random.Random(19920401)
    atlas = Image.new('RGBA', (64, 64), '#24262c')
    palettes = {
        'stone': ['#26272d', '#2b2c32', '#303239', '#34363d'],
        'iron': ['#34343b', '#383840', '#3e3d44', '#424149'],
        'blood': ['#34343b', '#383840', '#3d3d45'],
        'soot': ['#16161b', '#1c1b21', '#232127'],
        'rim': ['#3b3b43', '#414149', '#4a4850'],
        'bone': ['#a99c7e', '#bbae8e', '#c7bb9c'],
        'skull': ['#b4a78a'], 'back_skull': ['#b4a78a'], 'jaw': ['#b4a78a'],
        'coal': ['#1b181e', '#211b22', '#2c2028'],
        'side': ['#34343b', '#383840', '#3d3d45'],
        'underside': ['#24252b', '#292a30'],
    }
    for material, (origin_horizontal, origin_vertical) in TILES.items():
        for vertical in range(16):
            for horizontal in range(16):
                palette = palettes[material]
                color = palette[(horizontal // 3 + vertical // 2) % len(palette)] if material == 'bone' else generator.choice(palette)
                atlas.putpixel((origin_horizontal + horizontal, origin_vertical + vertical), tuple(bytes.fromhex(color[1:])) + (255,))
    drawing = ImageDraw.Draw(atlas)
    for material in ('stone', 'iron', 'rim', 'blood', 'side'):
        origin_horizontal, origin_vertical = TILES[material]
        drawing.line((origin_horizontal, origin_vertical, origin_horizontal + 15, origin_vertical), fill='#65606a' if material == 'rim' else '#4d4b54')
        drawing.line((origin_horizontal, origin_vertical + 1, origin_horizontal + 15, origin_vertical + 1), fill='#45434b' if material == 'rim' else '#383840')
    for material in ('blood', 'side'):
        origin_horizontal, origin_vertical = TILES[material]
        for horizontal, length in [(2, 5), (3, 3), (9, 4), (10, 2), (13, 5)]:
            drawing.line((origin_horizontal + horizontal, origin_vertical, origin_horizontal + horizontal, origin_vertical + length - 1), fill='#601d2a')
            if length >= 4:
                drawing.point((origin_horizontal + horizontal, origin_vertical + 1), fill='#92333d')
        for horizontal, vertical in [(0, 4), (6, 3), (7, 4), (12, 2)]:
            drawing.point((origin_horizontal + horizontal, origin_vertical + vertical), fill='#7e2834')
        for horizontal in (0, 8):
            drawing.point((origin_horizontal + horizontal, origin_vertical + 1), fill='#74717a')
    origin_horizontal, origin_vertical = TILES['rim']
    for horizontal in (2, 3, 9, 13):
        drawing.point((origin_horizontal + horizontal, origin_vertical), fill='#8f303a')
        drawing.point((origin_horizontal + horizontal, origin_vertical + 1), fill='#501a27')
    for material in ('skull', 'back_skull'):
        origin_horizontal, origin_vertical = TILES[material]
        drawing.rectangle((origin_horizontal, origin_vertical, origin_horizontal + 5, origin_vertical + 3), fill='#b9ad8d')
        drawing.line((origin_horizontal, origin_vertical, origin_horizontal + 5, origin_vertical), fill='#ded0aa')
        for horizontal in (0, 4):
            drawing.rectangle((origin_horizontal + horizontal, origin_vertical + 1, origin_horizontal + horizontal + 1, origin_vertical + 2), fill='#241e25')
            drawing.point((origin_horizontal + horizontal + 1, origin_vertical + 2), fill='#5a1428')
        drawing.point((origin_horizontal + 2, origin_vertical + 2), fill='#594139')
        drawing.point((origin_horizontal + 3, origin_vertical + 3), fill='#38252c')
        drawing.point((origin_horizontal, origin_vertical + 3), fill='#7d2532')
    origin_horizontal, origin_vertical = TILES['jaw']
    drawing.rectangle((origin_horizontal, origin_vertical, origin_horizontal + 3, origin_vertical + 1), fill='#a99b7b')
    drawing.point((origin_horizontal + 1, origin_vertical), fill='#3b2630')
    drawing.point((origin_horizontal + 3, origin_vertical), fill='#3b2630')
    drawing.point((origin_horizontal, origin_vertical + 1), fill='#7f2634')
    origin_horizontal, origin_vertical = TILES['coal']
    for horizontal, vertical in [(1, 0), (2, 2), (0, 2), (3, 1), (5, 4), (7, 2), (10, 3)]:
        drawing.point((origin_horizontal + horizontal, origin_vertical + vertical), fill='#8d2836')
    for horizontal, vertical in [(0, 0), (3, 2), (2, 1)]:
        drawing.point((origin_horizontal + horizontal, origin_vertical + vertical), fill='#c25145')
    return atlas


def make_fire():
    strip = Image.new('RGBA', (16, 16 * 16))
    palette = ['#350e20', '#5d1028', '#82132e', '#a01b32', '#c52c3d', '#dc4550', '#ed7863', '#ffd29a']
    for frame in range(16):
        for vertical in range(16):
            for horizontal in range(16):
                phase = math.sin((frame / 16) * math.tau + horizontal * 0.7 + vertical * 0.4)
                heat = 4 - abs(horizontal - 8) // 2 + vertical // 5
                heat -= 1 if vertical < 3 else 0
                heat += 1 if phase > 0.55 else -1 if phase < -0.55 else 0
                if (horizontal * 3 + vertical * 5 + frame) % 13 == 0:
                    heat += 1
                if horizontal == 8 and 4 <= vertical <= 8 and phase > 0.1:
                    heat = 7
                color = palette[max(0, min(7, heat))]
                strip.putpixel((horizontal, frame * 16 + vertical), tuple(bytes.fromhex(color[1:])) + (255,))
    return strip


def texture_record(name, path, texture_index):
    source = 'data:image/png;base64,' + base64.b64encode(path.read_bytes()).decode('ascii')
    with Image.open(path) as image:
        width, height = image.size
    return {
        'path': str(path), 'name': name + '.png', 'folder': 'block', 'namespace': 'ftboss',
        'id': str(texture_index), 'uuid': stable_uuid('texture:' + name),
        'source': source, 'width': width, 'height': height, 'uv_width': 64, 'uv_height': 64,
        'particle': texture_index == 0, 'mode': 'bitmap', 'saved': True,
        'render_mode': 'default', 'render_sides': 'front',
        'frame_time': 2, 'frame_interpolate': False, 'frame_order_type': 'loop',
    }


def outliner(cubes):
    def branch(key):
        name, parent, origin = GROUPS[key]
        children = [branch(child_key) for child_key, data in GROUPS.items() if data[1] == key]
        children.extend(stable_uuid('cube:' + cube.name) for cube in cubes if cube.group == key)
        return {'name': name, 'origin': origin, 'rotation': [0, 0, 0], 'uuid': stable_uuid('group:' + key), 'export': True, 'isOpen': True, 'children': children}
    return [branch('root')]


def build(stage):
    cubes = geometry()
    assert len(cubes) <= 56
    for cube in cubes:
        assert all(type(value) is int and 0 <= value <= 16 for value in cube.start + cube.end)
        assert all(value > 0 for value in cube.size)
    removed = hidden_faces(cubes)
    grey = stage == 'grey'
    TEXTURES.mkdir(parents=True, exist_ok=True)
    if grey:
        grey_path = ROOT / 'work' / 'grey.png'
        Image.new('RGBA', (64, 64), '#a8a4a0').save(grey_path)
        textures = [texture_record('bloodthorn_grey', grey_path, 0)]
    else:
        body_path = TEXTURES / (IDENTIFIER + '.png')
        fire_path = TEXTURES / (IDENTIFIER + '_fire.png')
        make_body().save(body_path)
        make_fire().save(fire_path)
        write_json(fire_path.with_suffix('.png.mcmeta'), {'animation': {'frametime': 2, 'interpolate': False, 'frames': list(range(16))}})
        textures = [texture_record(IDENTIFIER, body_path, 0), texture_record(IDENTIFIER + '_fire', fire_path, 1)]
    elements = []
    runtime_elements = []
    for cube in cubes:
        texture_index = 0 if grey or cube.material != 'fire' else 1
        faces = {}
        runtime_faces = {}
        for direction in DIRECTIONS:
            if direction in removed[cube.name]:
                faces[direction] = {'uv': [0, 0, 0, 0], 'texture': None}
                continue
            uv = uv_for(cube, direction, grey)
            faces[direction] = {'uv': uv, 'texture': texture_index}
            runtime_faces[direction] = {'uv': [value / 4 for value in uv], 'texture': '#' + str(texture_index)}
        elements.append({
            'name': cube.name, 'type': 'cube', 'uuid': stable_uuid('cube:' + cube.name),
            'from': cube.start, 'to': cube.end, 'rotation': [0, 0, 0],
            'origin': GROUPS[cube.group][2], 'box_uv': False, 'autouv': 0,
            'rescale': False, 'locked': False, 'visibility': True,
            'shade': cube.material != 'fire', 'faces': faces, 'color': 1,
        })
        runtime_element = {'name': cube.name, 'from': cube.start, 'to': cube.end, 'faces': runtime_faces}
        if cube.material == 'fire':
            runtime_element['shade'] = False
        runtime_elements.append(runtime_element)
    project = {
        'meta': {'format_version': '5.0', 'model_format': 'java_block', 'box_uv': False},
        'name': '血棘祭炉_' + ('整数灰模' if grey else '暗黑血祭_整数方块'),
        'model_identifier': IDENTIFIER,
        'java_block_version': '1.9.0',
        'resolution': {'width': 64, 'height': 64}, 'elements': elements,
        'outliner': outliner(cubes), 'textures': textures,
        'display': {
            'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.625, 0.625, 0.625]},
            'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.4, 0.4, 0.4]},
            'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [0.75, 0.75, 0.75]},
            'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [0.375, 0.375, 0.375]},
            'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2, 0], 'scale': [0.375, 0.375, 0.375]},
            'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
            'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
        },
    }
    project_path = ROOT / ('work/血棘祭炉_灰模.bbmodel' if grey else '可编辑模型/血棘祭炉_暗黑血祭_整数方块.bbmodel')
    write_json(project_path, project)
    face_count = sum(len(element['faces']) for element in runtime_elements)
    budget = {
        'stage': stage, 'budget': {'cuboids': 56, 'declared_faces': 300, 'materials': 2},
        'actual': {'cuboids': len(cubes), 'declared_faces': face_count, 'removed_hidden_faces': sum(map(len, removed.values())), 'group_count': len(GROUPS)},
        'by_group': dict(Counter(cube.group for cube in cubes)), 'removed_faces': removed,
        'bounds': [[0, 0, 0], [16, 16, 16]], 'unit_grid': 1,
        'geometry_rotation': [0, 0, 0], 'positive_volume_only': True,
        'geometry_animation': False, 'fire_texture_frames': 0 if grey else 16,
        'notes': ['完整面被实心邻件覆盖才删除；部分遮挡面保留，不为减面切碎。', '骨骼为 Blockbench 组织层级，Java 方块导出为静态装配。', 'Display 使用场景缩放，不改变整数模型几何；场景缩放不是几何亚像素。'],
    }
    write_json(ROOT / 'work' / ('cost_' + stage + '.json'), budget)
    if not grey:
        runtime = {
            'credit': 'Original bloodthorn brazier; integer cuboids only', 'texture_size': [64, 64],
            'ambientocclusion': False, 'gui_light': 'front',
            'textures': {'0': 'ftboss:block/' + IDENTIFIER, '1': 'ftboss:block/' + IDENTIFIER + '_fire', 'particle': 'ftboss:block/' + IDENTIFIER},
            'elements': runtime_elements, 'display': copy.deepcopy(project['display']),
        }
        write_json(ASSETS / 'ftboss/models/block' / (IDENTIFIER + '.json'), runtime)
        write_json(ASSETS / 'ftboss/models/item' / (IDENTIFIER + '.json'), {'parent': 'ftboss:block/' + IDENTIFIER})
        write_json(ROOT / '运行资源/pack.mcmeta', {'pack': {'pack_format': 15, 'description': 'Bloodthorn brazier candidate assets | Java 1.20.1 | does not register new blocks/items'}})
    write_json(ROOT / 'work/model_manifest.json', {'project_path': str(project_path), 'root': str(ROOT), 'stage': stage, 'cost': budget['actual']})
    print(json.dumps({'project': str(project_path), 'cost': budget['actual'], 'sha256': hashlib.sha256(project_path.read_bytes()).hexdigest()}, ensure_ascii=False))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--stage', choices=('grey', 'final'), required=True)
    arguments = parser.parse_args()
    build(arguments.stage)
