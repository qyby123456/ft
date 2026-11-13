import argparse
import base64
import concurrent.futures
import datetime
import hashlib
import io
import json
import os
import random
import subprocess
import time
import uuid
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


NAMESPACE = 'ftdungeonmobs'
PATCHER = Path('C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/22cfb1d302613eae/codex.exe')
FACES = {'north': (2, -1), 'south': (2, 1), 'east': (0, 1), 'west': (0, -1), 'up': (1, 1), 'down': (1, -1)}
LANES = ('masonry', 'structure', 'decoration')


def save_text(path, text):
    if path.exists():
        previous = path.read_text(encoding='utf-8')
        if previous == text:
            return
        change = f'*** Update File: {path}\n@@\n' + ''.join('-' + line + '\n' for line in previous.splitlines())
    else:
        change = f'*** Add File: {path}\n'
    change += ''.join('+' + line + '\n' for line in text.splitlines())
    result = subprocess.run([str(PATCHER), '--codex-run-as-apply-patch', '*** Begin Patch\n' + change + '*** End Patch'],
                            capture_output=True, text=True, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)


def save_json(path, data, compact=False):
    save_text(path, json.dumps(data, ensure_ascii=False, indent=None if compact else 2) + '\n')


def box(name, start, end, tile=0, front=None, top=None, art=False):
    overrides = {}
    if front is not None:
        overrides['north'] = front
    if top is not None:
        overrides['up'] = top
    return dict(name=name, start=list(start), end=list(end), tile=tile, overrides=overrides, art=art)


def module(name, chinese, lane, shapes, ingredient, light=0, native=None):
    return dict(id='dungeon_' + name, chinese=chinese, lane=lane, shapes=shapes, ingredient=ingredient, light=light, native=native)


