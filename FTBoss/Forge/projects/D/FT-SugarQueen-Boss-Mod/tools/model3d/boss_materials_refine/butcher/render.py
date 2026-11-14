import argparse
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter


ROOT = Path(__file__).resolve().parents[4]
RES = ROOT / 'src/main/resources/assets/ftboss'
ART = ROOT / 'art/boss_materials/refine/butcher'
WORK = ROOT / '_work/boss-materials-refine-20261021/butcher'
MODEL = RES / 'models/item/boss_materials/butcher_material.json'
ATLAS = RES / 'textures/item/boss_materials/butcher.png'
ICON = RES / 'textures/item/butcher_material.png'
SIDES = ('north', 'south', 'west', 'east', 'up', 'down')
FONT_PATH = Path('C:/Windows/Fonts/segoeui.ttf')


def font(size):
    return ImageFont.truetype(str(FONT_PATH), size) if FONT_PATH.exists() else ImageFont.load_default(size=size)


def vertices(element):
    min_x, min_y, min_z = element['from']
    max_x, max_y, max_z = element['to']
    faces = {
        'north': [[max_x, max_y, min_z], [max_x, min_y, min_z], [min_x, min_y, min_z], [min_x, max_y, min_z]],
        'south': [[min_x, max_y, max_z], [min_x, min_y, max_z], [max_x, min_y, max_z], [max_x, max_y, max_z]],
        'west': [[min_x, max_y, min_z], [min_x, min_y, min_z], [min_x, min_y, max_z], [min_x, max_y, max_z]],
        'east': [[max_x, max_y, max_z], [max_x, min_y, max_z], [max_x, min_y, min_z], [max_x, max_y, min_z]],
        'up': [[min_x, max_y, min_z], [min_x, max_y, max_z], [max_x, max_y, max_z], [max_x, max_y, min_z]],
        'down': [[min_x, min_y, max_z], [min_x, min_y, min_z], [max_x, min_y, min_z], [max_x, min_y, max_z]],
    }
    rotation = element.get('rotation')
    if rotation:
        angle = math.radians(rotation['angle'])
        cosine, sine = math.cos(angle), math.sin(angle)
        axis = rotation['axis']
        matrix = {'x': [[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]],
                  'y': [[cosine, 0, sine], [0, 1, 0], [-sine, 0, cosine]],
                  'z': [[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]]}[axis]
        origin = np.array(rotation['origin'], dtype=float)
        for face in faces:
            local = (np.array(faces[face]) - origin) @ np.array(matrix).T
            if rotation.get('rescale'):
                factor = 1 / math.cos(angle)
                for index in range(3):
                    if index != 'xyz'.index(axis):
                        local[:, index] *= factor
            faces[face] = local + origin
    return {face: np.array(points) for face, points in faces.items()}


def all_faces(model):
    result = []
    for element in model['elements']:
        points = vertices(element)
        for face, spec in element['faces'].items():
            u0, v0, u1, v1 = spec['uv']
            texcoords = np.array([[u0, v0], [u0, v1], [u1, v1], [u1, v0]])
            steps = spec.get('rotation', 0) // 90
            texcoords = np.roll(texcoords, -steps, axis=0)
            quad = points[face]
            normal = np.cross(quad[1] - quad[0], quad[2] - quad[0])
            normal /= np.linalg.norm(normal)
            result.append((quad, texcoords, normal, element.get('shade', True)))
    return result


def raster(model, texture, direction, size=512, extent=15.2, center=None):
    faces = all_faces(model)
    all_points = np.concatenate([face[0] for face in faces])
    if center is None:
        center = (all_points.min(axis=0) + all_points.max(axis=0)) / 2
    direction = np.array(direction, dtype=float)
    direction /= np.linalg.norm(direction)
    right = np.cross(np.array([0., 1., 0.]), direction)
    right /= np.linalg.norm(right)
    up = np.cross(direction, right)
    scale = size / extent
    pixels = np.zeros((size, size, 4), dtype=np.uint8)
    depth = np.full((size, size), -np.inf)
    light = np.array([-0.45, 0.82, -0.36])
    light /= np.linalg.norm(light)
    tex_height, tex_width = texture.shape[:2]
    for quad, texcoords, normal, shade in faces:
        if np.dot(normal, direction) <= 1e-8:
            continue
        relative = quad - center
        screen = np.column_stack((relative @ right * scale + size / 2, size / 2 - relative @ up * scale))
        zvalues = relative @ direction
        brightness = 0.71 + 0.29 * max(0, np.dot(normal, light)) if shade else 1.0
        for indices in ([0, 1, 2], [0, 2, 3]):
            triangle = screen[indices]
            min_x = max(0, int(math.floor(triangle[:, 0].min())))
            max_x = min(size - 1, int(math.ceil(triangle[:, 0].max())))
            min_y = max(0, int(math.floor(triangle[:, 1].min())))
            max_y = min(size - 1, int(math.ceil(triangle[:, 1].max())))
            if max_x < min_x or max_y < min_y:
                continue
            grid_y, grid_x = np.mgrid[min_y:max_y + 1, min_x:max_x + 1]
            grid_x = grid_x + 0.5
            grid_y = grid_y + 0.5
            first, second, third = triangle
            denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
            if abs(denominator) < 1e-10:
                continue
            weight0 = ((second[1] - third[1]) * (grid_x - third[0]) + (third[0] - second[0]) * (grid_y - third[1])) / denominator
            weight1 = ((third[1] - first[1]) * (grid_x - third[0]) + (first[0] - third[0]) * (grid_y - third[1])) / denominator
            weight2 = 1 - weight0 - weight1
            mask = (weight0 >= -1e-8) & (weight1 >= -1e-8) & (weight2 >= -1e-8)
            weights = np.stack((weight0, weight1, weight2), axis=-1)
            current_depth = weights @ zvalues[indices]
            uv = weights @ texcoords[indices]
            tex_x = np.clip(np.floor(uv[:, :, 0] / 16 * tex_width).astype(int), 0, tex_width - 1)
            tex_y = np.clip(np.floor(uv[:, :, 1] / 16 * tex_height).astype(int), 0, tex_height - 1)
            colors = texture[tex_y, tex_x].copy()
            mask &= colors[:, :, 3] > 0
            target_depth = depth[min_y:max_y + 1, min_x:max_x + 1]
            mask &= current_depth > target_depth
            colors[:, :, :3] = np.minimum(255, colors[:, :, :3] * brightness).astype(np.uint8)
            pixels[min_y:max_y + 1, min_x:max_x + 1][mask] = colors[mask]
            target_depth[mask] = current_depth[mask]
    return Image.fromarray(pixels)


