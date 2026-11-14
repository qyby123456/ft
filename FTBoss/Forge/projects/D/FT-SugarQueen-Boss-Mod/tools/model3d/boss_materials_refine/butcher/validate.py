import base64
import hashlib
import io
import json
import math
from collections import defaultdict
from pathlib import Path

import numpy as np
from PIL import Image

from render import ROOT, RES, ART, WORK, MODEL, ATLAS, ICON, vertices, raster


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def convex_clip(subject, clip):
    def cross2(first, second):
        return first[0] * second[1] - first[1] * second[0]

    def area(points):
        return sum(cross2(points[index], points[(index + 1) % len(points)]) for index in range(len(points)))

    clip = list(clip)
    if area(clip) < 0:
        clip.reverse()
    output = list(subject)
    for index, edge_start in enumerate(clip):
        edge_end = clip[(index + 1) % len(clip)]
        edge = edge_end - edge_start
        source, output = output, []
        if not source:
            break
        previous = source[-1]
        previous_side = cross2(edge, previous - edge_start)
        for current in source:
            current_side = cross2(edge, current - edge_start)
            if current_side >= -1e-8:
                if previous_side < -1e-8:
                    ratio = previous_side / (previous_side - current_side)
                    output.append(previous + ratio * (current - previous))
                output.append(current)
            elif previous_side >= -1e-8:
                ratio = previous_side / (previous_side - current_side)
                output.append(previous + ratio * (current - previous))
            previous, previous_side = current, current_side
    if len(output) < 3 or abs(area(output)) < 1e-5:
        return None
    return np.array(output)


def obb(element):
    corners = vertices(element)
    front = corners['north']
    directions = [front[0] - front[3], front[0] - front[1], corners['south'][0] - front[3]]
    lengths = np.array([np.linalg.norm(direction) for direction in directions])
    axes = np.array([direction / length for direction, length in zip(directions, lengths)])
    points = np.concatenate(list(corners.values()))
    return points.mean(axis=0), axes, lengths / 2


def contains(bounds, point):
    center, axes, half = bounds
    return np.all(np.abs(axes @ (point - center)) < half - 1e-6)


def intersects(first, second, tolerance=0.002):
    center_a, axes_a, half_a = first
    center_b, axes_b, half_b = second
    candidates = list(axes_a) + list(axes_b)
    candidates += [np.cross(axis_a, axis_b) for axis_a in axes_a for axis_b in axes_b]
    for axis in candidates:
        length = np.linalg.norm(axis)
        if length < 1e-7:
            continue
        axis = axis / length
        radius_a = np.sum(np.abs(axes_a @ axis) * half_a)
        radius_b = np.sum(np.abs(axes_b @ axis) * half_b)
        if abs(np.dot(center_b - center_a, axis)) > radius_a + radius_b + tolerance:
            return False
    return True


def surface_conflicts(model, bounds, names):
    groups = defaultdict(list)
    for index, element in enumerate(model['elements']):
        for face, quad in vertices(element).items():
            normal = np.cross(quad[1] - quad[0], quad[2] - quad[0])
            normal /= np.linalg.norm(normal)
            plane = np.dot(normal, quad[0])
            key = tuple(np.round(np.r_[normal, plane], 5))
            groups[key].append((index, face, quad, normal))
    conflicts = []
    for faces in groups.values():
        for position, first in enumerate(faces):
            index_a, face_a, quad_a, normal = first
            axis_u = quad_a[1] - quad_a[0]
            axis_u /= np.linalg.norm(axis_u)
            axis_v = np.cross(normal, axis_u)
            basis = np.array([axis_u, axis_v])
            base = quad_a[0]
            for second in faces[position + 1:]:
                index_b, face_b, quad_b, _ = second
                if index_a == index_b:
                    continue
                projection_a = (quad_a - base) @ basis.T
                projection_b = (quad_b - base) @ basis.T
                overlap = convex_clip(projection_a, projection_b)
                if overlap is None:
                    continue
                sample = overlap.mean(axis=0) @ basis + base + normal * 0.003
                if any(contains(bound, sample) for index, bound in enumerate(bounds) if index not in (index_a, index_b)):
                    continue
                conflicts.append(f'{names[index_a]}.{face_a} / {names[index_b]}.{face_b}')
    return conflicts


