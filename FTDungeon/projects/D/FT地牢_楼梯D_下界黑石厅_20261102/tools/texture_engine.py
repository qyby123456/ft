import copy
import functools
import io
import json
import math

from PIL import Image, ImageEnhance


ASSETS = None
STEPS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}


@functools.lru_cache(maxsize=None)
def asset(path):
    return json.loads(ASSETS.read('assets/minecraft/' + path))


@functools.lru_cache(maxsize=None)
def model(identifier):
    raw = asset('models/' + identifier.removeprefix('minecraft:') + '.json')
    parent = raw.get('parent', '')
    result = copy.deepcopy(model(parent)) if parent and not parent.startswith('builtin/') else {'textures': {}}
    result['textures'].update(raw.get('textures', {}))
    if 'elements' in raw:
        result['elements'] = raw['elements']
    return result


def matches(condition, properties):
    if 'OR' in condition:
        return any(matches(child, properties) for child in condition['OR'])
    if 'AND' in condition:
        return all(matches(child, properties) for child in condition['AND'])
    return all(properties.get(key) in str(value).split('|') for key, value in condition.items())


def box(start, end, texture):
    return {'textures': {}, 'elements': [{'from': start, 'to': end, 'faces': {face: {'texture': texture} for face in STEPS}}]}


