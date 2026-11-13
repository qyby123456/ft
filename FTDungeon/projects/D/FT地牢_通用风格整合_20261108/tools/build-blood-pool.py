import argparse
import base64
import io
import json
import math
import random
import subprocess
import uuid
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


NAMESPACE = 'ftdungeonmobs'
FACES = {'north': (2, -1), 'south': (2, 1), 'east': (0, 1), 'west': (0, -1), 'up': (1, 1), 'down': (1, -1)}
PATCHER = Path('C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/22cfb1d302613eae/codex.exe')
PENDING = []


def queue(path, value):
    text = json.dumps(value, ensure_ascii=False, indent=2) + '\n'
    kind = 'Update' if path.exists() else 'Add'
    if kind == 'Update':
        old = path.read_text(encoding='utf-8')
        if old == text:
            return
        patch = f'*** Update File: {path}\n@@\n' + ''.join('-' + line + '\n' for line in old.splitlines())
        patch += ''.join('+' + line + '\n' for line in text.splitlines())
    else:
        patch = f'*** Add File: {path}\n' + ''.join('+' + line + '\n' for line in text.splitlines())
    PENDING.append(patch)


def flush():
    for patch in PENDING:
        result = subprocess.run([str(PATCHER), '--codex-run-as-apply-patch', '*** Begin Patch\n' + patch + '*** End Patch'],
                                capture_output=True, text=True, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stdout + result.stderr)
    PENDING.clear()


def stone_tile(seed):
    generator = random.Random(seed)
    tile = Image.new('RGBA', (16, 16))
    drawing = ImageDraw.Draw(tile)
    for pixel_y in range(16):
        for pixel_x in range(16):
            value = generator.choice((39, 44, 49, 53, 59, 65))
            tile.putpixel((pixel_x, pixel_y), (value + 3, value, value + 5, 255))
    for row in (0, 8, 15):
        drawing.line((0, row, 15, row), fill='#201f27')
        drawing.line((0, min(15, row + 1), 15, min(15, row + 1)), fill='#55515b')
    drawing.line((7, 1, 7, 7), fill='#22212a')
    drawing.line((3, 9, 3, 14), fill='#24222b')
    drawing.line((12, 9, 12, 14), fill='#24222b')
    return tile


