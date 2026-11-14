import argparse
import hashlib
import json
from pathlib import Path
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFont

PROJECT = Path(__file__).resolve().parents[2]
ASSETS = PROJECT / 'src/main/resources/assets/ftboss'
ART = PROJECT / 'art/boss_materials'
NAMES = ('frost', 'butcher', 'tea', 'wine', 'cheese', 'capacitor', 'spore', 'apple', 'shepherd', 'tide')
TITLES = ('霜糖晶冠', '屠夫骨牙', '茶心纤维', '陈酿酒封', '奶酪月牙', '蓄能电芯', '虹彩孢子', '金苹果核', '牧野哨骨', '潮汐晶材')
CONCEPTS = ('糖霜晶锭', '血骨与獠牙', '茶脉纤维束', '紫酿封蜡锭', '熟成月牙酪', '铜极蓄能芯', '虹彩菌簇', '鎏金果核锭', '牧野束骨', '深海潮晶')
FONT = 'C:/Windows/Fonts/msyh.ttc'
VIEWS = (('正面', 0), ('背面', 180), ('左侧', -90), ('右侧', 90))


def font(size):
    return ImageFont.truetype(FONT, size)


def faces(cube):
    left, low, front = cube['from']
    right, high, back = cube['to']
    return {
        'north': [[right, high, front], [left, high, front], [left, low, front], [right, low, front]],
        'south': [[left, high, back], [right, high, back], [right, low, back], [left, low, back]],
        'west': [[left, high, front], [left, high, back], [left, low, back], [left, low, front]],
        'east': [[right, high, back], [right, high, front], [right, low, front], [right, low, back]],
        'up': [[left, high, back], [right, high, back], [right, high, front], [left, high, front]],
        'down': [[left, low, front], [right, low, front], [right, low, back], [left, low, back]],
    }


def triangle(image, depth, projected, uv, texture, shade):
    height, width = depth.shape
    minimum = np.maximum(np.floor(projected[:, :2].min(axis=0)).astype(int), [0, 0])
    maximum = np.minimum(np.ceil(projected[:, :2].max(axis=0)).astype(int), [width - 1, height - 1])
    if np.any(minimum > maximum):
        return
    first, second, third = projected
    denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
    if abs(denominator) < 1e-8:
        return
    horizontal, vertical = np.meshgrid(np.arange(minimum[0], maximum[0] + 1) + .5, np.arange(minimum[1], maximum[1] + 1) + .5)
    weight_a = ((second[1] - third[1]) * (horizontal - third[0]) + (third[0] - second[0]) * (vertical - third[1])) / denominator
    weight_b = ((third[1] - first[1]) * (horizontal - third[0]) + (first[0] - third[0]) * (vertical - third[1])) / denominator
    weight_c = 1 - weight_a - weight_b
    distance = weight_a * first[2] + weight_b * second[2] + weight_c * third[2]
    region = np.s_[minimum[1]:maximum[1] + 1, minimum[0]:maximum[0] + 1]
    texture_u = np.clip((weight_a * uv[0, 0] + weight_b * uv[1, 0] + weight_c * uv[2, 0]).astype(int), 0, texture.shape[1] - 1)
    texture_v = np.clip((weight_a * uv[0, 1] + weight_b * uv[1, 1] + weight_c * uv[2, 1]).astype(int), 0, texture.shape[0] - 1)
    mask = ((weight_a >= -1e-6) & (weight_b >= -1e-6) & (weight_c >= -1e-6) & (distance > depth[region]) & (texture[texture_v, texture_u, 3] > 0))
    colors = texture[texture_v, texture_u, :3].astype(float) * shade
    image[region][mask] = np.clip(colors[mask], 0, 255).astype(np.uint8)
    depth[region][mask] = distance[mask]


