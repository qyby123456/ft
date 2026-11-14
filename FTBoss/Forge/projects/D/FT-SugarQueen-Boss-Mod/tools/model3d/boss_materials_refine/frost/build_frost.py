import base64
import json
import math
from pathlib import Path
import uuid

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[4]
ASSETS = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_materials/refine/frost'
WORK = ROOT / '_work/boss-materials-refine-20261021/frost'
SIZE = 128
DENSITY = 2
PALETTES = {
    'ice': ['284c70', '528bac', '8dcddd', 'c9edf0', 'f5fff2'],
    'frost': ['647f9c', 'a8c7d7', 'd9e9eb', 'f5f7e9', 'fffdf1'],
    'rose': ['542d54', '88446d', 'bd6d96', 'e8a7bf', 'ffe4df'],
    'gold': ['664634', 'a8753c', 'd6a65a', 'f4d58a', 'fff4bf'],
    'crystal': ['345477', '5c9dbd', '9cd6e4', 'd4f3f2', 'fffef1'],
    'shadow': ['263449', '344d67', '49708c', '7394ab', 'a6bccb'],
}
ELEMENTS = []
ISLANDS = []


def emit_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def colors(material):
    return [tuple(bytes.fromhex(c)) + (255,) for c in PALETTES[material]]


def cube(name, low, high, material='ice', rotation=None, surfaces=None):
    element = {'name': name, 'from': low, 'to': high, 'faces': {}}
    if rotation:
        axis, angle, origin = rotation
        element['rotation'] = {'axis': axis, 'angle': angle, 'origin': origin, 'rescale': False}
    dimensions = np.array(high) - np.array(low)
    for direction in (surfaces or ['north', 'south', 'west', 'east', 'up', 'down']):
        axes = {'north': [0, 1], 'south': [0, 1], 'west': [2, 1], 'east': [2, 1], 'up': [0, 2], 'down': [0, 2]}[direction]
        span = [float(dimensions[axis]) for axis in axes]
        width, height = [max(1, math.ceil(s * DENSITY)) for s in span]
        island = {'name': name, 'face': direction, 'width': width, 'height': height, 'span': span, 'material': material, 'element': element}
        ISLANDS.append(island)
    ELEMENTS.append(element)


def geometry():
    cube('01_crystalline_ingot_core', [2.8, 4.1, 5.0], [13.2, 7.9, 11.0])
    cube('02_lower_cold_stratum', [3.1, 3.75, 5.25], [12.9, 4.2, 10.75], 'shadow')
    cube('03_sugar_coat', [3.2, 7.78, 5.25], [12.8, 8.38, 10.75], 'frost')
    for side, xpos in [('left', 2.9), ('right', 13.1)]:
        cube(f'04_{side}_cut_end', [xpos - .95, 4.2, 6.5], [xpos + .95, 7.8, 9.5], 'ice', ('y', 45, [xpos, 6, 8]))
        cube(f'05_{side}_end_coat', [xpos - .88, 7.72, 6.55], [xpos + .88, 8.25, 9.45], 'frost', ('y', 45, [xpos, 8, 8]))
    for direction, zpos, sign in [('front', 5.02, 1), ('back', 10.98, -1)]:
        cube(f'06_{direction}_upper_chamfer', [3.15, 7.4, zpos - .35], [12.85, 8.1, zpos + .35], 'frost', ('x', 45, [8, 7.75, zpos]))
        cube(f'07_{direction}_lower_chamfer', [3.3, 3.95, zpos - .25], [12.7, 4.45, zpos + .25], 'ice', ('x', 45, [8, 4.2, zpos]))
        cube(f'08_{direction}_rose_sugar_seam', [3.65, 5.1, zpos - .08], [12.35, 5.65, zpos + .08], 'rose')
        for index, xpos in enumerate([4.0, 5.4, 10.6, 12.0]):
            depth = .16
            cube(f'09_{direction}_icing_drip_{index}', [xpos - .23, 6.8 - (index % 2) * .4, zpos - depth], [xpos + .23, 7.7, zpos + depth], 'frost')
    cube('10_rose_crown_stamp_field', [6.38, 5.65, 4.72], [9.62, 7.55, 5.06], 'rose')
    cube('11_stamp_lower_gold_edge', [6.45, 5.61, 4.62], [9.55, 5.88, 4.86], 'gold')
    cube('12_stamp_crown_band', [6.85, 6.05, 4.5], [9.15, 6.35, 4.78], 'gold')
    for index, (xpos, height) in enumerate([(7.05, .7), (8, 1.0), (8.95, .7)]):
        cube(f'13_stamp_crown_tip_{index}', [xpos - .21, 6.28, 4.53], [xpos + .21, 6.28 + height, 4.8], 'gold', ('z', [-22.5, 0, 22.5][index], [xpos, 6.28, 4.66]))
    cube('14_stamp_rose_jewel', [7.75, 6.1, 4.32], [8.25, 6.6, 4.58], 'rose', ('z', 45, [8, 6.35, 4.45]))
    for index, (xpos, zpos, length, angle) in enumerate([(5.05, 7.0, 2.45, -22.5), (8.1, 8.3, 3.8, 0), (10.9, 7.15, 2.1, 22.5)]):
        cube(f'20_crystal_{index}_root', [xpos - .8, 8.15, zpos - .72], [xpos + .8, 8.7, zpos + .72], 'rose')
        cube(f'21_crystal_{index}_prism', [xpos - .62, 8.42, zpos - .58], [xpos + .62, 8.42 + length, zpos + .58], 'crystal', ('z', angle, [xpos, 8.42, zpos]))
        top_x = xpos - math.sin(math.radians(angle)) * (length - .16)
        top_y = 8.42 + math.cos(math.radians(angle)) * (length - .16)
        cube(f'22_crystal_{index}_point', [top_x - .58, top_y - .56, zpos - .54], [top_x + .58, top_y + .56, zpos + .54], 'crystal', ('z', 45, [top_x, top_y, zpos]))
        cube(f'23_crystal_{index}_edge_facet', [xpos - .77, 8.5, zpos - .43], [xpos - .52, 8.1 + length, zpos + .43], 'ice', ('z', angle, [xpos, 8.42, zpos]))
    for index, (xpos, zpos, angle) in enumerate([(4.15, 9.8, -22.5), (10.8, 10.0, 22.5)]):
        cube(f'30_rear_seed_{index}', [xpos - .62, 8.0, zpos - .5], [xpos + .62, 9.15, zpos + .5], 'crystal', ('z', angle, [xpos, 8.2, zpos]))
    for index, (xpos, zpos) in enumerate([(4.6, 5.7), (6.9, 9.9), (9.6, 5.8), (11.8, 9.2)]):
        cube(f'31_surface_sugar_grain_{index}', [xpos - .3, 8.21, zpos - .3], [xpos + .3, 8.6, zpos + .3], 'frost', ('y', 45, [xpos, 8.4, zpos]))


