import base64
import math
from pathlib import Path
import sys
import uuid

from PIL import ImageDraw

ROOT = Path(__file__).resolve().parents[4]
MATERIAL_TOOLS = ROOT / 'tools/model3d/boss_materials_refine/frost'
sys.path.insert(0, str(MATERIAL_TOOLS))
import build_frost as material

ASSETS = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_relics/refine/frost'
ITEM = 'frost_crown_charm'
material.ART = ART
material.PALETTES['enamel'] = ['49354f', '7e426c', 'b76394', 'eaa8c5', 'fff0e7']
cube = material.cube


def diamond(name, xpos, ypos, zpos, side, depth, style):
    cube(name, [xpos - side / 2, ypos - side / 2, zpos - depth / 2], [xpos + side / 2, ypos + side / 2, zpos + depth / 2], style, ('z', 45, [xpos, ypos, zpos]))


def ring(name, xpos, ypos, zpos, halfwidth, halfheight, thickness, depth, style='gold'):
    cube(name + '_left', [xpos - halfwidth, ypos - halfheight, zpos - depth / 2], [xpos - halfwidth + thickness, ypos + halfheight, zpos + depth / 2], style)
    cube(name + '_right', [xpos + halfwidth - thickness, ypos - halfheight, zpos - depth / 2], [xpos + halfwidth, ypos + halfheight, zpos + depth / 2], style)
    cube(name + '_top', [xpos - halfwidth + thickness - .04, ypos + halfheight - thickness - .03, zpos - depth * .53], [xpos + halfwidth - thickness + .04, ypos + halfheight + .03, zpos + depth * .53], style)
    cube(name + '_bottom', [xpos - halfwidth + thickness - .04, ypos - halfheight - .03, zpos - depth * .53], [xpos + halfwidth - thickness + .04, ypos - halfheight + thickness + .03, zpos + depth * .53], style)


def geometry():
    cube('01_crown_center_gold_mount', [5.7, 5.1, 7.1], [10.3, 7.4, 8.9], 'gold')
    cube('02_center_rose_enamel', [5.95, 5.55, 6.84], [10.05, 6.93, 7.18], 'enamel')
    cube('03_center_lower_rolled_edge', [5.62, 4.98, 6.98], [10.38, 5.4, 9.02], 'gold')
    cube('04_center_upper_icing_edge', [5.65, 7.18, 7.04], [10.35, 7.62, 8.96], 'frost')
    for side, center_x, angle in [('left', 4.4, -22.5), ('right', 11.6, 22.5)]:
        origin = [center_x, 6.35, 8.25]
        cube(f'05_{side}_curved_band', [center_x - 1.4, 5.22, 7.7], [center_x + 1.4, 7.32, 9.0], 'gold', ('y', angle, origin))
        cube(f'06_{side}_inlaid_rose_band', [center_x - 1.2, 5.65, 7.43], [center_x + 1.2, 6.9, 7.78], 'enamel', ('y', angle, origin))
        cube(f'07_{side}_upper_frost_roll', [center_x - 1.37, 7.1, 7.62], [center_x + 1.37, 7.55, 9.08], 'frost', ('y', angle, origin))
        cube(f'08_{side}_lower_gold_roll', [center_x - 1.38, 5.08, 7.61], [center_x + 1.38, 5.5, 9.1], 'gold', ('y', angle, origin))
        diamond(f'09_{side}_sugar_jewel_seat', center_x, 6.28, 7.38, .87, .44, 'gold')
        diamond(f'10_{side}_sugar_jewel', center_x, 6.28, 7.06, .63, .3, 'crystal')
    for index, (xpos, top, angle) in enumerate([(3.8, 9.7, 22.5), (5.8, 10.65, 22.5), (8, 11.6, 0), (10.2, 10.65, -22.5), (12.2, 9.7, -22.5)]):
        base_y = 7.26
        origin = [xpos, base_y, 8.1]
        side = .64 if index == 2 else .58
        rotation = ('z', angle, origin)
        cube(f'20_point_{index}_gold_stem', [xpos - side / 2, base_y - .12, 7.4], [xpos + side / 2, top - .42, 8.45], 'gold', rotation)
        cube(f'21_point_{index}_ice_inlay', [xpos - .16, base_y + .25, 7.21], [xpos + .16, top - .5, 7.5], 'crystal', rotation)
        local_y = top - .48
        tip_x = xpos - math.sin(math.radians(angle)) * (local_y - base_y)
        tip_y = base_y + math.cos(math.radians(angle)) * (local_y - base_y)
        diamond(f'22_point_{index}_crystal_setting', tip_x, tip_y, 7.93, .96 if index == 2 else .8, 1.0, 'gold')
        diamond(f'23_point_{index}_frost_gem', tip_x, tip_y + .03, 7.58, .76 if index == 2 else .61, .6, 'crystal')
    diamond('30_center_diamond_gold_seat', 8, 6.31, 6.61, 1.62, .66, 'gold')
    diamond('31_center_rose_cut_gem', 8, 6.31, 6.13, 1.2, .6, 'rose')
    diamond('32_center_rose_face_table', 8, 6.4, 5.78, .64, .2, 'enamel')
    for index, xpos in enumerate([6.95, 9.05]):
        cube(f'33_jewel_claw_{index}', [xpos - .17, 6.06, 5.96], [xpos + .17, 6.56, 6.7], 'gold', ('z', 22.5 if index == 0 else -22.5, [xpos, 6.31, 6.33]))
    cube('40_rear_bail_bridge', [7.52, 7.25, 8.6], [8.48, 11.9, 9.13], 'gold')
    ring('41_upper_bail', 8, 12.7, 8.8, .86, 1.17, .3, .58)
    cube('42_bail_shoulder_left', [7.08, 13.38, 8.42], [7.46, 14.05, 9.18], 'gold', ('z', -45, [7.34, 13.75, 8.8]))
    cube('43_bail_shoulder_right', [8.54, 13.38, 8.42], [8.92, 14.05, 9.18], 'gold', ('z', 45, [8.66, 13.75, 8.8]))
    ring('44_upper_chain_link', 8, 14.45, 8.87, .56, .82, .26, .53)
    cube('45_backplate_rose_inlay', [6.62, 5.9, 8.87], [9.38, 6.62, 9.13], 'enamel')
    diamond('46_backplate_royal_stamp', 8, 6.25, 9.23, .55, .24, 'gold')
    ring('50_drop_connector', 8, 4.73, 7.8, .5, .62, .23, .5)
    diamond('51_drop_gold_setting', 8, 3.48, 7.8, 1.38, 1.0, 'gold')
    diamond('52_drop_rose_crystal', 8, 3.43, 7.28, 1.08, .67, 'rose')
    diamond('53_drop_frost_highlight', 8, 3.6, 6.91, .5, .19, 'crystal')


