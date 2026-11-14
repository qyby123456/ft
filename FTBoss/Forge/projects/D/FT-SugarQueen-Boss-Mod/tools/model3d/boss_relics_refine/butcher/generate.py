import base64
import hashlib
import importlib.util
import io
import json
import math
import uuid
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[4]
RES = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_relics/refine/butcher'
WORK = ROOT / '_work/boss-relics-refine-20261021/butcher'
MODEL = RES / 'models/item/boss_relics/butcher_fang_charm.json'
ENTRY = RES / 'models/item/butcher_fang_charm.json'
GUI = RES / 'models/item/boss_relics/butcher_fang_charm_gui.json'
ATLAS = RES / 'textures/item/boss_relics/butcher_fang_charm.png'
ICON = RES / 'textures/item/butcher_fang_charm.png'
BBMODEL = ROOT / 'art/boss_relics/blockbench/butcher_fang_charm.bbmodel'
LIB_PATH = ROOT / 'tools/model3d/boss_materials_refine/butcher/generate.py'
SPEC = importlib.util.spec_from_file_location('butcher_material_generator', LIB_PATH)
LIB = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(LIB)
NAMESPACE = uuid.UUID('09c40bc4-5164-407a-b5b0-5dde134492fa')
GAMEPLAY = [
    RES / 'lang/zh_cn.json',
    ROOT / 'src/main/java/net/farmingtales/ftboss/item/BossRelicItem.java',
    ROOT / 'src/main/java/net/farmingtales/ftboss/util/BossRelicRules.java',
    ROOT / 'src/main/java/net/farmingtales/ftboss/item/FTBossItems.java',
    ROOT / 'src/main/resources/data/curios/tags/items/boss_relic.json',
    ROOT / 'src/main/resources/data/ftboss/curios/slots/boss_relic.json',
]


BASE_MATERIAL_FACE = LIB.material_face


