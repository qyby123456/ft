import base64
import hashlib
import io
import json
import math
import uuid
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[4]
RES = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_materials/refine/butcher'
WORK = ROOT / '_work/boss-materials-refine-20261021/butcher'
MODEL = RES / 'models/item/boss_materials/butcher_material.json'
ATLAS = RES / 'textures/item/boss_materials/butcher.png'
ICON = RES / 'textures/item/butcher_material.png'
BBMODEL = ROOT / 'art/boss_materials/blockbench/butcher_material.bbmodel'
SIDES = ('north', 'south', 'west', 'east', 'up', 'down')
PALETTE = {
    'cortex': ['#5d4a40', '#8d7254', '#b9a278', '#d7c295', '#eddbb0'],
    'enamel': ['#877253', '#bba577', '#e3d5ad', '#f7edce', '#fff7df'],
    'cut': ['#725745', '#aa8b60', '#d7bc87', '#f2dda8', '#fff0c7'],
    'marrow': ['#391e26', '#612a35', '#913843', '#b85459', '#da8477'],
    'fascia': ['#351e27', '#56232e', '#842f3c', '#aa4752', '#ce776f'],
    'blood': ['#381e26', '#632733', '#9b3545', '#bc4c55', '#d87a76'],
}
NAMESPACE = uuid.UUID('8a0a3c5c-8728-4fb3-97f5-23faec700c44')
DENSITY = 2
TEX_SIZE = 128


def save_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def box(name, start, size, material='cortex', angle=0, origin=None, overrides=None):
    end = [round(start[index] + size[index], 4) for index in range(3)]
    start = [round(value, 4) for value in start]
    origin = origin or [round((start[index] + end[index]) / 2, 4) for index in range(3)]
    return {'name': name, 'from': start, 'to': end, 'material': material, 'angle': angle, 'origin': origin, 'overrides': overrides or {}}


def tapered_fang(name, base_left, base_right, base_y, lean_right, layers):
    width = base_right - base_left
    height = width / math.tan(math.radians(22.5))
    apex = base_right if lean_right else base_left
    parts = []
    step = height / layers
    for index in range(layers):
        fraction = (index + 0.4) / layers
        remaining = 1 - fraction
        center_x = (base_left + base_right) / 2 * remaining + apex * fraction
        section_width = width * remaining + 0.035
        depth = 2.1
        parts.append(box(f'{name}_taper_{index:02}', [center_x - section_width / 2, base_y + step * index, 8.0 - depth / 2], [section_width, step, depth], 'enamel'))
    length = height / math.cos(math.radians(22.5))
    root = base_left if lean_right else base_right
    angle = -22.5 if lean_right else 22.5
    parts.append(box(f'{name}_inclined_cutting_face', [root - 0.14, base_y, 6.85], [0.28, length, 2.3], 'enamel', angle, [root, base_y, 8.0]))
    parts.append(box(f'{name}_straight_arris', [apex - 0.135, base_y + 0.013, 6.875], [0.27, height - 0.038, 2.25], 'enamel'))
    parts.append(box(f'{name}_point', [apex - 0.135, base_y + height - 0.16, 6.95], [0.27, 0.27, 2.1], 'enamel', 45))
    return parts


