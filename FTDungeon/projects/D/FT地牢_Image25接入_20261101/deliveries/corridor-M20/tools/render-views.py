import hashlib
import importlib.util
import json
import math
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SUPPORT = Path('D:/ft工程文件/FT地牢_R5_石匠工坊_20261102/tools')
sys.path.insert(0, str(SUPPORT))
from nbtio import Structure


specification = importlib.util.spec_from_file_location('m20_vanilla_models', SUPPORT / 'render_engine.py')
models = importlib.util.module_from_spec(specification)
specification.loader.exec_module(models)
models.ASSETS = zipfile.ZipFile(models.CLIENT)
INVISIBLE = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
VIEWS = [('north', '北向', (-0.42, -1)), ('south', '南向', (0.42, 1)),
         ('west', '西向', (-1, 0.22)), ('east', '东向', (1, -0.22))]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def opaque(block):
    return block is not None and block.name not in INVISIBLE and not any(
        fragment in block.name for fragment in ('glass', 'iron_bars', 'chain', 'lantern', 'trapdoor', 'button', '_slab'))


def filtered(room, camera, exterior):
    result = {}
    for position, block in room.blocks.items():
        if block.name in INVISIBLE:
            continue
        horizontal, vertical, depth = position
        if not exterior:
            roof = vertical >= 9
            anchor = vertical == 9 and (horizontal, depth) in {(6, 7), (5, 15), (7, 23), (6, 31)}
            near_side = horizontal <= 2 if camera[0] < 0 else horizontal >= 10
            near_end = depth == (0 if camera[1] < 0 else 39)
            if roof and not anchor or vertical >= 2 and (near_side or near_end):
                continue
        result[position] = block
    return result


