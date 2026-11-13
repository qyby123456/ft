import argparse
import base64
import io
import json
import math
import subprocess
import uuid
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


APPLY_PATCH = Path('C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/22cfb1d302613eae/codex.exe')
NORMALS = {'west': (0, -1), 'east': (0, 1), 'down': (1, -1), 'up': (1, 1), 'north': (2, -1), 'south': (2, 1)}
SOURCE_BUDGET = 140
SPLIT_BUDGET = 220
FACE_BUDGET = 900


def patch_json(path, data):
    if path.exists() and json.loads(path.read_text(encoding='utf-8')) == data:
        return
    if path.exists():
        removal = f'*** Begin Patch\n*** Delete File: {path.as_posix()}\n*** End Patch'
        subprocess.run([str(APPLY_PATCH), '--codex-run-as-apply-patch', removal], check=True, capture_output=True)
    rows = json.dumps(data, ensure_ascii=False, indent=2).splitlines()
    initial = f'*** Begin Patch\n*** Add File: {path.as_posix()}\n+PLACEHOLDER\n*** End Patch'
    subprocess.run([str(APPLY_PATCH), '--codex-run-as-apply-patch', initial], check=True, capture_output=True)
    for start in range(0, len(rows), 140):
        chunk = rows[start:start + 140]
        patch = f'*** Begin Patch\n*** Update File: {path.as_posix()}\n@@\n-PLACEHOLDER\n'
        patch += ''.join('+' + row + '\n' for row in chunk)
        if start + 140 < len(rows):
            patch += '+PLACEHOLDER\n'
        patch += '*** End Patch'
        subprocess.run([str(APPLY_PATCH), '--codex-run-as-apply-patch', patch], check=True, capture_output=True)


def atlas_texture(path):
    colors = [(64, 64, 70), (73, 72, 76), (39, 41, 48), (61, 62, 68),
              (55, 54, 56), (62, 66, 57), (63, 45, 48), (156, 148, 131),
              (159, 152, 137), (67, 63, 60), (45, 49, 58), (65, 44, 31),
              (51, 43, 36), (52, 126, 143), (114, 21, 37), (43, 65, 70),
              (65, 34, 43), (48, 49, 56), (106, 83, 54), (28, 31, 39),
              (56, 58, 65)]
    image = Image.new('RGBA', (128, 128), (32, 34, 42, 255))
    for tile, base in enumerate(colors):
        offset_x, offset_y = tile % 8 * 16, tile // 8 * 16
        for pixel_y in range(16):
            for pixel_x in range(16):
                noise = ((pixel_x * 37 + pixel_y * 61 + tile * 19 + pixel_x * pixel_y * 7) % 13) - 6
                value = list(channel + noise for channel in base)
                if tile in (0, 1, 5, 6, 17, 20):
                    course = pixel_y // 8
                    seam_x = (pixel_x + course * 8) % 16
                    if pixel_y % 8 == 0 or seam_x == 0:
                        value = [channel - 27 for channel in base]
                    elif pixel_y % 8 == 1 or seam_x == 1:
                        value = [channel + 10 for channel in base]
                    if tile in (5, 6) and (pixel_x * 3 + pixel_y * 7) % 17 < 4:
                        value = [52, 66, 35] if tile == 5 else [83, 24, 32]
                if tile in (3, 4, 9, 10, 18, 19):
                    value = [channel + (7 if pixel_x % 6 == 0 else -4) for channel in value]
                    if (pixel_x * 5 + pixel_y * 11) % 19 < 3:
                        value = [91, 59, 40]
                image.putpixel((offset_x + pixel_x, offset_y + pixel_y), tuple(max(0, min(255, channel)) for channel in value) + (255,))
    drawing = ImageDraw.Draw(image)
    for tile in (2, 9, 12):
        offset_x, offset_y = tile % 8 * 16, tile // 8 * 16
        for center_x in (3, 11):
            drawing.rectangle((offset_x + center_x, offset_y + 6, offset_x + center_x + 2, offset_y + 8), fill='#25262c')
            drawing.point((offset_x + center_x, offset_y + 6), fill='#b5a188')
            drawing.point((offset_x + center_x + 1, offset_y + 7), fill='#7e756b')
    offset_x, offset_y = 0, 16
    drawing.rectangle((offset_x + 2, offset_y + 5, offset_x + 6, offset_y + 9), fill='#17191e')
    drawing.rectangle((offset_x + 9, offset_y + 5, offset_x + 13, offset_y + 9), fill='#17191e')
    drawing.rectangle((offset_x + 7, offset_y + 10, offset_x + 8, offset_y + 12), fill='#222126')
    drawing.line((offset_x + 3, offset_y + 2, offset_x + 10, offset_y + 2), fill='#c4bca6')
    for pixel_x in (4, 7, 10):
        drawing.line((offset_x + pixel_x, offset_y + 14, offset_x + pixel_x, offset_y + 15), fill='#534c47')
    for tile, color in ((15, '#62d0d8'), (16, '#b43d4d')):
        offset_x, offset_y = tile % 8 * 16, tile // 8 * 16
        drawing.rectangle((offset_x + 1, offset_y + 1, offset_x + 14, offset_y + 14), outline='#8a8171')
        drawing.line([(offset_x + 7, offset_y + 4), (offset_x + 10, offset_y + 4),
                      (offset_x + 10, offset_y + 7), (offset_x + 12, offset_y + 7),
                      (offset_x + 12, offset_y + 9), (offset_x + 9, offset_y + 9),
                      (offset_x + 9, offset_y + 12), (offset_x + 6, offset_y + 12),
                      (offset_x + 6, offset_y + 9), (offset_x + 3, offset_y + 9),
                      (offset_x + 3, offset_y + 7), (offset_x + 7, offset_y + 7),
                      (offset_x + 7, offset_y + 4)], fill=color, width=1)
    image.save(path)
    return image