def background(size):
    image = Image.new('RGB', size, '#20272c')
    draw = ImageDraw.Draw(image)
    draw.line((0, size[1] - 45, size[0], size[1] - 45), fill='#364147')
    return image


def panel(rendered, title, subtitle, size=(620, 660)):
    image = background(size).convert('RGBA')
    draw = ImageDraw.Draw(image)
    draw.text((26, 20), title, font=font(22), fill='#eadfc7')
    draw.text((26, 51), subtitle, font=font(13), fill='#9eaaad')
    image.alpha_composite(rendered, ((size[0] - rendered.width) // 2, 85))
    return image


def generate_icon(model, texture):
    small = raster(model, texture, [0.35, 0.27, -1.0], size=32, extent=15.1)
    alpha = small.getchannel('A')
    outline = alpha.filter(ImageFilter.MaxFilter(3))
    icon = Image.new('RGBA', small.size, '#423032')
    icon.putalpha(outline)
    icon.alpha_composite(small)
    icon.save(ICON)
    return icon


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--keep-icon', action='store_true')
    args = parser.parse_args()
    model = json.loads(MODEL.read_text(encoding='utf-8'))
    texture = np.array(Image.open(ATLAS).convert('RGBA'))
    cameras = [('front', [0, 0, -1], 'FRONT / NORTH', 'Marrow cut, enamel arris, fascia bridge'),
               ('back', [0, 0, 1], 'BACK / SOUTH', 'Cortex plate, rear tendon and root junctions'),
               ('left', [-1, 0, 0], 'LEFT / WEST', 'Hollow cut depth and thick enamel'),
               ('right', [1, 0, 0], 'RIGHT / EAST', 'Bevels, roots and wrapped connective tissue')]
    sheet = Image.new('RGBA', (1240, 1320), '#20272c')
    for index, (key, camera, title, subtitle) in enumerate(cameras):
        rendered = raster(model, texture, camera, size=512)
        rendered.save(ART / f'{key}.png')
        item = panel(rendered, title, subtitle)
        item.save(ART / f'{key}_panel.png')
        sheet.alpha_composite(item, ((index % 2) * 620, (index // 2) * 660))
    sheet.save(ART / 'four_views.png')
    oblique = raster(model, texture, [-0.68, 0.42, -1.0], size=900, extent=15.5)
    oblique.save(ART / 'oblique.png')
    oblique_panel = panel(oblique, 'BUTCHER / BLOODIED FANG BONE', 'Final runtime geometry and per-face texture | 2 px per model unit', (1000, 1050))
    oblique_panel.save(ART / 'oblique_detail.png')
    rear = raster(model, texture, [0.6, 0.27, 1.0], size=700, extent=15.1)
    rear.save(ART / 'rear_oblique.png')
    icon = Image.open(ICON).convert('RGBA') if args.keep_icon else generate_icon(model, texture)
    old = Image.open(WORK / 'backup-before-refine/src/main/resources/assets/ftboss/textures/item/butcher_material.png').convert('RGBA')
    comparison = background((1000, 560)).convert('RGBA')
    draw = ImageDraw.Draw(comparison)
    draw.text((30, 22), 'BLOODIED FANG BONE / INVENTORY ICON', font=font(25), fill='#eadfc7')
    draw.text((30, 60), 'Same material as the runtime model. Transparent pixel-art sprite, separate GUI route.', font=font(16), fill='#a5b2b7')
    draw.text((60, 110), 'Previous 16 x 16', font=font(20), fill='#aeb7b9')
    draw.text((390, 110), f'Refined {icon.width} x {icon.height}', font=font(20), fill='#eadfc7')
    comparison.alpha_composite(old.resize((256, 256), Image.Resampling.NEAREST), (55, 155))
    comparison.alpha_composite(icon.resize((320, 320), Image.Resampling.NEAREST), (385, 145))
    comparison.alpha_composite(icon, (820, 195))
    draw.text((775, 245), 'Native 32 px', font=font(18), fill='#a5b2b7')
    comparison.alpha_composite(icon.resize((16, 16), Image.Resampling.NEAREST), (828, 327))
    draw.text((775, 370), '16 px slot', font=font(18), fill='#a5b2b7')
    draw.text((55, 495), 'Old blocky mark', font=font(16), fill='#829094')
    draw.text((385, 495), 'Cut cortex / tapered fangs / attached fascia', font=font(16), fill='#cbbba0')
    comparison.save(ART / 'icon_comparison.png')
    Image.open(ATLAS).resize((768, 768), Image.Resampling.NEAREST).save(ART / 'uv_atlas_enlarged.png')
    print(f'Rendered final runtime model: {len(model["elements"])} parts / four views + obliques + matching icon')


if __name__ == '__main__':
    main()
