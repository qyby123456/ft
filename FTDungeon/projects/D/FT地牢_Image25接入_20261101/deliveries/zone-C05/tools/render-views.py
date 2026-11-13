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


specification = importlib.util.spec_from_file_location('c05_vanilla_models', SUPPORT / 'render_engine.py')
models = importlib.util.module_from_spec(specification)
specification.loader.exec_module(models)
models.ASSETS = zipfile.ZipFile(models.CLIENT)
INVISIBLE = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
VIEWS = [('north', '北向', (-0.62, -1)), ('south', '南向', (0.62, 1)), ('west', '西向', (-1, 0.62)), ('east', '东向', (1, -0.62))]
SIZE = (1800, 1300)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def opaque(block):
    return block is not None and block.name not in INVISIBLE and not any(fragment in block.name for fragment in ('glass', 'water', 'amethyst_cluster', 'amethyst_bud', 'rail', 'chain', 'lantern', 'fence'))


def filtered(room, camera, exterior):
    result = {}
    for position, block in room.blocks.items():
        if block.name in INVISIBLE:
            continue
        horizontal, vertical, depth = position
        if not exterior:
            roof = vertical >= 11
            beam = vertical == 11 and depth in {8, 21} and 2 <= horizontal <= 22
            near_side = horizontal <= 2 if camera[0] < 0 else horizontal >= 22
            near_end = depth <= 2 if camera[1] < 0 else depth >= 22
            if roof and not beam or vertical >= 2 and (near_side or near_end):
                continue
        result[position] = block
    return result


def render(room, camera, direction, exterior):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.92
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
            brightness = 1.18 if normal[1] > 0.5 else 0.94 if normal[0] < 0 else 1.04
            faces.append((order, [project(point) for point in world], texture, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    bounds = [min(point[0] for point in projected), min(point[1] for point in projected), max(point[0] for point in projected), max(point[1] for point in projected)]
    scale = min((SIZE[0] - 130) / (bounds[2] - bounds[0]), (SIZE[1] - 220) / (bounds[3] - bounds[1]))
    offset_x = SIZE[0] / 2 - (bounds[0] + bounds[2]) * scale / 2
    offset_y = 140 + (SIZE[1] - 220) / 2 - (bounds[1] + bounds[3]) * scale / 2
    image = Image.new('RGBA', SIZE, '#101923')
    for order, points, texture, uv, rotation, brightness in sorted(faces, key=lambda item: item[0]):
        screen = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        models.warp(image, models.texture(texture, uv, rotation, brightness), screen)
    draw = ImageDraw.Draw(image)
    draw.text((36, 22), 'C05 水晶角洞 · ' + direction + ('完整外壳' if exterior else '内部剖视'), font=models.font(34), fill='#ecdcff')
    draw.text((36, 78), '实际最终 NBT · 25 × 14 × 25 · ' + ('完整洞顶 / 北入口 + 东出口' if exterior else '紫晶台 / 两处浅水 / 五格宽干燥转角通路'), font=models.font(22), fill='#8fd3d5')
    draw.text((36, SIZE[1] - 43), '1.20.1 原版模型和纹理软件预览；非游戏截图，不模拟动态光照。', font=models.font(20), fill='#a7b4c2')
    return image.convert('RGB')


def layout(room):
    image = Image.new('RGB', (1150, 1150), '#101923')
    draw = ImageDraw.Draw(image)
    cell = 37
    for horizontal in range(25):
        for depth in range(25):
            floor = room.get((horizontal, 1, depth)).name
            obstacle = room.get((horizontal, 2, depth)).name not in INVISIBLE
            route = 10 <= horizontal <= 14 and depth <= 14 or horizontal >= 10 and 10 <= depth <= 14
            color = '#445262' if obstacle else '#899098'
            if floor == 'minecraft:water':
                color = '#21769c'
            if route:
                color = '#78cbb9'
            left, top = 110 + horizontal * cell, 125 + depth * cell
            draw.rectangle((left, top, left + cell - 2, top + cell - 2), fill=color)
    draw.text((36, 22), 'C05 实际 NBT 平面：北 ↑ / 东 →', font=models.font(31), fill='#ecdcff')
    draw.text((36, 72), '绿色=保证净空通路；蓝色=有底浅池；深灰=脚层障碍', font=models.font(22), fill='#9dc8cc')
    draw.text((36, 1080), '北入口 (12,2,0) → 安全点 (12,2,4) → 东出口 (24,2,12)', font=models.font(24), fill='#ecdcff')
    return image


def main():
    room = Structure.read(ROOT / 'room.nbt')
    output_directory = ROOT / 'preview'
    output_directory.mkdir(parents=True, exist_ok=True)
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (SIZE[0] * 2, SIZE[1] * 2), '#101923')
        for index, (name, direction, camera) in enumerate(VIEWS):
            image = render(room, camera, direction, exterior)
            output = output_directory / f'{mode}-{name}.png'
            image.save(output)
            sheet.paste(image, (index % 2 * SIZE[0], index // 2 * SIZE[1]))
            records.append({'view': name, 'mode': mode, 'path': str(output.relative_to(ROOT)), 'sha256': digest(output), 'camera': list(camera), 'size': list(SIZE)})
            if not exterior and name == 'east':
                image.save(ROOT / 'preview.png')
            print(mode + '-' + name, flush=True)
        sheet.save(output_directory / f'four-{mode}.png')
    layout(room).save(output_directory / 'layout.png')
    manifest = {'status': 'PASS', 'id': 'zone-C05', 'sha256': digest(ROOT / 'room.nbt'), 'preview_sha256': digest(ROOT / 'preview.png'), 'count': len(records), 'views': records, 'source': 'Actual final room.nbt with vanilla 1.20.1 block models and textures', 'model_support': str(SUPPORT / 'render_engine.py'), 'model_support_sha256': digest(SUPPORT / 'render_engine.py'), 'client_jar': str(models.CLIENT), 'client_jar_sha256': digest(models.CLIENT), 'not_game_screenshots': True, 'cutaway': 'Interior views remove roof and near walls only in render memory; retain two cross beams and all visible pillars/hanging supports.', 'limitations': ['Software projection without dynamic lighting, shaders, modpack FPS or in-game client evidence.']}
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