def geometry():
    parts = [
        box('mandible_cortex', [4.35, 4.15, 6.6], [8.3, 2.75, 3.15], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('lower_cut_bevel', [4.55, 3.9, 6.95], [7.65, 0.7, 2.4], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('front_cortical_plate', [4.65, 4.65, 6.1], [7.6, 1.9, 0.65], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('back_cortical_plate', [4.85, 4.7, 9.55], [7.1, 1.8, 0.6], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('top_alveolar_ridge', [4.5, 6.4, 6.5], [8.1, 0.75, 3.2], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('lower_cortical_lip', [6.3, 4.0, 6.2], [4.1, 0.5, 0.7], angle=22.5, origin=[8.5, 5.5, 8.2]),
        box('heel_lamella', [3.65, 4.0, 7.1], [1.9, 2.6, 2.3], angle=-22.5, origin=[4.6, 5.3, 8.2], overrides={'west': 'cut'}),
        box('broken_spicule', [3.2, 3.55, 7.4], [1.0, 1.9, 1.55], angle=45, origin=[4.1, 4.5, 8.2], overrides={'north': 'cut'}),
        box('fracture_rim_top', [3.8, 5.85, 5.94], [1.8, 0.4, 0.75], 'cut'),
        box('fracture_rim_bottom', [3.8, 3.68, 5.92], [1.8, 0.4, 0.75], 'cut'),
        box('fracture_rim_left', [3.03, 4.4, 5.91], [0.42, 1.1, 0.75], 'cut'),
        box('fracture_rim_right', [5.97, 4.4, 5.93], [0.42, 1.1, 0.75], 'cut'),
        box('fracture_bevel_upper_left', [3.0, 5.54, 5.88], [1.15, 0.42, 0.8], 'cut', 45),
        box('fracture_bevel_upper_right', [5.28, 5.54, 5.87], [1.15, 0.42, 0.78], 'cut', -45),
        box('fracture_bevel_lower_left', [3.0, 3.95, 5.86], [1.15, 0.42, 0.78], 'cut', -45),
        box('fracture_bevel_lower_right', [5.28, 3.95, 5.85], [1.15, 0.42, 0.78], 'cut', 45),
        box('recessed_marrow_cut', [3.35, 4.05, 6.27], [2.75, 1.85, 0.5], 'marrow'),
        box('rear_marrow_reveal', [4.12, 4.1, 9.72], [1.6, 1.2, 0.45], 'marrow', -22.5),
        box('rear_cortex_chip', [3.7, 4.0, 9.75], [0.55, 1.85, 0.55], 'cut', -22.5),
        box('large_fang_socket', [6.0, 6.2, 6.2], [2.7, 1.35, 3.6], 'cut', 22.5, [7.35, 6.9, 8.0]),
        box('short_fang_socket', [9.8, 7.65, 6.45], [2.4, 1.15, 3.15], 'cut', -22.5, [10.95, 8.2, 8.0]),
        box('snapped_intermediate_tooth', [8.4, 7.55, 6.85], [1.35, 1.65, 2.4], 'enamel', -22.5, overrides={'up': 'cut'}),
        box('snapped_tooth_shard', [8.5, 8.5, 7.02], [0.65, 0.85, 1.95], 'cut', 22.5),
        box('fascia_underside', [6.15, 3.95, 7.3], [3.1, 0.85, 2.35], 'fascia', 22.5, [7.7, 4.4, 8.45]),
        box('fascia_front_bridge', [6.35, 5.85, 5.52], [3.7, 0.85, 0.68], 'fascia', 22.5, [8.2, 6.25, 5.86]),
        box('fascia_front_loop', [7.45, 5.8, 5.48], [0.82, 2.65, 0.8], 'fascia', -22.5, [7.86, 7.1, 5.88]),
        box('fascia_crown_wrap', [7.1, 7.9, 5.82], [1.05, 0.7, 4.45], 'fascia', -22.5, [7.63, 8.25, 8.05]),
        box('fascia_rear_loop', [7.42, 5.9, 9.85], [0.85, 2.6, 0.75], 'fascia', -22.5, [7.85, 7.2, 10.23]),
        box('fascia_rear_bridge', [6.4, 5.85, 9.9], [3.5, 0.78, 0.65], 'fascia', 22.5),
        box('tendon_pale_filament', [6.5, 6.34, 5.35], [3.0, 0.2, 0.25], 'fascia', 22.5, overrides={'north': 'cut'}),
        box('tendon_twisted_strand', [7.7, 6.25, 5.22], [0.25, 1.85, 0.35], 'fascia', -22.5),
        box('tendon_knotted_tail', [8.95, 5.25, 5.55], [0.65, 1.15, 0.7], 'fascia', 45),
        box('root_blood_fold', [10.25, 7.65, 6.08], [1.5, 0.48, 0.62], 'blood', -22.5),
        box('blood_residue_front', [9.2, 5.95, 5.83], [1.5, 0.65, 0.22], 'blood', 22.5),
        box('blood_residue_cut', [5.45, 4.1, 5.69], [0.78, 0.55, 0.18], 'blood', 22.5),
        box('blood_residue_rear', [6.75, 4.95, 10.12], [1.55, 0.65, 0.2], 'blood', -22.5),
        box('marrow_bone_splinter', [4.3, 4.55, 6.05], [0.26, 0.65, 0.36], 'cut', -22.5),
        box('cortical_fracture_chip', [5.3, 4.5, 5.71], [0.72, 0.35, 0.38], 'cut', 45),
    ]
    parts += tapered_fang('large_fang', 5.6, 8.4, 7.2, False, 17)
    parts += tapered_fang('short_fang', 10.5, 12.0, 8.6, True, 10)
    return parts


def face_size(part, face):
    width, height, depth = [part['to'][axis] - part['from'][axis] for axis in range(3)]
    return (width, height) if face in ('north', 'south') else (depth, height) if face in ('west', 'east') else (width, depth)


def material_face(width, height, material, face, name):
    colors = PALETTE[material]
    tile = Image.new('RGBA', (width, height), colors[2])
    draw = ImageDraw.Draw(tile)
    seed = hashlib.sha256((name + face).encode('ascii')).digest()
    if '_taper_' in name:
        if face in ('up', 'down'):
            draw.rectangle((0, 0, width - 1, height - 1), fill=colors[2] if face == 'up' else colors[1])
        else:
            draw.rectangle((0, 0, width - 1, height - 1), fill=colors[3] if face == 'north' else colors[2])
            if width >= 3:
                draw.line((0, 0, 0, height - 1), fill=colors[4])
                draw.line((width - 1, 0, width - 1, height - 1), fill=colors[1])
        return tile
    upper = max(0, height // 3)
    if material in ('cortex', 'cut', 'enamel'):
        draw.rectangle((0, 0, width - 1, upper), fill=colors[3])
        draw.rectangle((0, max(0, height - max(1, height // 4)), width - 1, height - 1), fill=colors[1])
        draw.line((0, 0, max(0, width - 2), 0), fill=colors[4])
        if width > 3:
            draw.line((width - 1, 1, width - 1, height - 1), fill=colors[0])
        if material == 'enamel':
            ridge = max(0, width // 3)
            draw.line((ridge, 1, max(0, ridge - 1), max(1, height - 2)), fill=colors[4])
            if width >= 5 and height >= 7:
                draw.line((width - 2, height // 2, width - 2, height - 2), fill=colors[1])
        elif material == 'cut':
            if width >= 4 and height >= 4:
                draw.line((1, height - 2, width // 2, height // 2, width - 2, height // 2 + 1), fill=colors[0])
                draw.point((max(0, width - 3), max(0, height - 2)), fill=colors[4])
        elif width >= 6 and height >= 4:
            crack_x = 2 + seed[0] % (width - 3)
            crack_y = max(1, height // 2)
            draw.line((crack_x, crack_y, max(1, crack_x - 1), min(height - 2, crack_y + 1)), fill=colors[0])
            draw.line((1, max(1, height - 2), max(2, width // 2), max(1, height - 3)), fill=colors[3])
        if face == 'south':
            shade = Image.new('RGBA', tile.size, '#584e4a')
            tile = Image.blend(tile, shade, 0.13)
    else:
        draw.rectangle((0, 0, width - 1, max(0, height // 3)), fill=colors[3])
        draw.rectangle((0, height - 1, width - 1, height - 1), fill=colors[0])
        if width >= 3:
            draw.line((0, max(0, height - 2), width - 1, max(0, height // 3)), fill=colors[1])
        if material == 'fascia':
            draw.line((0, max(0, height // 2), width - 1, max(0, height // 2 - 1)), fill=colors[4])
            if height >= 5:
                draw.line((0, height - 2, width - 1, height - 3), fill=colors[2])
        elif material == 'marrow' and width >= 3 and height >= 3:
            draw.rectangle((1, 1, max(1, width - 2), max(1, height - 2)), fill=colors[1])
            draw.point((width // 2, height // 2), fill=colors[4])
            draw.point((max(0, width // 2 - 1), height // 2), fill='#c8a78a')
        elif material == 'blood' and width >= 3:
            draw.line((0, 0, max(1, width - 2), 0), fill=colors[4])
    return tile


def build_assets(parts):
    islands = []
    for index, part in enumerate(parts):
        for face in SIDES:
            units_u, units_v = face_size(part, face)
            pixels_u = units_u * DENSITY
            pixels_v = units_v * DENSITY
            islands.append({'part': index, 'face': face, 'span': [pixels_u, pixels_v], 'size': [max(1, math.ceil(pixels_u)), max(1, math.ceil(pixels_v))]})
    islands.sort(key=lambda island: (-island['size'][1], -island['size'][0], island['part'], island['face']))
    atlas = Image.new('RGBA', (TEX_SIZE, TEX_SIZE), (0, 0, 0, 0))
    cursor_x, cursor_y, row_height = 1, 1, 0
    slots = {}
    for island in islands:
        width, height = island['size']
        if cursor_x + width + 2 > TEX_SIZE:
            cursor_x = 1
            cursor_y += row_height + 2
            row_height = 0
        if cursor_y + height + 1 >= TEX_SIZE:
            raise ValueError('Atlas packing overflow')
        part = parts[island['part']]
        material = part['overrides'].get(island['face'], part['material'])
        tile = material_face(width, height, material, island['face'], part['name'])
        atlas.paste(tile, (cursor_x, cursor_y))
        atlas.paste(tile.crop((0, 0, width, 1)), (cursor_x, cursor_y - 1))
        atlas.paste(tile.crop((0, height - 1, width, height)), (cursor_x, cursor_y + height))
        atlas.paste(tile.crop((0, 0, 1, height)), (cursor_x - 1, cursor_y))
        atlas.paste(tile.crop((width - 1, 0, width, height)), (cursor_x + width, cursor_y))
        for offset_x, source_x in [(-1, 0), (width, width - 1)]:
            for offset_y, source_y in [(-1, 0), (height, height - 1)]:
                atlas.putpixel((cursor_x + offset_x, cursor_y + offset_y), tile.getpixel((source_x, source_y)))
        uv = [cursor_x / 8, cursor_y / 8, (cursor_x + island['span'][0]) / 8, (cursor_y + island['span'][1]) / 8]
        slots[(island['part'], island['face'])] = {'uv': [round(value, 5) for value in uv], 'texture': '#atlas'}
        island['position'] = [cursor_x, cursor_y]
        island['material'] = material
        cursor_x += width + 2
        row_height = max(row_height, height)
    elements = []
    for index, part in enumerate(parts):
        element = {'from': part['from'], 'to': part['to'], 'faces': {face: slots[(index, face)] for face in SIDES}}
        if part['angle']:
            element['rotation'] = {'axis': 'z', 'angle': part['angle'], 'origin': part['origin'], 'rescale': False}
        elements.append(element)
    return atlas, elements, islands


def blockbench(parts, model, atlas):
    source = WORK / 'backup-before-refine/art/boss_materials/blockbench/butcher_material.bbmodel'
    project = json.loads(source.read_text(encoding='utf-8'))
    uuids = [str(uuid.uuid5(NAMESPACE, part['name'])) for part in parts]
    elements = []
    for part, runtime, identifier in zip(parts, model['elements'], uuids):
        elements.append({'name': part['name'], 'type': 'cube', 'uuid': identifier, 'from': part['from'], 'to': part['to'],
                         'origin': part['origin'], 'rotation': [0, 0, part['angle']], 'rescale': False, 'autouv': 0, 'color': 0,
                         'faces': {face: {'uv': [round(value * 8, 5) for value in data['uv']], 'texture': 0} for face, data in runtime['faces'].items()}})
    buffer = io.BytesIO()
    atlas.save(buffer, format='PNG')
    project.update({'name': 'Butcher Bone Fang Material', 'resolution': {'width': TEX_SIZE, 'height': TEX_SIZE}, 'elements': elements, 'outliner': uuids, 'display': model['display']})
    texture = project['textures'][0]
    texture.update({'name': 'butcher.png', 'mode': 'bitmap', 'source': 'data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode('ascii'),
                    'path': '', 'width': TEX_SIZE, 'height': TEX_SIZE, 'uv_width': TEX_SIZE, 'uv_height': TEX_SIZE, 'particle': True})
    project['textures'] = [texture]
    save_json(BBMODEL, project)


def main():
    original = json.loads((WORK / 'backup-before-refine/src/main/resources/assets/ftboss/models/item/boss_materials/butcher_material.json').read_text(encoding='utf-8'))
    parts = geometry()
    atlas, elements, islands = build_assets(parts)
    model = {'ambientocclusion': False, 'gui_light': 'front', 'textures': {'atlas': 'ftboss:item/boss_materials/butcher', 'particle': 'ftboss:item/butcher_material'}, 'elements': elements, 'display': original['display']}
    save_json(MODEL, model)
    atlas.save(ATLAS)
    blockbench(parts, model, atlas)
    save_json(WORK / 'geometry-manifest.json', {'parts': parts, 'uv_islands': islands, 'texture_size': TEX_SIZE, 'pixels_per_unit': DENSITY})
    print(f'Generated {len(parts)} connected parts / {len(islands)} individual UV islands / {TEX_SIZE} square atlas')


if __name__ == '__main__':
    main()
