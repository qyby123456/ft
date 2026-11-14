import argparse
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[4]
ASSETS = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_materials/refine/frost'
FONT = 'C:/Windows/Fonts/msyh.ttc'
BG = (22, 28, 43, 255)


def font(size):
    return ImageFont.truetype(FONT, size)


def rotation(axis, angle):
    radians = math.radians(angle)
    cosine, sine = math.cos(radians), math.sin(radians)
    if axis == 'x': return np.array([[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]])
    if axis == 'y': return np.array([[cosine, 0, sine], [0, 1, 0], [-sine, 0, cosine]])
    return np.array([[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]])


def vertices(element):
    left, low, north = element['from']
    right, high, south = element['to']
    planes = {
        'north': [[right, high, north], [left, high, north], [left, low, north], [right, low, north]],
        'south': [[left, high, south], [right, high, south], [right, low, south], [left, low, south]],
        'west': [[left, high, north], [left, high, south], [left, low, south], [left, low, north]],
        'east': [[right, high, south], [right, high, north], [right, low, north], [right, low, south]],
        'up': [[left, high, north], [right, high, north], [right, high, south], [left, high, south]],
        'down': [[left, low, south], [right, low, south], [right, low, north], [left, low, north]],
    }
    result = {key: np.array(value, dtype=float) for key, value in planes.items()}
    transform = element.get('rotation')
    if transform:
        origin = np.array(transform['origin'])
        matrix = rotation(transform['axis'], transform['angle'])
        scaling = np.ones(3)
        if transform.get('rescale'):
            scaling[:] = 1 / math.cos(math.radians(transform['angle']))
            scaling['xyz'.index(transform['axis'])] = 1
        result = {key: ((value - origin) @ matrix.T) * scaling + origin for key, value in result.items()}
    return result


def triangle(image, depth, projected, uv, texture, shading):
    height, width = depth.shape
    minimum = np.maximum(np.floor(projected[:, :2].min(axis=0)).astype(int), [0, 0])
    maximum = np.minimum(np.ceil(projected[:, :2].max(axis=0)).astype(int), [width - 1, height - 1])
    if np.any(minimum > maximum): return
    first, second, third = projected
    denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
    if abs(denominator) < 1e-9: return
    horizontal, vertical = np.meshgrid(np.arange(minimum[0], maximum[0] + 1) + .5, np.arange(minimum[1], maximum[1] + 1) + .5)
    weight_a = ((second[1] - third[1]) * (horizontal - third[0]) + (third[0] - second[0]) * (vertical - third[1])) / denominator
    weight_b = ((third[1] - first[1]) * (horizontal - third[0]) + (first[0] - third[0]) * (vertical - third[1])) / denominator
    weight_c = 1 - weight_a - weight_b
    distance = weight_a * first[2] + weight_b * second[2] + weight_c * third[2]
    region = np.s_[minimum[1]:maximum[1] + 1, minimum[0]:maximum[0] + 1]
    texture_u = np.clip(np.floor(weight_a * uv[0, 0] + weight_b * uv[1, 0] + weight_c * uv[2, 0]).astype(int), 0, texture.shape[1] - 1)
    texture_v = np.clip(np.floor(weight_a * uv[0, 1] + weight_b * uv[1, 1] + weight_c * uv[2, 1]).astype(int), 0, texture.shape[0] - 1)
    samples = texture[texture_v, texture_u]
    mask = (weight_a >= -1e-6) & (weight_b >= -1e-6) & (weight_c >= -1e-6) & (distance > depth[region]) & (samples[:, :, 3] >= 128)
    rgb = np.clip(samples[:, :, :3].astype(float) * shading, 0, 255).astype(np.uint8)
    image[region][mask] = np.column_stack((rgb[mask], np.full(np.count_nonzero(mask), 255, dtype=np.uint8)))
    depth[region][mask] = distance[mask]