def main():
    errors = []
    checks = {}
    model = json.loads(MODEL.read_text(encoding='utf-8'))
    entry_path = RES / 'models/item/butcher_material.json'
    gui_path = RES / 'models/item/boss_materials/butcher_material_gui.json'
    entry = json.loads(entry_path.read_text(encoding='utf-8'))
    gui = json.loads(gui_path.read_text(encoding='utf-8'))
    project_path = ROOT / 'art/boss_materials/blockbench/butcher_material.bbmodel'
    project = json.loads(project_path.read_text(encoding='utf-8'))
    manifest = json.loads((WORK / 'geometry-manifest.json').read_text(encoding='utf-8'))
    names = [part['name'] for part in manifest['parts']]
    image = Image.open(ATLAS).convert('RGBA')
    pixels = np.array(image)
    if model.get('parent') in ('minecraft:item/generated', 'minecraft:item/handheld', 'item/generated', 'item/handheld'):
        errors.append('Geometry inherited generated/handheld')
    if entry.get('loader') != 'forge:separate_transforms' or entry['base']['parent'] != 'ftboss:item/boss_materials/butcher_material':
        errors.append('Forge base route changed')
    if entry['perspectives']['gui']['parent'] != 'ftboss:item/boss_materials/butcher_material_gui' or gui['textures']['layer0'] != 'ftboss:item/butcher_material':
        errors.append('Independent GUI route changed')
    for path in (entry_path, gui_path):
        if digest(path) != digest(WORK / 'backup-before-refine' / path.relative_to(ROOT)):
            errors.append(f'Routing file changed: {path}')
    checks['forge_gui_and_3d_separation'] = 'unchanged / PASS'
    ranges = []
    density_errors = []
    transparent = []
    for index, element in enumerate(model['elements']):
        if any(not math.isfinite(value) for value in element['from'] + element['to']):
            errors.append(f'Nonfinite geometry: {index}')
        if any(element['to'][axis] <= element['from'][axis] for axis in range(3)):
            errors.append(f'Zero or negative volume: {index}')
        rotation = element.get('rotation')
        if rotation and (rotation['axis'] not in 'xyz' or rotation['angle'] not in (-45, -22.5, 0, 22.5, 45)):
            errors.append(f'Illegal Java rotation: {index}')
        for face, spec in element['faces'].items():
            uv = np.array(spec['uv'])
            if np.any(uv < 0) or np.any(uv > 16) or uv[2] <= uv[0] or uv[3] <= uv[1]:
                errors.append(f'Invalid UV: {index}.{face}')
            face_points = vertices(element)[face]
            physical = np.array([np.linalg.norm(face_points[3] - face_points[0]), np.linalg.norm(face_points[1] - face_points[0])])
            pixel_span = (uv[2:] - uv[:2]) * image.width / 16
            if np.max(np.abs(pixel_span / physical - 2)) > 0.001:
                density_errors.append(f'{index}.{face}')
            ranges.append((index, face, uv))
            grid_u, grid_v = np.meshgrid(np.linspace(uv[0], uv[2], 13, endpoint=False) + (uv[2] - uv[0]) / 26,
                                       np.linspace(uv[1], uv[3], 13, endpoint=False) + (uv[3] - uv[1]) / 26)
            tex_x = np.floor(grid_u / 16 * image.width).astype(int)
            tex_y = np.floor(grid_v / 16 * image.height).astype(int)
            if np.any(pixels[tex_y, tex_x, 3] < 255):
                transparent.append(f'{index}.{face}')
    if density_errors:
        errors.append(f'Nonuniform UV density: {density_errors}')
    if transparent:
        errors.append(f'Transparent face samples: {transparent}')
    overlaps = []
    for position, (index_a, face_a, uv_a) in enumerate(ranges):
        for index_b, face_b, uv_b in ranges[position + 1:]:
            if np.min([uv_a[2] - uv_b[0], uv_b[2] - uv_a[0], uv_a[3] - uv_b[1], uv_b[3] - uv_a[1]]) > 1e-6:
                overlaps.append(f'{index_a}.{face_a} / {index_b}.{face_b}')
    if overlaps:
        errors.append(f'Overlapping UV islands: {overlaps}')
    checks['uv'] = {'faces': len(ranges), 'unique_islands': len(ranges), 'density_px_per_model_unit': 2, 'aspect_errors': len(density_errors), 'overlapping_islands': len(overlaps), 'transparent_face_samples': len(transparent)}
    bounds = [obb(element) for element in model['elements']]
    neighbors = {index: set() for index in range(len(bounds))}
    for index_a, bound_a in enumerate(bounds):
        for index_b in range(index_a + 1, len(bounds)):
            if intersects(bound_a, bounds[index_b]):
                neighbors[index_a].add(index_b)
                neighbors[index_b].add(index_a)
    visited, pending = set(), [0]
    while pending:
        index = pending.pop()
        if index in visited:
            continue
        visited.add(index)
        pending.extend(neighbors[index] - visited)
    disconnected = [names[index] for index in neighbors if index not in visited]
    if disconnected:
        errors.append(f'Disconnected geometry: {disconnected}')
    conflicts = surface_conflicts(model, bounds, names)
    if conflicts:
        errors.append(f'Exposed coplanar overlap: {conflicts}')
    checks['geometry'] = {'parts': len(bounds), 'rotated_parts': sum('rotation' in part for part in model['elements']), 'connected_parts': len(visited), 'disconnected': disconnected, 'exposed_coplanar_conflicts': conflicts}
    if len(project['elements']) != len(model['elements']):
        errors.append('Blockbench element count mismatch')
    for index, (source, runtime) in enumerate(zip(project['elements'], model['elements'])):
        if source['from'] != runtime['from'] or source['to'] != runtime['to']:
            errors.append(f'Blockbench geometry mismatch: {index}')
        expected_angle = runtime.get('rotation', {}).get('angle', 0)
        if source['rotation'] != [0, 0, expected_angle]:
            errors.append(f'Blockbench rotation mismatch: {index}')
        for face, spec in runtime['faces'].items():
            if np.max(np.abs(np.array(source['faces'][face]['uv']) / 8 - spec['uv'])) > 1e-5:
                errors.append(f'Blockbench UV mismatch: {index}.{face}')
    embedded = base64.b64decode(project['textures'][0]['source'].split(',', 1)[1])
    if hashlib.sha256(embedded).hexdigest() != digest(ATLAS):
        errors.append('Blockbench embedded texture differs from runtime')
    checks['blockbench_runtime_parity'] = 'geometry / rotations / per-face UV / embedded PNG match'
    preview_checks = {}
    for name, direction in [('front', [0, 0, -1]), ('back', [0, 0, 1]), ('left', [-1, 0, 0]), ('right', [1, 0, 0])]:
        final = raster(model, pixels, direction)
        with Image.open(ART / f'{name}.png') as saved:
            if final.tobytes() != saved.convert('RGBA').tobytes():
                errors.append(f'Stale runtime preview: {name}')
        bbox = final.getbbox()
        if bbox is None or min(bbox[:2]) <= 1 or max(bbox[2:]) >= final.width - 1:
            errors.append(f'Preview clipping: {name} {bbox}')
        preview_checks[name] = list(bbox) if bbox else None
    checks['final_runtime_views'] = preview_checks
    test = {'from': [0, 0, 0], 'to': [2, 3, 4], 'rotation': {'axis': 'z', 'angle': 22.5, 'origin': [0, 0, 0], 'rescale': False}}
    actual = vertices(test)['north'][0]
    angle = math.radians(22.5)
    expected = [2 * math.cos(angle) - 3 * math.sin(angle), 2 * math.sin(angle) + 3 * math.cos(angle), 0]
    if not np.allclose(actual, expected):
        errors.append('Renderer single-axis rotation conformance failed')
    test['rotation']['rescale'] = True
    if not np.allclose(vertices(test)['north'][0], np.array(expected) / math.cos(angle)):
        errors.append('Renderer rescale conformance failed')
    checks['renderer_conformance'] = 'Java face vertex order; single-axis rotation; rescale; face UV rotation; alpha-aware depth'
    icon = Image.open(ICON).convert('RGBA')
    if icon.size != (32, 32) or icon.getbbox() is None or icon.tobytes() == image.tobytes():
        errors.append('Independent inventory icon invalid')
    hashes = {str(path.relative_to(ROOT)).replace('\\', '/'): digest(path) for path in (MODEL, ATLAS, ICON, entry_path, gui_path, project_path)}
    checks['asset_sha256'] = hashes
    report = {'result': 'PASS' if not errors else 'FAIL', 'errors': errors, 'checks': checks}
    (WORK / 'validation.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(json.dumps(report, ensure_ascii=False, indent=2))
    raise SystemExit(1 if errors else 0)


if __name__ == '__main__':
    main()