def specifications():
    result = [
        module('ashlar', '深砌地牢砖', 'masonry', [box('整块错缝石砖', (0, 0, 0), (16, 16, 16), 0, top=1)], 'polished_blackstone_bricks'),
        module('cracked_tiles', '断纹地牢地砖', 'masonry', [box('裂纹地砖', (0, 0, 0), (16, 16, 16), 0, top=2)], 'cracked_stone_bricks'),
        module('mossy_bricks', '腐苔地牢砖', 'masonry', [box('暗绿苔砖', (0, 0, 0), (16, 16, 16), 3, top=3)], 'vine'),
        module('blood_channel', '血槽地砖', 'masonry', [box('槽底', (0, 0, 0), (16, 9, 16), 0, top=11),
               box('左侧压边', (0, 9, 0), (5, 16, 16), 19), box('右侧压边', (11, 9, 0), (16, 16, 16), 19),
               box('血痕凹面', (5, 9, 0), (11, 10, 16), 11)], 'red_dye'),
        module('bone_mosaic', '骨纹嵌砌石', 'masonry', [box('嵌骨立方', (0, 0, 0), (16, 16, 16), 5, top=5, art=True)], 'bone'),
        module('rune_inlay', '猩红符印砖', 'masonry', [box('符印石砖', (0, 0, 0), (16, 16, 16), 0, front=12, top=6)], 'redstone', 3),
        module('blackstone_stairs', '深砌地牢楼梯', 'masonry', [box('下阶', (0, 0, 0), (16, 8, 16), 0),
               box('上阶', (0, 8, 8), (16, 16, 16), 0)], None, native='stairs'),
        module('blackstone_slab', '深砌地牢台阶', 'masonry', [box('半砖', (0, 0, 0), (16, 8, 16), 0)], None, native='slab'),
        module('pillar_base', '地牢阶座', 'structure', [box('宽底', (0, 0, 0), (16, 4, 16), 19),
               box('中层收口', (1, 4, 1), (15, 8, 15), 17), box('柱根', (2, 8, 2), (14, 16, 14), 16)], 'polished_blackstone'),
        module('pillar_shaft', '束纹石柱', 'structure', [box('柱芯', (3, 0, 3), (13, 16, 13), 16),
               box('前束筋', (5, 0, 2), (11, 16, 3), 17), box('后束筋', (5, 0, 13), (11, 16, 14), 17),
               box('左束筋', (2, 0, 5), (3, 16, 11), 17), box('右束筋', (13, 0, 5), (14, 16, 11), 17)], 'chiseled_polished_blackstone'),
        module('pillar_capital', '地牢叠檐柱冠', 'structure', [box('柱头', (2, 0, 2), (14, 8, 14), 16),
               box('过渡檐', (1, 8, 1), (15, 12, 15), 17), box('宽檐', (0, 12, 0), (16, 16, 16), 19)], 'smooth_stone'),
        module('arch_left', '阶梯拱券左件', 'structure', [box('左拱脚', (0, 0, 3), (4, 8, 13), 0),
               box('左中拱', (0, 8, 3), (9, 12, 13), 19), box('左拱顶', (0, 12, 3), (16, 16, 13), 17)], 'stone_bricks'),
        module('arch_right', '阶梯拱券右件', 'structure', [box('右拱脚', (12, 0, 3), (16, 8, 13), 0),
               box('右中拱', (7, 8, 3), (16, 12, 13), 19), box('右拱顶', (0, 12, 3), (16, 16, 13), 17)], 'deepslate_bricks'),
        module('arch_keystone', '血印拱顶楔石', 'structure', [box('拱楣', (0, 10, 3), (16, 16, 13), 17),
               box('中部下垂石', (4, 5, 2), (12, 10, 14), 19, front=12, art=True),
               box('方形坠石', (6, 2, 4), (10, 5, 12), 0)], 'red_nether_bricks'),
        module('wall_buttress', '阶梯扶壁', 'structure', [box('墙根', (0, 0, 8), (16, 5, 16), 19),
               box('中段扶石', (2, 5, 10), (14, 10, 16), 0), box('上段扶石', (4, 10, 12), (12, 16, 16), 17)], 'cobbled_deepslate'),
        module('crenellated_parapet', '断齿城垛', 'structure', [box('女墙', (0, 0, 7), (16, 9, 16), 0),
               box('左垛齿', (0, 9, 7), (5, 16, 16), 19), box('右垛齿', (11, 9, 7), (16, 16, 16), 19)], 'deepslate_tiles'),
        module('iron_grate', '锈铁囚栏', 'decoration', [box('下横梁', (0, 1, 7), (16, 3, 9), 8),
               box('上横梁', (0, 13, 7), (16, 15, 9), 8)]
               + [box('竖栏' + str(index), (index, 0, 7), (index + 2, 16, 9), 7) for index in (1, 7, 13)], 'iron_bars'),
        module('chain_anchor', '方环锁链锚', 'decoration', [box('铆钉背板', (3, 3, 13), (13, 13, 16), 8),
               box('方环左', (4, 4, 8), (6, 12, 11), 7), box('方环右', (10, 4, 8), (12, 12, 11), 7),
               box('方环上', (6, 10, 8), (10, 12, 11), 8), box('方环下', (6, 4, 8), (10, 6, 11), 8),
               box('锚栓', (7, 7, 11), (9, 9, 13), 7)], 'chain'),
        module('spike_barrier', '阶梯铁刺座', 'decoration', [box('铁底座', (0, 0, 0), (16, 3, 16), 8)]
               + [shape for across in (3, 10) for depth in (3, 10) for shape in (
                   box('刺根', (across - 1, 3, depth - 1), (across + 4, 7, depth + 4), 7),
                   box('刺身', (across, 7, depth), (across + 3, 11, depth + 3), 7),
                   box('方刺尖', (across + 1, 11, depth + 1), (across + 2, 15, depth + 2), 9))], 'iron_ingot'),
        module('bone_pile', '残骨方颅堆', 'decoration', [box('碎石底', (1, 0, 2), (15, 2, 14), 4),
               box('长骨甲', (2, 2, 3), (14, 4, 5), 9), box('长骨乙', (4, 2, 6), (6, 4, 15), 9),
               box('长骨丙', (7, 4, 5), (15, 6, 7), 9), box('方颅', (2, 4, 7), (9, 11, 14), 9, front=20, art=True),
               box('下颌', (3, 3, 6), (8, 5, 13), 9)], 'bone_block'),
        module('sarcophagus', '封缚方棺', 'decoration', [box('石棺足', (1, 0, 0), (15, 3, 16), 19),
               box('方棺身', (2, 3, 1), (14, 10, 15), 0), box('棺盖', (1, 10, 0), (15, 13, 16), 15, top=15, art=True),
               box('束铁甲', (1, 3, 3), (15, 14, 5), 8), box('束铁乙', (1, 3, 11), (15, 14, 13), 8)], 'bone_meal'),
        module('soul_lantern', '幽魂笼灯', 'decoration', [box('灯座', (3, 0, 3), (13, 3, 13), 8),
               box('魂光芯', (5, 3, 5), (11, 11, 11), 13, art=True)]
               + [box('笼角', (across, 3, depth), (across + 1, 11, depth + 1), 7) for across in (3, 12) for depth in (3, 12)]
               + [box('笼顶', (3, 11, 3), (13, 13, 13), 8), box('提环左', (5, 13, 7), (7, 16, 9), 7),
                  box('提环右', (9, 13, 7), (11, 16, 9), 7), box('提环顶', (7, 15, 7), (9, 16, 9), 8)], 'soul_lantern', 11),
        module('wall_cresset', '血焰壁龛灯', 'decoration', [box('壁灯背板', (4, 3, 14), (12, 15, 16), 8),
               box('横托臂', (7, 3, 5), (9, 5, 14), 7), box('灯盏底', (3, 4, 2), (13, 6, 12), 8),
               box('左盏沿', (3, 6, 2), (5, 9, 12), 7), box('右盏沿', (11, 6, 2), (13, 9, 12), 7),
               box('前盏沿', (5, 6, 2), (11, 9, 4), 7), box('后盏沿', (5, 6, 10), (11, 9, 12), 7),
               box('实心血焰', (6, 6, 5), (10, 12, 9), 14, art=True), box('方焰尖', (7, 12, 6), (9, 15, 8), 14, art=True)], 'magma_cream', 12),
        module('runic_seal', '地牢血祭封印座', 'decoration', [box('封印底座', (1, 0, 1), (15, 2, 15), 19),
               box('左阶', (0, 0, 4), (1, 3, 12), 17), box('右阶', (15, 0, 4), (16, 3, 12), 17),
               box('前阶', (4, 0, 0), (12, 3, 1), 17), box('后阶', (4, 0, 15), (12, 3, 16), 17),
               box('凹纹祭印', (3, 2, 3), (13, 4, 13), 18, top=18, art=True)], 'amethyst_shard', 5),
    ]
    for entry in result:
        if not 0 < len(entry['shapes']) <= 16:
            raise AssertionError('Cuboid budget: ' + entry['id'])
        for shape in entry['shapes']:
            if any(not isinstance(value, int) or value < 0 or value > 16 for side in ('start', 'end') for value in shape[side]):
                raise AssertionError('Noninteger or out-of-cell geometry')
            if any(shape['start'][axis] >= shape['end'][axis] for axis in range(3)):
                raise AssertionError('Zero-volume cuboid')
    return result


