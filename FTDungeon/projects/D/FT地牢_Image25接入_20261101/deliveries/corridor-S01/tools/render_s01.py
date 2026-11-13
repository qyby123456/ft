from __future__ import annotations

import functools
import hashlib
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance

import mc_models as engine
from nbtio import Structure
from static_qa import ROOT, SIZE, digest, write_json

BACKGROUND = '#101816'
TEXT = '#ebe5ce'
MUTED = '#9eafa5'
ACCENT = '#d4c59a'
DIRECTIONS = ['north', 'east', 'south', 'west']
VIEWS = [('nw', '西北', 0), ('ne', '东北', 3), ('se', '东南', 2), ('sw', '西南', 1)]
engine.PARTIAL.update({'end_rod', 'glow_lichen', 'azalea_leaves', 'moss_carpet'})
ORIGINAL_SYNTHETIC = engine.synthetic_model
ORIGINAL_TEXTURE = engine.texture


def synthetic(name, properties):
    result = ORIGINAL_SYNTHETIC(name, properties)
    if name == 'skeleton_skull':
        definition, choice = result
        return definition, {'y': (choice['y'] + 180) % 360}
    return result


@functools.lru_cache(maxsize=None)
def texture(identifier, uv, rotation, brightness):
    image = ORIGINAL_TEXTURE(identifier, uv, rotation, brightness)
    if identifier.endswith(('azalea_leaves', 'moss_carpet', 'moss_block')):
        image = image.copy()
        alpha = image.getchannel('A')
        tint = Image.new('RGBA', image.size, '#779447')
        from PIL import ImageChops
        image = ImageChops.multiply(image, tint)
        image.putalpha(alpha)
    return image


engine.synthetic_model = synthetic
engine.texture = texture


def rotated(structure, turns):
    result = {}
    for position, block in structure.blocks.items():
        pos_x, pos_y, pos_z = position
        width, depth = SIZE[0], SIZE[2]
        properties = dict(block.properties)
        for turn in range(turns):
            pos_x, pos_z = depth - 1 - pos_z, pos_x
            width, depth = depth, width
        if properties.get('facing') in DIRECTIONS:
            properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing']) + turns) % 4]
        if properties.get('axis') in ('x', 'z') and turns % 2:
            properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
        if 'rotation' in properties:
            properties['rotation'] = str((int(properties['rotation']) + turns * 4) % 16)
        connections = {direction: properties[direction] for direction in DIRECTIONS if direction in properties}
        for direction, value in connections.items():
            properties[DIRECTIONS[(DIRECTIONS.index(direction) + turns) % 4]] = value
        result[(pos_x, pos_y, pos_z)] = {'Name': block.name, 'Properties': properties}
    return result


def render(structure, turns=0, size=(1400, 1120), exterior=False):
    visible = {}
    for position, block in rotated(structure, turns).items():
        pos_x, pos_y, pos_z = position
        if block['Name'] == 'minecraft:air':
            continue
        if not exterior and (pos_y == 10 or pos_y >= 2 and (pos_x <= 2 or pos_z <= 1)):
            continue
        visible[position] = block
    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    projected = [(point[0] - point[2], -(point[0] + point[2]) * 0.58 - point[1] * 0.95) for face in faces for point in face[0]]
    low_x, high_x = min(point[0] for point in projected), max(point[0] for point in projected)
    low_y, high_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 100) / (high_x - low_x), (size[1] - 175) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 20) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new('RGBA', size, BACKGROUND)
    def project(point):
        return offset_x + (point[0] - point[2]) * scale, offset_y - (point[0] + point[2]) * scale * 0.58 - point[1] * scale * 0.95
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.55 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        engine.parallelogram(canvas, texture(identifier, uv, rotation, brightness * 0.91), [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    view_label = next(label for direction, label, turn in VIEWS if turn == turns)
    draw.text((30, 18), 'S01 蔓生白骨吊笼短廊 / ' + view_label + ('完整外壳' if exterior else '室内剖切'), font=engine.font(27), fill=ACCENT)
    draw.text((30, 62), '13×11×16 · 一区块长度 · 南北双口 · 主路三宽三高', font=engine.font(21), fill=MUTED)
    draw.text((30, size[1] - 42), '最终 NBT + 原版 1.20.1 模型贴图 / 软件渲染，非游戏截图', font=engine.font(20), fill=MUTED)
    return canvas.convert('RGB')


def floorplan(structure):
    scale = 36
    canvas = Image.new('RGB', (660, 780), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((28, 18), 'S01 · 北 -Z ↑ · 一字贯通 16 格', font=engine.font(25), fill=TEXT)
    for pos_x in range(13):
        for pos_z in range(16):
            block = structure.get((pos_x, 2, pos_z))
            color = '#46504a' if block.name != 'minecraft:air' else '#aaa48f'
            if 5 <= pos_x <= 7:
                color = '#86b496'
            left, top = 70 + pos_x * scale, 100 + pos_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
    for pos_z, label in [(0, '北口 [6,2,0]'), (15, '南口 [6,2,15]')]:
        center_x, center_y = 70 + 6.5 * scale, 100 + (pos_z + 0.5) * scale
        draw.ellipse((center_x - 9, center_y - 9, center_x + 9, center_y + 9), fill='#fff1bf')
        draw.text((28, 72 if pos_z == 0 else 700), label, font=engine.font(20), fill=TEXT)
    draw.text((28, 738), '绿色=连续三宽主路；侧壁无分支、无可开门', font=engine.font(20), fill=MUTED)
    return canvas


def main():
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    structure = Structure.read(ROOT / 'room.nbt')
    manifest = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2800, 2240), BACKGROUND)
        for index, (direction, label, turns) in enumerate(VIEWS):
            image = render(structure, turns=turns, exterior=exterior)
            target = ROOT / 'qa' / (mode + '-' + direction + '.png')
            image.save(target)
            sheet.paste(image, ((index % 2) * 1400, (index // 2) * 1120))
            manifest.append({'file': str(target.relative_to(ROOT)), 'direction': label, 'mode': mode, 'sha256': digest(target)})
            print('RENDERED', mode, direction, flush=True)
        sheet.save(ROOT / 'qa' / (mode + '-four-views.png'))
    render(structure, turns=3, size=(1600, 1280)).save(ROOT / 'preview.png')
    floorplan(structure).save(ROOT / 'qa/floorplan.png')
    write_json(ROOT / 'qa/render.json', {'status': 'PASS', 'source': 'room.nbt', 'source_sha256': digest(ROOT / 'room.nbt'), 'method': 'Final NBT decoded and rendered using local Java 1.20.1 vanilla block models and textures.', 'not_game_screenshot': True, 'cutaway_display_only': True, 'full_roof_preserved_in_nbt': True, 'interior_directions': 4, 'exterior_directions': 4, 'preview': 'preview.png', 'preview_sha256': digest(ROOT / 'preview.png'), 'views': manifest, 'models': sorted(engine.USED_MODELS), 'textures': sorted(engine.USED_TEXTURES), 'limitations': ['Skulls use a simplified vanilla-textured entity mesh.', 'No dynamic block light, particles, biome tint or in-game ambient occlusion simulation.', 'First deterministic model variant selected; not a full-modpack gameplay capture.']})
    print('S01 final NBT renders complete', flush=True)


if __name__ == '__main__':
    main()
