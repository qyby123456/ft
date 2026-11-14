import base64
import hashlib
import importlib.util
import json

import numpy as np
from PIL import Image

from generate import ROOT, RES, ART, WORK, MODEL, ENTRY, GUI, ATLAS, ICON, BBMODEL
from render import LIB as RENDER


SPEC = importlib.util.spec_from_file_location('butcher_material_validator', ROOT / 'tools/model3d/boss_materials_refine/butcher/validate.py')
LIB = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(LIB)


def main():
    errors = []
    model = json.loads(MODEL.read_text(encoding='utf-8'))
    entry = json.loads(ENTRY.read_text(encoding='utf-8'))
    gui = json.loads(GUI.read_text(encoding='utf-8'))
    source = json.loads(BBMODEL.read_text(encoding='utf-8'))
    manifest = json.loads((WORK / 'geometry-manifest.json').read_text(encoding='utf-8'))
    names = [part['name'] for part in manifest['parts']]
    texture = np.array(Image.open(ATLAS).convert('RGBA'))
    if entry.get('loader') != 'forge:separate_transforms' or entry['base']['parent'] != 'ftboss:item/boss_relics/butcher_fang_charm':
        errors.append('Missing separate_transforms base route')
    if entry['perspectives']['gui']['parent'] != 'ftboss:item/boss_relics/butcher_fang_charm_gui' or gui.get('parent') != 'minecraft:item/generated' or gui['textures']['layer0'] != 'ftboss:item/butcher_fang_charm':
        errors.append('GUI sprite routing mismatch')
    if model.get('parent'):
        errors.append('Geometry must not inherit generated/handheld')
    face_count = 0
    uv_rects = []
    alpha_errors = 0
    density_errors = 0
    for index, element in enumerate(model['elements']):
        if set(element['faces']) != {'north', 'south', 'east', 'west', 'up', 'down'}:
            errors.append(f'Missing faces: {index}')
        if any(element['to'][axis] <= element['from'][axis] for axis in range(3)):
            errors.append(f'Nonpositive box size: {index}')
        rotation = element.get('rotation')
        if rotation and (rotation['axis'] not in ('x', 'y', 'z') or rotation['angle'] not in (-45, -22.5, 0, 22.5, 45)):
            errors.append(f'Illegal rotation: {index}')
        for face, spec in element['faces'].items():
            face_count += 1
            uv = np.array(spec['uv'])
            if not np.isfinite(uv).all() or np.min(uv) < 0 or np.max(uv) > 16 or uv[2] <= uv[0] or uv[3] <= uv[1]:
                errors.append(f'Invalid UV: {index}.{face}')
            if spec['texture'] != '#atlas':
                errors.append(f'Incorrect texture reference: {index}.{face}')
            quad = RENDER.vertices(element)[face]
            dimensions = np.array([np.linalg.norm(quad[3] - quad[0]), np.linalg.norm(quad[1] - quad[0])])
            density = (uv[2:] - uv[:2]) * texture.shape[0] / 16 / dimensions
            density_errors += int(np.max(np.abs(density - 2)) > 0.001)
            uv_rects.append(uv)
            grid_u, grid_v = np.meshgrid(np.linspace(uv[0], uv[2], 11, endpoint=False) + (uv[2] - uv[0]) / 22,
                                       np.linspace(uv[1], uv[3], 11, endpoint=False) + (uv[3] - uv[1]) / 22)
            tex_x = np.floor(grid_u / 16 * texture.shape[1]).astype(int)
            tex_y = np.floor(grid_v / 16 * texture.shape[0]).astype(int)
            alpha_errors += int(np.any(texture[tex_y, tex_x, 3] < 255))
        bb_element = source['elements'][index]
        if bb_element['from'] != element['from'] or bb_element['to'] != element['to'] or bb_element['rotation'] != [0, 0, element.get('rotation', {}).get('angle', 0)]:
            errors.append(f'Blockbench geometry mismatch: {index}')
        if rotation and bb_element['origin'] != rotation['origin']:
            errors.append(f'Blockbench pivot mismatch: {index}')
        for face, spec in element['faces'].items():
            if not np.allclose(np.array(bb_element['faces'][face]['uv']) / 8, spec['uv'], atol=0.000001):
                errors.append(f'Blockbench UV mismatch: {index}.{face}')
    uv_overlaps = 0
    for index, first in enumerate(uv_rects):
        for second in uv_rects[index + 1:]:
            uv_overlaps += int(min(first[2] - second[0], second[2] - first[0], first[3] - second[1], second[3] - first[1]) > 1e-7)
    if alpha_errors or density_errors or uv_overlaps:
        errors.append(f'UV defects: alpha={alpha_errors}, density={density_errors}, overlaps={uv_overlaps}')
    bounds = [LIB.obb(element) for element in model['elements']]
    connections = {index: set() for index in range(len(bounds))}
    for index, first in enumerate(bounds):
        for other in range(index + 1, len(bounds)):
            if LIB.intersects(first, bounds[other]):
                connections[index].add(other)
                connections[other].add(index)
    visited, pending = set(), [0]
    while pending:
        index = pending.pop()
        if index not in visited:
            visited.add(index)
            pending.extend(connections[index] - visited)
    detached = [name for index, name in enumerate(names) if index not in visited]
    conflicts = LIB.surface_conflicts(model, bounds, names)
    if detached:
        errors.append('Detached parts: ' + ', '.join(detached))
    if conflicts:
        errors.append('Exposed coplanar overlap: ' + ' / '.join(conflicts))
    embedded = base64.b64decode(source['textures'][0]['source'].split(',', 1)[1])
    if hashlib.sha256(embedded).hexdigest() != LIB.digest(ATLAS):
        errors.append('Embedded texture differs from runtime atlas')
    for path, previous_hash in json.loads((WORK / 'gameplay-baseline.json').read_text(encoding='utf-8')).items():
        if path == 'src/main/resources/assets/ftboss/lang/zh_cn.json':
            language = json.loads((ROOT / path).read_text(encoding='utf-8'))
            for key, expected in {'item.ftboss.butcher_fang_charm': '屠夫骨牙符', 'tooltip.ftboss.boss_relic.butcher_fang_charm': '屠戮之牙：+3 攻击伤害'}.items():
                if language.get(key) != expected:
                    errors.append('Butcher relic language contract changed: ' + key)
            continue
        if LIB.digest(ROOT / path) != previous_hash:
            errors.append(f'Gameplay file changed externally or unexpectedly: {path}')
    view_bounds = {}
    for key, direction in [('front', [0, 0, -1]), ('back', [0, 0, 1]), ('left', [-1, 0, 0]), ('right', [1, 0, 0])]:
        final = RENDER.raster(model, texture, direction, size=512, extent=16.0)
        saved = Image.open(ART / f'{key}.png').convert('RGBA')
        if final.tobytes() != saved.tobytes():
            errors.append('Stale preview: ' + key)
        bbox = final.getbbox()
        if bbox is None or min(bbox[:2]) < 2 or max(bbox[2:]) > 510:
            errors.append('Preview clipped: ' + key)
        view_bounds[key] = bbox
    if Image.open(ICON).size != (32, 32):
        errors.append('Incorrect independent icon size')
    report = {'result': 'PASS' if not errors else 'FAIL', 'errors': errors, 'parts': len(bounds), 'rotated': sum('rotation' in element for element in model['elements']),
              'faces': face_count, 'density': '2 px/model-unit', 'aspect_errors': density_errors, 'transparent_faces': alpha_errors,
              'uv_overlap': uv_overlaps, 'connected_parts': len(visited), 'coplanar_conflicts': conflicts,
              'blockbench': 'runtime geometry / rotation / UV / embedded texture parity checked', 'gameplay': 'Java / Curios baseline hashes unchanged; relevant language keys unchanged; unrelated concurrent language edits preserved',
              'runtime_views': view_bounds, 'asset_sha256': {str(path.relative_to(ROOT)).replace('\\', '/'): LIB.digest(path) for path in [MODEL, ENTRY, GUI, ATLAS, ICON, BBMODEL]}}
    (WORK / 'validation.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(json.dumps(report, ensure_ascii=False, indent=2))
    raise SystemExit(1 if errors else 0)


if __name__ == '__main__':
    main()
