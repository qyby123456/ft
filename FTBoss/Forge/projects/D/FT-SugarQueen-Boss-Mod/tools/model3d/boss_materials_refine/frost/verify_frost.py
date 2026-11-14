import base64
import hashlib
import json
from pathlib import Path

import numpy as np
from PIL import Image

from render_frost import ASSETS, ROOT, vertices, rotation

WORK = ROOT / '_work/boss-materials-refine-20261021/frost'


def load_json(path):
    return json.loads(path.read_text(encoding='utf-8'))


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def box_data(element):
    points = np.unique(np.concatenate(list(vertices(element).values())), axis=0)
    transform = element.get('rotation')
    axes = rotation(transform['axis'], transform['angle']) if transform else np.eye(3)
    return points, axes.T


def connected(first, second):
    points_a, axes_a = first
    points_b, axes_b = second
    candidates = list(axes_a) + list(axes_b) + [np.cross(axis_a, axis_b) for axis_a in axes_a for axis_b in axes_b]
    for axis in candidates:
        if np.linalg.norm(axis) < 1e-7: continue
        axis = axis / np.linalg.norm(axis)
        projection_a, projection_b = points_a @ axis, points_b @ axis
        if projection_a.max() < projection_b.min() - .002 or projection_b.max() < projection_a.min() - .002: return False
    return True


def coplanar_exterior(elements):
    planes = []
    for element in elements:
        for face, points in vertices(element).items():
            normal = -np.cross(points[1] - points[0], points[2] - points[0])
            normal /= np.linalg.norm(normal)
            planes.append((element['name'], face, points, normal))
    conflicts = []
    for index, (name_a, face_a, points_a, normal_a) in enumerate(planes):
        for name_b, face_b, points_b, normal_b in planes[index + 1:]:
            if name_a == name_b or np.dot(normal_a, normal_b) < .999999: continue
            if abs(np.dot(points_b[0] - points_a[0], normal_a)) > .00001: continue
            vectors = [points_a[1] - points_a[0], points_a[3] - points_a[0], points_b[1] - points_b[0], points_b[3] - points_b[0]]
            overlapping = True
            for vector in vectors:
                axis = vector / np.linalg.norm(vector)
                projection_a, projection_b = points_a @ axis, points_b @ axis
                if min(projection_a.max(), projection_b.max()) - max(projection_a.min(), projection_b.min()) < .00001:
                    overlapping = False
                    break
            if overlapping: conflicts.append([name_a, face_a, name_b, face_b])
    return conflicts


def validate(item, family, bb_path, report_path):
    entry_path = ASSETS / f'models/item/{item}.json'
    model_path = ASSETS / f'models/item/{family}/{item}.json'
    gui_path = ASSETS / f'models/item/{family}/{item}_gui.json'
    entry, model, gui = [load_json(p) for p in [entry_path, model_path, gui_path]]
    assert entry['loader'] == 'forge:separate_transforms'
    assert entry['base']['parent'] == f'ftboss:item/{family}/{item}'
    assert entry['perspectives']['gui']['parent'] == f'ftboss:item/{family}/{item}_gui'
    assert gui['textures']['layer0'] == f'ftboss:item/{item}'
    assert model.get('parent') not in ('minecraft:item/generated', 'minecraft:item/handheld')
    texture_path = ASSETS / ('textures/' + model['textures']['atlas'].split(':')[1] + '.png')
    icon_path = ASSETS / f'textures/item/{item}.png'
    texture = np.array(Image.open(texture_path).convert('RGBA'))
    assert Image.open(icon_path).size == (32, 32)
    assert len(model['display']) == 8
    densities, aspect_errors, face_count = [], [], 0
    boxes = []
    for element in model['elements']:
        assert np.all(np.array(element['to']) > element['from'])
        transform = element.get('rotation')
        if transform:
            assert transform['axis'] in 'xyz' and transform['angle'] in (-45, -22.5, 0, 22.5, 45)
        box = box_data(element)
        assert box[0].min() > -16 and box[0].max() < 32
        boxes.append(box)
        dimensions = np.array(element['to']) - element['from']
        assert len(element['faces']) == 6
        for direction, data in element['faces'].items():
            face_count += 1
            assert data['texture'] == '#atlas'
            uv = np.array(data['uv'])
            assert uv.min() >= 0 and uv.max() <= 16
            axes = {'north': [0, 1], 'south': [0, 1], 'east': [2, 1], 'west': [2, 1], 'up': [0, 2], 'down': [0, 2]}[direction]
            span = dimensions[axes]
            tex_span = (uv[2:] - uv[:2]) / 16 * np.array(texture.shape[:2][::-1])
            density = tex_span / span
            densities.extend(density.tolist())
            aspect_errors.append(abs(density[0] / density[1] - 1))
            assert abs(density[0] / density[1] - 1) < .005
            assert np.all(np.abs(density - 2) < .005)
            px0, py0, px1, py1 = uv / 16 * texture.shape[0]
            samples_x = np.linspace(px0 + 1e-5, px1 - 1e-5, 9).astype(int)
            samples_y = np.linspace(py0 + 1e-5, py1 - 1e-5, 9).astype(int)
            assert texture[np.ix_(samples_y, samples_x)][:, :, 3].min() == 255
    graph = {index: set() for index in range(len(boxes))}
    for first_index, first in enumerate(boxes):
        for second_index in range(first_index + 1, len(boxes)):
            if connected(first, boxes[second_index]):
                graph[first_index].add(second_index)
                graph[second_index].add(first_index)
    seen, queue = {0}, [0]
    while queue:
        index = queue.pop()
        for neighbor in graph[index] - seen:
            seen.add(neighbor)
            queue.append(neighbor)
    assert len(seen) == len(boxes), [model['elements'][index]['name'] for index in graph if index not in seen]
    coplanar = coplanar_exterior(model['elements'])
    assert not coplanar, coplanar
    bb = load_json(bb_path)
    assert len(bb['elements']) == len(model['elements'])
    assert base64.b64decode(bb['textures'][0]['source'].split(',')[1]) == texture_path.read_bytes()
    for editable, runtime in zip(bb['elements'], model['elements']):
        assert editable['from'] == runtime['from'] and editable['to'] == runtime['to']
        angles = [0, 0, 0]
        transform = runtime.get('rotation')
        if transform: angles['xyz'.index(transform['axis'])] = transform['angle']
        assert editable['rotation'] == angles
        for direction, face in runtime['faces'].items():
            assert np.allclose(np.array(editable['faces'][direction]['uv']) * 16 / texture.shape[0], face['uv'])
    paths = [entry_path, model_path, gui_path, texture_path, icon_path, bb_path]
    result = {'item': 'ftboss:' + item, 'result': 'PASS', 'elements': len(boxes), 'rotatedElements': sum(bool(e.get('rotation', {}).get('angle')) for e in model['elements']), 'faces': face_count, 'pixelDensityRange': [min(densities), max(densities)], 'maxAspectRelativeError': max(aspect_errors), 'opaqueFaceCoverage': '100%', 'obbConnectedElements': len(seen), 'sameFacingCoplanarPairs': len(coplanar), 'editableRuntimeMatch': True, 'files': {str(path.relative_to(ROOT)).replace('\\', '/'): sha(path) for path in paths}, 'gameplayTested': False, 'deployed': False, 'docsImpact': 'none'}
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return result


if __name__ == '__main__':
    validate('frost_material', 'boss_materials', ROOT / 'art/boss_materials/blockbench/frost_material.bbmodel', WORK / 'verification.json')
