import argparse
import base64
import copy
import hashlib
import json
from io import BytesIO
from pathlib import Path

from PIL import Image


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def prune_outliner(nodes, retained, groups):
    result = []
    for node in nodes:
        if isinstance(node, str):
            if node in retained:
                result.append(node)
            continue
        children = prune_outliner(node.get('children', []), retained, groups)
        if children:
            group = copy.deepcopy(node)
            group['children'] = children
            groups.add(group['uuid'])
            result.append(group)
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    project = Path(__file__).resolve().parent.parent
    assets = project / 'src/main/resources/assets/ftdungeonmobs'
    original_path = assets / 'models/block/bloodthorn_brazier.json'
    original_bytes = original_path.read_bytes()
    original = json.loads(original_bytes)
    body = [element for element in original['elements']
            if all(face['texture'] != '#1' for face in element['faces'].values())]
    assert len(original['elements']) == 43 and len(body) == 32
    assert sum(len(element['faces']) for element in body) == 161
    texture = assets / 'textures/block/bloodthorn_brazier.png'
    cooled_texture = texture.with_name('bloodthorn_brazier_unlit.png')
    original_image = Image.open(texture).convert('RGBA')
    cooled_image = original_image.copy()
    coal_pixels = set()
    for element in body:
        if element['name'].startswith('方炭'):
            for face in element['faces'].values():
                left, top, right, bottom = [round(value * 4) for value in face['uv']]
                coal_pixels.update((pixel_x, pixel_y) for pixel_x in range(min(left, right), max(left, right))
                                   for pixel_y in range(min(top, bottom), max(top, bottom)))
    assert len(coal_pixels) == 11
    for pixel_x, pixel_y in coal_pixels:
        red, green, blue, alpha = original_image.getpixel((pixel_x, pixel_y))
        shade = 20 + round(max(red, green, blue) * 0.12)
        cooled_image.putpixel((pixel_x, pixel_y), (shade, shade + 1, shade + 3, alpha))
    cooled_image.save(cooled_texture)
    unlit = copy.deepcopy(original)
    unlit['elements'] = body
    unlit['textures'] = {'0': 'ftdungeonmobs:block/bloodthorn_brazier_unlit',
                         'particle': 'ftdungeonmobs:block/bloodthorn_brazier_unlit'}
    assert set(unlit['textures']) == {'0', 'particle'}
    runtime_path = assets / 'models/block/bloodthorn_brazier_unlit.json'
    write_json(runtime_path, unlit)

    source_path = project / 'model_sources/bloodthorn_brazier/bloodthorn_brazier.bbmodel'
    source = json.loads(source_path.read_text(encoding='utf-8'))
    editable = copy.deepcopy(source)
    editable['name'] = '血棘祭炉_未开启_熄灭'
    editable['elements'] = [element for element in source['elements']
                            if all(face.get('texture') != 1 for face in element['faces'].values())]
    assert len(editable['elements']) == 32
    retained = {element['uuid'] for element in editable['elements']}
    groups = set()
    editable['outliner'] = prune_outliner(source['outliner'], retained, groups)
    editable['groups'] = [group for group in source['groups'] if group['uuid'] in groups]
    editable['textures'] = [copy.deepcopy(source['textures'][0])]
    editable['textures'][0]['namespace'] = 'ftdungeonmobs'
    editable['textures'][0]['folder'] = 'block'
    editable['textures'][0]['name'] = 'bloodthorn_brazier_unlit.png'
    editable['textures'][0]['relative_path'] = '../../src/main/resources/assets/ftdungeonmobs/textures/block/bloodthorn_brazier_unlit.png'
    embedded = base64.b64decode(editable['textures'][0]['source'].split(',', 1)[1])
    assert Image.open(BytesIO(embedded)).convert('RGBA').tobytes() == Image.open(texture).convert('RGBA').tobytes()
    editable['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(cooled_texture.read_bytes()).decode('ascii')
    write_json(source_path.with_name('bloodthorn_brazier_unlit.bbmodel'), editable)

    output = arguments.output
    output.mkdir(parents=True, exist_ok=True)
    delivered = copy.deepcopy(editable)
    delivered['textures'][0]['relative_path'] = 'bloodthorn_brazier_unlit.png'
    write_json(output / '血棘祭炉_未开启_熄灭.bbmodel', delivered)
    (output / 'bloodthorn_brazier_unlit.png').write_bytes(cooled_texture.read_bytes())
    write_json(output / 'bloodthorn_brazier_unlit.json', unlit)
    assert original_path.read_bytes() == original_bytes
    report = {
        'state': 'tier=0', 'defaultPlacement': 'unlit', 'blockLight': 0,
        'cycle': ['unlit', 1, 2, 3, 4, 5, 'unlit'],
        'unlit': {'cuboids': 32, 'faces': 161, 'groups': len(groups),
                  'textures': 1, 'textureSize': [64, 64], 'animationFrames': 0},
        'lit': {'cuboids': 43, 'faces': 217, 'flameCuboids': 11,
                'blockLight': 14, 'palettes': 5, 'animationFrames': 16},
        'bodyGeometryAndUvUnchanged': True, 'litAssetsUnchanged': True,
        'unlitTextureChangedPixels': len(coal_pixels),
        'unlitTextureChangeScope': 'Only existing coal UV pixels; cold dark charcoal; no blood or metal changes',
        'litModelSha256': hashlib.sha256(original_bytes).hexdigest(),
        'unlitModelSha256': hashlib.sha256(runtime_path.read_bytes()).hexdigest(),
        'duplicateBodyCandidatesAdded': 0, 'additionalTextures': 1,
        'clientTested': False,
    }
    write_json(output / '熄灭状态验证.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
