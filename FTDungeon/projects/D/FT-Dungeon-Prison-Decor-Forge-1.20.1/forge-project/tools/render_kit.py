import argparse
import base64
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from export_kit import DISPLAY, FACES, NORMALS, PROJECT, RESOURCES, ROOT, resolve_native_model, texture_file, texture_reference


FONT = Path('C:/Windows/Fonts/msyh.ttc')
LANE_NAMES = {'masonry': '01 / 冷石建筑', 'furnishings': '02 / 囚室陈设', 'lighting': '03 / 幽光铁作'}
LANE_COLORS = {'masonry': '#849cab', 'furnishings': '#c19775', 'lighting': '#68d1c8'}


def font(size):
    return ImageFont.truetype(str(FONT), size)


def face_vertices(element, face):
    lower_x, lower_y, lower_z = element['from']
    upper_x, upper_y, upper_z = element['to']
    return np.array({
        'north': [(upper_x, upper_y, lower_z), (lower_x, upper_y, lower_z), (lower_x, lower_y, lower_z), (upper_x, lower_y, lower_z)],
        'south': [(lower_x, upper_y, upper_z), (upper_x, upper_y, upper_z), (upper_x, lower_y, upper_z), (lower_x, lower_y, upper_z)],
        'east': [(upper_x, upper_y, upper_z), (upper_x, upper_y, lower_z), (upper_x, lower_y, lower_z), (upper_x, lower_y, upper_z)],
        'west': [(lower_x, upper_y, lower_z), (lower_x, upper_y, upper_z), (lower_x, lower_y, upper_z), (lower_x, lower_y, lower_z)],
        'up': [(lower_x, upper_y, lower_z), (upper_x, upper_y, lower_z), (upper_x, upper_y, upper_z), (lower_x, upper_y, upper_z)],
        'down': [(lower_x, lower_y, upper_z), (upper_x, lower_y, upper_z), (upper_x, lower_y, lower_z), (lower_x, lower_y, lower_z)],
    }[face], dtype=float)