def definitions(name, properties):
    if name == 'chest':
        definition = box([1, 0, 1], [15, 14, 15], 'entity/chest/normal')
        regions = {'north': [14, 33, 28, 43], 'south': [42, 33, 56, 43], 'west': [0, 33, 14, 43], 'east': [28, 33, 42, 43], 'up': [14, 0, 28, 14], 'down': [28, 0, 42, 14]}
        definition['elements'][0]['faces'] = {face: {'texture': 'entity/chest/normal', 'uv': [value / 4 for value in bounds]} for face, bounds in regions.items()}
        definition['elements'].extend(box([7, 6, 0], [9, 10, 1], 'block/gold_block')['elements'])
        return [(definition, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    if name == 'skeleton_skull':
        definition = box([4, 0, 4], [12, 8, 12], 'entity/skeleton/skeleton')
        regions = {'north': [2, 4, 4, 8], 'south': [6, 4, 8, 8], 'west': [0, 4, 2, 8], 'east': [4, 4, 6, 8], 'up': [2, 0, 4, 4], 'down': [4, 0, 6, 4]}
        definition['elements'][0]['faces'] = {face: {'texture': 'entity/skeleton/skeleton', 'uv': bounds} for face, bounds in regions.items()}
        return [(definition, {'y': int(properties['rotation']) * 22.5})]
    if name == 'dark_oak_wall_sign':
        return [(box([0, 4, 14], [16, 12, 15], 'block/dark_oak_planks'), {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    blockstate = asset('blockstates/' + name + '.json')
    choices = []
    if 'variants' in blockstate:
        for variant, value in blockstate['variants'].items():
            condition = dict(entry.split('=', 1) for entry in variant.split(',') if entry)
            if matches(condition, properties):
                choices = [value[0] if isinstance(value, list) else value]
                break
        if not choices:
            raise ValueError(f'Unmatched blockstate {name}: {properties}')
    else:
        for part in blockstate.get('multipart', []):
            if 'when' not in part or matches(part['when'], properties):
                value = part['apply']
                choices.append(value[0] if isinstance(value, list) else value)
    return [(model(choice['model']), choice) for choice in choices]


def vertices(element, face):
    left, bottom, front = element['from']
    right, top, back = element['to']
    return {
        'north': [(left, top, front), (right, top, front), (right, bottom, front), (left, bottom, front)],
        'west': [(left, top, back), (left, top, front), (left, bottom, front), (left, bottom, back)],
        'up': [(left, top, back), (right, top, back), (right, top, front), (left, top, front)],
        'south': [(right, top, back), (left, top, back), (left, bottom, back), (right, bottom, back)],
        'east': [(right, top, front), (right, top, back), (right, bottom, back), (right, bottom, front)],
        'down': [(left, bottom, front), (right, bottom, front), (right, bottom, back), (left, bottom, back)],
    }[face]


def rotate(position, axis, angle, origin=(8, 8, 8)):
    centered = [position[index] - origin[index] for index in range(3)]
    first, second = {'x': (1, 2), 'y': (0, 2), 'z': (0, 1)}[axis]
    cosine, sine = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    centered[first], centered[second] = centered[first] * cosine - centered[second] * sine, centered[first] * sine + centered[second] * cosine
    return tuple(centered[index] + origin[index] for index in range(3))


def default_uv(element, face):
    left, bottom, front = element['from']
    right, top, back = element['to']
    return {'north': [16 - right, 16 - top, 16 - left, 16 - bottom], 'south': [left, 16 - top, right, 16 - bottom], 'west': [front, 16 - top, back, 16 - bottom], 'east': [16 - back, 16 - top, 16 - front, 16 - bottom], 'up': [left, front, right, back], 'down': [left, 16 - back, right, 16 - front]}[face]


@functools.lru_cache(maxsize=None)
def geometry(name, properties_tuple):
    result = []
    for definition, choice in definitions(name, dict(properties_tuple)):
        for element in definition.get('elements', []):
            for face, surface in element['faces'].items():
                points = vertices(element, face)
                if 'rotation' in element:
                    options = element['rotation']
                    points = [rotate(point, options['axis'], options['angle'], options['origin']) for point in points]
                points = [rotate(rotate(point, 'x', -choice.get('x', 0)), 'y', choice.get('y', 0)) for point in points]
                first = [points[1][axis] - points[0][axis] for axis in range(3)]
                second = [points[2][axis] - points[1][axis] for axis in range(3)]
                normal = (first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0])
                length = math.sqrt(sum(value * value for value in normal))
                if length < 0.00001:
                    continue
                normal = tuple(value / length for value in normal)
                cull = None
                if 'cullface' in surface:
                    cull = rotate(rotate(STEPS[surface['cullface']], 'x', -choice.get('x', 0), (0, 0, 0)), 'y', choice.get('y', 0), (0, 0, 0))
                    cull = tuple(round(value) for value in cull)
                identifier = surface['texture']
                while identifier.startswith('#'):
                    identifier = definition['textures'][identifier[1:]]
                result.append(([tuple(value / 16 for value in point) for point in points], normal, cull, identifier.removeprefix('minecraft:'), tuple(surface.get('uv', default_uv(element, face))), surface.get('rotation', 0)))
    return result


@functools.lru_cache(maxsize=None)
def texture(identifier, bounds, rotation, brightness):
    image = Image.open(io.BytesIO(ASSETS.read('assets/minecraft/textures/' + identifier + '.png'))).convert('RGBA')
    if identifier.startswith('block/') and image.height > image.width:
        image = image.crop((0, 0, image.width, image.width))
    left, top, right, bottom = (bounds[0] * image.width / 16, bounds[1] * image.height / 16, bounds[2] * image.width / 16, bounds[3] * image.height / 16)
    image = image.crop((min(left, right), min(top, bottom), max(left, right), max(top, bottom)))
    if right < left:
        image = image.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if bottom < top:
        image = image.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    image = image.resize((48, 48), Image.Resampling.NEAREST).rotate(-rotation)
    return ImageEnhance.Brightness(image).enhance(brightness)


def warp(canvas, image, points):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    width = math.ceil(max(point[0] for point in points)) - minimum_x + 1
    height = math.ceil(max(point[1] for point in points)) - minimum_y + 1
    anchor_x, anchor_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    first_x, first_y = ((points[1][axis] - points[0][axis]) / image.width for axis in range(2))
    second_x, second_y = ((points[3][axis] - points[0][axis]) / image.height for axis in range(2))
    determinant = first_x * second_y - second_x * first_y
    if abs(determinant) < 0.00001 or width <= 0 or height <= 0:
        return
    coefficients = (second_y / determinant, -second_x / determinant, (-second_y * anchor_x + second_x * anchor_y) / determinant, -first_y / determinant, first_x / determinant, (first_y * anchor_x - first_x * anchor_y) / determinant)
    canvas.alpha_composite(image.transform((width, height), Image.Transform.AFFINE, coefficients, Image.Resampling.NEAREST), (minimum_x, minimum_y))

