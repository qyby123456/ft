import argparse
import functools
import io
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = None
DIRECTIONS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0),
              'east': (1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}


def resource(kind, name, extension='json'):
    namespace, location = name.split(':', 1) if ':' in name else ('minecraft', name)
    return ARCHIVE.read(f'assets/{namespace}/{kind}/{location}.{extension}')


@functools.lru_cache(maxsize=None)
def model(name):
    value = json.loads(resource('models', name))
    parent = model(value['parent']) if value.get('parent', '').split(':')[-1] not in ('', 'builtin/entity') else {}
    return {**parent, **value, 'textures': {**parent.get('textures', {}), **value.get('textures', {})}}


@functools.lru_cache(maxsize=None)
def texture(name):
    image = Image.open(io.BytesIO(resource('textures', name, 'png'))).convert('RGBA')
    return np.array(image.crop((0, 0, image.width, min(image.width, image.height))))


def rotation(points, axis, degrees, origin=(0.5, 0.5, 0.5), rescale=False):
    points = np.array(points, dtype=float) - origin
    radians = math.radians(degrees)
    cosine, sine = math.cos(radians), math.sin(radians)
    if axis == 'y':
        matrix = np.array([[cosine, 0, -sine], [0, 1, 0], [sine, 0, cosine]])
    elif axis == 'x':
        matrix = np.array([[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]])
    else:
        matrix = np.array([[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]])
    points = points @ matrix.T
    if rescale:
        for index, name in enumerate(('x', 'y', 'z')):
            if name != axis:
                points[:, index] /= abs(cosine)
    return points + origin


def corners(minimum, maximum, direction):
    left, bottom, back = minimum
    right, top, front = maximum
    return {'north': [(right, top, back), (left, top, back), (left, bottom, back), (right, bottom, back)],
            'south': [(left, top, front), (right, top, front), (right, bottom, front), (left, bottom, front)],
            'west': [(left, top, back), (left, top, front), (left, bottom, front), (left, bottom, back)],
            'east': [(right, top, front), (right, top, back), (right, bottom, back), (right, bottom, front)],
            'up': [(left, top, back), (right, top, back), (right, top, front), (left, top, front)],
            'down': [(left, bottom, front), (right, bottom, front), (right, bottom, back), (left, bottom, back)]}[direction]


def default_uv(minimum, maximum, direction):
    left, bottom, back = minimum
    right, top, front = maximum
    return {'north': [16 - right, 16 - top, 16 - left, 16 - bottom],
            'south': [left, 16 - top, right, 16 - bottom],
            'west': [back, 16 - top, front, 16 - bottom],
            'east': [16 - front, 16 - top, 16 - back, 16 - bottom],
            'up': [left, back, right, front], 'down': [left, 16 - front, right, 16 - back]}[direction]


def matches(condition, properties):
    if 'OR' in condition:
        return any(matches(child, properties) for child in condition['OR'])
    if 'AND' in condition:
        return all(matches(child, properties) for child in condition['AND'])
    return all(properties.get(key) in str(value).split('|') for key, value in condition.items())


def selected_models(state):
    name = state['Name'].split(':')[1]
    properties = state.get('Properties', {})
    definitions = json.loads(resource('blockstates', name))
    selected = []
    for key, variant in definitions.get('variants', {}).items():
        if matches(dict(piece.split('=') for piece in key.split(',') if piece), properties):
            selected.append(variant[0] if isinstance(variant, list) else variant)
            break
    for part in definitions.get('multipart', []):
        if matches(part.get('when', {}), properties):
            selected.append(part['apply'][0] if isinstance(part['apply'], list) else part['apply'])
    return selected


def cube_surfaces(minimum, maximum, texture_name):
    for direction in DIRECTIONS:
        yield np.array(corners(minimum, maximum, direction)), texture(texture_name), np.array([(0, 0), (1, 0), (1, 1), (0, 1)]), None


def chest_surfaces(state):
    kind = state['Properties']['type']
    atlas = texture('entity/chest/normal_' + kind) if kind != 'single' else texture('entity/chest/normal')
    atlas_height, atlas_width = atlas.shape[:2]
    for minimum, maximum, offset in (((0, 0, 1), (16, 10, 15), (0, 19)), ((0, 9, 1), (16, 14, 15), (0, 0))):
        width, height, depth = (maximum[index] - minimum[index] for index in range(3))
        texture_x, texture_y = offset
        regions = {'north': (texture_x + depth, texture_y + depth, width, height),
                   'south': (texture_x + depth + width + depth, texture_y + depth, width, height),
                   'west': (texture_x, texture_y + depth, depth, height),
                   'east': (texture_x + depth + width, texture_y + depth, depth, height),
                   'up': (texture_x + depth, texture_y, width, depth),
                   'down': (texture_x + depth + width, texture_y, width, depth)}
        for direction, region in regions.items():
            texture_x, texture_y, region_width, region_height = region
            coordinates = np.array([(texture_x, texture_y), (texture_x + region_width, texture_y),
                                    (texture_x + region_width, texture_y + region_height), (texture_x, texture_y + region_height)]) / [atlas_width, atlas_height]
            yield np.array(corners(np.array(minimum) / 16, np.array(maximum) / 16, direction)), atlas, coordinates, None
    handle_minimum = (0.88, 0.44, 0) if kind == 'left' else (0, 0.44, 0)
    handle_maximum = (1, 0.72, 0.10) if kind == 'left' else (0.12, 0.72, 0.10)
    yield from cube_surfaces(handle_minimum, handle_maximum, 'block/gold_block')


