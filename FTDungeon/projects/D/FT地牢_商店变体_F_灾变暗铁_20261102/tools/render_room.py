import copy
import functools
import hashlib
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont

from nbtio import Structure, plain


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
ASSETS = None
BANNER_IMAGE = None
STEPS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}
INVISIBLE = {'air', 'cave_air', 'void_air'}
PARTIAL = {'chest', 'skeleton_skull', 'dark_oak_wall_sign', 'flower_pot', 'potted_fern', 'potted_crimson_fungus', 'potted_warped_fungus', 'candle', 'yellow_candle', 'red_candle', 'brewing_stand', 'lantern', 'chain', 'glass', 'lava', 'black_wall_banner', 'amethyst_cluster'}


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
    if name == 'lava':
        return [(box([0, 0, 0], [16, 14.2, 16], 'block/lava_still'), {})]
    if name == 'black_wall_banner':
        definition = box([2, -13, 14], [14, 15, 15], 'custom/banner')
        for surface in definition['elements'][0]['faces'].values():
            surface['uv'] = [0, 0, 16, 16]
        definition['elements'].extend(box([0, 14, 14], [16, 16, 16], 'block/polished_blackstone')['elements'])
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
        return [(definition, {'y': 180 - int(properties['rotation']) * 22.5})]
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
    image = BANNER_IMAGE.copy() if identifier == 'custom/banner' else Image.open(io.BytesIO(ASSETS.read('assets/minecraft/textures/' + identifier + '.png'))).convert('RGBA')
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
    return block is not None and block.name.removeprefix('minecraft:') not in INVISIBLE | PARTIAL and not block.name.endswith(('_slab', '_stairs'))


def render(room, camera, label, exterior=False, size=(1600, 1250), native=False):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.58
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = {}
    for position, block in room.blocks.items():
        if block.name.removeprefix('minecraft:') in INVISIBLE:
            continue
        pos_x, pos_y, pos_z = position
        corner = pos_x in (0, 16) and pos_z in (0, 16)
        near_x = pos_x == (0 if camera_x < 0 else 16)
        near_z = pos_z == (0 if camera_z < 0 else 16)
        if not exterior and (pos_y == 10 or pos_y >= 1 and (near_x or near_z) and not corner):
            continue
        visible[position] = block

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

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
    image = Image.new('RGBA', size, '#141218')
    for order, points, identifier, bounds, rotation, brightness in sorted(faces, key=lambda entry: entry[0]):
        coordinates = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        warp(image, texture(identifier, bounds, rotation, brightness), coordinates)
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'F 灾变暗铁商店  /  ' + label, font=font(32), fill='#efe4cc')
    source = '原生世界回采 NBT' if native else '最终交付 NBT'
    draw.text((35, 76), source + ' · 17×11×17 · ' + ('完整屋顶外壳' if exterior else '仅预览剖开屋顶及近侧墙，保留墙柱、拱肋与梁'), font=font(20), fill='#d5ae72')
    draw.text((35, size[1] - 42), '实际 NBT 软件材质预览，非游戏截图；旗帜/头骨/箱体/液面外观近似，不模拟光照。', font=font(18), fill='#9aa9af')
    return image.convert('RGB')


def banner_texture(room):
    all_patterns = [plain(payload).get('Patterns') for position, payload in room.block_entities.items() if room.get(position).name == 'minecraft:black_wall_banner']
    if not all_patterns or any(patterns != all_patterns[0] for patterns in all_patterns):
        raise ValueError('Renderer expects identical actual banner payloads')
    result = Image.new('RGBA', (20, 40), '#1d1d21')
    pattern_names = {'mr': 'rhombus', 'cs': 'stripe_center', 'bo': 'border'}
    dye_colors = {4: '#fed83d', 14: '#b02e26'}
    for entry in all_patterns[0]:
        texture_name = pattern_names[entry['Pattern']]
        pattern = Image.open(io.BytesIO(ASSETS.read('assets/minecraft/textures/entity/banner/' + texture_name + '.png'))).convert('RGBA').crop((1, 1, 21, 41))
        color = Image.new('RGBA', result.size, dye_colors[entry['Color']])
        result.paste(color, (0, 0), pattern.getchannel('A'))
    return result


