import functools
import hashlib
import io
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageOps

import render_engine as engine
from nbtio import Structure
from prepare import digest, save


ROOT = Path(__file__).resolve().parents[1]
ORIGINAL_DEFINITIONS = engine.definitions
ORIGINAL_TEXTURE = engine.texture
DIRECTIONS = [('north', '北向内部剖视', (-0.18, -1)), ('south', '南向内部剖视', (0.18, 1)), ('west', '西向内部剖视', (-1, 0.18)), ('east', '东向内部剖视', (1, -0.18))]


def definitions(name, properties):
    if name.endswith('_wall_banner'):
        cloth = engine.box([2, -12, 14], [14, 15, 15], 'custom/banner')
        for face in cloth['elements'][0]['faces'].values():
            face['uv'] = [0, 0, 16, 16]
        return [(cloth, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    return ORIGINAL_DEFINITIONS(name, properties)


@functools.lru_cache(maxsize=None)
def texture(identifier, bounds, rotation, brightness):
    if identifier != 'custom/banner':
        return ORIGINAL_TEXTURE(identifier, bounds, rotation, brightness)
    cloth = Image.open(io.BytesIO(engine.ASSETS.read('assets/minecraft/textures/entity/banner/base.png'))).convert('RGBA')
    cloth = ImageOps.colorize(cloth.convert('L'), black='#351118', white='#9e2929').convert('RGBA')
    for pattern in ('skull', 'border'):
        layer = Image.open(io.BytesIO(engine.ASSETS.read('assets/minecraft/textures/entity/banner/' + pattern + '.png'))).convert('RGBA')
        dark = Image.new('RGBA', layer.size, '#211b20')
        dark.putalpha(layer.getchannel('A'))
        cloth.alpha_composite(dark)
    cloth = cloth.crop((1, 1, 21, 41)).resize((48, 96), Image.Resampling.NEAREST)
    return ImageEnhance.Brightness(cloth).enhance(brightness)


def perspective_warp(canvas, image, points):
    minimum_x = max(0, math.floor(min(point[0] for point in points)))
    minimum_y = max(0, math.floor(min(point[1] for point in points)))
    maximum_x = min(canvas.width, math.ceil(max(point[0] for point in points)) + 1)
    maximum_y = min(canvas.height, math.ceil(max(point[1] for point in points)) + 1)
    if maximum_x <= minimum_x or maximum_y <= minimum_y:
        return
    output = [(point[0] - minimum_x, point[1] - minimum_y) for point in points]
    inputs = [(0, 0), (image.width, 0), (image.width, image.height), (0, image.height)]
    matrix, values = [], []
    for (target_x, target_y), (source_x, source_y) in zip(output, inputs):
        matrix.extend([[target_x, target_y, 1, 0, 0, 0, -source_x * target_x, -source_x * target_y], [0, 0, 0, target_x, target_y, 1, -source_y * target_x, -source_y * target_y]])
        values.extend([source_x, source_y])
    try:
        coefficients = np.linalg.solve(np.array(matrix), np.array(values))
    except np.linalg.LinAlgError:
        return
    canvas.alpha_composite(image.transform((maximum_x - minimum_x, maximum_y - minimum_y), Image.Transform.PERSPECTIVE, coefficients, Image.Resampling.NEAREST), (minimum_x, minimum_y))


def eye_level(room):
    image = Image.new('RGBA', (1536, 1024), '#17151b')
    camera = (7.5, 2.62, 1.1)
    focal = 750
    faces = []
    for position, block in room.blocks.items():
        if block.name.removeprefix('minecraft:') in engine.INVISIBLE:
            continue
        if position[2] < 1:
            continue
        for points, normal, cull, identifier, bounds, rotation in engine.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if cull and engine.opaque(room.blocks.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            center = [sum(point[axis] for point in world) / 4 for axis in range(3)]
            if sum(normal[axis] * (camera[axis] - center[axis]) for axis in range(3)) <= 0:
                continue
            if min(point[2] - camera[2] for point in world) <= 0.15:
                continue
            projected = [(768 + (point[0] - camera[0]) * focal / (point[2] - camera[2]), 512 - (point[1] - camera[1]) * focal / (point[2] - camera[2])) for point in world]
            brightness = (0.94 if normal[1] > 0.5 else 0.72) * max(0.65, 1 - (center[2] - camera[2]) * 0.017)
            if block.name == 'minecraft:end_rod':
                brightness = 1.8
            if block.name == 'minecraft:candle':
                brightness = 1.2
            faces.append((center[2], projected, identifier, bounds, rotation, brightness))
    for depth, points, identifier, bounds, rotation, brightness in sorted(faces, key=lambda entry: -entry[0]):
        perspective_warp(image, texture(identifier, bounds, rotation, brightness), points)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 1536, 62), fill='#17151b')
    draw.text((28, 13), 'S13  陈旧血誓祭仪短廊  /  最终 NBT · 居中内部透视', font=engine.font(26), fill='#eadaca')
    draw.rectangle((0, 972, 1536, 1024), fill='#17151b')
    draw.text((28, 984), '实际方块模型软件预览，非实机；原生白光，不模拟着色器/动态光照。', font=engine.font(20), fill='#ada6a7')
    return image.convert('RGB')


def plan(room):
    image = Image.new('RGB', (1300, 1100), '#17151b')
    draw = ImageDraw.Draw(image)
    draw.text((35, 25), 'S13 短廊 / 俯视脚部通路', font=engine.font(32), fill='#eadaca')
    left, top, cell = 65, 120, 50
    for pos_x in range(15):
        for pos_z in range(16):
            color = '#4c5057'
            name = room.get((pos_x, 1, pos_z)).name
            if name == 'minecraft:air': color = '#827c77'
            if 6 <= pos_x <= 8: color = '#5c9290'
            if name == 'minecraft:candle': color = '#ddc68e'
            if name == 'minecraft:cobweb': color = '#c3b9b5'
            if pos_x in (2, 3, 4, 10, 11, 12) and pos_z in (3, 4, 5, 10, 11, 12): color = '#625b6e'
            draw.rectangle((left + cell * pos_x, top + cell * pos_z, left + cell * (pos_x + 1) - 2, top + cell * (pos_z + 1) - 2), fill=color)
    labels = ['N 北入口 (7,1,0)', 'S 南出口 (7,1,15)', '安全点 (7,1,3)', 'X=6…8 三格宽主路', 'Y=1…3 全长为空气', 'Z=0…15 恰好16格', '紫色：上方装饰吊笼', '地面无流体/无伤害', '完整石屋顶 Y=10', '8红旗 / 4空吊笼', '8末地烛 / 12蜡烛组', '无实体、容器或奖励']
    for index, label in enumerate(labels):
        draw.text((850, 140 + index * 60), label, font=engine.font(21), fill='#c7bebb')
    draw.text((35, 1005), '图中上方为北；吊笼底 Y=4，非落地阻挡。平面仅表达相对布局。', font=engine.font(22), fill='#b9afac')
    return image


def main():
    room = Structure.read(ROOT / 'room.nbt')
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    engine.definitions = definitions
    engine.texture = texture
    engine.PARTIAL.update({'iron_bars', 'iron_trapdoor', 'candle', 'end_rod', 'cobweb', 'red_wall_banner'})
    destination = ROOT / 'views'
    destination.mkdir(exist_ok=True)
    manifest = []
    for direction, label, camera in DIRECTIONS:
        for exterior in (False, True):
            kind = 'exterior' if exterior else 'interior'
            path = destination / (direction + '-' + kind + '.png')
            engine.render(room, camera, label.replace('内部剖视', '完整外壳') if exterior else label, exterior=exterior, size=(1500, 1200)).save(path)
            manifest.append({'direction': direction, 'kind': kind, 'path': str(path), 'sha256': digest(path), 'source_nbt_sha256': digest(ROOT / 'room.nbt')})
            print(path.name, flush=True)
    eye_level(room).save(ROOT / 'preview.png')
    plan(room).save(destination / 'plan.png')
    sheet = Image.new('RGB', (1800, 1440), '#17151b')
    for index, (direction, label, camera) in enumerate(DIRECTIONS):
        view = Image.open(destination / (direction + '-interior.png'))
        view.thumbnail((900, 720))
        sheet.paste(view, ((index % 2) * 900, (index // 2) * 720))
    sheet.save(destination / 'four-view-sheet.png')
    save(ROOT / 'qa/render-manifest.json', {'status': 'PASS', 'renderer': 'Vanilla 1.20.1 JSON models/textures + Python software projection', 'source_nbt_sha256': digest(ROOT / 'room.nbt'), 'views': manifest, 'preview_sha256': digest(ROOT / 'preview.png'), 'limitations': ['Software views, not client screenshots.', 'Banner cloth uses native skull/border texture masks on a rigid plane; no cloth animation.', 'No shader, simulated colored light or FPS claims.']})


if __name__ == '__main__':
    main()