def render(name, yaw, elevation=0, size=(290, 220)):
    model = json.loads((ASSETS / 'models/item/boss_materials' / (name + '_material.json')).read_text(encoding='utf-8'))
    texture = np.array(Image.open(ASSETS / 'textures' / (model['textures']['atlas'].split(':')[1] + '.png')).convert('RGBA'))
    width, height = size
    image = np.full((height, width, 3), [29, 40, 50], dtype=np.uint8)
    depth = np.full((height, width), -np.inf)
    angle, lift = np.radians([yaw, elevation])
    camera = np.array([np.sin(angle) * np.cos(lift), np.sin(lift), -np.cos(angle) * np.cos(lift)])
    right = np.array([-np.cos(angle), 0, -np.sin(angle)])
    up = np.cross(camera, right)
    basis = np.array([right, -up, camera])
    scale = min(width / 18, height / 17)
    light = np.array([-0.35, 0.85, -0.4])
    light /= np.linalg.norm(light)
    normals = {'north': [0, 0, -1], 'south': [0, 0, 1], 'east': [1, 0, 0], 'west': [-1, 0, 0], 'up': [0, 1, 0], 'down': [0, -1, 0]}
    for cube in model['elements']:
        for direction, points in faces(cube).items():
            normal = np.array(normals[direction], dtype=float)
            if np.dot(normal, camera) <= 1e-8:
                continue
            shade = .7 + .3 * max(0, np.dot(normal, light))
            projected = (np.array(points) - [8, 8, 8]) @ basis.T
            projected[:, :2] = projected[:, :2] * scale + [width / 2, height / 2]
            left, top, right_uv, bottom = cube['faces'][direction]['uv']
            uv = np.array([[left, top], [right_uv, top], [right_uv, bottom], [left, bottom]]) * texture.shape[0] / 16
            for indices in ((0, 1, 2), (0, 2, 3)):
                triangle(image, depth, projected[list(indices)], uv[list(indices)], texture, shade)
    assert np.any(np.isfinite(depth)), name + ': empty view'
    assert not any(np.any(np.isfinite(edge)) for edge in (depth[:2], depth[-2:], depth[:, :2], depth[:, -2:])), name + ': clipping'
    return Image.fromarray(image)


def verify():
    report = []
    for name in NAMES:
        model_path = ASSETS / 'models/item/boss_materials' / (name + '_material.json')
        model = json.loads(model_path.read_text(encoding='utf-8'))
        texture_path = ASSETS / 'textures/item/boss_materials' / (name + '.png')
        texture = Image.open(texture_path).convert('RGBA')
        icon = Image.open(ASSETS / 'textures/item' / (name + '_material.png')).convert('RGBA')
        assert icon.size == (16, 16) and texture.size == (64, 64)
        assert 'parent' not in model
        for cube in model['elements']:
            assert all(0 <= start < end <= 16 for start, end in zip(cube['from'], cube['to'])), name
            assert len(cube['faces']) == 6
            for face in cube['faces'].values():
                assert all(0 <= coordinate <= 16 for coordinate in face['uv'])
                left, top, right_uv, bottom = [int(coordinate * 4) for coordinate in face['uv']]
                assert texture.crop((left, top, max(right_uv, left + 1), max(bottom, top + 1))).getchannel('A').getextrema() == (255, 255), name
        for first_index, first in enumerate(model['elements']):
            for second in model['elements'][first_index + 1:]:
                for axis in range(3):
                    for edge in ('from', 'to'):
                        if abs(first[edge][axis] - second[edge][axis]) > 1e-7:
                            continue
                        remaining = [other for other in range(3) if other != axis]
                        overlap = all(min(first['to'][other], second['to'][other]) - max(first['from'][other], second['from'][other]) > 1e-7 for other in remaining)
                        assert not overlap, name + ': coplanar faces ' + first['name'] + ' / ' + second['name']
        connected = {0}
        while True:
            previous = len(connected)
            for index, cube in enumerate(model['elements']):
                if index in connected:
                    continue
                if any(all(min(cube['to'][axis], model['elements'][other]['to'][axis]) - max(cube['from'][axis], model['elements'][other]['from'][axis]) >= -1e-7 for axis in range(3)) for other in connected):
                    connected.add(index)
            if len(connected) == previous:
                break
        assert len(connected) == len(model['elements']), name + ': disconnected cubes ' + str(set(range(len(model['elements']))) - connected)
        report.append({'id': name, 'cubes': len(model['elements']), 'connectedCubes': len(connected), 'modelSha256': hashlib.sha256(model_path.read_bytes()).hexdigest(), 'textureSha256': hashlib.sha256(texture_path.read_bytes()).hexdigest(), 'views': [title for title, _ in VIEWS]})
    return report