@functools.lru_cache(maxsize=None)
def geometry(serialized):
    state = json.loads(serialized)
    if state['Name'] == 'minecraft:chest':
        return list(chest_surfaces(state))
    result = []
    for variant in selected_models(state):
        definition = model(variant['model'])
        for element in definition.get('elements', []):
            minimum, maximum = np.array(element['from']) / 16, np.array(element['to']) / 16
            for direction, face in element['faces'].items():
                points = np.array(corners(minimum, maximum, direction))
                if 'rotation' in element:
                    options = element['rotation']
                    points = rotation(points, options['axis'], options['angle'], np.array(options['origin']) / 16, options.get('rescale', False))
                if variant.get('x'):
                    points = rotation(points, 'x', variant['x'])
                if variant.get('y'):
                    points = rotation(points, 'y', variant['y'])
                reference = face['texture']
                while reference.startswith('#'):
                    reference = definition['textures'][reference[1:]]
                uv = face.get('uv', default_uv(element['from'], element['to'], direction))
                coordinates = np.array([(uv[0], uv[1]), (uv[2], uv[1]), (uv[2], uv[3]), (uv[0], uv[3])]) / 16
                coordinates = np.roll(coordinates, face.get('rotation', 0) // 90, axis=0)
                cull = np.array(DIRECTIONS[face['cullface']], dtype=float) if 'cullface' in face else None
                if cull is not None:
                    cull = rotation([cull], 'x', variant.get('x', 0), (0, 0, 0))[0]
                    cull = rotation([cull], 'y', variant.get('y', 0), (0, 0, 0))[0].round().astype(int)
                result.append((points, texture(reference), coordinates, cull))
    if state['Name'] == 'minecraft:bell':
        for minimum, maximum in (((.3, .3, .3), (.7, .75, .7)), ((.22, .22, .22), (.78, .36, .78)), ((.42, .7, .42), (.58, .86, .58))):
            result.extend(cube_surfaces(minimum, maximum, 'block/gold_block'))
    return result


def solid(state):
    name = state['Name']
    return not any(part in name for part in ('air', 'slab', 'stairs', 'trapdoor', 'door', 'button', 'plate', 'bars', 'chain', 'bell', 'chest', 'campfire', 'rod'))


def render(blueprint, mode, width, height):
    palette = blueprint['palette']
    roof = {tuple(position) for position in blueprint['roof']}
    blocks = {}
    for entry in blueprint['blocks']:
        position = tuple(entry['pos'])
        state = palette[entry['state']]
        if state['Name'] in ('minecraft:air', 'minecraft:redstone_block') or position in roof:
            continue
        block_x, block_y, block_z = position
        if mode == 'plan' and block_y > (4 if not (8 <= block_x <= 24 and 12 <= block_z <= 28) else 8):
            continue
        if mode != 'plan' and block_z <= 7 and block_y >= 5 and not (12 <= block_x <= 20 and block_z <= 5):
            continue
        blocks[position] = state
    if mode == 'plan':
        camera = np.array([16.5, 100, 18.5])
        forward, right, upward = np.array([0, -1, 0]), np.array([1, 0, 0]), np.array([0, 0, -1])
    else:
        camera = np.array([16.5, 44, -43])
        forward = np.array([16.5, 5, 20]) - camera
        forward /= np.linalg.norm(forward)
        right = np.cross(forward, [0, 1, 0])
        right /= np.linalg.norm(right)
        upward = np.cross(right, forward)
    meshes = []
    for position, state in blocks.items():
        for points, pixels, coordinates, cull in geometry(json.dumps(state, sort_keys=True)):
            if cull is not None:
                neighbor = tuple(position[axis] + int(cull[axis]) for axis in range(3))
                if neighbor in blocks and solid(blocks[neighbor]):
                    continue
            world = points + position
            normal = np.cross(world[1] - world[0], world[3] - world[0])
            normal /= max(0.00001, np.linalg.norm(normal))
            if np.dot(normal, camera - world.mean(axis=0)) < 0:
                normal = -normal
            relative = world - camera
            distances = relative @ forward
            horizontal = relative @ right
            vertical = -(relative @ upward)
            projected = np.stack((horizontal, vertical), axis=1)
            if mode != 'plan':
                projected /= distances[:, None]
            illumination = .78 + .34 * max(0, np.dot(normal, np.array([-.3, .88, -.36])))
            emission = state['Name'] in ('minecraft:redstone_lamp', 'minecraft:campfire')
            center = world.mean(axis=0)
            warmth = max(math.exp(-np.linalg.norm(center - [fire_x, 7, fire_z]) / 5) for fire_x, fire_z in ((11, 15), (21, 15), (11, 25), (21, 25)))
            tint = np.array([1.12 + warmth * .22, 1.06 + warmth * .05, .98 - warmth * .08]) * illumination
            meshes.append((projected, 1 / distances, pixels, coordinates, tint, emission))
    projected_all = np.concatenate([mesh[0] for mesh in meshes])
    minimum, maximum = projected_all.min(axis=0), projected_all.max(axis=0)
    margin_top, margin_bottom = 140, 65
    scale = min((width - 100) / (maximum[0] - minimum[0]), (height - margin_top - margin_bottom) / (maximum[1] - minimum[1]))
    offset = np.array([(width - scale * (minimum[0] + maximum[0])) / 2,
                       margin_top + (height - margin_top - margin_bottom - scale * (maximum[1] - minimum[1])) / 2 - minimum[1] * scale])
    frame = np.zeros((height, width, 3), dtype=np.uint8)
    frame[:] = [12, 17, 21]
    depth_buffer = np.zeros((height, width), dtype=np.float32)
    bloom = np.zeros((height, width, 3), dtype=np.uint8)
    for projected, inverse_depth, pixels, coordinates, tint, emission in meshes:
        screen = projected * scale + offset
        for indexes in ((0, 1, 2), (0, 2, 3)):
            vertices = screen[list(indexes)]
            minimum_x = max(0, int(math.floor(vertices[:, 0].min())))
            maximum_x = min(width - 1, int(math.ceil(vertices[:, 0].max())))
            minimum_y = max(0, int(math.floor(vertices[:, 1].min())))
            maximum_y = min(height - 1, int(math.ceil(vertices[:, 1].max())))
            if maximum_x < minimum_x or maximum_y < minimum_y:
                continue
            first, second, third = vertices
            determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
            if abs(determinant) < .001:
                continue
            pixel_y, pixel_x = np.mgrid[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
            weight_first = ((second[1] - third[1]) * (pixel_x + .5 - third[0]) + (third[0] - second[0]) * (pixel_y + .5 - third[1])) / determinant
            weight_second = ((third[1] - first[1]) * (pixel_x + .5 - third[0]) + (first[0] - third[0]) * (pixel_y + .5 - third[1])) / determinant
            weights = np.stack((weight_first, weight_second, 1 - weight_first - weight_second), axis=-1)
            inverse = inverse_depth[list(indexes)]
            depths = weights @ inverse
            destination_depth = depth_buffer[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
            mask = np.all(weights >= -.0001, axis=-1) & (depths > destination_depth)
            if not mask.any():
                continue
            interpolated = weights @ (coordinates[list(indexes)] * inverse[:, None]) / depths[..., None]
            texture_x = np.clip((interpolated[..., 0] * pixels.shape[1]).astype(int), 0, pixels.shape[1] - 1)
            texture_y = np.clip((interpolated[..., 1] * pixels.shape[0]).astype(int), 0, pixels.shape[0] - 1)
            sampled = pixels[texture_y, texture_x]
            mask &= sampled[..., 3] >= 100
            colored = np.clip(sampled[..., :3] * tint, 0, 255).astype(np.uint8)
            destination = frame[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
            destination[mask] = colored[mask]
            destination_depth[mask] = depths[mask]
            glow = bloom[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
            glow[mask] = np.where((emission & (colored[..., 0] > 175))[..., None], colored // 2, 0)[mask]
    image = Image.fromarray(frame)
    image = ImageChops.screen(image, Image.fromarray(bloom).filter(ImageFilter.GaussianBlur(12)))
    drawing = ImageDraw.Draw(image)
    font_path = 'C:/Windows/Fonts/msyh.ttc'
    drawing.text((52, 32), '锈铜宝库', font=ImageFont.truetype(font_path, 40), fill='#ecd1a1')
    drawing.text((55, 88), 'COPPER TREASURY  /  33 × 17 × 37  /  JAVA 1.20.1', font=ImageFont.truetype(font_path, 16), fill='#83a89d')
    drawing.text((52, height - 35), '方块数据渲染 · 屋顶与近侧墙剖切 · 非实机截图' if mode != 'plan' else '俯视布局 · 上方为北 / 入口 · 屋顶已隐藏', font=ImageFont.truetype(font_path, 17), fill='#8e9b9f')
    return image


def main():
    global ARCHIVE
    parser = argparse.ArgumentParser()
    parser.add_argument('--textures-jar', type=Path, required=True)
    arguments = parser.parse_args()
    blueprint = json.loads((ROOT / 'qa/blueprint.json').read_text(encoding='utf-8'))
    (ROOT / 'preview').mkdir(exist_ok=True)
    with zipfile.ZipFile(arguments.textures_jar) as ARCHIVE:
        render(blueprint, 'hero', 1900, 1450).save(ROOT / 'preview/copper-vault-cutaway.png')
        render(blueprint, 'plan', 1350, 1570).save(ROOT / 'preview/copper-vault-plan.png')
    print(json.dumps({'preview': str(ROOT / 'preview'), 'status': 'PASS'}, ensure_ascii=False))


if __name__ == '__main__':
    main()
