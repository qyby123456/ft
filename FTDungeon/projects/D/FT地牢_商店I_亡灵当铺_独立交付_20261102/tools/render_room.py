import copy
import functools
import hashlib
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont

from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
ASSETS = None
STEPS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}
INVISIBLE = {'air', 'cave_air', 'void_air'}
PARTIAL = {'chest', 'skeleton_skull', 'dark_oak_wall_sign', 'flower_pot', 'potted_fern', 'candle', 'yellow_candle', 'brewing_stand', 'lantern', 'soul_lantern', 'chain', 'red_wall_banner', 'glass', 'glass_pane', 'iron_bars', 'iron_trapdoor', 'soul_campfire', 'anvil', 'grindstone', 'tripwire_hook', 'amethyst_cluster'}


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


@functools.lru_cache(maxsize=None)
def asset(path):
    return json.loads(ASSETS.read('assets/minecraft/' + path))


@functools.lru_cache(maxsize=None)
def model(identifier):
    raw = asset('models/' + identifier.removeprefix('minecraft:') + '.json')
    parent = raw.get('parent', '')
    result = copy.deepcopy(model(parent)) if parent and not parent.startswith('builtin/') else {'textures': {}}
    result['textures'].update(raw.get('textures', {}))
    if 'elements' in raw:
        result['elements'] = raw['elements']
    return result


def matches(condition, properties):
    if 'OR' in condition:
        return any(matches(child, properties) for child in condition['OR'])
    if 'AND' in condition:
        return all(matches(child, properties) for child in condition['AND'])
    return all(properties.get(key) in str(value).split('|') for key, value in condition.items())


def box(start, end, texture):
    return {'textures': {}, 'elements': [{'from': start, 'to': end, 'faces': {face: {'texture': texture} for face in STEPS}}]}