def animated_texture(path, kind):
    image = Image.new('RGBA', (16, 256))
    for frame in range(16):
        for pixel_y in range(16):
            for pixel_x in range(16):
                if kind == 'flame':
                    phase = (pixel_y + frame + pixel_x * 3) % 16
                    colors = [(161, 47, 9), (219, 76, 9), (255, 125, 13), (255, 180, 39), (255, 221, 106)]
                    index = min(4, max(0, 4 - pixel_y // 4 + (1 if phase < 4 else 0)))
                else:
                    colors = [(18, 18, 18), (32, 32, 32), (49, 49, 49), (72, 72, 72), (103, 103, 103), (145, 145, 145)]
                    distance = max(abs(pixel_x - 7.5), abs(pixel_y - 8))
                    index = max(0, min(5, int(7 - distance) // 2 + ((frame + pixel_x + pixel_y) % 16 < 3)))
                    if pixel_x in (2, 13) or pixel_y in (1, 14):
                        index = max(0, index - 1)
                    if kind == 'entry':
                        colors = [(17, 17, 17), (30, 30, 30), (47, 47, 47), (69, 69, 69), (98, 98, 98), (138, 138, 138)]
                image.putpixel((pixel_x, frame * 16 + pixel_y), colors[index] + (255,))
    image.save(path)
    patch_json(path.with_suffix('.png.mcmeta'), {'animation': {'frametime': 3, 'interpolate': False, 'frames': list(range(16))}})
    return image


def boxes(returning):
    result = []
    def add(name, start, end, tile, group, texture='body', stretch=False):
        result.append(dict(name=name, start=start, end=end, original_start=start, original_end=end,
                           tile=tile, group=group, texture=texture, stretch=stretch))
    add('石砖门槛', [0, 0, 4], [48, 3, 16], 20, '01_石砖承重框')
    for side in (0, 1):
        offset = side * 38
        add('外扩柱脚', [offset, 3, 3], [offset + 10, 9, 16], 17, '01_石砖承重框')
        for course, (height, top) in enumerate(((9, 21), (24, 37), (40, 50))):
            add('错缝砖柱', [offset + 1, height, 5], [offset + 9, top, 16],
                (0, 5, 1)[course] if returning else (6, 5, 0)[course], '01_石砖承重框')
        for height in (21, 37):
            add('凸出台阶砖带', [offset, height, 4], [offset + 10, height + 3, 16], 1, '02_柱带与拱肩')
        add('方形柱冠', [offset, 50, 3], [offset + 10, 54, 16], 17, '02_柱带与拱肩')
        shoulder = 10 if side == 0 else 35
        add('阶梯拱肩', [shoulder, 50, 6], [shoulder + 3, 54, 16], 0, '02_柱带与拱肩')
        rail_x = 9 if side == 0 else 37
        add('铁闸导轨', [rail_x, 3, 5], [rail_x + 2, 53, 8], 19, '02_柱带与拱肩')
        torch_x = 2 + offset
        add('壁灯锈铁背板', [torch_x, 25, 3], [torch_x + 6, 33, 5], 4, '06_暖焰壁灯')
        add('壁灯上背板', [torch_x, 36, 3], [torch_x + 6, 39, 5], 4, '06_暖焰壁灯')
        add('壁灯方木柄', [torch_x + 2, 26, 1], [torch_x + 4, 35, 3], 11, '06_暖焰壁灯')
        add('壁灯方铁碗', [torch_x, 33, 0], [torch_x + 6, 36, 5], 12, '06_暖焰壁灯')
        add('壁灯厚焰', [torch_x + 1, 36, 1], [torch_x + 5, 40, 4], 0, '06_暖焰壁灯', 'flame', True)
        add('壁灯阶梯焰尖', [torch_x + 2, 40, 2], [torch_x + 4, 43, 3], 0, '06_暖焰壁灯', 'flame', True)
        drum_x = 5 + side * 32
        for bottom, top, near, far in ((55, 57, 3, 7), (57, 61, 1, 9), (61, 63, 3, 7)):
            add('阶梯方绞盘', [drum_x, bottom, near], [drum_x + 6, top, far], 4, '04_绞盘和链条')
        add('绞盘方轴', [drum_x - 2, 58, 3], [drum_x + 8, 60, 6], 18, '04_绞盘和链条')
        chain_x = 10 + side * 24
        for link, bottom in enumerate(range(17, 52, 7)):
            near, far = (1, 3) if link % 2 == 0 else (2, 5)
            add('矩形铁链左边', [chain_x, bottom, near], [chain_x + 1, bottom + 7, far], 4, '04_绞盘和链条')
            add('矩形铁链右边', [chain_x + 3, bottom, near], [chain_x + 4, bottom + 7, far], 4, '04_绞盘和链条')
            add('矩形铁链上边', [chain_x + 1, bottom + 6, near], [chain_x + 3, bottom + 7, far], 4, '04_绞盘和链条')
            add('矩形铁链下边', [chain_x + 1, bottom, near], [chain_x + 3, bottom + 1, far], 4, '04_绞盘和链条')
    add('厚石门楣', [0, 54, 5], [48, 64, 16], 0, '01_石砖承重框')
    add('门楣铁扣', [13, 54, 3], [35, 57, 5], 9, '02_柱带与拱肩')
    add('颅骨底牌', [17, 51, 3], [31, 64, 5], 2, '05_门楣方颅')
    add('方颅头骨', [19, 54, 0], [29, 63, 3], 8, '05_门楣方颅', stretch=True)
    add('方颅下颌', [21, 51, 1], [27, 54, 3], 7, '05_门楣方颅')
    add('方颅左颞', [18, 56, 1], [19, 61, 3], 7, '05_门楣方颅')
    add('方颅右颞', [29, 56, 1], [30, 61, 3], 7, '05_门楣方颅')
    for bar in range(7):
        start_x = 11 + bar * 4
        add('铁闸竖条', [start_x, 5, 6], [start_x + 2, 52, 8], 10, '03_铁闸与尖齿')
        add('阶梯方尖齿', [start_x, 3, 6], [start_x + 1, 5, 8], 18, '03_铁闸与尖齿')
    for height in (11, 28, 49):
        add('铆钉横梁', [10, height, 4], [38, height + 3, 6], 9, '03_铁闸与尖齿')
    add('冷雾空间封门', [10, 3, 13], [38, 54, 15], 0, '07_冷雾与门槛符', 'portal', True)
    add('门槛符牌', [18, 3, 0], [30, 4, 9], 15 if returning else 16, '07_冷雾与门槛符', stretch=True)
    if not returning:
        add('颅骨血祭垂印', [23, 51, 0], [25, 54, 1], 14, '05_门楣方颅')
    return result


def split_boxes(source):
    result = []
    for box in source:
        for row in range(4):
            for column in range(3):
                origin = [column * 16, row * 16, 0]
                start = [max(box['start'][axis], origin[axis]) for axis in range(3)]
                end = [min(box['end'][axis], origin[axis] + 16) for axis in range(3)]
                if all(end[axis] > start[axis] for axis in range(3)):
                    result.append(dict(box, start=start, end=end, part=row * 3 + column, origin=origin))
    return result


def hidden(box, direction, all_boxes):
    axis, sign = NORMALS[direction]
    axes = [value for value in range(3) if value != axis]
    plane = box['end'][axis] if sign > 0 else box['start'][axis]
    for first in range(box['start'][axes[0]], box['end'][axes[0]]):
        for second in range(box['start'][axes[1]], box['end'][axes[1]]):
            point = [0, 0, 0]
            point[axis] = plane + sign * 0.1
            point[axes[0]], point[axes[1]] = first + 0.5, second + 0.5
            if not any(other is not box and all(other['start'][index] <= point[index] < other['end'][index] for index in range(3)) for other in all_boxes):
                return False
    return True


def moving(box):
    return box['group'] == '03_铁闸与尖齿' and box['name'] != '铁闸导轨'


def motion_neighbors(box, source):
    return [other for other in source if moving(other) == moving(box)]


def face_uv(box, direction):
    horizontal = 2 if direction in ('west', 'east') else 0
    vertical = 2 if direction in ('up', 'down') else 1
    start, end = box['start'], box['end']
    if box['stretch']:
        original_start, original_end = box['original_start'], box['original_end']
        first = (start[horizontal] - original_start[horizontal]) / (original_end[horizontal] - original_start[horizontal]) * 16
        last = (end[horizontal] - original_start[horizontal]) / (original_end[horizontal] - original_start[horizontal]) * 16
        top = (start[vertical] - original_start[vertical]) / (original_end[vertical] - original_start[vertical]) * 16
        bottom = (end[vertical] - original_start[vertical]) / (original_end[vertical] - original_start[vertical]) * 16
        uv = [first, top, last, bottom] if direction in ('up', 'down') else [first, 16 - bottom, last, 16 - top]
    else:
        first = start[horizontal] % 16
        top = start[vertical] % 16 if direction in ('up', 'down') else (-end[vertical]) % 16
        uv = [first, top, first + end[horizontal] - start[horizontal], top + end[vertical] - start[vertical]]
    if box['texture'] == 'body':
        offset_x, offset_y = box['tile'] % 8 * 16, box['tile'] // 8 * 16
        return [(value + (offset_x if index % 2 == 0 else offset_y)) / 8 for index, value in enumerate(uv)]
    return uv


def model_element(box, all_boxes):
    faces = {direction: {'uv': face_uv(box, direction), 'texture': '#' + box['texture']} for direction in NORMALS if not hidden(box, direction, all_boxes)}
    if box['texture'] == 'portal':
        for face in faces.values():
            face['tintindex'] = 0
    result = {'name': box['name'], 'from': [box['start'][axis] - box['origin'][axis] for axis in range(3)],
              'to': [box['end'][axis] - box['origin'][axis] for axis in range(3)], 'faces': faces}
    if box['texture'] != 'body':
        result['shade'] = False
    return result


def editable(path, source, textures, name):
    texture_ids = {'body': 0, 'portal': 1, 'flame': 2}
    definitions = []
    for kind, image in textures.items():
        output = io.BytesIO()
        image.crop((0, 0, image.width, image.width)).save(output, 'PNG')
        definitions.append({'name': 'dungeon_gate_' + kind + '.png', 'id': str(texture_ids[kind]),
                            'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, name + kind)),
                            'source': 'data:image/png;base64,' + base64.b64encode(output.getvalue()).decode(),
                            'mode': 'bitmap', 'width': image.width, 'height': image.width,
                            'uv_width': image.width, 'uv_height': image.width, 'saved': False})
    elements, groups = [], {}
    for index, box in enumerate(source):
        runtime = model_element(box, motion_neighbors(box, source))
        if not runtime['faces']:
            continue
        identity = str(uuid.uuid5(uuid.NAMESPACE_URL, name + str(index)))
        faces = {}
        for direction in NORMALS:
            if direction in runtime['faces']:
                multiplier = 8 if box['texture'] == 'body' else 1
                faces[direction] = {'uv': [value * multiplier for value in face_uv(box, direction)],
                                    'texture': texture_ids[box['texture']]}
            else:
                faces[direction] = {'uv': [0, 0, 0, 0], 'texture': None}
        elements.append({'name': box['name'], 'type': 'cube', 'uuid': identity, 'from': box['start'], 'to': box['end'],
                         'origin': [24, 0, 8], 'rotation': [0, 0, 0], 'autouv': 0, 'box_uv': False,
                         'shade': box['texture'] == 'body', 'faces': faces})
        groups.setdefault(box['group'], []).append(identity)
    children = [{'name': group, 'origin': [24, 0, 8], 'rotation': [0, 0, 0],
                 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, name + group)), 'children': members}
                for group, members in groups.items()]
    patch_json(path, {'meta': {'format_version': '4.10', 'model_format': 'free', 'box_uv': False},
                      'name': name + '_stone_portcullis', 'resolution': {'width': 128, 'height': 128},
                      'elements': elements, 'outliner': [{'name': 'dungeon_gate_root', 'origin': [24, 0, 8],
                      'rotation': [0, 0, 0], 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, name + '_root')),
                      'children': children}], 'textures': definitions})
    return len(elements), len(children) + 1


