from pathlib import Path
import base64
import hashlib
import json
import math
import numpy as np
from PIL import Image
from render import ROOT, ASSET, OUT, rotation_matrix, face_geometry

WORK = ROOT / '_work/boss-relics-refine-20261021/wine'
MODEL = ASSET / 'models/item/boss_relics/aged_wine_seal.json'
ATLAS = ASSET / 'textures/item/boss_relics/aged_wine_seal.png'
BBMODEL = ROOT / 'art/boss_relics/blockbench/aged_wine_seal.bbmodel'
model = json.loads(MODEL.read_text(encoding='utf-8'))
bbmodel = json.loads(BBMODEL.read_text(encoding='utf-8'))
manifest = json.loads((WORK / 'manifest.json').read_text(encoding='utf-8'))
atlas = np.array(Image.open(ATLAS).convert('RGBA'))
errors = []


def check(condition, message):
    if not condition:
        errors.append(message)


check('parent' not in model, 'Geometry must not inherit generated/handheld')
check(len(model['elements']) == len(bbmodel['elements']), 'Blockbench element count differs')
check(base64.b64decode(bbmodel['textures'][0]['source'].split(',')[1]) == ATLAS.read_bytes(), 'Embedded Blockbench atlas differs')
check(set(model['display']) == {'thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand', 'ground', 'head', 'fixed', 'gui'}, 'Display slots incomplete')
check(Image.open(ASSET / 'textures/item/aged_wine_seal.png').size == (32, 32), 'GUI icon must be 32px')
wrapper = json.loads((ASSET / 'models/item/aged_wine_seal.json').read_text(encoding='utf-8'))
check(wrapper.get('loader') == 'forge:separate_transforms', 'Forge transform split lost')
check(wrapper['base']['parent'] == 'ftboss:item/boss_relics/aged_wine_seal', 'Incorrect 3D target')
check(wrapper['perspectives']['gui']['parent'] == 'ftboss:item/boss_relics/aged_wine_seal_gui', 'Incorrect GUI target')
gui = json.loads((ASSET / 'models/item/boss_relics/aged_wine_seal_gui.json').read_text(encoding='utf-8'))
check(gui['textures']['layer0'] == 'ftboss:item/aged_wine_seal', 'Incorrect GUI texture')
face_count = 0
maximum_density_error = 0.0
minimum_coverage = 1.0
boxes = []
for index, element in enumerate(model['elements']):
    size = np.array(element['to']) - element['from']
    check(np.all(size > 0), f'Empty cube {element["name"]}')
    rotation = element.get('rotation')
    matrix = np.eye(3)
    if rotation:
        check(rotation['axis'] in 'xyz', f'Invalid axis {element["name"]}')
        check(rotation['angle'] in [-45, -22.5, 0, 22.5, 45], f'Invalid angle {element["name"]}')
        matrix = rotation_matrix(rotation['axis'], rotation['angle'])
    center = (np.array(element['from']) + element['to']) / 2
    if rotation:
        center = (center - rotation['origin']) @ matrix.T + rotation['origin']
    boxes.append((center, matrix, size / 2))
    bb_element = bbmodel['elements'][index]
    check(bb_element['from'] == element['from'] and bb_element['to'] == element['to'], f'Blockbench coordinates differ {index}')
    check(np.allclose(bb_element['rotation'], np.array([rotation['angle'] if rotation and rotation['axis'] == axis else 0 for axis in 'xyz'])), f'Blockbench rotation differs {index}')
    check(len(element['faces']) == 6, f'Missing face {element["name"]}')
    for face_name, face in element['faces'].items():
        face_count += 1
        uv = np.array(face['uv'])
        check(np.all((uv >= 0) & (uv <= 16)), f'UV bounds {index}/{face_name}')
        if face_name in ['north', 'south']:
            dimensions = size[[0, 1]]
        elif face_name in ['east', 'west']:
            dimensions = size[[2, 1]]
        else:
            dimensions = size[[0, 2]]
        pixel_size = np.abs(uv[2:] - uv[:2]) * atlas.shape[0] / 16
        density = pixel_size / dimensions
        density_error = float(np.max(np.abs(density - 2)))
        maximum_density_error = max(maximum_density_error, density_error)
        check(density_error < 0.001, f'UV density distortion {index}/{face_name}: {density}')
        check(np.allclose(np.array(bb_element['faces'][face_name]['uv']), uv * atlas.shape[0] / 16, atol=0.00001), f'Blockbench UV differs {index}/{face_name}')
        x_coordinates = np.linspace(min(uv[0], uv[2]), max(uv[0], uv[2]), max(2, math.ceil(pixel_size[0] * 2)), endpoint=False)
        y_coordinates = np.linspace(min(uv[1], uv[3]), max(uv[1], uv[3]), max(2, math.ceil(pixel_size[1] * 2)), endpoint=False)
        sample_x = np.clip((x_coordinates * atlas.shape[1] / 16).astype(int), 0, atlas.shape[1] - 1)
        sample_y = np.clip((y_coordinates * atlas.shape[0] / 16).astype(int), 0, atlas.shape[0] - 1)
        coverage = float(np.mean(atlas[sample_y[:, None], sample_x[None, :], 3] == 255))
        minimum_coverage = min(minimum_coverage, coverage)
        check(coverage == 1.0, f'Transparent face {index}/{face_name}')
        points, _ = face_geometry(element, face_name)
        check(np.all(points >= -16) and np.all(points <= 32), f'Runtime bounds {index}/{face_name}')