def main():
    global ASSETS
    parser = argparse.ArgumentParser()
    parser.add_argument('--jar', type=Path)
    args = parser.parse_args()
    ART.mkdir(parents=True, exist_ok=True)
    proof = []
    if args.jar:
        extracted = PROJECT / '_work/boss-materials-redraw-20261021/runtime-assets/assets/ftboss'
        with zipfile.ZipFile(args.jar) as archive:
            for name in NAMES:
                item = name + '_material'
                relatives = ('models/item/' + item + '.json', 'models/item/boss_materials/' + item + '.json', 'models/item/boss_materials/' + item + '_gui.json', 'textures/item/' + item + '.png', 'textures/item/boss_materials/' + name + '.png')
                for relative in relatives:
                    data = archive.read('assets/ftboss/' + relative)
                    assert data == (ASSETS / relative).read_bytes(), relative + ': source/packaged mismatch'
                    target = extracted / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(data)
                    proof.append({'path': relative, 'sha256': hashlib.sha256(data).hexdigest()})
        ASSETS = extracted
    report = verify()
    overview = Image.new('RGB', (1500, 1010), '#141e28')
    draw = ImageDraw.Draw(overview)
    draw.text((28, 20), 'Boss 掉落材料 / 独立造型与立体模型', font=font(31), fill='#f4ebd6')
    draw.text((28, 67), '16×16 物品栏像素图标 · 手持与掉落独立 3D 模型', font=font(18), fill='#a2bdca')
    for index, name in enumerate(NAMES):
        column, row = index % 5, index // 5
        origin_x, origin_y = column * 300, row * 430 + 115
        overview.paste(render(name, -32, 22, (280, 235)), (origin_x + 10, origin_y))
        icon = Image.open(ASSETS / 'textures/item' / (name + '_material.png')).convert('RGBA').resize((112, 112), Image.Resampling.NEAREST)
        overview.paste(icon, (origin_x + 94, origin_y + 230), icon)
        draw.text((origin_x + 35, origin_y + 354), TITLES[index], font=font(23), fill='#efe5ce')
        draw.text((origin_x + 35, origin_y + 389), CONCEPTS[index], font=font(17), fill='#9fb8c8')
    overview.save(ART / 'overview.png')
    for group in range(2):
        sheet = Image.new('RGB', (1200, 1400), '#141e28')
        draw = ImageDraw.Draw(sheet)
        draw.text((22, 15), 'Boss 材料 / 正面、背面、左侧、右侧 / ' + ('最终 JAR 资源' if args.jar else '源资源'), font=font(23), fill='#eee5d3')
        for row in range(5):
            index = group * 5 + row
            for column, (title, yaw) in enumerate(VIEWS):
                sheet.paste(render(NAMES[index], yaw), (column * 300 + 5, row * 265 + 65))
                draw.text((column * 300 + 16, row * 265 + 289), TITLES[index] + ' · ' + title, font=font(17), fill='#c2d1d9')
        sheet.save(ART / ('four_views_' + str(group + 1) + '.png'))
    result = {'kind': 'static-resource-geometry-and-render-check', 'jar': str(args.jar) if args.jar else None, 'items': report, 'runtimeResources': proof, 'docsImpact': 'none', 'gameplayAcceptance': False}
    (ART / 'verification.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + chr(10), encoding='utf-8')
    print('PASS: 10 materials; connected geometry, UV coverage, bounds, 40 cardinal views, overview')


if __name__ == '__main__':
    main()