def sample_color(box, direction, point, textures):
    horizontal = 2 if direction in ('west', 'east') else 0
    vertical = 2 if direction in ('up', 'down') else 1
    uv = face_uv(box, direction)
    factor_x = (point[horizontal] - box['start'][horizontal]) / (box['end'][horizontal] - box['start'][horizontal])
    factor_y = (point[vertical] - box['start'][vertical]) / (box['end'][vertical] - box['start'][vertical])
    if direction not in ('up', 'down'):
        factor_y = 1 - factor_y
    texture = textures[box['texture']]
    pixel_x = min(texture.width - 1, max(0, int((uv[0] + (uv[2] - uv[0]) * factor_x) * texture.width / 16)))
    pixel_y = min(texture.width - 1, max(0, int((uv[1] + (uv[3] - uv[1]) * factor_y) * texture.width / 16)))
    color = texture.getpixel((pixel_x, pixel_y))[:3]
    if box['texture'] == 'portal':
        tint = textures.get('portal_tint', (85, 204, 235))
        color = tuple(value * tint[index] // 255 for index, value in enumerate(color))
    return color


def voxels(source):
    result = {}
    for box in source:
        for block_x in range(box['start'][0], box['end'][0]):
            for block_y in range(box['start'][1], box['end'][1]):
                for block_z in range(box['start'][2], box['end'][2]):
                    result[(block_x, block_y, block_z)] = box
    return result


def render_model(source, textures, view):
    occupied = voxels(source)
    if view == 'isometric':
        canvas = Image.new('RGB', (540, 620), '#12151d')
        drawing = ImageDraw.Draw(canvas)
        polygons = []
        def project(point):
            return (60 + (point[0] + point[2] * 0.5) * 7.5, 560 + (-point[1] + point[0] * 0.2 - point[2] * 0.4) * 7.5)
        for (block_x, block_y, block_z), box in occupied.items():
            for direction in ('north', 'east', 'up'):
                axis, sign = NORMALS[direction]
                neighbor = [block_x, block_y, block_z]
                neighbor[axis] += sign
                if tuple(neighbor) in occupied:
                    continue
                axes = [value for value in range(3) if value != axis]
                vertices = []
                for first, second in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    point = [block_x, block_y, block_z]
                    point[axis] += 1 if sign > 0 else 0
                    point[axes[0]] += first
                    point[axes[1]] += second
                    vertices.append(point)
                center = [sum(vertex[index] for vertex in vertices) / 4 for index in range(3)]
                color = sample_color(box, direction, center, textures)
                brightness = 1 if box['texture'] != 'body' else {'north': 1.1, 'east': 0.72, 'up': 1.28}[direction]
                color = tuple(min(255, int(channel * brightness)) for channel in color)
                depth = center[2] - center[0] * 0.5 - center[1] * 0.4
                polygons.append((depth, [project(vertex) for vertex in vertices], color))
        for depth, polygon, color in sorted(polygons, key=lambda face: face[0], reverse=True):
            drawing.polygon(polygon, fill=color)
        return canvas
    direction = {'front': 'north', 'back': 'south', 'left': 'west', 'right': 'east'}[view]
    horizontal = 2 if view in ('left', 'right') else 0
    depth_axis, sign = NORMALS[direction]
    canvas = Image.new('RGB', (16 if horizontal == 2 else 48, 64), '#12151d')
    sorted_voxels = sorted(occupied.items(), key=lambda item: item[0][depth_axis], reverse=sign < 0)
    for point, box in sorted_voxels:
        sample = [value + 0.5 for value in point]
        color = sample_color(box, direction, sample, textures)
        pixel_x = point[horizontal]
        if view in ('back', 'left'):
            pixel_x = canvas.width - pixel_x - 1
        canvas.putpixel((pixel_x, 63 - point[1]), color)
    return canvas.resize((canvas.width * 8, 512), Image.Resampling.NEAREST)


def previews(output, definitions):
    font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 21)
    for view in ('isometric', 'front'):
        canvas = Image.new('RGB', (1120, 720), '#12151d')
        drawing = ImageDraw.Draw(canvas)
        for index, (name, source, textures) in enumerate(definitions):
            panel = render_model(source, textures, view)
            canvas.paste(panel, (index * 550 + (550 - panel.width) // 2, 80))
            drawing.text((45 + index * 550, 20), 'RETURN - SOUL ROUTE' if index else 'ENTRY - BLOOD SEAL', font=font, fill='#d9d1bf')
        drawing.text((35, 685), 'Reference-inspired cuboid model preview / not a Minecraft screenshot', font=font, fill='#8b95a4')
        canvas.save(output / f'dungeon-gates-{view}.png')
    sheet = Image.new('RGB', (1440, 1160), '#12151d')
    drawing = ImageDraw.Draw(sheet)
    for row, (name, source, textures) in enumerate(definitions):
        for column, view in enumerate(('front', 'back', 'left', 'right')):
            panel = render_model(source, textures, view)
            panel.thumbnail((330, 510), Image.Resampling.NEAREST)
            sheet.paste(panel, (column * 360 + (360 - panel.width) // 2, row * 580 + 45))
            drawing.text((column * 360 + 24, row * 580 + 12), ('RETURN ' if row else 'ENTRY ') + view.upper(), font=font, fill='#d9d1bf')
    sheet.save(output / 'dungeon-gates-orthographic.png')


def motion_preview(output, definitions):
    font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 20)
    frames = []
    sequence = [0] * 8 + list(range(1, 41)) + [40] * 12 + list(range(39, -1, -1)) + [0] * 8
    for step in sequence:
        sink = round(49 * step / 40)
        canvas = Image.new('RGB', (920, 650), '#12151d')
        drawing = ImageDraw.Draw(canvas)
        for index, (name, source, textures) in enumerate(definitions):
            posed = []
            for box in source:
                if not moving(box):
                    posed.append(box)
                    continue
                start, end = box['start'].copy(), box['end'].copy()
                start[1], end[1] = max(3, start[1] - sink), end[1] - sink
                if start[1] >= end[1]:
                    continue
                original_start, original_end = box['original_start'].copy(), box['original_end'].copy()
                original_start[1] -= sink
                original_end[1] -= sink
                posed.append(dict(box, start=start, end=end, original_start=original_start, original_end=original_end))
            panel = render_model(posed, textures, 'front')
            canvas.paste(panel, (index * 460 + 38, 65))
            drawing.text((index * 460 + 38, 20), 'RETURN - SOUL ROUTE' if index else 'ENTRY - BLOOD SEAL',
                         font=font, fill='#d9d1bf')
        drawing.text((38, 586), f'Iron gate travel {step}/40 ticks / 2 seconds each way / floor-clipped at Y=3', font=font, fill='#b5c2d1')
        drawing.text((38, 618), 'Software texture preview, integer-step sampling; runtime uses smooth partial ticks.', font=font, fill='#8b95a4')
        frames.append(canvas)
    frames[0].save(output / 'dungeon-gates-open-close.gif', save_all=True, append_images=frames[1:],
                   duration=50, loop=0, optimize=False, disposal=2)
    frames[48].save(output / 'dungeon-gates-open.png')


def palette_preview(output, definitions):
    colors = (0x42464D, 0x55CCEB, 0x54DB75, 0xF4C64E, 0xB66CE8, 0xE34555)
    labels = ('0 UNLIT', '1 BLUE', '2 GREEN', '3 GOLD', '4 PURPLE', '5 BLOOD')
    font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 19)
    canvas = Image.new('RGB', (1560, 805), '#12151d')
    drawing = ImageDraw.Draw(canvas)
    for row, (name, source, textures) in enumerate(definitions):
        for tier, color in enumerate(colors):
            if row == 1 and tier == 0:
                drawing.text((tier * 260 + 14, row * 380 + 85), 'RETURN GATE', font=font, fill='#d9d1bf')
                drawing.text((tier * 260 + 14, row * 380 + 120), 'Always open for exit', font=font, fill='#a8c5d9')
                drawing.text((tier * 260 + 14, row * 380 + 155), 'Uses locked round tier', font=font, fill='#a8c5d9')
                drawing.text((tier * 260 + 14, row * 380 + 190), 'No admission timeout', font=font, fill='#a8c5d9')
                continue
            tinted = dict(textures, portal_tint=((color >> 16) & 255, (color >> 8) & 255, color & 255))
            posed = source if tier == 0 else [box for box in source if not moving(box)]
            panel = render_model(posed, tinted, 'front').resize((240, 320), Image.Resampling.NEAREST)
            canvas.paste(panel, (tier * 260 + 10, row * 380 + 38))
            drawing.text((tier * 260 + 14, row * 380 + 12), ('RETURN ' if row else 'ENTRY ') + labels[tier],
                         font=font, fill='#d9d1bf')
    drawing.text((20, 773), 'Software tint preview, not Minecraft. One neutral portal texture per gate; no extra cuboids or animation copies.',
                 font=font, fill='#8b95a4')
    canvas.save(output / 'dungeon-gate-tier-colors.png')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--destination', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    assets = arguments.destination / 'src/main/resources/assets/ftdungeonmobs'
    source_dir = arguments.destination / 'model_sources/dungeon_gates'
    for directory in ('textures/block', 'models/block', 'models/item', 'blockstates'):
        (assets / directory).mkdir(parents=True, exist_ok=True)
    source_dir.mkdir(parents=True, exist_ok=True)
    arguments.output.mkdir(parents=True, exist_ok=True)
    atlas = atlas_texture(assets / 'textures/block/dungeon_gate.png')
    flame = animated_texture(assets / 'textures/block/dungeon_gate_flame.png', 'flame')
    summaries, definitions = [], []
    for returning in (False, True):
        name = 'dungeon_return_gate' if returning else 'dungeon_entry_gate'
        portal = animated_texture(assets / f'textures/block/{name}_portal.png', 'return' if returning else 'entry')
        textures = {'body': atlas, 'portal': portal, 'flame': flame}
        source = boxes(returning)
        split = split_boxes(source)
        model_elements = [model_element(box, motion_neighbors(box, split)) for box in split]
        assert len(source) <= SOURCE_BUDGET and len(split) <= SPLIT_BUDGET
        assert sum(len(element['faces']) for element in model_elements) <= FACE_BUDGET
        multipart = []
        texture_names = {'body': 'ftdungeonmobs:block/dungeon_gate', 'portal': f'ftdungeonmobs:block/{name}_portal',
                         'flame': 'ftdungeonmobs:block/dungeon_gate_flame', 'particle': 'ftdungeonmobs:block/dungeon_gate'}
        for part in range(12):
            elements = [element for box, element in zip(split, model_elements)
                        if box['part'] == part and element['faces'] and not moving(box)]
            patch_json(assets / f'models/block/{name}_{part}.json', {'ambientocclusion': True, 'textures': texture_names, 'elements': elements})
            moving_elements = [element for box, element in zip(split, model_elements)
                               if box['part'] == part and element['faces'] and moving(box)]
            if not returning:
                patch_json(assets / f'models/block/dungeon_gate_moving_{part}.json',
                           {'ambientocclusion': True, 'textures': {'body': texture_names['body'], 'particle': texture_names['particle']},
                            'elements': moving_elements})
            for facing, rotation in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
                multipart.append({'when': {'facing': facing, 'part': str(part)},
                                  'apply': {'model': f'ftdungeonmobs:block/{name}_{part}', 'y': rotation}})
                multipart.append({'when': {'facing': facing, 'part': str(part), 'open': 'false', 'moving': 'false'},
                                  'apply': {'model': f'ftdungeonmobs:block/dungeon_gate_moving_{part}', 'y': rotation}})
        patch_json(assets / f'blockstates/{name}.json', {'multipart': multipart})
        item_elements = []
        for box in source:
            element = model_element(dict(box, origin=[0, 0, 0], stretch=True), source)
            if element['faces']:
                element['from'] = [math.floor(box['start'][0] / 2) - 4, math.floor(box['start'][1] / 2), math.floor(box['start'][2] / 2) + 4]
                element['to'] = [math.ceil(box['end'][0] / 2) - 4, math.ceil(box['end'][1] / 2), math.ceil(box['end'][2] / 2) + 4]
                item_elements.append(element)
        display = {'gui': {'rotation': [15, -30, 0], 'translation': [0, -2, 0], 'scale': [0.44, 0.44, 0.44]},
                   'ground': {'translation': [0, 1, 0], 'scale': [0.25, 0.25, 0.25]},
                   'fixed': {'translation': [0, -3, 0], 'scale': [0.4, 0.4, 0.4]},
                   'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 0, 0], 'scale': [0.25, 0.25, 0.25]},
                   'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -1, 0], 'scale': [0.25, 0.25, 0.25]}}
        patch_json(assets / f'models/item/{name}.json', {'parent': 'minecraft:block/block', 'ambientocclusion': True,
                   'textures': texture_names, 'elements': item_elements, 'display': display})
        editable_count, groups = editable(source_dir / f'{name}.bbmodel', split, textures, name)
        summaries.append({'id': name, 'structuralCuboids': len(source), 'editableCuboids': editable_count, 'editableGroups': groups,
                          'splitCuboids': len(split), 'exportCuboids': sum(bool(element['faces']) for element in model_elements),
                          'faces': sum(len(element['faces']) for element in model_elements),
                          'removedFullyHiddenFaces': len(split) * 6 - sum(len(element['faces']) for element in model_elements),
                          'movingCuboids': sum(bool(element['faces']) for box, element in zip(split, model_elements) if moving(box)),
                          'movingFaces': sum(len(element['faces']) for box, element in zip(split, model_elements) if moving(box)),
                          'parts': 12, 'facingStates': 4, 'openStates': 2, 'movingStates': 2, 'widthBlocks': 3, 'heightBlocks': 4,
                          'itemCuboids': len(item_elements), 'frames': 16, 'ticksPerFrame': 3})
        definitions.append((name, split, textures))
    previews(arguments.output, definitions)
    motion_preview(arguments.output, definitions)
    palette_preview(arguments.output, definitions)
    patch_json(arguments.output / 'model-cost.json', {'budgetStructuralCuboids': SOURCE_BUDGET, 'budgetSplitCuboids': SPLIT_BUDGET,
               'budgetVisibleFaces': FACE_BUDGET, 'gates': summaries, 'sharedBodyTexture': [128, 128],
               'portalTextureEach': [16, 256], 'sharedFlameTexture': [16, 256], 'performanceMeasured': False,
               'reference': 'dungeon-gate-concept-20261101/out; stone skull frame, cuboid winch, chain, portcullis, warm torches',
               'movement': '40-tick sinking/rising iron gate; single anchor renderer; 12 shared native moving models; clip at Y=3/16; static frame never moves',
               'animationGeometryCopies': 1, 'animationTicks': 40, 'sinkUnits': 49, 'blockEntitiesPerGate': 1,
               'tintStates': 6, 'nativeStatesPerGate': 1152, 'extraTintTextures': 0, 'extraTintCuboids': 0,
               'preview': 'software textured cuboid render, not an in-game screenshot'})
    print(json.dumps(summaries, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