def definitions(name, properties):
    if name == 'red_wall_banner':
        definition = box([1, -12, 14], [15, 12, 15], 'block/red_wool')
        definition['elements'].extend(box([1, -12, 13.8], [15, -9, 14], 'block/black_wool')['elements'])
        definition['elements'].extend(box([0, 12, 13], [16, 14, 16], 'block/dark_oak_planks')['elements'])
        for element in definition['elements']:
            for surface in element['faces'].values():
                surface['uv'] = [0, 0, 16, 16]
        return [(definition, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    if name == 'chest':
        definition = box([1, 0, 1], [15, 14, 15], 'entity/chest/normal')
        regions = {'north': [14, 33, 28, 43], 'south': [42, 33, 56, 43], 'west': [0, 33, 14, 43], 'east': [28, 33, 42, 43], 'up': [14, 0, 28, 14], 'down': [28, 0, 42, 14]}
        definition['elements'][0]['faces'] = {face: {'texture': 'entity/chest/normal', 'uv': [value / 4 for value in bounds]} for face, bounds in regions.items()}
        definition['elements'].extend(box([7, 6, 0], [9, 10, 1], 'block/gold_block')['elements'])
        return [(definition, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    if name == 'skeleton_skull':
        definition = box([4, 0, 4], [12, 8, 12], 'entity/skeleton/skeleton')
        regions = {'north': [2, 4, 4, 8], 'south': [6, 4, 8, 8], 'west': [0, 4, 2, 8], 'east': [4, 4, 6, 8], 'up': [2, 0, 4, 4], 'down': [4, 0, 6, 4]}
        definition['elements'][0]['faces'] = {face: {'texture': 'entity/skeleton/skeleton', 'uv': bounds} for face, bounds in regions.items()}
        return [(definition, {'y': int(properties['rotation']) * 22.5})]
    if name == 'dark_oak_wall_sign':
        return [(box([0, 4, 14], [16, 12, 15], 'block/dark_oak_planks'), {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    blockstate = asset('blockstates/' + name + '.json')
    choices = []
    if 'variants' in blockstate:
        for variant, value in blockstate['variants'].items():
            condition = dict(entry.split('=', 1) for entry in variant.split(',') if entry)
            if matches(condition, properties):
                choices = [value[0] if isinstance(value, list) else value]
                break
        if not choices:
            raise ValueError(f'Unmatched blockstate {name}: {properties}')
    else:
        for part in blockstate.get('multipart', []):
            if 'when' not in part or matches(part['when'], properties):
                value = part['apply']
                choices.append(value[0] if isinstance(value, list) else value)
    return [(model(choice['model']), choice) for choice in choices]


def vertices(element, face):
    left, bottom, front = element['from']
    right, top, back = element['to']
    return {
        'north': [(left, top, front), (right, top, front), (right, bottom, front), (left, bottom, front)],
        'west': [(left, top, back), (left, top, front), (left, bottom, front), (left, bottom, back)],
        'up': [(left, top, back), (right, top, back), (right, top, front), (left, top, front)],
        'south': [(right, top, back), (left, top, back), (left, bottom, back), (right, bottom, back)],
        'east': [(right, top, front), (right, top, back), (right, bottom, back), (right, bottom, front)],
        'down': [(left, bottom, front), (right, bottom, front), (right, bottom, back), (left, bottom, back)],
    }[face]


def rotate(position, axis, angle, origin=(8, 8, 8)):
    centered = [position[index] - origin[index] for index in range(3)]
    first, second = {'x': (1, 2), 'y': (0, 2), 'z': (0, 1)}[axis]
    cosine, sine = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    centered[first], centered[second] = centered[first] * cosine - centered[second] * sine, centered[first] * sine + centered[second] * cosine
    return tuple(centered[index] + origin[index] for index in range(3))


def default_uv(element, face):
    left, bottom, front = element['from']
    right, top, back = element['to']
    return {'north': [16 - right, 16 - top, 16 - left, 16 - bottom], 'south': [left, 16 - top, right, 16 - bottom], 'west': [front, 16 - top, back, 16 - bottom], 'east': [16 - back, 16 - top, 16 - front, 16 - bottom], 'up': [left, front, right, back], 'down': [left, 16 - back, right, 16 - front]}[face]


@functools.lru_cache(maxsize=None)
def geometry(name, properties_tuple):
    result = []
    for definition, choice in definitions(name, dict(properties_tuple)):
        for element in definition.get('elements', []):
            for face, surface in element['faces'].items():
                points = vertices(element, face)
                if 'rotation' in element:
                    options = element['rotation']
                    points = [rotate(point, options['axis'], options['angle'], options['origin']) for point in points]
                points = [rotate(rotate(point, 'x', -choice.get('x', 0)), 'y', choice.get('y', 0)) for point in points]
                first = [points[1][axis] - points[0][axis] for axis in range(3)]
                second = [points[2][axis] - points[1][axis] for axis in range(3)]
                normal = (first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0])
                length = math.sqrt(sum(value * value for value in normal))
                if length < 0.00001:
                    continue
                normal = tuple(value / length for value in normal)
                cull = None
                if 'cullface' in surface:
                    cull = rotate(rotate(STEPS[surface['cullface']], 'x', -choice.get('x', 0), (0, 0, 0)), 'y', choice.get('y', 0), (0, 0, 0))
                    cull = tuple(round(value) for value in cull)
                identifier = surface['texture']
                while identifier.startswith('#'):
                    identifier = definition['textures'][identifier[1:]]
                result.append(([tuple(value / 16 for value in point) for point in points], normal, cull, identifier.removeprefix('minecraft:'), tuple(surface.get('uv', default_uv(element, face))), surface.get('rotation', 0)))
    return result


@functools.lru_cache(maxsize=None)
def texture(identifier, bounds, rotation, brightness):
    image = Image.open(io.BytesIO(ASSETS.read('assets/minecraft/textures/' + identifier + '.png'))).convert('RGBA')
    if identifier.startswith('block/') and image.height > image.width:
        image = image.crop((0, 0, image.width, image.width))
    left, top, right, bottom = (bounds[0] * image.width / 16, bounds[1] * image.height / 16, bounds[2] * image.width / 16, bounds[3] * image.height / 16)
    image = image.crop((min(left, right), min(top, bottom), max(left, right), max(top, bottom)))
    if right < left:
        image = image.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if bottom < top:
        image = image.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    image = image.resize((48, 48), Image.Resampling.NEAREST).rotate(-rotation)
    return ImageEnhance.Brightness(image).enhance(brightness)


def warp(canvas, image, points):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    width = math.ceil(max(point[0] for point in points)) - minimum_x + 1
    height = math.ceil(max(point[1] for point in points)) - minimum_y + 1
    anchor_x, anchor_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    first_x, first_y = ((points[1][axis] - points[0][axis]) / image.width for axis in range(2))
    second_x, second_y = ((points[3][axis] - points[0][axis]) / image.height for axis in range(2))
    determinant = first_x * second_y - second_x * first_y
    if abs(determinant) < 0.00001 or width <= 0 or height <= 0:
        return
    coefficients = (second_y / determinant, -second_x / determinant, (-second_y * anchor_x + second_x * anchor_y) / determinant, -first_y / determinant, first_x / determinant, (first_y * anchor_x - first_x * anchor_y) / determinant)
    canvas.alpha_composite(image.transform((width, height), Image.Transform.AFFINE, coefficients, Image.Resampling.NEAREST), (minimum_x, minimum_y))


def opaque(block):
    return block is not None and block.name.removeprefix('minecraft:') not in INVISIBLE | PARTIAL and not block.name.endswith('_slab')


def render(room, camera, label, exterior=False, size=(1600, 1250), native=False):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.86
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = {}
    for position, block in room.blocks.items():
        if block.name.removeprefix('minecraft:') in INVISIBLE:
            continue
        pos_x, pos_y, pos_z = position
        corner = pos_x in (0, room.size[0] - 1) and pos_z in (0, room.size[2] - 1)
        near_x = pos_x == (0 if camera_x < 0 else room.size[0] - 1)
        near_z = pos_z == (0 if camera_z < 0 else room.size[2] - 1)
        if not exterior and (pos_y == room.size[1] - 1 or pos_y >= 1 and (near_x or near_z) and not corner):
            continue
        if not exterior and pos_y == 6 and 0 < pos_x < room.size[0] - 1 and pos_z in (3, 8):
            continue
        visible[position] = block

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return -camera_z * point[0] + camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    faces = []
    for position, block in visible.items():
        name = block.name.removeprefix('minecraft:')
        for points, normal, cull, identifier, bounds, rotation in geometry(name, block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            order = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / 4
            brightness = 1.28 if normal[1] > 0.5 else 1.05 if normal[0] < 0 else 1.16
            faces.append((order, [project(point) for point in world], identifier, bounds, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 120) / (maximum_x - minimum_x), (size[1] - 205) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 135 + (size[1] - 205) / 2 - (minimum_y + maximum_y) * scale / 2
    image = Image.new('RGBA', size, '#121d23')
    for order, points, identifier, bounds, rotation, brightness in sorted(faces, key=lambda entry: entry[0]):
        coordinates = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        warp(image, texture(identifier, bounds, rotation, brightness), coordinates)
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'I · 亡灵当铺  /  ' + label, font=font(32), fill='#efe4cc')
    source = '原生世界回采 NBT' if native else '最终交付 NBT'
    draw.text((35, 76), source + ' · 13×8×11 · ' + ('完整屋顶外壳' if exterior else '屋顶、内横梁与近侧墙仅作预览剖切；四角柱保留'), font=font(20), fill='#9fc9bd')
    draw.text((35, size[1] - 42), '软件材质预览，非游戏截图；箱子 / 头骨 / 旗帜 / 告示牌为近似实体外观，无光照模拟。', font=font(18), fill='#9aa9af')
    return image.convert('RGB')


def plan(room, blueprint):
    image = Image.new('RGB', (1280, 1150), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((38, 25), '空间逻辑 / 实际方块坐标平面', font=font(32), fill='#efe4cc')
    draw.text((38, 80), '北 ↑  |  玩家脚部 Y=1  |  所有尺寸单位为方块', font=font(20), fill='#9fc9bd')
    cell, left, top = 60, 100, 170
    features = blueprint['features']
    categories = {}
    colors = {'counter': '#a67541', 'shelves': '#6e563c', 'barrels': '#b29359', 'pillars': '#a4b5ae', 'cage_base': '#6b818c', 'display_base': '#3b9297', 'brazier_base': '#4bb5bd', 'brazier_guard': '#466d79', 'weapon_rack': '#747382'}
    letters = {'counter': '柜', 'shelves': '架', 'barrels': '骨', 'pillars': '柱', 'cage_base': '笼', 'display_base': '展', 'brazier_base': '火', 'brazier_guard': '栏', 'weapon_rack': '械'}
    for name in colors:
        for pos_x, pos_y, pos_z in features[name]:
            if pos_y == 1:
                categories[(pos_x, pos_z)] = name
    ring = {(position[0], position[2]) for position in blueprint['counter_clearance']}
    for pos_z in range(11):
        for pos_x in range(13):
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            block = room.get((pos_x, 1, pos_z))
            category = categories.get((pos_x, pos_z))
            fill = colors.get(category, '#293b41' if block.name != 'minecraft:air' else '#354b4f')
            if 5 <= pos_x <= 7 and 0 <= pos_z <= 5:
                fill = '#32626a'
            draw.rectangle(rectangle, fill=fill)
            if (pos_x, pos_z) in ring:
                draw.rectangle(rectangle, outline='#7ecba5', width=3)
            if category:
                draw.text((rectangle[0] + 20, rectangle[1] + 17), letters[category], font=font(23), fill='#f8ecd4')
            elif pos_z == 0 and 5 <= pos_x <= 7:
                draw.text((rectangle[0] + 22, rectangle[1] + 17), '入', font=font(23), fill='#cbf6ee')
    for coordinate in range(13):
        draw.text((left + coordinate * cell + 24, top - 32), str(coordinate), font=font(18), fill='#95b0b3')
        if coordinate < 11:
            draw.text((left - 35, top + coordinate * cell + 20), str(coordinate), font=font(18), fill='#95b0b3')
    draw.text((940, 185), 'X →', font=font(23), fill='#95b0b3')
    draw.text((940, 235), 'Z ↓', font=font(23), fill='#95b0b3')
    draw.text((940, 330), '3 格净宽', font=font(28), fill='#9fddd1')
    draw.text((940, 380), '3 格净高', font=font(28), fill='#9fddd1')
    draw.text((940, 480), '绿色边框', font=font(22), fill='#7ecba5')
    draw.text((940, 520), '柜台四周 ≥1 格', font=font(22), fill='#7ecba5')
    draw.text((940, 610), '吊灯投影', font=font(22), fill='#e7c97c')
    draw.text((940, 650), '(5,3) / (7,3)', font=font(20), fill='#e7c97c')
    for pos_x in (5, 7):
        center_x, center_y = left + (pos_x + 0.5) * cell, top + 3.5 * cell
        draw.ellipse((center_x - 8, center_y - 8, center_x + 8, center_y + 8), fill='#f4cb65', outline='#ffe5a9', width=2)
    draw.text((40, 900), '柜台：X=4…8 / Z=6   ·   安全内点：(6,1,3)   ·   北入口：(6,1,0)', font=font(23), fill='#efe4cc')
    draw.text((40, 960), '左笼 / 右展柜 / 后墙圣物架 / 西墙武器架；四角骨桶；火盆前设铁栏。', font=font(22), fill='#a4bab9')
    draw.text((40, 1020), '从实际 NBT 读取；无实体、库存与交易。灵魂营火有石基和隔栏；非裸火。', font=font(20), fill='#a4bab9')
    return image


def main():
    global ASSETS
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    ASSETS = zipfile.ZipFile(CLIENT)
    preview = ROOT / 'preview'
    views = [('north', '北侧 / 门口方向', (-0.7, -1)), ('south', '南侧 / 圣物架方向', (0.7, 1)), ('west', '西侧 / 铁笼与武器架', (-1, 0.6)), ('east', '东侧 / 展柜与暗红旗', (1, -0.6))]
    panels = []
    for name, label, camera in views:
        image = render(room, camera, label)
        image.save(preview / (name + '.png'))
        panels.append(image)
    panels[0].save(ROOT / 'out/preview.png')
    four = Image.new('RGB', (3200, 2500), '#121d23')
    for index, image in enumerate(panels):
        four.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
    four.save(preview / 'four-views.png')
    exterior = render(room, (-0.7, -1), '北侧外壳 / 完整屋顶', exterior=True)
    exterior.save(preview / 'exterior.png')
    topdown = plan(room, blueprint)
    topdown.save(preview / 'plan.png')
    sheet = Image.new('RGB', (2480, 1650), '#0e171c')
    draw = ImageDraw.Draw(sheet)
    draw.text((50, 32), 'I · 亡灵当铺  ·  可回滚独立结构', font=font(44), fill='#f0e5cd')
    draw.text((50, 100), 'SHOP_I_UNDEAD_PAWN（暂定） / 原版 1.20.1 / 13×8×11 / 北侧三格接口 / 未接入核心与正式存档', font=font(26), fill='#99c3b7')
    sheet.paste(panels[0].resize((1472, 1150), Image.Resampling.LANCZOS), (12, 174))
    sheet.paste(topdown.resize((930, 835), Image.Resampling.LANCZOS), (1515, 210))
    draw.text((1540, 1090), '① 四柱嵌墙角，家具不穿柱', font=font(25), fill='#e8d6ac')
    draw.text((1540, 1140), '② 独立柜台，四周至少一格', font=font(25), fill='#e8d6ac')
    draw.text((1540, 1190), '③ 双灯链接顶梁（梁仅预览隐藏）', font=font(25), fill='#e8d6ac')
    draw.text((1540, 1240), '④ 空容器，无奖励与预放实体', font=font(25), fill='#e8d6ac')
    draw.line((50, 1410, 2430, 1410), fill='#3a5357', width=2)
    draw.text((50, 1450), '结构数据渲染，不是 AI 成品替代或游戏截图。最终模板屋顶完整；剖切只作用于预览。', font=font(29), fill='#b6c7c4')
    draw.text((50, 1510), '商店陈设已落块；交易、店主、FT 大厅道路和正式槽位属于后续协调接入，不在本次交付中伪造。', font=font(25), fill='#99aeb0')
    sheet.save(preview / 'delivery-sheet.png')
    captures = []
    native_path = ROOT / 'qa/native-placed-none.nbt'
    if native_path.exists():
        native_room = Structure.read(native_path)
        render(native_room, views[0][2], '原生放置 100 tick 后回采', native=True).save(preview / 'native-world-capture.png')
        captures.append({'nbt': 'qa/native-placed-none.nbt', 'sha256': hashlib.sha256(native_path.read_bytes()).hexdigest(), 'preview': 'preview/native-world-capture.png'})
    manifest = {'source_nbt_sha256': hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest(), 'source': 'Actual delivered block states; vanilla client.jar models and textures', 'client_jar': str(CLIENT), 'client_jar_sha256': hashlib.sha256(CLIENT.read_bytes()).hexdigest(), 'synthetic_entity_appearances': ['chest', 'skeleton_skull', 'red_wall_banner (red cloth and black stripe approximation)', 'dark_oak_wall_sign (text not rendered)'], 'limitations': ['Software orthographic render; no client shader, light simulation, biome tint, flame animation or player screenshot.', 'Cutaway removes roof, internal ceiling crossbeams and near exterior walls in memory only; original NBT is never modified.'], 'native_captures': captures}
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