def atlas(painter=None):
    canvas = Image.new('RGBA', (SIZE, SIZE))
    cursor_x, cursor_y, row_height = 1, 1, 0
    for island in sorted(ISLANDS, key=lambda entry: -entry['height']):
        width, height = island['width'], island['height']
        if cursor_x + width + 2 >= SIZE:
            cursor_x, cursor_y, row_height = 1, cursor_y + row_height + 3, 0
        if cursor_y + height + 2 >= SIZE:
            raise ValueError('Frost atlas packing overflow')
        island['rect'] = [cursor_x, cursor_y, cursor_x + width, cursor_y + height]
        span_x, span_y = island['span']
        sample_w, sample_h = span_x * DENSITY, span_y * DENSITY
        inset_x, inset_y = (width - sample_w) / 2, (height - sample_h) / 2
        uv = [(cursor_x + inset_x) * 16 / SIZE, (cursor_y + inset_y) * 16 / SIZE, (cursor_x + inset_x + sample_w) * 16 / SIZE, (cursor_y + inset_y + sample_h) * 16 / SIZE]
        island['element']['faces'][island['face']] = {'uv': [round(v, 4) for v in uv], 'texture': '#atlas'}
        patch = (painter or paint)(island)
        canvas.paste(patch, (cursor_x, cursor_y))
        expanded = patch.resize((width + 2, height + 2), Image.Resampling.NEAREST)
        canvas.paste(expanded, (cursor_x - 1, cursor_y - 1))
        canvas.paste(patch, (cursor_x, cursor_y))
        cursor_x += width + 3
        row_height = max(row_height, height)
    return canvas