def plan(room, blueprint):
    image = Image.new('RGB', (1500, 1250), '#141218')
    draw = ImageDraw.Draw(image)
    draw.text((40, 28), 'F / 赤契铁窖 · 实际坐标动线平面', font=font(34), fill='#f0dfc2')
    draw.text((40, 85), '北 ↑   17×11×17   脚部 Y=1 / 柜台四侧至少一格净空', font=font(23), fill='#d5ae72')
    cell, left, top = 54, 100, 185
    features = blueprint['features']
    colors = {'pillars': '#7a7b85', 'shelves': '#806c4a', 'counter': '#bf9645', 'display_bases': '#786596', 'empty_storage': '#98734e'}
    letters = {'pillars': '柱', 'shelves': '架', 'counter': '柜', 'display_bases': '展', 'empty_storage': '箱'}
    categories = {}
    for category in colors:
        for pos_x, pos_y, pos_z in features[category]:
            if pos_y == 1:
                categories[(pos_x, pos_z)] = category
    main_aisle = {(position[0], position[2]) for position in blueprint['entrance_to_counter_aisle']}
    ring = {(position[0], position[2]) for position in blueprint['counter_clearance']}
    lava_cells = {(cell['source'][0], cell['source'][2]) for cell in blueprint['lava_cells']}
    for pos_z in range(17):
        for pos_x in range(17):
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            fill = '#303039' if room.get((pos_x, 1, pos_z)).name == 'minecraft:air' else '#1c1c23'
            if room.get((pos_x, 0, pos_z)).name == 'minecraft:red_terracotta':
                fill = '#8d3f41'
            if room.get((pos_x, 0, pos_z)).name == 'minecraft:gilded_blackstone':
                fill = '#665136'
            category = categories.get((pos_x, pos_z))
            label = letters[category] if category else ''
            if category:
                fill = colors[category]
            if (pos_x, pos_z) in lava_cells:
                fill, label = '#f77c32', '熔'
            draw.rectangle(rectangle, fill=fill)
            if (pos_x, pos_z) in main_aisle | ring:
                draw.rectangle(rectangle, outline='#65b1a6', width=3)
            if label:
                draw.text((rectangle[0] + 12, rectangle[1] + 10), label, font=font(23), fill='#fff0d4')
    for index in range(17):
        draw.text((left + index * cell + 13, top - 35), str(index), font=font(18), fill='#b6acbd')
        draw.text((left - 40, top + index * cell + 13), str(index), font=font(18), fill='#b6acbd')
    for lamp in blueprint['lamp_supports']:
        pos_x, _, pos_z = lamp['lantern']
        center = (left + (pos_x + 0.5) * cell, top + (pos_z + 0.5) * cell)
        draw.ellipse((center[0] - 13, center[1] - 13, center[0] + 13, center[1] + 13), outline='#ffd77f', width=3)
    notes = ['绿框：主路 / 绕柜环路', '橙格：墙内封闭熔岩', '灰格：墙线嵌入承重柱', '紫格：落地封闭玻璃展柜', '金格：九格独立柜台', '棕格：贴实墙货架 / 空箱', '黄圈：通道上方悬灯', '', '入口脚点 (8,1,0)', '安全点 (8,1,5)', '店主预留 (8,1,12)', '不生成角色或奖励', '', '两源熔岩每源六面封闭', '玻璃屏障不可穿越', '最终 NBT 屋顶完整']
    for index, line in enumerate(notes):
        draw.text((1060, 190 + index * 46), line, font=font(19), fill='#d9c7ad')
    draw.text((100, 1150), '显示来自最终 NBT 的占地与精确蓝图；非概念图替代。墙体熔岩只在 Y=2，位于后墙厚度内。', font=font(21), fill='#a99aaa')
    return image