def paint_relic(island):
    patch = material.paint(island)
    width, height = patch.size
    if island['material'] == 'gold' and width >= 3 and height >= 3 and island['face'] != 'down':
        draw = ImageDraw.Draw(patch)
        palette = material.colors('gold')
        draw.line((0, 0, 0, height - 2), fill=palette[4])
        draw.line((width - 1, 1, width - 1, height - 1), fill=palette[0])
        if width >= 7 and height >= 4:
            draw.line((1, 1, width - 2, 1), fill=palette[3])
            draw.line((2, height - 2, width - 2, height - 2), fill=palette[1])
        if width > 11 and height > 4:
            draw.line((width // 3, height - 2, width // 3 + 2, 2), fill=palette[4])
    return patch


def build():
    ART.mkdir(parents=True, exist_ok=True)
    geometry()
    texture = material.atlas(paint_relic)
    texture_path = ASSETS / f'textures/item/boss_relics/{ITEM}.png'
    texture_path.parent.mkdir(parents=True, exist_ok=True)
    texture.save(texture_path)
    model = {'ambientocclusion': False, 'textures': {'atlas': f'ftboss:item/boss_relics/{ITEM}', 'particle': f'ftboss:item/{ITEM}'}, 'elements': material.ELEMENTS, 'display': material.make_display()}
    model['display']['fixed'] = {'rotation': [0, 180, 0], 'scale': [.88, .88, .88]}
    model['display']['firstperson_righthand'] = {'rotation': [0, -22.5, 0], 'translation': [1.5, .5, 0], 'scale': [.75, .75, .75]}
    model['display']['firstperson_lefthand'] = {'rotation': [0, 22.5, 0], 'translation': [1.5, .5, 0], 'scale': [.75, .75, .75]}
    material.emit_json(ASSETS / f'models/item/boss_relics/{ITEM}.json', model)
    material.emit_json(ASSETS / f'models/item/boss_relics/{ITEM}_gui.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'ftboss:item/{ITEM}'}})
    material.emit_json(ASSETS / f'models/item/{ITEM}.json', {'loader': 'forge:separate_transforms', 'ambientocclusion': False, 'gui_light': 'front', 'textures': {'particle': f'ftboss:item/{ITEM}'}, 'base': {'parent': f'ftboss:item/boss_relics/{ITEM}'}, 'perspectives': {'gui': {'parent': f'ftboss:item/boss_relics/{ITEM}_gui'}}})
    editable = []
    identifiers = []
    for element in material.ELEMENTS:
        identifier = str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost_relic/' + element['name']))
        identifiers.append(identifier)
        transform = element.get('rotation', {})
        angles = [0, 0, 0]
        if transform: angles['xyz'.index(transform['axis'])] = transform['angle']
        editable.append({'name': element['name'], 'type': 'cube', 'uuid': identifier, 'from': element['from'], 'to': element['to'], 'origin': transform.get('origin', [8, 8, 8]), 'rotation': angles, 'rescale': False, 'autouv': 0, 'faces': {face: {'uv': [value * material.SIZE / 16 for value in data['uv']], 'texture': 0} for face, data in element['faces'].items()}})
    bbmodel = {'meta': {'format_version': '4.10', 'model_format': 'java_block', 'box_uv': False}, 'name': '霜糖王冠坠 / 五尖霜晶金冠', 'model_identifier': ITEM, 'resolution': {'width': material.SIZE, 'height': material.SIZE}, 'elements': editable, 'outliner': [{'name': 'Gold crystal crown pendant', 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost_relic/root')), 'origin': [8, 8, 8], 'children': identifiers}], 'textures': [{'name': ITEM + '.png', 'id': '0', 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftboss:frost_relic/texture')), 'namespace': 'ftboss', 'folder': 'item/boss_relics', 'width': material.SIZE, 'height': material.SIZE, 'uv_width': material.SIZE, 'uv_height': material.SIZE, 'source': 'data:image/png;base64,' + base64.b64encode(texture_path.read_bytes()).decode('ascii')}], 'display': model['display']}
    material.emit_json(ROOT / f'art/boss_relics/blockbench/{ITEM}.bbmodel', bbmodel)
    material.emit_json(ART / 'uv-islands.json', [{key: value for key, value in island.items() if key != 'element'} for island in material.ISLANDS])
    print(f'Built frost crown relic only: {len(material.ELEMENTS)} elements, {len(material.ISLANDS)} individual UV islands')


if __name__ == '__main__':
    build()