def make_atlas(lit):
    atlas = Image.new('RGBA', (64, 64), '#39353e')
    for index in range(16):
        atlas.paste(stone_tile(180 + index), ((index % 4) * 16, (index // 4) * 16))
    drawing = ImageDraw.Draw(atlas)
    rune = '#ff7538' if lit else '#6e393d'
    shadow = '#9f1921' if lit else '#38242c'
    for tile_x, tile_y in ((16, 0), (32, 0), (48, 16), (0, 32), (16, 32)):
        drawing.line((tile_x + 4, tile_y + 3, tile_x + 4, tile_y + 12), fill=shadow, width=3)
        drawing.line((tile_x + 4, tile_y + 3, tile_x + 4, tile_y + 12), fill=rune)
        drawing.line((tile_x + 4, tile_y + 4, tile_x + 11, tile_y + 8, tile_x + 4, tile_y + 11), fill=rune)
        drawing.line((tile_x + 10, tile_y + 3, tile_x + 10, tile_y + 12), fill=rune)
        drawing.point((tile_x + 7, tile_y + 14), fill=rune)
    for tile_x, tile_y in ((48, 0), (0, 16)):
        drawing.rectangle((tile_x + 2, tile_y + 2, tile_x + 13, tile_y + 10), fill='#bdb09c')
        drawing.rectangle((tile_x + 4, tile_y + 10, tile_x + 11, tile_y + 14), fill='#aa9984')
        drawing.line((tile_x + 3, tile_y + 2, tile_x + 12, tile_y + 2), fill='#e0d0b8')
        drawing.rectangle((tile_x + 4, tile_y + 5, tile_x + 6, tile_y + 8), fill='#ee5429' if lit else '#241e25')
        drawing.rectangle((tile_x + 9, tile_y + 5, tile_x + 11, tile_y + 8), fill='#ee5429' if lit else '#241e25')
        drawing.rectangle((tile_x + 7, tile_y + 8, tile_x + 8, tile_y + 10), fill='#33272b')
        for tooth in (5, 7, 9):
            drawing.line((tile_x + tooth, tile_y + 12, tile_x + tooth, tile_y + 14), fill='#493c37')
    drawing.rectangle((16, 16, 31, 31), fill='#a2917e')
    drawing.line((17, 17, 30, 17), fill='#dbcfb5')
    drawing.line((17, 18, 17, 29), fill='#c9b99c')
    drawing.rectangle((32, 16, 47, 31), fill='#4d444b')
    drawing.line((32, 16, 47, 16), fill='#9c8d85')
    drawing.line((32, 30, 47, 30), fill='#29232b')
    for offset in (34, 44):
        drawing.rectangle((offset, 19, offset + 1, 20), fill='#b1a08e')
        drawing.rectangle((offset, 27, offset + 1, 28), fill='#74645e')
    for tile_x, tile_y in ((48, 16), (0, 32), (16, 32)):
        drawing.rectangle((tile_x + 2, tile_y + 2, tile_x + 13, tile_y + 13), outline='#8b7771')
        drawing.rectangle((tile_x + 4, tile_y + 4, tile_x + 11, tile_y + 11), fill='#5e0c1c' if lit else '#251e29')
        drawing.line((tile_x + 5, tile_y + 6, tile_x + 10, tile_y + 6), fill=rune)
        drawing.line((tile_x + 8, tile_y + 4, tile_x + 8, tile_y + 10), fill=rune)
    for streak in (2, 8, 12):
        drawing.line((streak, 3, streak, 6 + streak % 5), fill='#6c1826')
        drawing.point((streak, 8 + streak % 5), fill='#8a2730')
    return atlas


def blood_texture(size):
    strip = Image.new('RGBA', (size, size * 16))
    for frame in range(16):
        for pixel_y in range(size):
            for pixel_x in range(size):
                wave = math.sin((pixel_x + frame) * math.pi / 8) + math.cos((pixel_y - frame) * math.pi / 8)
                grain = (pixel_x * 7 + pixel_y * 13 + frame * 3) % 11
                red = int(91 + wave * 14 + grain * 3)
                strip.putpixel((pixel_x, frame * size + pixel_y), (red, 8 + grain, 23 + grain, 255))
        drawing = ImageDraw.Draw(strip)
        ripple_x = (frame * 2 + 4) % size
        ripple_y = frame * size + (frame + 7) % size
        drawing.line((ripple_x, ripple_y, min(size - 1, ripple_x + 3), ripple_y), fill='#d44243')
    return strip


def box(name, start, end, tile=0, overrides=None):
    return dict(name=name, start=list(start), end=list(end), tile=tile, overrides=overrides or {})


def definitions():
    return {
        'bloodstone_bricks': [box('血痕黑石砖', (0, 0, 0), (16, 16, 16))],
        'blood_rune_block': [box('符文石', (0, 0, 0), (16, 16, 16), 1)],
        'blood_pool_rim': [box('压边底座', (0, 0, 0), (16, 3, 16)), box('外池壁', (0, 3, 10), (16, 13, 16), 1),
                           box('符文压顶', (0, 13, 9), (16, 16, 16), 6, {'up': 2})],
        'blood_pool_corner': [box('转角底座', (0, 0, 0), (16, 3, 16)), box('后池壁', (0, 3, 10), (16, 13, 16), 1),
                              box('侧池壁', (10, 3, 0), (16, 13, 10), 1), box('后压顶', (0, 13, 9), (16, 16, 16), 6, {'up': 2}),
                              box('侧压顶', (9, 13, 0), (16, 16, 9), 6, {'up': 2})],
        'blood_skull_relief': [box('石壁', (0, 0, 3), (16, 14, 16)), box('方颅骨饰', (2, 4, 0), (14, 14, 4), 5, {'north': 3}),
                              box('方下颌', (4, 2, 0), (12, 4, 3), 5), box('顶压石', (0, 14, 0), (16, 16, 16), 6)],
        'blood_skull_shrine': [box('祭座', (0, 0, 2), (16, 3, 16)), box('符文座', (2, 3, 4), (14, 5, 16), 1),
                              box('方颅祭像', (2, 5, 3), (14, 14, 15), 5, {'north': 3}),
                              box('左方角', (0, 12, 7), (4, 16, 15), 6), box('右方角', (12, 12, 7), (16, 16, 15), 6),
                              box('中阶冠', (4, 14, 9), (12, 16, 15), 6)],
        'blood_offering_altar': [box('祭台基座', (0, 0, 0), (16, 3, 16)), box('祭台碑身', (1, 3, 1), (15, 11, 15), 1, {'north': 3}),
                                 box('献祭台面', (0, 11, 0), (16, 14, 16), 6, {'up': 7}),
                                 box('盆左沿', (2, 14, 2), (4, 16, 14), 6), box('盆右沿', (12, 14, 2), (14, 16, 14), 6),
                                 box('盆前沿', (4, 14, 2), (12, 16, 4), 6), box('盆后沿', (4, 14, 12), (12, 16, 14), 6)],
        'bloodstone_stairs': [box('台阶底', (0, 0, 0), (16, 8, 16)), box('台阶上', (0, 8, 8), (16, 16, 16))],
        'bloodstone_slab': [box('半砖', (0, 0, 0), (16, 8, 16))],
    }


def hidden(source, target, direction):
    axis, sign = FACES[direction]
    plane = target['end' if sign > 0 else 'start'][axis]
    other_axes = [value for value in range(3) if value != axis]
    for neighbor in source:
        if neighbor is target:
            continue
        occludes = neighbor['start'][axis] <= plane < neighbor['end'][axis] if sign > 0 else neighbor['start'][axis] < plane <= neighbor['end'][axis]
        if occludes and all(neighbor['start'][other] <= target['start'][other]
                            and neighbor['end'][other] >= target['end'][other] for other in other_axes):
            return True
    return False


def element(source, target, full_cube=False):
    result = dict(name=target['name'], **{'from': target['start'], 'to': target['end']}, faces={})
    for direction in FACES:
        if hidden(source, target, direction):
            continue
        tile = target['overrides'].get(direction, target['tile'])
        tile_x, tile_y = tile % 4 * 4, tile // 4 * 4
        face = dict(uv=[tile_x, tile_y, tile_x + 4, tile_y + 4], texture='#atlas')
        if full_cube:
            face['cullface'] = direction
        result['faces'][direction] = face
    return result


def editable(path, name, elements, atlas):
    buffer = io.BytesIO()
    atlas.save(buffer, format='PNG')
    cubes = []
    for index, model in enumerate(elements):
        identity = str(uuid.uuid5(uuid.NAMESPACE_URL, name + str(index)))
        cubes.append(dict(name=model['name'], type='cube', uuid=identity, **{'from': model['from'], 'to': model['to']},
                          origin=[8, 0, 8], rotation=[0, 0, 0], autouv=0, box_uv=False,
                          faces={direction: dict(uv=[value * 4 for value in model['faces'][direction]['uv']], texture=0)
                                 if direction in model['faces'] else dict(uv=[0, 0, 0, 0], texture=None) for direction in FACES}))
    root = dict(name=name + '_root', uuid=str(uuid.uuid5(uuid.NAMESPACE_URL, name)), origin=[8, 0, 8], rotation=[0, 0, 0],
                children=[dict(name='01_主体与雕饰', uuid=str(uuid.uuid5(uuid.NAMESPACE_URL, name + '_body')),
                               origin=[8, 0, 8], rotation=[0, 0, 0], children=[cube['uuid'] for cube in cubes])])
    queue(path, dict(meta=dict(format_version='4.10', model_format='free', box_uv=False), name=name,
                     resolution=dict(width=64, height=64), elements=cubes, outliner=[root],
                     textures=[dict(name='sacrifice_stone.png', id='0', uuid=str(uuid.uuid5(uuid.NAMESPACE_URL, 'blood_pool_atlas')),
                                    source='data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode(), mode='bitmap',
                                    width=64, height=64, uv_width=64, uv_height=64, saved=True)]))


def outer(across, depth):
    return abs(across) <= 4 and abs(depth) <= 4 and abs(across) + abs(depth) <= 6


def inner(across, depth):
    return abs(across) <= 3 and abs(depth) <= 3 and abs(across) + abs(depth) <= 5


def scene(definitions_by_name, atlas, blood, brazier):
    result = []
    decoration = 0
    for across in range(-4, 5):
        for depth in range(-4, 5):
            if not outer(across, depth):
                continue
            result += translated(definitions_by_name['bloodstone_bricks'], (across * 16, -16, depth * 16))
            if inner(across, depth):
                if across == 0 and depth == 0:
                    result += translated(definitions_by_name['bloodstone_bricks'], (0, 0, 0))
                    result += translated(definitions_by_name['blood_skull_shrine'], (0, 16, 0))
                else:
                    result.append(dict(name='blood', start=[across * 16, 0, depth * 16], end=[across * 16 + 16, 14, depth * 16 + 16],
                                       tile=0, overrides={}, blood=True))
            else:
                if abs(across) == 4 and abs(depth) == 2:
                    kind = 'blood_pool_corner'
                else:
                    kind = 'blood_skull_relief' if decoration % 3 == 0 else 'blood_rune_block' if (decoration + 1) % 2 == 0 else 'blood_pool_rim'
                    decoration += 1
                if across == 0 and depth == -4:
                    kind = 'blood_offering_altar'
                turns = (3 if across > 0 else 1) if abs(across) > abs(depth) else (0 if depth > 0 else 2)
                if kind == 'blood_offering_altar':
                    turns = 0
                result += translated(definitions_by_name[kind], (across * 16, 0, depth * 16), turns)
    for across in (-4, 4):
        for depth in (-4, 4):
            for height in (-16, 16):
                result += translated(definitions_by_name['bloodstone_bricks'], (across * 16, height, depth * 16))
            result += translated(definitions_by_name['blood_skull_relief'], (across * 16, 0, depth * 16))
            result += translated(brazier, (across * 16, 32, depth * 16))
    for across in (-1, 0, 1):
        result += translated(definitions_by_name['bloodstone_stairs'], (across * 16, -16, -5 * 16))
        result += translated(definitions_by_name['bloodstone_slab'], (across * 16, -16, -6 * 16))
    return render(result, atlas, blood, (1080, 860))


def translated(source, offset, turns=0):
    result = []
    for original in source:
        copy = dict(original)
        corners = [original['start'][:], original['end'][:]]
        for unused in range(turns):
            corners = [[16 - point[2], point[1], point[0]] for point in corners]
        copy['start'] = [min(point[axis] for point in corners) + offset[axis] for axis in range(3)]
        copy['end'] = [max(point[axis] for point in corners) + offset[axis] for axis in range(3)]
        overrides = dict(original['overrides'])
        for unused in range(turns):
            order = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north', 'up': 'up', 'down': 'down'}
            overrides = {order[direction]: tile for direction, tile in overrides.items()}
        copy['overrides'] = overrides
        result.append(copy)
    return result


def render(source, atlas, blood, dimensions):
    occupied = {}
    for index, shape in enumerate(source):
        for across in range(shape['start'][0], shape['end'][0]):
            for height in range(shape['start'][1], shape['end'][1]):
                for depth in range(shape['start'][2], shape['end'][2]):
                    occupied[(across, height, depth)] = index
    def project(point):
        return ((point[0] + point[2]) * 0.71, (point[0] - point[2]) * 0.38 - point[1])
    projected = [project(corner) for shape in source for corner in (shape['start'], shape['end'])]
    minimum = [min(point[axis] for point in projected) for axis in range(2)]
    maximum = [max(point[axis] for point in projected) for axis in range(2)]
    scale = min((dimensions[axis] - 50) / max(1, maximum[axis] - minimum[axis]) for axis in range(2))
    offset = [(dimensions[axis] - (maximum[axis] - minimum[axis]) * scale) / 2 - minimum[axis] * scale for axis in range(2)]
    canvas = Image.new('RGB', dimensions, '#14141c')
    drawing = ImageDraw.Draw(canvas)
    polygons = []
    for point, index in occupied.items():
        shape = source[index]
        for direction in ('north', 'east', 'up'):
            axis, sign = FACES[direction]
            neighbor = list(point)
            neighbor[axis] += sign
            if tuple(neighbor) in occupied:
                continue
            other = [value for value in range(3) if value != axis]
            vertices = []
            for first, second in ((0, 0), (1, 0), (1, 1), (0, 1)):
                vertex = list(point)
                vertex[axis] += 1 if sign > 0 else 0
                vertex[other[0]] += first
                vertex[other[1]] += second
                projected_point = project(vertex)
                vertices.append(tuple(projected_point[component] * scale + offset[component] for component in range(2)))
            tile = shape['overrides'].get(direction, shape['tile'])
            horizontal = 2 if direction == 'east' else 0
            vertical = 2 if direction == 'up' else 1
            pixel_x = int((point[horizontal] - shape['start'][horizontal] + 0.5) * 16 / (shape['end'][horizontal] - shape['start'][horizontal])) % 16
            pixel_y = int((point[vertical] - shape['start'][vertical] + 0.5) * 16 / (shape['end'][vertical] - shape['start'][vertical])) % 16
            if direction != 'up':
                pixel_y = 15 - pixel_y
            if shape.get('nativeFaces') is not None:
                native_face = shape['nativeFaces'].get(direction)
                if native_face is None:
                    continue
                texture = shape['nativeTexture']
                uv = native_face['uv']
                sample_x = min(texture.width - 1, max(0, int((uv[0] + (uv[2] - uv[0]) * (pixel_x + 0.5) / 16) * texture.width / 16)))
                sample_y = min(texture.height - 1, max(0, int((uv[1] + (uv[3] - uv[1]) * (pixel_y + 0.5) / 16) * texture.height / 16)))
                color = texture.getpixel((sample_x, sample_y))[:3]
            else:
                color = blood.getpixel((point[0] % 16, point[2] % 16))[:3] if shape.get('blood') else atlas.getpixel((tile % 4 * 16 + pixel_x, tile // 4 * 16 + pixel_y))[:3]
            shade = {'up': 1.15, 'north': 0.92, 'east': 0.72}[direction]
            color = tuple(min(255, int(value * shade)) for value in color)
            polygons.append((point[0] - point[2] + point[1] * 0.7, vertices, color))
    for distance, polygon, color in sorted(polygons, key=lambda item: item[0]):
        drawing.polygon(polygon, fill=color)
    return canvas


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--destination', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    project = arguments.destination.resolve()
    output = arguments.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    resources = project / 'src/main/resources'
    assets = resources / f'assets/{NAMESPACE}'
    textures = assets / 'textures/block'
    textures.mkdir(parents=True, exist_ok=True)
    idle, lit = make_atlas(False), make_atlas(True)
    idle.save(textures / 'sacrifice_stone.png')
    lit.save(textures / 'sacrifice_stone_lit.png')
    idle.crop((0, 0, 16, 16)).save(textures / 'bloodstone_bricks.png')
    still, flowing = blood_texture(16), blood_texture(32)
    for name, image in [('sacrificial_blood_still', still), ('sacrificial_blood_flow', flowing)]:
        image.save(textures / f'{name}.png')
        queue(textures / f'{name}.png.mcmeta', {'animation': {'frametime': 4, 'interpolate': False}})
    bucket = Image.new('RGBA', (16, 16))
    drawing = ImageDraw.Draw(bucket)
    drawing.rectangle((3, 5, 12, 11), fill='#c1b4a7')
    drawing.rectangle((4, 10, 11, 13), fill='#837d83')
    drawing.rectangle((5, 13, 10, 14), fill='#5a5661')
    drawing.rectangle((4, 5, 11, 7), fill='#9c152b')
    drawing.line((4, 4, 5, 2, 10, 2, 11, 4), fill='#c4c0b5')
    item_textures = assets / 'textures/item'
    item_textures.mkdir(parents=True, exist_ok=True)
    bucket.save(item_textures / 'sacrificial_blood_bucket.png')
    queue(assets / 'models/item/sacrificial_blood_bucket.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NAMESPACE}:item/sacrificial_blood_bucket'}})
    queue(assets / 'models/block/sacrificial_blood.json', {'textures': {'particle': f'{NAMESPACE}:block/sacrificial_blood_still'}})
    queue(assets / 'blockstates/sacrificial_blood.json', {'variants': {'': {'model': f'{NAMESPACE}:block/sacrificial_blood'}}})
    definitions_by_name = definitions()
    costs = []
    display = {'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.6, 0.6, 0.6]},
               'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
               'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]}}
    for name, source in definitions_by_name.items():
        elements = [element(source, shape, name in ('bloodstone_bricks', 'blood_rune_block')) for shape in source]
        elements = [value for value in elements if value['faces']]
        if len(elements) > 16 or sum(len(value['faces']) for value in elements) > 96:
            raise AssertionError('Per-block geometry budget exceeded')
        model = {'parent': 'minecraft:block/block', 'ambientocclusion': True,
                 'textures': {'atlas': f'{NAMESPACE}:block/sacrifice_stone', 'particle': f'{NAMESPACE}:block/bloodstone_bricks'},
                 'elements': elements, 'display': display}
        if name not in ('bloodstone_stairs', 'bloodstone_slab'):
            queue(assets / f'models/block/{name}.json', model)
        decorative = name not in ('bloodstone_bricks', 'bloodstone_stairs', 'bloodstone_slab')
        if decorative:
            queue(assets / f'models/block/{name}_lit.json', {'parent': f'{NAMESPACE}:block/{name}', 'textures': {'atlas': f'{NAMESPACE}:block/sacrifice_stone_lit'}})
            variants = {}
            for facing, rotation in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
                for phase in range(4 if name == 'blood_offering_altar' else 2):
                    illuminated = phase in (1, 2) if name == 'blood_offering_altar' else phase == 1
                    condition = f'facing={facing},' + (f'phase={phase}' if name == 'blood_offering_altar' else 'lit=' + str(bool(phase)).lower())
                    variants[condition] = {'model': f'{NAMESPACE}:block/{name}' + ('_lit' if illuminated else ''), 'y': rotation}
            queue(assets / f'blockstates/{name}.json', {'variants': variants})
        elif name == 'bloodstone_bricks':
            queue(assets / f'blockstates/{name}.json', {'variants': {'': {'model': f'{NAMESPACE}:block/{name}'}}})
        native_variants(assets, name)
        queue(assets / f'models/item/{name}.json', {'parent': f'{NAMESPACE}:block/{name}'})
        editable(project / f'model_sources/blood_pool/{name}.bbmodel', name, elements, idle)
        costs.append(dict(id=name, sourceCuboids=len(source), exportedCuboids=len(elements), faces=sum(len(value['faces']) for value in elements),
                          blockEntities=1 if name == 'blood_offering_altar' else 0))
        functions = [{'function': 'minecraft:set_count', 'count': 2, 'conditions': [{'condition': 'minecraft:block_state_property',
                      'block': f'{NAMESPACE}:{name}', 'properties': {'type': 'double'}}]}] if name == 'bloodstone_slab' else []
        queue(resources / f'data/{NAMESPACE}/loot_tables/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [
            {'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}], 'entries': [
                {'type': 'minecraft:item', 'name': f'{NAMESPACE}:{name}', 'functions': functions}]}]})
    queue(resources / f'data/{NAMESPACE}/tags/blocks/blood_pool_boundary.json', {'replace': False,
          'values': [f'{NAMESPACE}:{name}' for name in definitions_by_name if name not in ('bloodstone_stairs', 'bloodstone_slab', 'blood_skull_shrine', 'blood_offering_altar')]})
    for points, items in [(1, ['bone', 'rotten_flesh', 'gold_nugget']), (2, ['gold_ingot']), (4, ['emerald']),
                          (8, ['diamond', 'skeleton_skull', 'wither_skeleton_skull', 'zombie_head', 'creeper_head', 'piglin_head'])]:
        queue(resources / f'data/{NAMESPACE}/tags/items/blood_offerings/points_{points}.json', {'replace': False, 'values': ['minecraft:' + item for item in items]})
    path = resources / 'data/minecraft/tags/blocks/mineable/pickaxe.json'
    existing = json.loads(path.read_text(encoding='utf-8')) if path.exists() else {'replace': False, 'values': []}
    existing['values'] = sorted(set(existing['values']) | {f'{NAMESPACE}:{name}' for name in definitions_by_name})
    queue(path, existing)
    recipes(resources, definitions_by_name)
    translations(assets)
    flush()
    brazier_model = json.loads((assets / 'models/block/bloodthorn_brazier_unlit.json').read_text(encoding='utf-8'))
    brazier_texture = Image.open(textures / 'bloodthorn_brazier_unlit.png').convert('RGBA')
    brazier = [dict(name=value['name'], start=value['from'], end=value['to'], tile=0, overrides={},
                    nativeFaces=value['faces'], nativeTexture=brazier_texture) for value in brazier_model['elements']]
    preview(output, definitions_by_name, idle, lit, still, brazier)
    queue(output / 'model-cost.json', dict(blocks=costs, budgetCuboidsPerBlock=16, budgetFacesPerBlock=96,
          atlasEach=[64, 64], atlasCopies=2, fluidTextures=[[16, 256], [32, 512]], textureAnimationFrames=16,
          blockEntitiesPerCompletedPool=1, customRenderer=False, geometryFrameCopies=0,
          requiredBloodSources=44, borderCells=24, floorCells=69, referenceImagesUnmodified=True, actualFpsMeasured=False))
    flush()
    print(json.dumps(costs, ensure_ascii=False))


def native_variants(assets, name):
    if name == 'bloodstone_slab':
        for suffix, parent in [('', 'slab'), ('_top', 'slab_top')]:
            queue(assets / f'models/block/{name}{suffix}.json', {'parent': 'minecraft:block/' + parent,
                  'textures': {side: f'{NAMESPACE}:block/bloodstone_bricks' for side in ('top', 'bottom', 'side')}})
        queue(assets / f'blockstates/{name}.json', {'variants': {
            'type=bottom': {'model': f'{NAMESPACE}:block/{name}'}, 'type=top': {'model': f'{NAMESPACE}:block/{name}_top'},
            'type=double': {'model': f'{NAMESPACE}:block/bloodstone_bricks'}}})
    if name == 'bloodstone_stairs':
        for suffix, parent in [('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')]:
            queue(assets / f'models/block/{name}{suffix}.json', {'parent': 'minecraft:block/' + parent,
                  'textures': {side: f'{NAMESPACE}:block/bloodstone_bricks' for side in ('top', 'bottom', 'side')}})
        variants = {}
        for facing, base_rotation in [('east', 0), ('south', 90), ('west', 180), ('north', 270)]:
            for half in ('bottom', 'top'):
                for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                    rotation = base_rotation - (90 if shape.endswith('left') else 0)
                    if half == 'top' and shape != 'straight':
                        rotation += 90
                    suffix = '_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''
                    variants[f'facing={facing},half={half},shape={shape}'] = {'model': f'{NAMESPACE}:block/{name}{suffix}',
                                                                             'x': 180 if half == 'top' else 0, 'y': rotation % 360, 'uvlock': True}
        queue(assets / f'blockstates/{name}.json', {'variants': variants})


def preview(output, definitions_by_name, idle, lit, still, brazier):
    font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 22)
    sheet = Image.new('RGB', (1100, 1030), '#14141c')
    drawing = ImageDraw.Draw(sheet)
    for index, (name, source) in enumerate(definitions_by_name.items()):
        panel = render(source, lit, still, (350, 260))
        sheet.paste(panel, ((index % 3) * 365, (index // 3) * 320 + 40))
        drawing.text(((index % 3) * 365 + 10, (index // 3) * 320 + 9), name.replace('blood_', '').replace('bloodstone_', ''), font=font, fill='#dbc9b9')
    drawing.text((22, 995), '9 modular blocks / integer cuboids / software preview, not Minecraft', font=font, fill='#9a95a3')
    sheet.save(output / 'blood-pool-building-kit.png')
    states = Image.new('RGB', (2160, 970), '#14141c')
    drawing = ImageDraw.Draw(states)
    for index, (name, atlas) in enumerate([('IDLE - empty altar', idle), ('OFFERING / ACTIVE - runes lit', lit)]):
        panel = scene(definitions_by_name, atlas, still, brazier)
        states.paste(panel, (index * 1080, 55))
        drawing.text((index * 1080 + 30, 22), name, font=font, fill='#dbc9b9')
    drawing.text((28, 925), 'Reference-inspired modular blood pool. Redstone trigger + cancellable Forge event; no automatic waves or beam shader.', font=font, fill='#a59ba4')
    states.save(output / 'blood-pool-preview.png')


def recipes(resources, source):
    for name in source:
        if name == 'bloodstone_bricks':
            recipe = {'type': 'minecraft:crafting_shapeless', 'ingredients': [{'item': 'minecraft:polished_blackstone_bricks'}, {'item': 'minecraft:red_dye'}], 'result': {'item': f'{NAMESPACE}:{name}', 'count': 4}}
        elif name == 'bloodstone_stairs':
            recipe = {'type': 'minecraft:crafting_shaped', 'pattern': ['S  ', 'SS ', 'SSS'], 'key': {'S': {'item': f'{NAMESPACE}:bloodstone_bricks'}}, 'result': {'item': f'{NAMESPACE}:{name}', 'count': 4}}
        elif name == 'bloodstone_slab':
            recipe = {'type': 'minecraft:crafting_shaped', 'pattern': ['SSS'], 'key': {'S': {'item': f'{NAMESPACE}:bloodstone_bricks'}}, 'result': {'item': f'{NAMESPACE}:{name}', 'count': 6}}
        else:
            extras = {'blood_rune_block': ['redstone'], 'blood_pool_rim': ['iron_nugget'],
                      'blood_pool_corner': ['iron_nugget', 'iron_nugget'], 'blood_skull_relief': ['bone'],
                      'blood_skull_shrine': ['bone', 'gold_ingot'], 'blood_offering_altar': ['redstone', 'gold_ingot', 'bone']}
            recipe = {'type': 'minecraft:crafting_shapeless', 'ingredients': [{'item': f'{NAMESPACE}:bloodstone_bricks'}]
                      + [{'item': 'minecraft:' + extra} for extra in extras[name]],
                      'result': {'item': f'{NAMESPACE}:{name}', 'count': 1}}
        queue(resources / f'data/{NAMESPACE}/recipes/{name}.json', recipe)
    queue(resources / f'data/{NAMESPACE}/recipes/sacrificial_blood_bucket.json', {'type': 'minecraft:crafting_shapeless',
          'ingredients': [{'item': 'minecraft:bucket'}, {'item': 'minecraft:red_dye'}] + [{'item': 'minecraft:rotten_flesh'}] * 4,
          'result': {'item': f'{NAMESPACE}:sacrificial_blood_bucket', 'count': 1}})


def translations(assets):
    chinese = {'bloodstone_bricks': '血痕黑石砖', 'blood_rune_block': '血祭符文石', 'blood_pool_rim': '血池直沿',
               'blood_pool_corner': '血池阶梯转角', 'blood_skull_relief': '方颅浮雕石', 'blood_skull_shrine': '方颅祭像',
               'bloodstone_stairs': '血痕黑石楼梯', 'bloodstone_slab': '血痕黑石台阶', 'blood_offering_altar': '血池献祭台'}
    messages = {
        'busy': ('仪式正在激活或冷却中，不接收供品。', 'The ritual is active or cooling down; no offerings accepted.'),
        'charge': ('献祭充能 %s / %s；充满后空手右键确认激活。', 'Offering charge %s / %s. Empty-hand right-click when full.'),
        'ready': ('充能已满，请空手右键确认祭祀。', 'Ready. Empty-hand right-click to confirm the ritual.'),
        'invalid_offering': ('供品：骨头/腐肉/金粒1点，金锭2点，绿宝石4点，钻石/头颅8点。', 'Offer bones/flesh/nuggets (1), gold ingots (2), emeralds (4), diamonds or mob heads (8).'),
        'missing_blood': ('血池未满：需要44格源血液，流动血液不计。', 'Fill all 44 pool cells with source blood; flowing blood does not count.'),
        'missing_floor': ('血池底部必须有完整坚固方块，不能漏底。', 'The pool needs solid full-top supports beneath every blood cell.'),
        'missing_border': ('池沿不完整：按9×9阶梯八边池补齐24格边界。', 'Complete the 24 boundary cells of the stepped 9x9 octagonal pool.'),
        'missing_shrine': ('中央需要血痕黑石砖，其上放置朝向祭台的方颅祭像。', 'Place bloodstone bricks at the center, then a skull shrine facing the altar above.'),
        'missing_unloaded': ('血池结构越界或部分区块未加载。', 'Part of the pool is out of bounds or unloaded.'),
        'cancelled': ('祭祀被服务端扩展取消；充能未消耗。', 'A server extension cancelled activation; charge was kept.'),
        'activated': ('血祭已激活：输出15级红石10秒；随后冷却30秒。', 'Ritual activated: redstone 15 for 10 seconds, then 30 seconds cooldown.'),
        'build_bounds': ('搭建区域越界或区块未加载；未放置任何方块。', 'Build area is out of bounds or unloaded; nothing was placed.'),
        'build_occupied': ('搭建被 %s 的现有方块/液体阻挡；未放置任何方块。', 'An existing block/fluid at %s blocks construction; nothing was placed.'),
        'built': ('血池示例已搭建，共放置 %s 格；这是建筑与祭祀触发器，不自动开启怪物波次。', 'Built %s pool blocks. This provides a ritual trigger, not automatic monster waves.')}
    for language in ('zh_cn', 'en_us'):
        path = assets / f'lang/{language}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        for name, translated_name in chinese.items():
            data[f'block.{NAMESPACE}.{name}'] = translated_name if language == 'zh_cn' else name.replace('_', ' ').title()
        data[f'block.{NAMESPACE}.sacrificial_blood'] = '祭祀血液' if language == 'zh_cn' else 'Sacrificial Blood'
        data[f'fluid_type.{NAMESPACE}.sacrificial_blood'] = data[f'block.{NAMESPACE}.sacrificial_blood']
        data[f'item.{NAMESPACE}.sacrificial_blood_bucket'] = '祭祀血液桶' if language == 'zh_cn' else 'Sacrificial Blood Bucket'
        data[f'tooltip.{NAMESPACE}.blood_pool.altar'] = '9×9阶梯八边血池；16点供品→空手确认→红石/事件触发' if language == 'zh_cn' else '9x9 stepped pool; 16 offering points, then empty-hand confirm: redstone/event trigger'
        for key, values in messages.items():
            data[f'message.{NAMESPACE}.blood_pool.{key}'] = values[0 if language == 'zh_cn' else 1]
        queue(path, data)


if __name__ == '__main__':
    main()