def paint(island):
    width, height = island['width'], island['height']
    material = island['material']
    palette = colors(material)
    patch = Image.new('RGBA', (width, height))
    draw = ImageDraw.Draw(patch)
    for py in range(height):
        for px in range(width):
            normalized = (px + .5) / width
            vertical = (py + .5) / height
            level = 2
            if vertical < .2: level = 3
            if vertical > .75: level = 1
            if material in ('ice', 'crystal'):
                facet = normalized + vertical * .24
                level = 3 if .20 < facet < .50 else 2
                if facet > .86: level = 1
                if vertical < .12: level = 4
            if island['face'] == 'down': level = min(level, 1)
            if material == 'frost':
                level = 3 if vertical < .67 else 2
                if (px + 2 * py) % 11 == 0 and width > 4 and height > 4: level -= 1
            if material == 'gold': level = 4 if vertical < .22 else 3 if vertical < .65 else 1
            patch.putpixel((px, py), palette[level])
    if width >= 7 and height >= 5:
        draw.line((1, 0, width - 2, 0), fill=palette[4])
        draw.line((width - 1, 1, width - 1, height - 1), fill=palette[1])
        draw.line((1, height - 1, width - 2, height - 1), fill=palette[1])
        if material in ('ice', 'crystal'):
            start = max(1, width // 3)
            draw.line([(start, height - 2), (start + 1, height // 2), (min(width - 2, start + 4), 1)], fill=palette[4], width=1)
            if width > 14:
                draw.line([(width - 4, height - 3), (width - 7, height // 2), (width - 6, height // 2 - 2)], fill=palette[0])
                draw.line([(width - 5, height - 3), (width - 8, height // 2)], fill=palette[2])
        elif material == 'rose' and width > 7:
            for px in range(2, width - 1, 5):
                draw.line((px, 1, px + 1, height - 2), fill=palette[3])
    return patch


def make_display():
    return {
        'gui': {'rotation': [25, -35, 0], 'scale': [.9, .9, .9]},
        'ground': {'rotation': [0, 0, 0], 'translation': [0, 2.3, 0], 'scale': [.52, .52, .52]},
        'fixed': {'rotation': [0, 180, 0], 'scale': [.8, .8, .8]},
        'head': {'rotation': [0, 180, 0], 'translation': [0, 10, 0], 'scale': [1, 1, 1]},
        'thirdperson_righthand': {'rotation': [0, -90, 0], 'translation': [0, 1, -1], 'scale': [.62, .62, .62]},
        'thirdperson_lefthand': {'rotation': [0, 90, 0], 'translation': [0, 1, -1], 'scale': [.62, .62, .62]},
        'firstperson_righthand': {'rotation': [0, -25, 0], 'translation': [1.5, 1, 0], 'scale': [.72, .72, .72]},
        'firstperson_lefthand': {'rotation': [0, 25, 0], 'translation': [1.5, 1, 0], 'scale': [.72, .72, .72]},
    }


def build():
    ART.mkdir(parents=True, exist_ok=True)
    geometry()
    texture = atlas()
    texture_path = ASSETS / 'textures/item/boss_materials/frost.png'
    texture.save(texture_path)
    model = {'ambientocclusion': False, 'textures': {'atlas': 'ftboss:item/boss_materials/frost', 'particle': 'ftboss:item/frost_material'}, 'elements': ELEMENTS, 'display': make_display()}
    emit_json(ASSETS / 'models/item/boss_materials/frost_material.json', model)
    elements = []
    identifiers = []
    for element in ELEMENTS:
        identifier = str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost/' + element['name']))
        identifiers.append(identifier)
        rotation = element.get('rotation', {})
        angles = [0, 0, 0]
        if rotation: angles['xyz'.index(rotation['axis'])] = rotation['angle']
        elements.append({'name': element['name'], 'type': 'cube', 'uuid': identifier, 'from': element['from'], 'to': element['to'], 'origin': rotation.get('origin', [8, 8, 8]), 'rotation': angles, 'rescale': rotation.get('rescale', False), 'autouv': 0, 'faces': {face: {'uv': [v * SIZE / 16 for v in data['uv']], 'texture': 0} for face, data in element['faces'].items()}})
    texture_data = base64.b64encode(texture_path.read_bytes()).decode('ascii')
    bbmodel = {'meta': {'format_version': '4.10', 'model_format': 'java_block', 'box_uv': False}, 'name': '霜糖晶冠 / 精制霜糖晶锭', 'model_identifier': 'frost_material', 'resolution': {'width': SIZE, 'height': SIZE}, 'elements': elements, 'outliner': [{'name': 'Frost sugar ingot', 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost/root')), 'origin': [8, 8, 8], 'children': identifiers}], 'textures': [{'name': 'frost.png', 'id': '0', 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost/texture')), 'relative_path': '../../../src/main/resources/assets/ftboss/textures/item/boss_materials/frost.png', 'namespace': 'ftboss', 'folder': 'item/boss_materials', 'width': SIZE, 'height': SIZE, 'uv_width': SIZE, 'uv_height': SIZE, 'source': 'data:image/png;base64,' + texture_data}], 'display': model['display']}
    emit_json(ROOT / 'art/boss_materials/blockbench/frost_material.bbmodel', bbmodel)
    emit_json(ART / 'uv-islands.json', [{key: value for key, value in island.items() if key != 'element'} for island in ISLANDS])
    print(f'Built frost only: {len(ELEMENTS)} elements / {len(ISLANDS)} per-face islands / {SIZE}px atlas / {DENSITY}px per unit')


if __name__ == '__main__':
    build()