def uv_vertices(details):
    lower_u, lower_v, upper_u, upper_v = details['uv']
    points = np.array([(lower_u, lower_v), (upper_u, lower_v), (upper_u, upper_v), (lower_u, upper_v)], dtype=float)
    return np.roll(points, -(details.get('rotation', 0) // 90), axis=0)


def render(model, size=(320, 260), yaw=42, pitch=27, dark=False):
    width, height = size
    yaw, pitch = math.radians(yaw), math.radians(pitch)
    camera = np.array([math.sin(yaw) * math.cos(pitch), math.sin(pitch), -math.cos(yaw) * math.cos(pitch)])
    right = np.array([-math.cos(yaw), 0, -math.sin(yaw)])
    up = np.cross(camera, right)
    basis = np.stack((right, -up, camera), axis=1)
    scale = min(width / 27, height / 24)
    background = np.array([22, 31, 41] if dark else [34, 45, 56], dtype=float)
    color_buffer = np.zeros((height, width, 3), dtype=float) + background
    depth_buffer = np.full((height, width), -np.inf)
    light = np.array([-0.35, 0.85, -0.4])
    light /= np.linalg.norm(light)
    image_cache = {}
    for element in model['elements']:
        for face, details in element['faces'].items():
            normal = np.array(NORMALS[face])
            if np.dot(normal, camera) <= 0:
                continue
            texture = texture_reference(model, details['texture'])
            if texture not in image_cache:
                image_cache[texture] = np.asarray(Image.open(texture_file(texture)).convert('RGB'), dtype=float)
            pixels = image_cache[texture]
            transformed = (face_vertices(element, face) - [8, 8, 8]) @ basis
            projected = transformed.copy()
            projected[:, 0] = transformed[:, 0] * scale + width / 2
            projected[:, 1] = transformed[:, 1] * scale + height / 2 + 4
            texture_points = uv_vertices(details) * np.array([pixels.shape[1], pixels.shape[0]]) / 16
            shade = (0.45 if dark else 0.68) + (0.25 if dark else 0.32) * max(0, np.dot(normal, light))
            for triangle in ((0, 1, 2), (0, 2, 3)):
                positions = projected[list(triangle)]
                coordinates = texture_points[list(triangle)]
                minimum_x = max(0, int(math.floor(positions[:, 0].min())))
                maximum_x = min(width - 1, int(math.ceil(positions[:, 0].max())))
                minimum_y = max(0, int(math.floor(positions[:, 1].min())))
                maximum_y = min(height - 1, int(math.ceil(positions[:, 1].max())))
                if minimum_x > maximum_x or minimum_y > maximum_y:
                    continue
                matrix = np.stack((positions[1, :2] - positions[0, :2], positions[2, :2] - positions[0, :2]), axis=1)
                if abs(np.linalg.det(matrix)) < 0.000001:
                    continue
                mesh_x, mesh_y = np.meshgrid(np.arange(minimum_x, maximum_x + 1) + 0.5,
                                              np.arange(minimum_y, maximum_y + 1) + 0.5)
                points = np.stack((mesh_x - positions[0, 0], mesh_y - positions[0, 1]), axis=-1)
                weights = points @ np.linalg.inv(matrix).T
                weight_first, weight_second = weights[:, :, 0], weights[:, :, 1]
                valid = (weight_first >= -0.00001) & (weight_second >= -0.00001) & (weight_first + weight_second <= 1.00001)
                depth = positions[0, 2] + weight_first * (positions[1, 2] - positions[0, 2]) + weight_second * (positions[2, 2] - positions[0, 2])
                region = depth_buffer[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
                valid &= depth > region
                sampled = coordinates[0] + weights[:, :, 0, None] * (coordinates[1] - coordinates[0]) + weights[:, :, 1, None] * (coordinates[2] - coordinates[0])
                sampled_x = np.clip(np.floor(sampled[:, :, 0]).astype(int), 0, pixels.shape[1] - 1)
                sampled_y = np.clip(np.floor(sampled[:, :, 1]).astype(int), 0, pixels.shape[0] - 1)
                color_region = color_buffer[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
                color_region[valid] = pixels[sampled_y[valid], sampled_x[valid]] * shade
                region[valid] = depth[valid]
    return Image.fromarray(np.clip(color_buffer, 0, 255).astype(np.uint8))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    arguments = parser.parse_args()
    catalog = json.loads((ROOT / 'catalog.json').read_text(encoding='utf-8'))
    preview = ROOT / 'preview'
    views = preview / 'four-view'
    views.mkdir(parents=True, exist_ok=True)
    poster = Image.new('RGB', (1880, 1348), '#141e29')
    drawing = ImageDraw.Draw(poster)
    drawing.text((34, 20), 'THE FORGOTTEN CELLS', font=font(34), fill='#e6e2d5')
    drawing.text((34, 69), 'FT 地牢装饰 · 1.1.0 / 指定十二款重制，保留其余十二款', font=font(22), fill='#a4bdc8')
    drawing.text((34, 104), '冷石建筑 × 8     囚室陈设 × 8     幽光铁作 × 8   |   Java 1.20.1 · Forge', font=font(16), fill='#758d9c')
    textures = {}
    payload = []
    audit_views = []
    with zipfile.ZipFile(arguments.minecraft_jar) as archive:
        for index, entry in enumerate(catalog):
            model_path = RESOURCES / f'assets/ftdungeonprops/models/block/{entry["id"]}.json'
            model = resolve_native_model(json.loads(model_path.read_text()), archive)
            front = render(model, (298, 230))
            across, height = 20 + index % 6 * 308, 148 + index // 6 * 291
            drawing.rounded_rectangle((across, height, across + 298, height + 277), radius=9, fill='#222d38', outline='#344654')
            poster.paste(front, (across, height + 1))
            drawing.line((across + 12, height + 235, across + 286, height + 235), fill=LANE_COLORS[entry['lane']], width=2)
            drawing.text((across + 12, height + 243), f'{index + 1:02d}  {entry["name_zh"]}', font=font(19), fill='#e2e2d6')
            front_path = preview / (entry['id'] + '.png')
            front.save(front_path)
            sheet = Image.new('RGB', (660, 648), '#141e29')
            sheet_draw = ImageDraw.Draw(sheet)
            sheet_draw.text((16, 10), entry['name_zh'] + ' / ' + entry['id'], font=font(18), fill='#e8e5dc')
            for view_index, (label, yaw) in enumerate((('东北 / front-right', 42), ('东南 / back-right', 138),
                                                       ('西南 / back-left', 222), ('西北 / front-left', 318))):
                image = render(model, (320, 260), yaw=yaw)
                destination = views / f'{entry["id"]}-{view_index + 1}.png'
                image.save(destination)
                x_position, y_position = 10 + view_index % 2 * 325, 48 + view_index // 2 * 294
                sheet.paste(image, (x_position, y_position))
                sheet_draw.text((x_position + 8, y_position + 264), label, font=font(14), fill='#9dafbc')
                audit_views.append(destination.relative_to(ROOT).as_posix())
            sheet.save(views / (entry['id'] + '-sheet.png'))
            off_model = None
            if entry['light']:
                off_path = model_path.with_name(entry['id'] + '_off.json')
                off_model = resolve_native_model(json.loads(off_path.read_text()), archive)
                comparison = Image.new('RGB', (660, 328), '#141e29')
                comparison.paste(render(model, (320, 270), dark=True), (5, 35))
                comparison.paste(render(off_model, (320, 270), dark=True), (335, 35))
                comparison_draw = ImageDraw.Draw(comparison)
                comparison_draw.text((14, 8), entry['name_zh'] + f'  /  LIT=true ({entry["light"]})', font=font(16), fill='#76d5c9')
                comparison_draw.text((344, 8), 'LIT=false (0)', font=font(16), fill='#a6acb4')
                comparison_draw.text((15, 306), '固定低照明软件渲染；不是游戏光传播或着色器发光测试', font=font(12), fill='#8494a1')
                comparison.save(preview / (entry['id'] + '-on-off.png'))
            for current in [model, *([off_model] if off_model else [])]:
                for element in current['elements']:
                    for face in element['faces'].values():
                        reference = texture_reference(current, face['texture'])
                        textures[reference] = 'data:image/png;base64,' + base64.b64encode(texture_file(reference).read_bytes()).decode()
            payload.append({**{key: entry[key] for key in ('id', 'name_zh', 'name_en', 'lane', 'mount', 'light', 'description_zh')},
                            'remade': 'material_set' in entry, 'number': index + 1,
                            'model': model, 'off_model': off_model,
                            'image': 'data:image/png;base64,' + base64.b64encode(front_path.read_bytes()).decode()})
    drawing.text((34, 1320), '实际导出 JSON 渲染 · 非 Minecraft 客户端截图 · 3 条独立制作聊天，主聊天串行集成验收', font=font(15), fill='#8295a3')
    poster.save(preview / 'all-24.png')
    template = (PROJECT / 'tools/gallery_template.html').read_text(encoding='utf-8')
    data = json.dumps({'entries': payload, 'textures': textures}, ensure_ascii=False)
    (preview / 'index.html').write_text(template.replace('__ASSET_DATA__', data), encoding='utf-8')
    report = {'models_rendered': len(payload), 'four_view_images': len(audit_views), 'light_comparisons': 7,
              'renderer': 'Orthographic textured CPU rasterizer with depth buffer, nearest sampling, fixed camera/light',
              'source': 'Final exported block JSON plus Minecraft 1.20.1 built-in parents',
              'cameras': {'yaw_degrees': [42, 138, 222, 318], 'pitch_degrees': 27},
              'limitations': ['Software previews are not Blockbench or Minecraft client acceptance.',
                              'Preview does not simulate light propagation, shaders, item hand occlusion or FPS.'],
              'files': audit_views}
    (ROOT / 'qa/render-coverage.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'models': len(payload), 'four_views': len(audit_views), 'preview': str(preview / 'index.html')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