def separating_distance(first, second):
    center_a, matrix_a, half_a = first
    center_b, matrix_b, half_b = second
    axes = [*matrix_a.T, *matrix_b.T]
    axes.extend(np.cross(axis_a, axis_b) for axis_a in matrix_a.T for axis_b in matrix_b.T)
    maximum_gap = -float('inf')
    for axis in axes:
        if np.linalg.norm(axis) < 1e-7:
            continue
        axis = axis / np.linalg.norm(axis)
        radius_a = np.sum(half_a * np.abs(matrix_a.T @ axis))
        radius_b = np.sum(half_b * np.abs(matrix_b.T @ axis))
        gap = abs(np.dot(center_b - center_a, axis)) - radius_a - radius_b
        maximum_gap = max(maximum_gap, gap)
    return maximum_gap


adjacent = [set() for _ in boxes]
closest = []
for first_index in range(len(boxes)):
    nearest = float('inf')
    for second_index in range(first_index + 1, len(boxes)):
        distance = separating_distance(boxes[first_index], boxes[second_index])
        nearest = min(nearest, distance)
        if distance < -0.0001:
            adjacent[first_index].add(second_index)
            adjacent[second_index].add(first_index)
    closest.append(nearest)
components = []
unseen = set(range(len(boxes)))
while unseen:
    pending = [unseen.pop()]
    component = []
    while pending:
        current = pending.pop()
        component.append(current)
        for neighbor in adjacent[current] & unseen:
            unseen.remove(neighbor)
            pending.append(neighbor)
    components.append(component)
check(len(components) == 1, 'Detached geometry: ' + str([[model['elements'][index]['name'] for index in component] for component in components[1:]]))

for resource in model['textures'].values():
    namespace, relative = resource.split(':')
    check((ROOT / f'src/main/resources/assets/{namespace}/textures/{relative}.png').exists(), f'Missing texture {resource}')
for key in ['front', 'back', 'left', 'right', 'detail_oblique', 'detail_rear_oblique', 'four_views', 'icon_comparison', 'material_relic_comparison']:
    check((OUT / f'{key}.png').exists(), f'Missing rendered view {key}')
render_log = json.loads((OUT / 'render_log.json').read_text(encoding='utf-8'))
for view_name, entry in render_log.items():
    left, top, right, bottom = entry['silhouette_bbox']
    check(left > 4 and top > 4 and right < 596 and bottom < 596, f'Render clipping: {view_name}')
for index, island in enumerate(manifest['uvIslands']):
    left, top = island['left'], island['top']
    check(left >= 1 and top >= 1, 'Missing atlas padding')
    check(left + island['width'] < atlas.shape[1] - 1 and top + island['height'] < atlas.shape[0] - 1, 'Atlas boundary padding')
    for other in manifest['uvIslands'][index + 1:]:
        overlap_x = min(left + island['width'], other['left'] + other['width']) - max(left, other['left'])
        overlap_y = min(top + island['height'], other['top'] + other['height']) - max(top, other['top'])
        check(overlap_x <= 0 or overlap_y <= 0, 'UV islands overlap')

resources = [MODEL, ATLAS, BBMODEL, ASSET / 'textures/item/aged_wine_seal.png', ASSET / 'models/item/aged_wine_seal.json', ASSET / 'models/item/boss_relics/aged_wine_seal_gui.json']
hashes = {str(resource.relative_to(ROOT)).replace('\\', '/'): hashlib.sha256(resource.read_bytes()).hexdigest() for resource in resources}
report = {'pass': not errors, 'elements': len(model['elements']), 'rotated': sum('rotation' in element for element in model['elements']), 'faces': face_count, 'atlas': list(atlas.shape[:2]), 'minimum_face_opaque_coverage': minimum_coverage, 'maximum_density_error': maximum_density_error, 'connected_components': len(components), 'component_sizes': [len(component) for component in components], 'blockbench_embedded_atlas_identical': base64.b64decode(bbmodel['textures'][0]['source'].split(',')[1]) == ATLAS.read_bytes(), 'errors': errors, 'sha256': hashes}
(WORK / 'validation.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
print(json.dumps(report, indent=2))
raise SystemExit(0 if not errors else 1)