def orthographic(room, camera, direction, exterior, size=(1800, 1100)):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.82
    vertical_factor = 1 / math.hypot(1, camera_y)
    visible = filtered(room, camera, exterior)

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_factor * (camera_y * horizontal - point[1])

    faces = []
    for position, block in visible.items():
        for vertices, normal, cull, texture, uv, rotation in models.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in vertices]
            order = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / len(world)
            brightness = 1.13 if normal[1] > 0.5 else 0.94 if normal[0] < 0 else 1.02
            faces.append((order, [project(point) for point in world], texture, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    bounds = [min(point[0] for point in projected), min(point[1] for point in projected),
              max(point[0] for point in projected), max(point[1] for point in projected)]
    scale = min((size[0] - 130) / (bounds[2] - bounds[0]), (size[1] - 205) / (bounds[3] - bounds[1]))
    offset_x = size[0] / 2 - (bounds[0] + bounds[2]) * scale / 2
    offset_y = 125 + (size[1] - 205) / 2 - (bounds[1] + bounds[3]) * scale / 2
    image = Image.new('RGBA', size, '#111c24')
    for order, points, texture, uv, rotation, brightness in sorted(faces, key=lambda item: item[0]):
        screen = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        models.warp(image, models.texture(texture, uv, rotation, brightness), screen)
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'M20 铜械机廊 · ' + direction + ('完整外壳' if exterior else '内部剖视'), font=models.font(32), fill='#eddbbc')
    draw.text((35, 72), '最终 NBT / 13 × 11 × 40 / ' + ('完整顶盖与两端门洞' if exterior else '仅显示剖切；保留梁、管线、吊笼、铜盖锚点'), font=models.font(21), fill='#8fc5bd')
    draw.text((35, size[1] - 43), '1.20.1 原版模型及纹理软件渲染，非游戏截图；不模拟动态光照。', font=models.font(19), fill='#9dabb1')
    return image.convert('RGB')


def linear_solve(matrix, vector):
    count = len(vector)
    augmented = [list(row) + [value] for row, value in zip(matrix, vector)]
    for column in range(count):
        pivot = max(range(column, count), key=lambda row: abs(augmented[row][column]))
        augmented[column], augmented[pivot] = augmented[pivot], augmented[column]
        divisor = augmented[column][column]
        if abs(divisor) < 0.00000001:
            return None
        augmented[column] = [value / divisor for value in augmented[column]]
        for row in range(count):
            if row != column:
                multiplier = augmented[row][column]
                augmented[row] = [value - multiplier * pivot_value for value, pivot_value in zip(augmented[row], augmented[column])]
    return tuple(row[-1] for row in augmented)


def perspective_warp(canvas, texture, points):
    left = max(0, math.floor(min(point[0] for point in points)))
    top = max(0, math.floor(min(point[1] for point in points)))
    right = min(canvas.width, math.ceil(max(point[0] for point in points)))
    bottom = min(canvas.height, math.ceil(max(point[1] for point in points)))
    if right <= left or bottom <= top:
        return
    matrix = []
    vector = []
    for (horizontal, vertical), (source_x, source_y) in zip(points, [(0, 0), (texture.width, 0), (texture.width, texture.height), (0, texture.height)]):
        horizontal -= left
        vertical -= top
        matrix.extend([[horizontal, vertical, 1, 0, 0, 0, -source_x * horizontal, -source_x * vertical],
                       [0, 0, 0, horizontal, vertical, 1, -source_y * horizontal, -source_y * vertical]])
        vector.extend([source_x, source_y])
    coefficients = linear_solve(matrix, vector)
    if coefficients:
        layer = texture.transform((right - left, bottom - top), Image.Transform.PERSPECTIVE, coefficients, Image.Resampling.NEAREST)
        canvas.alpha_composite(layer, (left, top))


def perspective(room):
    image = Image.new('RGBA', (1536, 1024), '#080d11')
    camera = (6.5, 3.62, 1.2)
    focal = 890
    visible = {position: block for position, block in room.blocks.items() if block.name not in INVISIBLE}
    faces = []
    for position, block in visible.items():
        for vertices, normal, cull, texture, uv, rotation in models.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in vertices]
            center = tuple(sum(point[axis] for point in world) / 4 for axis in range(3))
            if sum(normal[axis] * (camera[axis] - center[axis]) for axis in range(3)) <= 0.00001:
                continue
            if any(point[2] - camera[2] < 0.25 for point in world):
                continue
            points = [(768 + (point[0] - camera[0]) * focal / (point[2] - camera[2]),
                       512 - (point[1] - camera[1]) * focal / (point[2] - camera[2])) for point in world]
            distance = center[2] - camera[2]
            brightness = max(0.58, 1.02 - distance * 0.009)
            if texture.endswith('shroomlight'):
                brightness = 1.2
            faces.append((distance, points, texture, uv, rotation, brightness))
    for distance, points, texture, uv, rotation, brightness in sorted(faces, key=lambda entry: -entry[0]):
        perspective_warp(image, models.texture(texture, uv, rotation, round(brightness, 2)), points)
    return image.convert('RGB')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (3600, 2200), '#111c24')
        for index, (name, direction, camera) in enumerate(VIEWS):
            image = orthographic(room, camera, direction, exterior)
            output = ROOT / 'preview' / f'{mode}-{name}.png'
            image.save(output)
            sheet.paste(image, ((index % 2) * 1800, (index // 2) * 1100))
            records.append({'view': name, 'mode': mode, 'path': str(output.relative_to(ROOT)), 'sha256': digest(output), 'camera': list(camera)})
        sheet.save(ROOT / 'preview' / f'four-{mode}.png')
    perspective(room).save(ROOT / 'preview.png')
    manifest = {'status': 'PASS', 'sha256': digest(ROOT / 'room.nbt'), 'reference_sha256': json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))['reference_sha256'],
                'preview_sha256': digest(ROOT / 'preview.png'), 'views': records, 'count': len(records),
                'model_support': str(SUPPORT / 'render_engine.py'), 'model_support_sha256': digest(SUPPORT / 'render_engine.py'),
                'client_jar': str(models.CLIENT), 'client_jar_sha256': digest(models.CLIENT),
                'source': 'Final room.nbt; Minecraft Java 1.20.1 block models and textures',
                'not_game_screenshots': True, 'limitations': ['Software projection; no dynamic lighting, shader, FPS or full modpack evidence.',
                'Interior views remove only roof and near side/end shell in memory; preserve cage anchors; final NBT remains complete.',
                'Perspective preview clips geometry less than 0.25 blocks in front of camera; final NBT is unmodified.']}
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'views': 8, 'sha256': manifest['sha256']}))


if __name__ == '__main__':
    main()