def material_face(width, height, material, face, name):
    if material not in ('brass', 'steel', 'leather'):
        return BASE_MATERIAL_FACE(width, height, material, face, name)
    colors = LIB.PALETTE[material]
    tile = Image.new('RGBA', (width, height), colors[2])
    draw = ImageDraw.Draw(tile)
    if material in ('brass', 'steel'):
        draw.line((0, 0, width - 1, 0), fill=colors[4])
        draw.line((0, 0, 0, height - 1), fill=colors[3])
        if width >= 3:
            draw.line((width - 1, 1, width - 1, height - 1), fill=colors[0])
        if height >= 3:
            draw.line((1, height - 1, width - 1, height - 1), fill=colors[1])
        if height >= 6:
            draw.line((min(1, width - 1), 2, min(1, width - 1), height // 2), fill=colors[4])
        if width >= 7:
            draw.line((2, min(1, height - 1), width // 2, min(1, height - 1)), fill=colors[3])
            draw.point((width - 3, height - 1), fill=colors[0])
    else:
        draw.rectangle((0, 0, width - 1, max(0, height // 3)), fill=colors[3])
        draw.line((0, height - 1, width - 1, height - 1), fill=colors[0])
        if width > 4 and height > 2:
            for position in range(1, width - 1, 3):
                draw.point((position, 1), fill=colors[4])
            draw.line((1, height - 2, max(1, width - 2), height - 2), fill=colors[1])
    return tile


def save_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def box(name, start, size, material='brass', angle=0, origin=None, overrides=None):
    return LIB.box(name, start, size, material, angle, origin, overrides)


def octagon(name, center, width, height, thickness, depth, material, bevel):
    center_x, center_y, center_z = center
    half_x, half_y = width / 2, height / 2
    left, right = center_x - half_x, center_x + half_x
    bottom, top = center_y - half_y, center_y + half_y
    parts = [
        box(name + '_top', [left + bevel, top - thickness / 2, center_z - depth / 2], [width - 2 * bevel, thickness, depth], material),
        box(name + '_bottom', [left + bevel, bottom - thickness / 2, center_z - depth / 2 + 0.014], [width - 2 * bevel, thickness, depth], material),
        box(name + '_left', [left - thickness / 2, bottom + bevel, center_z - depth / 2 + 0.026], [thickness, height - 2 * bevel, depth], material),
        box(name + '_right', [right - thickness / 2, bottom + bevel, center_z - depth / 2 + 0.038], [thickness, height - 2 * bevel, depth], material),
    ]
    diagonals = [
        ('upper_left', [left + bevel / 2, top - bevel / 2], 45),
        ('upper_right', [right - bevel / 2, top - bevel / 2], -45),
        ('lower_left', [left + bevel / 2, bottom + bevel / 2], -45),
        ('lower_right', [right - bevel / 2, bottom + bevel / 2], 45),
    ]
    length = math.sqrt(2) * bevel + 0.16
    for index, (suffix, point, angle) in enumerate(diagonals):
        parts.append(box(name + '_' + suffix, [point[0] - length / 2, point[1] - thickness / 2, center_z - depth / 2 - 0.012 * (index + 1)], [length, thickness, depth], material, angle))
    return parts


def geometry():
    parts = octagon('forged_neck_ring', [8.0, 12.85, 8.32], 6.4, 3.8, 0.42, 0.7, 'brass', 0.95)
    parts += octagon('swivel_eye', [8.0, 10.35, 7.95], 1.2, 1.65, 0.34, 0.68, 'steel', 0.28)
    parts += [
        box('swivel_pin', [7.72, 9.45, 7.37], [0.56, 1.0, 1.32], 'brass'),
        box('harness_backplate', [5.55, 7.55, 7.65], [4.9, 1.75, 1.65], 'leather'),
        box('harness_raised_crest', [5.95, 8.85, 7.7], [4.1, 0.78, 1.5], 'leather'),
        box('harness_left_chamfer', [5.03, 7.92, 7.67], [1.4, 0.95, 1.6], 'leather', 45),
        box('harness_right_chamfer', [9.57, 7.92, 7.7], [1.4, 0.95, 1.55], 'leather', -45),
        box('harness_lower_tongue', [6.6, 6.55, 7.62], [2.8, 1.1, 1.65], 'leather'),
        box('crimson_front_inlay', [5.76, 7.69, 7.22], [4.48, 1.42, 0.51], 'fascia'),
        box('rear_cushion_liner', [6.04, 7.82, 9.18], [3.96, 1.1, 0.4], 'leather'),
        box('gilt_top_rail', [5.94, 9.18, 7.04], [4.12, 0.35, 0.55], 'brass'),
        box('gilt_bottom_rail', [6.22, 7.3, 7.05], [3.56, 0.36, 0.54], 'brass'),
        box('gilt_left_shoulderguard', [5.11, 8.25, 7.0], [1.25, 0.35, 0.54], 'brass', 45),
        box('gilt_right_shoulderguard', [9.64, 8.25, 7.012], [1.25, 0.35, 0.54], 'brass', -45),
        box('gilt_left_clamp', [5.35, 7.62, 6.965], [0.35, 1.02, 2.56], 'brass', 22.5),
        box('gilt_right_clamp', [10.31, 7.62, 7.02], [0.35, 1.02, 2.52], 'brass', -22.5),
        box('rear_gilt_rail', [6.1, 9.02, 9.46], [3.8, 0.32, 0.31], 'brass'),
        box('front_lashing_upper', [6.14, 8.43, 6.8], [3.72, 0.39, 0.45], 'fascia', -22.5),
        box('front_lashing_lower', [6.1, 7.95, 6.72], [3.8, 0.32, 0.45], 'fascia', 22.5),
        box('rear_lashing', [6.18, 8.15, 9.57], [3.65, 0.43, 0.4], 'fascia', 22.5),
        box('lashing_left_return', [5.98, 8.28, 7.15], [0.47, 0.43, 2.6], 'fascia', 22.5),
        box('lashing_right_return', [9.63, 8.28, 7.17], [0.47, 0.43, 2.56], 'fascia', -22.5),
        box('antique_bone_seal', [7.26, 8.04, 6.42], [1.45, 1.05, 0.48], 'cut'),
        box('seal_bevel_top', [7.4, 9.02, 6.51], [1.16, 0.28, 0.36], 'brass'),
        box('seal_bevel_bottom', [7.4, 7.81, 6.5], [1.16, 0.28, 0.35], 'brass'),
        box('seal_bevel_left', [7.05, 8.14, 6.5], [0.3, 0.78, 0.36], 'brass'),
        box('seal_bevel_right', [8.63, 8.14, 6.52], [0.3, 0.78, 0.34], 'brass'),
        box('seal_tooth_rune', [7.91, 8.18, 6.26], [0.22, 0.64, 0.23], 'blood', 22.5),
        box('seal_cross_rune', [7.55, 8.53, 6.25], [0.78, 0.19, 0.23], 'blood'),
        box('fang_root_collar', [6.6, 6.88, 6.68], [2.9, 0.52, 2.58], 'brass'),
        box('collar_crimson_recess', [6.85, 6.94, 6.47], [2.4, 0.31, 0.31], 'blood'),
    ]
    for index, (position_x, position_y) in enumerate([(5.85, 8.72), (10.0, 8.72), (6.35, 7.66), (9.5, 7.66)]):
        parts.append(box(f'rivet_front_{index}', [position_x - 0.18, position_y - 0.18, 6.76 - index * 0.009], [0.36, 0.36, 0.36], 'brass', 45))
        parts.append(box(f'rivet_back_{index}', [position_x - 0.16, position_y - 0.16, 9.23 + index * 0.009], [0.32, 0.32, 0.62], 'steel', 45))
    tooth = LIB.tapered_fang('ritual_fang', 6.6, 9.2, 1.5, False, 16)
    for part in tooth:
        old_from, old_to = part['from'][1], part['to'][1]
        part['from'][1], part['to'][1] = round(10 - old_to, 4), round(10 - old_from, 4)
        part['origin'][1] = round(10 - part['origin'][1], 4)
        part['angle'] = -part['angle']
    parts += tooth
    return parts


def main():
    baseline = WORK / 'gameplay-baseline.json'
    if not baseline.exists():
        save_json(baseline, {str(path.relative_to(ROOT)).replace('\\', '/'): hashlib.sha256(path.read_bytes()).hexdigest() for path in GAMEPLAY})
    LIB.PALETTE.update({
        'brass': ['#463b35', '#785837', '#b0874b', '#d5b66c', '#f3d99a'],
        'steel': ['#2d3035', '#4c4849', '#757371', '#aaa49a', '#d3c9b0'],
        'leather': ['#301f28', '#4e2632', '#79313e', '#a34652', '#c66c70'],
    })
    LIB.material_face = material_face
    parts = geometry()
    atlas, elements, islands = LIB.build_assets(parts)
    display = {
        'gui': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
        'ground': {'rotation': [0, 0, 0], 'translation': [0, 2.5, 0], 'scale': [0.55, 0.55, 0.55]},
        'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [0.85, 0.85, 0.85]},
        'head': {'rotation': [0, 180, 0], 'translation': [0, 10, 0], 'scale': [1, 1, 1]},
        'thirdperson_righthand': {'rotation': [0, -90, 0], 'translation': [0, 1.0, -1.0], 'scale': [0.65, 0.65, 0.65]},
        'thirdperson_lefthand': {'rotation': [0, 90, 0], 'translation': [0, 1.0, -1.0], 'scale': [0.65, 0.65, 0.65]},
        'firstperson_righthand': {'rotation': [0, -25, 0], 'translation': [1.0, 1.0, 0], 'scale': [0.72, 0.72, 0.72]},
        'firstperson_lefthand': {'rotation': [0, 25, 0], 'translation': [1.0, 1.0, 0], 'scale': [0.72, 0.72, 0.72]},
    }
    model = {'ambientocclusion': False, 'gui_light': 'front', 'textures': {'atlas': 'ftboss:item/boss_relics/butcher_fang_charm', 'particle': 'ftboss:item/butcher_fang_charm'}, 'elements': elements, 'display': display}
    save_json(MODEL, model)
    save_json(ENTRY, {'loader': 'forge:separate_transforms', 'ambientocclusion': False, 'gui_light': 'front', 'textures': {'particle': 'ftboss:item/butcher_fang_charm'},
                      'base': {'parent': 'ftboss:item/boss_relics/butcher_fang_charm'}, 'perspectives': {'gui': {'parent': 'ftboss:item/boss_relics/butcher_fang_charm_gui'}}})
    save_json(GUI, {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'ftboss:item/butcher_fang_charm'}})
    atlas.save(ATLAS)
    identifiers = [str(uuid.uuid5(NAMESPACE, part['name'])) for part in parts]
    bb_elements = []
    for part, element, identifier in zip(parts, elements, identifiers):
        bb_elements.append({'name': part['name'], 'type': 'cube', 'uuid': identifier, 'from': part['from'], 'to': part['to'], 'origin': part['origin'],
                            'rotation': [0, 0, part['angle']], 'rescale': False, 'autouv': 0, 'color': 0,
                            'faces': {face: {'uv': [round(value * 8, 5) for value in data['uv']], 'texture': 0} for face, data in element['faces'].items()}})
    template = json.loads((ROOT / 'art/boss_materials/blockbench/butcher_material.bbmodel').read_text(encoding='utf-8'))
    buffer = io.BytesIO()
    atlas.save(buffer, format='PNG')
    template.update({'name': 'Butcher Fang Charm', 'resolution': {'width': 128, 'height': 128}, 'elements': bb_elements, 'outliner': identifiers, 'display': display})
    texture = template['textures'][0]
    texture.update({'name': 'butcher_fang_charm.png', 'source': 'data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode('ascii'), 'uuid': str(uuid.uuid5(NAMESPACE, 'texture'))})
    template['textures'] = [texture]
    save_json(BBMODEL, template)
    save_json(WORK / 'geometry-manifest.json', {'parts': parts, 'uv_islands': islands, 'pixels_per_unit': 2, 'texture_size': 128})
    print(f'Generated butcher_fang_charm: {len(parts)} elements, {len(islands)} per-face UV islands, wearable assembly')


if __name__ == '__main__':
    main()
