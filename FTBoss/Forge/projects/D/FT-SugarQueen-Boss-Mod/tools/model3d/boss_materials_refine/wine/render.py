from pathlib import Path
import argparse
import json
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[4]
ASSET = ROOT / 'src/main/resources/assets/ftboss'
OUT = ROOT / 'art/boss_materials/refine/wine'
OUT.mkdir(parents=True, exist_ok=True)
FONT_PATH = 'C:/Windows/Fonts/consola.ttf'

def unit(vector):
    return vector / np.linalg.norm(vector)

def rotation_matrix(axis, degrees):
    angle = math.radians(degrees)
    cosine, sine = math.cos(angle), math.sin(angle)
    if axis == 'x':
        return np.array([[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]])
    if axis == 'y':
        return np.array([[cosine, 0, sine], [0, 1, 0], [-sine, 0, cosine]])
    return np.array([[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]])

def face_geometry(element, name):
    low_x, low_y, low_z = element['from']
    high_x, high_y, high_z = element['to']
    vertices = {
        'north': [[high_x, high_y, low_z], [low_x, high_y, low_z], [low_x, low_y, low_z], [high_x, low_y, low_z]],
        'south': [[low_x, high_y, high_z], [high_x, high_y, high_z], [high_x, low_y, high_z], [low_x, low_y, high_z]],
        'east': [[high_x, high_y, high_z], [high_x, high_y, low_z], [high_x, low_y, low_z], [high_x, low_y, high_z]],
        'west': [[low_x, high_y, low_z], [low_x, high_y, high_z], [low_x, low_y, high_z], [low_x, low_y, low_z]],
        'up': [[low_x, high_y, low_z], [high_x, high_y, low_z], [high_x, high_y, high_z], [low_x, high_y, high_z]],
        'down': [[low_x, low_y, high_z], [high_x, low_y, high_z], [high_x, low_y, low_z], [low_x, low_y, low_z]]
    }
    normals = {'north': [0, 0, -1], 'south': [0, 0, 1], 'east': [1, 0, 0], 'west': [-1, 0, 0], 'up': [0, 1, 0], 'down': [0, -1, 0]}
    points, normal = np.array(vertices[name], dtype=float), np.array(normals[name], dtype=float)
    transform = element.get('rotation')
    if transform:
        origin = np.array(transform['origin'], dtype=float)
        matrix = rotation_matrix(transform['axis'], transform['angle'])
        points = (points - origin) @ matrix.T
        if transform.get('rescale'):
            scales = np.full(3, 1 / math.cos(math.radians(transform['angle'])))
            scales['xyz'.index(transform['axis'])] = 1
            points *= scales
        points += origin
        normal = matrix @ normal
    return points, normal

def render(model, textures, camera, width=600, height=460, scale=39, background=True, outline=False):
    camera = unit(np.array(camera, dtype=float))
    screen_right = unit(np.cross([0, 1, 0], camera))
    screen_up = unit(np.cross(camera, screen_right))
    center = np.array([8, 7.9, 8])
    depth_buffer = np.full((height, width), -np.inf)
    frame = np.zeros((height, width, 4), dtype=np.uint8)
    light = unit(np.array([-0.6, 1.0, -0.65]))
    visible_faces = []
    for element in model['elements']:
        for face_name, face in element['faces'].items():
            points, normal = face_geometry(element, face_name)
            if np.dot(normal, camera) <= 1e-7:
                continue
            relative = points - center
            projected = np.column_stack([width / 2 + relative @ screen_right * scale, height / 2 - relative @ screen_up * scale, relative @ camera])
            texture_key = face['texture'].lstrip('#')
            texture = textures[texture_key]
            low_u, low_v, high_u, high_v = np.array(face['uv']) / 16
            coordinates = np.array([[low_u, low_v], [high_u, low_v], [high_u, high_v], [low_u, high_v]])
            coordinates = np.roll(coordinates, -int(face.get('rotation', 0) / 90), axis=0)
            brightness = min(1.13, 0.73 + 0.39 * max(0, np.dot(normal, light)))
            for triangle in [(0, 1, 2), (0, 2, 3)]:
                triangle = list(triangle)
                vertices = projected[triangle]
                min_x = max(0, int(np.floor(vertices[:, 0].min())))
                max_x = min(width - 1, int(np.ceil(vertices[:, 0].max())))
                min_y = max(0, int(np.floor(vertices[:, 1].min())))
                max_y = min(height - 1, int(np.ceil(vertices[:, 1].max())))
                if min_x > max_x or min_y > max_y:
                    continue
                grid_y, grid_x = np.mgrid[min_y:max_y + 1, min_x:max_x + 1].astype(float)
                grid_x += 0.5
                grid_y += 0.5
                first, second, third = vertices
                denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
                if abs(denominator) < 1e-10:
                    continue
                weight_a = ((second[1] - third[1]) * (grid_x - third[0]) + (third[0] - second[0]) * (grid_y - third[1])) / denominator
                weight_b = ((third[1] - first[1]) * (grid_x - third[0]) + (first[0] - third[0]) * (grid_y - third[1])) / denominator
                weight_c = 1 - weight_a - weight_b
                inside = (weight_a >= -1e-7) & (weight_b >= -1e-7) & (weight_c >= -1e-7)
                depth = weight_a * first[2] + weight_b * second[2] + weight_c * third[2]
                sample_uv = weight_a[..., None] * coordinates[triangle[0]] + weight_b[..., None] * coordinates[triangle[1]] + weight_c[..., None] * coordinates[triangle[2]]
                texture_x = np.clip((sample_uv[..., 0] * texture.shape[1]).astype(int), 0, texture.shape[1] - 1)
                texture_y = np.clip((sample_uv[..., 1] * texture.shape[0]).astype(int), 0, texture.shape[0] - 1)
                color = texture[texture_y, texture_x].copy()
                color[..., :3] = np.clip(color[..., :3].astype(float) * brightness, 0, 255).astype(np.uint8)
                region_depth = depth_buffer[min_y:max_y + 1, min_x:max_x + 1]
                region_frame = frame[min_y:max_y + 1, min_x:max_x + 1]
                mask = inside & (depth > region_depth + 1e-8) & (color[..., 3] > 127)
                region_depth[mask] = depth[mask]
                region_frame[mask] = color[mask]
            visible_faces.append((element['name'], face_name))
    image = Image.fromarray(frame)
    if outline:
        mask = image.getchannel('A')
        expanded = mask.filter(ImageFilter.MaxFilter(3))
        edge = Image.new('RGBA', image.size, (35, 22, 32, 255))
        edge.putalpha(expanded)
        edge.alpha_composite(image)
        image = edge
    if background:
        base = Image.new('RGBA', (width, height), '#20252e')
        draw = ImageDraw.Draw(base)
        draw.ellipse((width * 0.2, height * 0.71, width * 0.8, height * 0.84), fill='#181b23')
        base.alpha_composite(image)
        image = base
    return image, visible_faces