def render(model, texture, yaw, elevation=0, size=(480, 400), transparent=False, center=(8, 8, 8), world_width=17):
    width, height = size
    image = np.zeros((height, width, 4), dtype=np.uint8)
    if not transparent: image[:] = BG
    depth = np.full((height, width), -np.inf)
    angle, lift = np.radians([yaw, elevation])
    camera = np.array([np.sin(angle) * np.cos(lift), np.sin(lift), -np.cos(angle) * np.cos(lift)])
    right = np.array([-np.cos(angle), 0, -np.sin(angle)])
    up = np.cross(camera, right)
    basis = np.array([right, -up, camera])
    scale = min(width, height) / world_width
    light = np.array([-.4, .82, -.48])
    light /= np.linalg.norm(light)
    for element in model['elements']:
        planes = vertices(element)
        for direction, data in element['faces'].items():
            points = planes[direction]
            normal = np.cross(points[1] - points[0], points[2] - points[0])
            normal = -normal
            normal /= np.linalg.norm(normal)
            if np.dot(normal, camera) < 1e-7: continue
            shaded = .73 + .27 * max(0, np.dot(normal, light))
            projected = (points - np.array(center)) @ basis.T
            projected[:, 0] = projected[:, 0] * scale + width / 2
            projected[:, 1] = projected[:, 1] * scale + height / 2
            u0, v0, u1, v1 = np.array(data['uv']) / 16 * np.array([texture.shape[1], texture.shape[0], texture.shape[1], texture.shape[0]])
            uv = np.array([[u0, v0], [u1, v0], [u1, v1], [u0, v1]])
            uv = np.roll(uv, data.get('rotation', 0) // 90, axis=0)
            for indices in ([0, 1, 2], [0, 2, 3]):
                triangle(image, depth, projected[indices], uv[indices], texture, shaded)
    return Image.fromarray(image)


def load_runtime():
    model_path = ASSETS / 'models/item/boss_materials/frost_material.json'
    model = json.loads(model_path.read_text(encoding='utf-8'))
    texture_path = ASSETS / ('textures/' + model['textures']['atlas'].split(':')[1] + '.png')
    return model, np.array(Image.open(texture_path).convert('RGBA'))


def icon(model, texture):
    item = render(model, texture, -30, 25, (32, 32), True, center=(8, 7.8, 8), world_width=15.8)
    alpha = item.getchannel('A')
    border = alpha.filter(ImageFilter.MaxFilter(3))
    silhouette = Image.new('RGBA', item.size, (35, 30, 53, 0))
    silhouette.putalpha(border)
    silhouette.alpha_composite(item)
    silhouette.save(ASSETS / 'textures/item/frost_material.png')


def previews():
    ART.mkdir(parents=True, exist_ok=True)
    model, texture = load_runtime()
    views = [('正面 FRONT', 0), ('背面 BACK', 180), ('左侧 LEFT', -90), ('右侧 RIGHT', 90)]
    sheet = Image.new('RGBA', (1120, 960), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((34, 24), '霜糖晶冠配件 · 最终运行时四视图', font=font(27), fill='#f5e9d7')
    draw.text((34, 64), '46 结构件 / 276 逐面 UV / 2 px·unit⁻¹ / Java 单轴旋转', font=font(17), fill='#a9baca')
    for index, (label, yaw) in enumerate(views):
        frame = render(model, texture, yaw, size=(540, 360), center=(8, 8, 8), world_width=16)
        xpos = 10 + (index % 2) * 555
        ypos = 115 + (index // 2) * 410
        sheet.paste(frame, (xpos, ypos))
        draw.text((xpos + 24, ypos + 359), label, font=font(20), fill='#d4dfe9')
        frame.save(ART / f'{["front", "back", "left", "right"][index]}.png')
    sheet.convert('RGB').save(ART / 'four_views.png')
    oblique = render(model, texture, -32, 25, (1000, 700), center=(8, 7.8, 8), world_width=16)
    draw = ImageDraw.Draw(oblique)
    draw.text((34, 26), '霜糖切面晶锭', font=font(31), fill='#f5e9d7')
    draw.text((34, 68), '乳白糖衣 · 玫粉霜层 · 冰蓝结晶 · 金色王冠印', font=font(18), fill='#a9baca')
    oblique.convert('RGB').save(ART / 'oblique_detail.png')
    rear = render(model, texture, 145, 27, (720, 560), center=(8, 7.8, 8), world_width=16)
    rear.convert('RGB').save(ART / 'rear_oblique.png')
    icon_image = Image.open(ASSETS / 'textures/item/frost_material.png').convert('RGBA')
    compare = Image.new('RGBA', (1000, 550), BG)
    draw = ImageDraw.Draw(compare)
    draw.text((30, 24), '背包图标 / 同件材料的斜视像素投影', font=font(25), fill='#f5e9d7')
    draw.text((30, 70), '原尺寸 32×32', font=font(18), fill='#a9baca')
    compare.alpha_composite(icon_image, (72, 130))
    compare.alpha_composite(icon_image.resize((352, 352), Image.Resampling.NEAREST), (220, 115))
    draw.text((220, 485), '最近邻 11× · 保留像素层次', font=font(18), fill='#a9baca')
    old_path = ROOT / '_work/boss-materials-refine-20261021/frost/backup-before-refine/src__main__resources__assets__ftboss__textures__item__frost_material.png'
    if old_path.exists():
        old = Image.open(old_path).convert('RGBA')
        compare.alpha_composite(old.resize((224, 224), Image.Resampling.NEAREST), (675, 168))
        draw.text((665, 485), '重制前 16×16', font=font(18), fill='#a9baca')
    compare.convert('RGB').save(ART / 'icon_comparison.png')
    Image.fromarray(texture).resize((768, 768), Image.Resampling.NEAREST).save(ART / 'atlas_preview.png')
    print('Rendered final runtime assets: 4 cardinal views + front/rear obliques + icon comparison')


def reference_queen():
    source = json.loads((ASSETS / 'geo/sugar_queen_v3.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
    elements = []
    for bone in source['bones']:
        for source_cube in bone.get('cubes', []):
            low = source_cube['origin']
            high = [start + size for start, size in zip(low, source_cube['size'])]
            if min(source_cube['size']) <= 0: continue
            element = {'from': low, 'to': high, 'faces': {}}
            for face, entry in source_cube['uv'].items():
                u0, v0 = entry['uv']
                du, dv = entry['uv_size']
                element['faces'][face] = {'uv': [u0 / 16, v0 / 16, (u0 + du) / 16, (v0 + dv) / 16]}
            active = [(axis, value) for axis, value in zip('xyz', source_cube.get('rotation', [0, 0, 0])) if value]
            if len(active) == 1:
                element['rotation'] = {'axis': active[0][0], 'angle': active[0][1], 'origin': source_cube.get('pivot', bone['pivot'])}
            elements.append(element)
    texture = np.array(Image.open(ASSETS / 'textures/entity/sugar_queen_v3.png').convert('RGBA'))
    reference = render({'elements': elements}, texture, -20, 8, (700, 900), center=(0, 23, 0), world_width=54)
    reference.convert('RGB').save(ART / 'references/queen_actual_model.png')
    print('Read-only queen model/skin reference rendered')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--make-icon', action='store_true')
    parser.add_argument('--reference', action='store_true')
    args = parser.parse_args()
    if args.reference: reference_queen()
    if args.make_icon: icon(*load_runtime())
    previews()