def atlas_image():
    atlas = Image.new('RGBA', (128, 64), '#393741')
    tiles = []
    for index in range(32):
        tile = Image.new('RGBA', (16, 16), '#393741')
        drawing = ImageDraw.Draw(tile)
        generator = random.Random(18000 + index)
        for unused in range(35):
            across, depth = generator.randrange(16), generator.randrange(16)
            color = generator.choice(('#32313b', '#403e48', '#47434e', '#514b55'))
            drawing.rectangle((across, depth, min(15, across + 1), depth), fill=color)
        if index in (0, 3, 4, 16):
            for depth in (0, 8, 15):
                drawing.line((0, depth, 15, depth), fill='#1e2029')
                if depth < 15:
                    drawing.line((0, depth + 1, 15, depth + 1), fill='#5d5660')
            drawing.line((7, 1, 7, 7), fill='#21202a')
            drawing.line((3, 9, 3, 14), fill='#22222b')
            drawing.line((12, 9, 12, 14), fill='#22222b')
        if index in (1, 2, 5, 6, 15, 17, 18, 19):
            drawing.rectangle((0, 0, 15, 15), outline='#24232c')
            drawing.line((1, 1, 14, 1), fill='#70626b')
            drawing.line((1, 2, 1, 14), fill='#58505c')
        if index == 2:
            drawing.line((7, 0, 7, 5, 5, 5, 5, 10, 10, 10, 10, 15), fill='#191d27', width=1)
            drawing.line((8, 1, 8, 4, 6, 4), fill='#80727a')
        if index == 3:
            for across, depth in ((2, 4), (10, 9), (5, 13), (13, 2)):
                drawing.rectangle((across, depth, min(15, across + 2), min(15, depth + 1)), fill='#465c43')
                drawing.point((across, depth), fill='#697955')
        if index in (4, 11):
            for across in (2, 6, 12):
                drawing.line((across, 2, across, 6 + across % 7), fill='#792638', width=2)
                drawing.point((across, 4), fill='#b63b48')
            if index == 11:
                drawing.rectangle((4, 0, 11, 15), fill='#531c31')
                drawing.line((5, 0, 5, 15), fill='#a33043')
                drawing.line((9, 0, 9, 15), fill='#7f2038')
        if index == 5:
            for depth in (3, 10):
                drawing.line((3, depth, 12, depth), fill='#bdad94', width=2)
                for across in (2, 12):
                    drawing.rectangle((across, depth - 1, across + 1, depth + 2), fill='#d3c2a7')
        if index in (6, 12, 18):
            drawing.rectangle((3, 3, 12, 12), outline='#732c3e')
            drawing.line((5, 4, 5, 11, 10, 11, 10, 7, 7, 7, 7, 4, 11, 4), fill='#dd553f')
            for across in (2, 13):
                drawing.point((across, 7), fill='#f38d54')
        if index in (7, 8):
            drawing.rectangle((0, 0, 15, 15), fill='#393c48')
            drawing.line((0, 0, 15, 0), fill='#83828a')
            drawing.line((0, 0, 0, 15), fill='#655c67')
            drawing.line((15, 0, 15, 15), fill='#1d2430')
            if index == 8:
                for across in (2, 12):
                    for depth in (2, 12):
                        drawing.rectangle((across, depth, across + 1, depth + 1), fill='#afa394')
                drawing.line((5, 4, 5, 10), fill='#885540')
        if index == 9:
            drawing.rectangle((0, 0, 15, 15), fill='#b5a48f')
            for depth in (2, 9):
                drawing.line((0, depth, 15, depth), fill='#d3c2a4')
            drawing.line((0, 15, 15, 15), fill='#74675f')
        if index in (13, 14):
            drawing.rectangle((0, 0, 15, 15), fill='#235364' if index == 13 else '#9c233c')
            drawing.rectangle((3, 2, 12, 14), fill='#65c2c1' if index == 13 else '#dc5a42')
            drawing.rectangle((5, 5, 10, 13), fill='#c4ede0' if index == 13 else '#ffd184')
            drawing.line((7, 2, 7, 12), fill='#f0f0ca' if index == 13 else '#ffb15b', width=2)
        if index == 15:
            drawing.rectangle((3, 2, 12, 13), outline='#b1a08b')
            drawing.line((7, 4, 7, 12), fill='#b44848', width=2)
            drawing.line((5, 6, 10, 6), fill='#b44848')
        if index == 16:
            for across in (3, 7, 11):
                drawing.line((across, 0, across, 15), fill='#6b5966')
                drawing.line((across + 1, 0, across + 1, 15), fill='#292735')
        if index == 17:
            drawing.line((0, 4, 15, 4), fill='#7e6871')
            drawing.line((0, 10, 15, 10), fill='#211e2b')
        if index == 20:
            drawing.rectangle((1, 1, 14, 11), fill='#d2bfa0')
            drawing.rectangle((3, 11, 12, 14), fill='#ac957e')
            drawing.rectangle((3, 4, 5, 7), fill='#332934')
            drawing.rectangle((10, 4, 12, 7), fill='#332934')
            drawing.rectangle((7, 7, 8, 10), fill='#695347')
            for across in (5, 8, 11):
                drawing.line((across, 12, across, 14), fill='#5c493f')
        tiles.append(tile)
        atlas.paste(tile, ((index % 8) * 16, (index // 8) * 16))
    return atlas


def covered(shapes, shape, direction):
    axis, sign = FACES[direction]
    plane = shape['end' if sign > 0 else 'start'][axis]
    others = [other for other in range(3) if other != axis]
    for neighbor in shapes:
        if neighbor is shape:
            continue
        inside = neighbor['start'][axis] <= plane < neighbor['end'][axis] if sign > 0 else neighbor['start'][axis] < plane <= neighbor['end'][axis]
        if inside and all(neighbor['start'][other] <= shape['start'][other]
                          and neighbor['end'][other] >= shape['end'][other] for other in others):
            return True
    return False


def elements(entry):
    result = []
    for shape in entry['shapes']:
        faces = {}
        for direction, (axis, sign) in FACES.items():
            if covered(entry['shapes'], shape, direction):
                continue
            tile = shape['overrides'].get(direction, shape['tile'])
            horizontal = 2 if axis == 0 else 0
            vertical = 2 if axis == 1 else 1
            width = 16 if shape['art'] else shape['end'][horizontal] - shape['start'][horizontal]
            height = 16 if shape['art'] else shape['end'][vertical] - shape['start'][vertical]
            tile_x, tile_y = tile % 8 * 16, tile // 8 * 16
            face = dict(uv=[tile_x / 8, tile_y / 4, (tile_x + width) / 8, (tile_y + height) / 4], texture='#atlas')
            if len(entry['shapes']) == 1 and shape['start'] == [0, 0, 0] and shape['end'] == [16, 16, 16]:
                face['cullface'] = direction
            faces[direction] = face
        if faces:
            result.append(dict(name=shape['name'], **{'from': shape['start'], 'to': shape['end']}, faces=faces))
    if len(result) > 16 or sum(len(value['faces']) for value in result) > 96:
        raise AssertionError('Exported geometry budget: ' + entry['id'])
    return result


def editable(path, entry, exported, atlas):
    encoded = io.BytesIO()
    atlas.save(encoded, format='PNG')
    cubes = []
    root_id = str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftarchitecture/' + entry['id']))
    body_id = str(uuid.uuid5(uuid.NAMESPACE_URL, root_id + '/body'))
    for index, shape in enumerate(exported):
        faces = {direction: dict(uv=[value * (8 if coordinate % 2 == 0 else 4) for coordinate, value in enumerate(face['uv'])], texture=0)
                 for direction, face in shape['faces'].items()}
        faces.update({direction: dict(uv=[0, 0, 0, 0], texture=None) for direction in FACES if direction not in faces})
        cubes.append(dict(name=shape['name'], uuid=str(uuid.uuid5(uuid.NAMESPACE_URL, root_id + '/' + str(index))), type='cube',
                          **{'from': shape['from'], 'to': shape['to']}, origin=[8, 0, 8], rotation=[0, 0, 0], autouv=0, box_uv=False, faces=faces))
    save_json(path, dict(meta=dict(format_version='4.10', model_format='free', box_uv=False), name=entry['chinese'],
              resolution=dict(width=128, height=64), elements=cubes,
              outliner=[dict(name=entry['id'] + '_root', uuid=root_id, origin=[8, 0, 8], rotation=[0, 0, 0],
                             children=[dict(name='01_主体与轮廓', uuid=body_id, origin=[8, 0, 8], rotation=[0, 0, 0],
                                            children=[cube['uuid'] for cube in cubes])])],
              textures=[dict(name='dungeon_architecture.png', id='0', uuid=str(uuid.uuid5(uuid.NAMESPACE_URL, 'ftarchitecture/atlas')),
                             source='data:image/png;base64,' + base64.b64encode(encoded.getvalue()).decode(), mode='bitmap',
                             width=128, height=64, uv_width=128, uv_height=64, saved=True)]), compact=True)


def native_models(assets, entry):
    name = entry['id']
    textures = {side: NAMESPACE + ':block/dungeon_ashlar_tile' for side in ('top', 'bottom', 'side')}
    if entry['native'] == 'slab':
        for suffix, parent in (('', 'slab'), ('_top', 'slab_top')):
            save_json(assets / f'models/block/{name}{suffix}.json', dict(parent='minecraft:block/' + parent, textures=textures))
        save_json(assets / f'blockstates/{name}.json', {'variants': {
            'type=bottom': {'model': f'{NAMESPACE}:block/{name}'}, 'type=top': {'model': f'{NAMESPACE}:block/{name}_top'},
            'type=double': {'model': f'{NAMESPACE}:block/dungeon_ashlar'}}})
    else:
        for suffix, parent in (('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')):
            save_json(assets / f'models/block/{name}{suffix}.json', dict(parent='minecraft:block/' + parent, textures=textures))
        variants = {}
        for facing, initial in (('east', 0), ('south', 90), ('west', 180), ('north', 270)):
            for half in ('bottom', 'top'):
                for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                    rotation = initial - (90 if shape.endswith('left') else 0)
                    if half == 'top' and shape != 'straight':
                        rotation += 90
                    suffix = '_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else ''
                    variants[f'facing={facing},half={half},shape={shape}'] = dict(model=f'{NAMESPACE}:block/{name}{suffix}',
                             x=180 if half == 'top' else 0, y=rotation % 360, uvlock=True)
        save_json(assets / f'blockstates/{name}.json', dict(variants=variants))


def render(exported, atlas, dimensions=(300, 260), turns=0):
    shapes = []
    for original in exported:
        shape = dict(original)
        points = [original['from'][:], original['to'][:]]
        faces = dict(original['faces'])
        for unused in range(turns):
            points = [[16 - point[2], point[1], point[0]] for point in points]
            order = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north', 'up': 'up', 'down': 'down'}
            faces = {order[direction]: face for direction, face in faces.items()}
        shape['from'] = [min(point[axis] for point in points) for axis in range(3)]
        shape['to'] = [max(point[axis] for point in points) for axis in range(3)]
        shape['faces'] = faces
        shapes.append(shape)
    occupied = {}
    for index, shape in enumerate(shapes):
        for across in range(shape['from'][0], shape['to'][0]):
            for height in range(shape['from'][1], shape['to'][1]):
                for depth in range(shape['from'][2], shape['to'][2]):
                    occupied[(across, height, depth)] = index
    def projection(point):
        return ((point[0] + point[2]) * 0.71, (point[0] - point[2]) * 0.38 - point[1])
    projected = [projection([across, height, depth]) for shape in shapes
                 for across in (shape['from'][0], shape['to'][0]) for height in (shape['from'][1], shape['to'][1])
                 for depth in (shape['from'][2], shape['to'][2])]
    minimum = [min(point[axis] for point in projected) for axis in range(2)]
    maximum = [max(point[axis] for point in projected) for axis in range(2)]
    scale = min((dimensions[axis] - 34) / max(1, maximum[axis] - minimum[axis]) for axis in range(2))
    offset = [(dimensions[axis] - (maximum[axis] - minimum[axis]) * scale) / 2 - minimum[axis] * scale for axis in range(2)]
    image = Image.new('RGB', dimensions, '#171821')
    drawing = ImageDraw.Draw(image)
    polygons = []
    for point, index in occupied.items():
        shape = shapes[index]
        for direction in ('north', 'east', 'up'):
            if direction not in shape['faces']:
                continue
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
                screen = projection(vertex)
                vertices.append(tuple(screen[coordinate] * scale + offset[coordinate] for coordinate in range(2)))
            horizontal = 2 if axis == 0 else 0
            vertical = 2 if axis == 1 else 1
            horizontal_ratio = (point[horizontal] - shape['from'][horizontal] + 0.5) / (shape['to'][horizontal] - shape['from'][horizontal])
            vertical_ratio = (point[vertical] - shape['from'][vertical] + 0.5) / (shape['to'][vertical] - shape['from'][vertical])
            if direction != 'up':
                vertical_ratio = 1 - vertical_ratio
            uv = shape['faces'][direction]['uv']
            pixel_x = min(127, int((uv[0] + (uv[2] - uv[0]) * horizontal_ratio) * 8))
            pixel_y = min(63, int((uv[1] + (uv[3] - uv[1]) * vertical_ratio) * 4))
            color = atlas.getpixel((pixel_x, pixel_y))[:3]
            shade = {'north': 0.92, 'east': 0.74, 'up': 1.12}[direction]
            color = tuple(min(255, int(value * shade)) for value in color)
            polygons.append((point[0] - point[2] + point[1] * 0.7, vertices, color))
    for distance, vertices, color in sorted(polygons, key=lambda value: value[0]):
        drawing.polygon(vertices, fill=color)
    return image


def recipe(entry):
    if entry['native']:
        pattern = ['S  ', 'SS ', 'SSS'] if entry['native'] == 'stairs' else ['SSS']
        return dict(type='minecraft:crafting_shaped', pattern=pattern, key={'S': {'item': f'{NAMESPACE}:dungeon_ashlar'}},
                    result={'item': NAMESPACE + ':' + entry['id'], 'count': 4 if entry['native'] == 'stairs' else 6})
    if entry['id'] == 'dungeon_ashlar':
        ingredients = [{'item': 'minecraft:polished_blackstone_bricks'}, {'item': 'minecraft:deepslate_bricks'}]
        count = 4
    else:
        ingredients = [{'item': f'{NAMESPACE}:dungeon_ashlar'}, {'item': 'minecraft:' + entry['ingredient']}]
        count = 1
    return dict(type='minecraft:crafting_shapeless', ingredients=ingredients, result={'item': NAMESPACE + ':' + entry['id'], 'count': count})


def build_lane(project_string, output_string, lane):
    project, output = Path(project_string), Path(output_string)
    start = time.time()
    assets = project / 'src/main/resources/assets' / NAMESPACE
    data = project / 'src/main/resources/data' / NAMESPACE
    atlas = Image.open(assets / 'textures/block/dungeon_architecture.png').convert('RGBA')
    costs = []
    for entry in specifications():
        if entry['lane'] != lane:
            continue
        exported = elements(entry)
        name = entry['id']
        if entry['native']:
            native_models(assets, entry)
        else:
            save_json(assets / f'models/block/{name}.json', dict(parent='minecraft:block/block', ambientocclusion=True,
                      textures={'atlas': NAMESPACE + ':block/dungeon_architecture', 'particle': NAMESPACE + ':block/dungeon_ashlar_tile'}, elements=exported))
            save_json(assets / f'blockstates/{name}.json', {'variants': {
                'facing=' + facing: dict(model=NAMESPACE + ':block/' + name, y=rotation)
                for facing, rotation in (('north', 0), ('east', 90), ('south', 180), ('west', 270))}})
        save_json(assets / f'models/item/{name}.json', dict(parent=NAMESPACE + ':block/' + name))
        save_json(data / f'recipes/{name}.json', recipe(entry))
        functions = [{'function': 'minecraft:set_count', 'count': 2, 'conditions': [
            {'condition': 'minecraft:block_state_property', 'block': NAMESPACE + ':' + name, 'properties': {'type': 'double'}}]}] if entry['native'] == 'slab' else []
        save_json(data / f'loot_tables/blocks/{name}.json', {'type': 'minecraft:block', 'pools': [
            {'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
             'entries': [{'type': 'minecraft:item', 'name': NAMESPACE + ':' + name, 'functions': functions}]}]})
        editable(project / f'model_sources/dungeon_architecture/{name}.bbmodel', entry, exported, atlas)
        render(exported, atlas).save(output / f'{name}.png')
        costs.append(dict(id=name, chinese=entry['chinese'], lane=lane, sourceCuboids=len(entry['shapes']),
                          exportedCuboids=0 if entry['native'] else len(exported), exportedFaces=0 if entry['native'] else sum(len(value['faces']) for value in exported),
                          previewCuboids=len(exported), light=entry['light'], nativeParent=entry['native']))
    report = dict(lane=lane, pid=os.getpid(), started=start, finished=time.time(), models=costs)
    save_json(output / f'worker-{lane}.json', report)
    return report


def shared_resources(project, entries):
    resources = project / 'src/main/resources'
    assets = resources / f'assets/{NAMESPACE}'
    for language in ('zh_cn', 'en_us'):
        path = assets / f'lang/{language}.json'
        translations = json.loads(path.read_text(encoding='utf-8'))
        for entry in entries:
            translations[f'block.{NAMESPACE}.{entry["id"]}'] = entry['chinese'] if language == 'zh_cn' else entry['id'].replace('_', ' ').title()
        save_json(path, translations)
    save_json(resources / f'data/{NAMESPACE}/tags/blocks/dungeon_architecture.json', dict(replace=False, values=[NAMESPACE + ':' + entry['id'] for entry in entries]))
    path = resources / 'data/minecraft/tags/blocks/mineable/pickaxe.json'
    tag = json.loads(path.read_text(encoding='utf-8'))
    tag['values'] = sorted(set(tag['values']) | {'#' + NAMESPACE + ':dungeon_architecture'})
    save_json(path, tag)
    lines = ['package net.farmingtales.ftdungeonmobs;', '', 'import java.util.List;', '',
             'public final class DungeonArchitectureCatalog {',
             '    public record Model(String id, int light, String nativeParent, boolean full, int[][] boxes) {}', '',
             '    public static final List<Model> MODELS = List.of(']
    for index, entry in enumerate(entries):
        shapes = entry['shapes']
        bounds = ', '.join('{' + ', '.join(str(value) for value in shape['start'] + shape['end']) + '}' for shape in shapes)
        full = len(shapes) == 1 and shapes[0]['start'] == [0, 0, 0] and shapes[0]['end'] == [16, 16, 16]
        native = '"' + entry['native'] + '"' if entry['native'] else '""'
        lines.append(f'            new Model("{entry["id"]}", {entry["light"]}, {native}, {str(full).lower()}, new int[][]{{{bounds}}})' + (',' if index < len(entries) - 1 else ');'))
    lines += ['', '    private DungeonArchitectureCatalog() {}', '}']
    save_text(project / 'src/main/java/net/farmingtales/ftdungeonmobs/DungeonArchitectureCatalog.java', '\n'.join(lines) + '\n')


def overview(output, entries, atlas):
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 21)
    heading = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 34)
    sheet = Image.new('RGB', (1440, 2040), '#171821')
    drawing = ImageDraw.Draw(sheet)
    drawing.text((28, 20), 'FT 地牢建筑方块 · 24 款拼装套件', font=heading, fill='#e1c9b7')
    drawing.text((30, 74), '黑石 / 血痕 / 骨饰 / 锈铁   ·   16×16 整数长方体   ·   软件预览，非游戏截图', font=font, fill='#a39ba7')
    for index, entry in enumerate(entries):
        column, row = index % 4, index // 4
        panel = Image.open(output / f'{entry["id"]}.png').convert('RGB').resize((340, 280))
        sheet.paste(panel, (column * 360 + 10, row * 315 + 135))
        drawing.text((column * 360 + 24, row * 315 + 415), f'{index + 1:02d}  ' + entry['chinese'], font=font, fill='#d8c5b4')
    sheet.save(output / 'dungeon-architecture-overview.png')
    for name in ('dungeon_arch_left', 'dungeon_bone_pile', 'dungeon_wall_cresset', 'dungeon_chain_anchor'):
        entry = next(value for value in entries if value['id'] == name)
        sheet = Image.new('RGB', (1200, 650), '#171821')
        drawing = ImageDraw.Draw(sheet)
        drawing.text((20, 12), entry['chinese'] + ' · 四向几何与贴图检查', font=heading, fill='#dcc6b5')
        for turns in range(4):
            sheet.paste(render(elements(entry), atlas, (300, 520), turns), (turns * 300, 75))
            drawing.text((turns * 300 + 20, 605), ('North', 'East', 'South', 'West')[turns], font=font, fill='#a69aa6')
        sheet.save(output / f'{name}-four-views.png')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--project', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--sample', action='store_true')
    arguments = parser.parse_args()
    project, output = arguments.project.resolve(), arguments.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    entries = specifications()
    atlas = atlas_image()
    costs = [dict(id=entry['id'], plannedCuboids=len(entry['shapes']), maximumFaces=len(entry['shapes']) * 6) for entry in entries]
    save_json(output / 'geometry-budget.json', dict(perBlockCuboids=16, perBlockFaces=96, planned=costs, customRenderer=False, addedBlockEntities=0))
    if arguments.sample:
        entry = next(value for value in entries if value['id'] == 'dungeon_pillar_base')
        render(elements(entry), atlas, (800, 700)).save(output / 'prototype-pillar-base.png')
        print('PROTOTYPE_READY: integer pillar base; budgets checked before parallel export')
        return
    textures = project / f'src/main/resources/assets/{NAMESPACE}/textures/block'
    textures.mkdir(parents=True, exist_ok=True)
    atlas.save(textures / 'dungeon_architecture.png')
    atlas.crop((0, 0, 16, 16)).save(textures / 'dungeon_ashlar_tile.png')
    reports = []
    with concurrent.futures.ProcessPoolExecutor(max_workers=3) as executor:
        futures = [executor.submit(build_lane, str(project), str(output), lane) for lane in LANES]
        for future in concurrent.futures.as_completed(futures):
            report = future.result()
            reports.append(report)
            print(f'WORKER_COMPLETE: {report["lane"]}; pid={report["pid"]}; models={len(report["models"])}', flush=True)
    if len({report['pid'] for report in reports}) != 3:
        raise AssertionError('Three distinct parallel worker processes required')
    shared_resources(project, entries)
    overview(output, entries, atlas)
    models = [model for lane in LANES for report in reports if report['lane'] == lane for model in report['models']]
    save_json(output / 'model-cost.json', dict(models=models, customCuboids=sum(model['exportedCuboids'] for model in models),
              customFaces=sum(model['exportedFaces'] for model in models), nativeParentModels=2, texturePixels=[[128, 64], [16, 16]],
              workers=reports, actualFpsMeasured=False, gameScreenshot=False, blockEntities=0, customRenderers=0,
              timestamp=datetime.datetime.now().astimezone().isoformat()))
    print('ARCHITECTURE_EXPORT_COMPLETE: 24 models, three parallel worker processes; shared integration applied serially')


if __name__ == '__main__':
    main()