def load_model():
    model = json.loads((ASSET / 'models/item/boss_materials/wine_material.json').read_text(encoding='utf-8'))
    textures = {}
    for key, resource in model['textures'].items():
        namespace, relative = resource.split(':')
        textures[key] = np.array(Image.open(ROOT / f'src/main/resources/assets/{namespace}/textures/{relative}.png').convert('RGBA'))
    return model, textures

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--make-icon', action='store_true')
    arguments = parser.parse_args()
    model, textures = load_model()
    if arguments.make_icon:
        icon, _ = render(model, textures, [1.0, 0.75, -1.6], 32, 32, 1.95, False, True)
        icon.save(ASSET / 'textures/item/wine_material.png')
    views = [('front', [0, 0, -1]), ('back', [0, 0, 1]), ('left', [-1, 0, 0]), ('right', [1, 0, 0])]
    font = ImageFont.truetype(FONT_PATH, 20)
    small = ImageFont.truetype(FONT_PATH, 16)
    sheet = Image.new('RGB', (1200, 1040), '#151a22')
    draw = ImageDraw.Draw(sheet)
    draw.text((30, 22), 'WINE / AGED CRYSTAL + EMBOSSED WAX SEAL', fill='#e5d2ac', font=font)
    draw.text((30, 53), 'Final runtime JSON + PNG | true face UV | element rotations | opaque cutout', fill='#95a4b6', font=small)
    log = {}
    for index, (name, camera) in enumerate(views):
        image, visible = render(model, textures, camera)
        image.save(OUT / f'{name}.png')
        left, top = (index % 2) * 600, 92 + (index // 2) * 474
        sheet.paste(image, (left, top))
        draw.text((left + 24, top + 18), name.upper(), fill='#d9be8c', font=font)
        silhouette, _ = render(model, textures, camera, background=False)
        log[name] = {'camera': camera, 'visible_faces': len(visible), 'silhouette_bbox': silhouette.getbbox()}
    sheet.save(OUT / 'four_views.png')
    detail, _ = render(model, textures, [1.0, 0.72, -1.6], 1120, 760, 72)
    detail_draw = ImageDraw.Draw(detail)
    detail_draw.text((32, 28), 'WINE  /  CELLAR CRYSTAL', fill='#e9d1a2', font=ImageFont.truetype(FONT_PATH, 28))
    detail_draw.text((32, 68), 'Cut wine crystal | copper-rimmed iron hoops | grape wax imprint', fill='#9da8b8', font=small)
    detail.save(OUT / 'detail_oblique.png')
    rear, _ = render(model, textures, [-1.0, 0.72, 1.6], 840, 620, 57)
    rear.save(OUT / 'detail_rear_oblique.png')
    icon = Image.open(ASSET / 'textures/item/wine_material.png').convert('RGBA')
    comparison = Image.new('RGB', (1120, 560), '#20252e')
    compare_draw = ImageDraw.Draw(comparison)
    compare_draw.text((30, 24), 'ONE MATERIAL / MODEL AND INVENTORY ICON', fill='#e5d2ac', font=font)
    model_preview, _ = render(model, textures, [1.0, 0.75, -1.6], 550, 450, 35)
    comparison.paste(model_preview, (0, 85))
    comparison.paste(icon.resize((320, 320), Image.Resampling.NEAREST), (585, 100), icon.resize((320, 320), Image.Resampling.NEAREST))
    comparison.paste(icon, (965, 122), icon)
    icon16 = icon.resize((16, 16), Image.Resampling.NEAREST)
    comparison.paste(icon16, (973, 212), icon16)
    compare_draw.text((605, 445), '32 x 32 / NEAREST 10x', fill='#d9be8c', font=small)
    compare_draw.text((943, 164), '32px', fill='#d9be8c', font=small)
    compare_draw.text((943, 250), '16px', fill='#d9be8c', font=small)
    compare_draw.text((40, 510), 'Actual model + atlas sampled at each pixel; no average-face colors.', fill='#95a4b6', font=small)
    comparison.save(OUT / 'icon_comparison.png')
    atlas = Image.open(ASSET / 'textures/item/boss_materials/wine.png').convert('RGBA')
    atlas.resize((1024, 1024), Image.Resampling.NEAREST).save(OUT / 'atlas_nearest.png')
    (OUT / 'render_log.json').write_text(json.dumps(log, indent=2) + '\n', encoding='utf-8')
    print('Rendered four views, oblique details and actual icon comparison:', OUT)

if __name__ == '__main__':
    main()