def main():
    global ASSETS, BANNER_IMAGE
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    ASSETS = zipfile.ZipFile(CLIENT)
    BANNER_IMAGE = banner_texture(room)
    preview = ROOT / 'preview'
    preview.mkdir(exist_ok=True)
    cameras = [('north', '北 / 正面交易区', (-0.7, -1)), ('south', '南 / 柜后与北门', (0.7, 1)), ('west', '西 / 柜台侧向净空', (-1, 0.6)), ('east', '东 / 展柜与封闭炉芯', (1, -0.6))]
    panels = []
    for name, label, camera in cameras:
        panel = render(room, camera, label)
        panel.save(preview / (name + '.png'))
        panels.append(panel)
    panels[0].save(ROOT / 'out/preview.png')
    four = Image.new('RGB', (3200, 2500), '#141218')
    for index, panel in enumerate(panels):
        four.paste(panel, ((index % 2) * 1600, (index // 2) * 1250))
    four.save(preview / 'four-views.png')
    render(room, (-0.7, -1), '完整屋顶 / 北向 3×3 接口', exterior=True).save(preview / 'exterior.png')
    plan(room, blueprint).save(preview / 'plan.png')
    captures = []
    native_path = ROOT / 'qa/native-placed-none.nbt'
    if native_path.exists():
        native_room = Structure.read(native_path)
        render(native_room, cameras[0][2], '原生放置稳定 200 tick 后回采', native=True).save(preview / 'native-world-capture.png')
        captures.append({'nbt': 'qa/native-placed-none.nbt', 'sha256': hashlib.sha256(native_path.read_bytes()).hexdigest(), 'preview': 'preview/native-world-capture.png'})
    comparison = Image.new('RGB', (2600, 1280), '#141218')
    draw = ImageDraw.Draw(comparison)
    draw.text((45, 30), 'F · 设计转译核对 / 概念语言保留，安全结构重新落块', font=font(38), fill='#f0dfc2')
    concept = Image.open(ROOT / 'reference/shop-variant-F-cataclysm-iron.png').convert('RGB')
    concept.thumbnail((1200, 900), Image.Resampling.LANCZOS)
    comparison.paste(concept, (35, 190))
    comparison.paste(panels[0].resize((1330, 1039), Image.Resampling.LANCZOS), (1250, 130))
    draw.text((50, 1060), '原图：已存在的 AI 设计图（未新增付费生成）', font=font(24), fill='#cfaa73')
    draw.text((50, 1130), '转译：黑石暗铁 / 金饰拱肋 / 红符文 / 双展柜 / 空货架', font=font(23), fill='#e1d0b7')
    draw.text((50, 1180), '调整：完整屋顶、墙线柱、独立柜台净空、灯链、封闭墙炉。右侧不是游戏截图。', font=font(24), fill='#bbaec0')
    comparison.save(preview / 'reference-comparison.png')
    manifest = {
        'source_nbt_sha256': hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest(),
        'source': 'Actual delivered block states and block-entity banner patterns; original Minecraft 1.20.1 client.jar models/textures',
        'client_jar': str(CLIENT), 'client_jar_sha256': hashlib.sha256(CLIENT.read_bytes()).hexdigest(),
        'views': [name for name, _, _ in cameras], 'native_captures': captures,
        'approximations': ['Chest and skull block entities use simplified boxes and original texture regions.', 'Banner cloth is static approximate geometry; dye pattern masks are composed from actual delivered banner NBT.', 'Lava uses a static first animation frame and a simplified source surface.', 'No lighting, shadows, shader, biome tint, GUI, functional trade or player view is simulated.'],
        'cutaway': 'Only roof and two camera-near outer walls are omitted in memory. Wall-line pillars, beams, furniture, glass and arch remain. Delivered NBT is never cut.',
    }
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}))


if __name__ == '__main__':
    main()
