import functools
import hashlib
import json
import math
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path(__file__).resolve().parent
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure

import asset_engine as engine
engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
engine.renderer = engine
engine.box_model = engine.box
engine.parallelogram = engine.warp
base_definitions = engine.definitions
base_texture = engine.renderer.texture
INVISIBLE = {'air', 'cave_air', 'void_air', 'structure_void', 'light'}
VIEWS = (('north', '北侧', (-0.32, -1)), ('south', '南侧', (0.32, 1)), ('west', '西侧', (-1, 0.28)), ('east', '东侧', (1, -0.28)))


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


def definitions(name, properties):
    if name == 'red_wall_banner':
        definition = engine.box_model([2, -12, 14], [14, 14, 15], 'preview/ritual_banner')
        definition['elements'].extend(engine.box_model([1, 14, 13], [15, 16, 16], 'block/dark_oak_planks')['elements'])
        for element in definition['elements']:
            for surface in element['faces'].values():
                surface['uv'] = [0, 0, 16, 16]
        return [(definition, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    return base_definitions(name, properties)


@functools.lru_cache(maxsize=None)
def texture(identifier, uv, rotation, brightness):
    if identifier != 'preview/ritual_banner':
        return base_texture(identifier, uv, rotation, brightness)
    image = Image.new('RGBA', (32, 64), '#9b1d26')
    draw = ImageDraw.Draw(image)
    draw.polygon(((16, 14), (25, 30), (16, 46), (7, 30)), fill='#241b1e')
    draw.rectangle((14, 2, 18, 61), fill='#9b1d26')
    draw.rectangle((0, 0, 31, 63), outline='#261b1f', width=3)
    return ImageEnhance.Brightness(image).enhance(brightness)


engine.definitions = definitions
engine.renderer.texture = texture


@functools.lru_cache(maxsize=None)
def opaque(block):
    if block is None or block.name.removeprefix('minecraft:') in INVISIBLE:
        return False
    parts = engine.geometry(block.name.removeprefix('minecraft:'), block.properties)
    return len(parts) == 6 and all(all(abs(value - round(value)) < 0.001 for value in point) for part in parts for point in part[0])


engine.opaque = opaque


def visible_blocks(room, camera, exterior):
    result = {}
    for position, block in room.blocks.items():
        if block.name.removeprefix('minecraft:') in INVISIBLE:
            continue
        across, elevation, depth = position
        if not exterior:
            near_wall = across <= 1 if camera[0] < 0 else across >= 11
            arch = depth % 8 == 0 and 2 <= across <= 10 and elevation <= 9
            if elevation >= 8 and not arch:
                continue
            if near_wall and elevation >= 1:
                continue
        result[position] = block
    return result


def orthographic(room, metadata, camera, label, exterior):
    size = (1600, 1120)
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.83
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = visible_blocks(room, camera, exterior)

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    faces = []
    for position, block in visible.items():
        for points, normal, cull, identifier, uv, rotation in engine.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and engine.opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            depth = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / 4
            brightness = 1.18 if normal[1] > 0.5 else 1.08 if normal[0] < 0 else 1.12
            faces.append((depth, [project(point) for point in world], identifier, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 100) / (maximum_x - minimum_x), (size[1] - 180) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 110 + (size[1] - 180) / 2 - (minimum_y + maximum_y) * scale / 2
    image = Image.new('RGBA', size, '#171a20')
    for depth, points, identifier, uv, rotation, brightness in sorted(faces, key=lambda item: item[0]):
        coordinates = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        engine.renderer.parallelogram(image, texture(identifier, uv, rotation, brightness), coordinates)
    draw = ImageDraw.Draw(image)
    draw.text((32, 20), 'M03 · 血誓烛廊  /  ' + label, font=font(32), fill='#eae0cc')
    draw.text((32, 70), '13 × 12 × 41 · 最终 NBT · ' + ('完整封闭屋面与外壳' if exterior else '屋面和近侧外墙仅在此图剖切，保留拱列与装饰'), font=font(20), fill='#c09e96')
    draw.text((32, size[1] - 36), '原版 1.20.1 材质软件渲染，非游戏截图；旗帜实体外观为对应纹样近似。', font=font(18), fill='#9da1aa')
    return image.convert('RGB')


def solve(matrix, values):
    rows = [list(row) + [value] for row, value in zip(matrix, values)]
    length = len(rows)
    for column in range(length):
        pivot = max(range(column, length), key=lambda row: abs(rows[row][column]))
        rows[column], rows[pivot] = rows[pivot], rows[column]
        divisor = rows[column][column]
        if abs(divisor) < 1e-9:
            return None
        rows[column] = [value / divisor for value in rows[column]]
        for row in range(length):
            if row != column:
                factor = rows[row][column]
                rows[row] = [value - factor * base for value, base in zip(rows[row], rows[column])]
    return tuple(row[-1] for row in rows)


def perspective_quad(canvas, tile, points):
    left = max(0, math.floor(min(point[0] for point in points)))
    top = max(0, math.floor(min(point[1] for point in points)))
    right = min(canvas.width, math.ceil(max(point[0] for point in points)))
    bottom = min(canvas.height, math.ceil(max(point[1] for point in points)))
    if left >= right or top >= bottom:
        return
    matrix, values = [], []
    for (point_x, point_y), (texture_x, texture_y) in zip(points, ((0, 0), (tile.width, 0), (tile.width, tile.height), (0, tile.height))):
        point_x -= left
        point_y -= top
        matrix.extend(((point_x, point_y, 1, 0, 0, 0, -texture_x * point_x, -texture_x * point_y), (0, 0, 0, point_x, point_y, 1, -texture_y * point_x, -texture_y * point_y)))
        values.extend((texture_x, texture_y))
    coefficients = solve(matrix, values)
    if coefficients is None:
        return
    warped = tile.transform((right - left, bottom - top), Image.Transform.PERSPECTIVE, coefficients, Image.Resampling.NEAREST)
    canvas.alpha_composite(warped, (left, top))


def perspective(room, direction):
    size = (1536, 1024)
    eye = (6.5, 2.62, 0.3 if direction == 'north' else 40.7)
    sign = 1 if direction == 'north' else -1
    focal = size[0] / (2 * math.tan(math.radians(73) / 2))
    visible = {position: block for position, block in room.blocks.items() if block.name.removeprefix('minecraft:') not in INVISIBLE}
    faces = []
    for position, block in visible.items():
        for points, normal, cull, identifier, uv, rotation in engine.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if cull and engine.opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            center = tuple(sum(point[axis] for point in world) / 4 for axis in range(3))
            if sum(normal[axis] * (eye[axis] - center[axis]) for axis in range(3)) <= 0.0001:
                continue
            depth = [sign * (point[2] - eye[2]) for point in world]
            if min(depth) <= 0.12:
                continue
            projected = [(size[0] / 2 + sign * (point[0] - eye[0]) * focal / distance, size[1] / 2 - (point[1] - eye[1]) * focal / distance) for point, distance in zip(world, depth)]
            if max(point[0] for point in projected) < 0 or min(point[0] for point in projected) >= size[0] or max(point[1] for point in projected) < 0 or min(point[1] for point in projected) >= size[1]:
                continue
            light_distance = min(math.dist(center, (across + 0.5, 3.6, candle_depth + 0.5)) for across in (2, 10) for station in (4, 12, 20, 28, 36) for candle_depth in (station - 1, station + 1))
            brightness = 0.53 + 0.45 / (1 + light_distance * 0.18)
            if block.name == 'minecraft:candle':
                brightness = 1.08
            faces.append((sum(depth) / 4, projected, identifier, uv, rotation, brightness))
    image = Image.new('RGBA', size, '#08090d')
    for depth, points, identifier, uv, rotation, brightness in sorted(faces, key=lambda entry: -entry[0]):
        perspective_quad(image, texture(identifier, uv, rotation, brightness), points)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, size[0], 75), fill=(9, 11, 16, 230))
    draw.text((24, 12), 'M03 · 血誓烛廊  |  ' + ('北入口 → 南出口' if direction == 'north' else '南出口 → 北入口'), font=font(28), fill='#e9ddc9')
    draw.text((24, 49), '完整最终 NBT 中轴透视 · 非实机截图 · 原版材质 / 近似烛光 / 实心红色嵌槽', font=font(17), fill='#b69791')
    return image.convert('RGB')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2400, 1680), '#171a20')
        for index, (direction, label, camera) in enumerate(VIEWS):
            image = orthographic(room, metadata, camera, label + ('完整外观' if exterior else '内部剖切'), exterior)
            target = ROOT / 'views' / (mode + '-' + direction + '.png')
            image.save(target)
            sheet.paste(image.resize((1200, 840), Image.Resampling.LANCZOS), ((index % 2) * 1200, (index // 2) * 840))
            records.append({'path': target.relative_to(ROOT).as_posix(), 'kind': mode, 'direction': direction})
        target = ROOT / 'views' / (mode + '-four-directions.png')
        sheet.save(target)
        records.append({'path': target.relative_to(ROOT).as_posix(), 'kind': mode, 'direction': 'sheet'})
    for direction in ('north', 'south'):
        target = ROOT / ('preview.png' if direction == 'north' else 'views/perspective-south.png')
        perspective(room, direction).save(target)
        records.append({'path': target.relative_to(ROOT).as_posix(), 'kind': 'uncut axis perspective', 'direction': direction})
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    for record in records:
        record['sha256'] = hashlib.sha256((ROOT / record['path']).read_bytes()).hexdigest()
    (ROOT / 'qa/visuals.json').write_text(json.dumps({'status': 'RENDERED', 'source': 'final room.nbt', 'structure_sha256': digest, 'renderer': 'Minecraft 1.20.1 asset/block-model textured software renderer', 'cutaway_changes_final_nbt': False, 'game_screenshot': False, 'images': records}, ensure_ascii=False, indent=2), encoding='utf-8')
    print('Rendered four interior, four exterior, two sheets, two uncut axis perspectives from ' + digest)


if __name__ == '__main__':
    main()
