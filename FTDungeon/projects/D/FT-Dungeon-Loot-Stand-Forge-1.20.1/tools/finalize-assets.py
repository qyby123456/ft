import argparse
import base64
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--reference', type=Path, required=True)
    arguments = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    model_path = root / 'model_sources/地牢随机战利品展示台_动态版.bbmodel'
    model = json.loads(model_path.read_text(encoding='utf-8'))
    reference = json.loads((arguments.reference / '可编辑模型/低台座物品展示台.bbmodel').read_text(encoding='utf-8'))
    references = {element['uuid']: element for element in reference['elements']}
    retained = {element['uuid'] for element in model['elements']}
    removed = set(references) - retained
    assert len(retained) == 71 and len(removed) == 42
    for element in model['elements']:
        original = references[element['uuid']]
        for key in ('name', 'from', 'to', 'origin', 'rotation', 'inflate', 'shade', 'faces'):
            assert element.get(key) == original.get(key), (element['name'], key)
    cleanup = json.loads((arguments.reference / '验证/隐藏面保守清理.json').read_text(encoding='utf-8'))
    exposed = [entry for entry in cleanup['candidates'] if entry['uuid'] in retained and entry['occluder_uuid'] in removed]
    assert not exposed
    runtime_path = root / 'src/main/resources/assets/ftdungeonloot/models/block/loot_stand.json'
    runtime = json.loads(runtime_path.read_text(encoding='utf-8'))
    assert runtime == json.loads((root / 'verification/reopened-java-model.json').read_text(encoding='utf-8'))
    texture = root / 'src/main/resources/assets/ftdungeonloot/textures/block/loot_stand.png'
    embedded = base64.b64decode(model['textures'][0]['source'].split(',')[1])
    assert hashlib.sha256(embedded).digest() == hashlib.sha256(texture.read_bytes()).digest()
    with Image.open(texture) as image:
        assert image.size == (256, 256) and image.convert('RGBA').getchannel('A').getextrema() == (255, 255)
    comparisons = []
    for baseline in sorted((root / 'preview/saved').glob('*.png')):
        with Image.open(baseline) as first, Image.open(root / 'preview/reopened' / baseline.name) as second:
            first = first.convert('RGBA')
            second = second.convert('RGBA')
            assert first.size == second.size
            difference = ImageChops.difference(first, second)
            pixels = difference.tobytes()
            changed = sum(pixels[offset:offset + 4] != b'\0\0\0\0' for offset in range(0, len(pixels), 4))
            assert changed == 0, (baseline.name, changed)
            comparisons.append({'view': baseline.name, 'different_pixels': changed})
    assert len(comparisons) == 16
    names = ['front', 'right', 'back', 'left', 'hero', 'rear', 'top', 'bottom']
    sheet = Image.new('RGB', (1600, 860), '#303232')
    drawing = ImageDraw.Draw(sheet)
    for index, name in enumerate(names):
        with Image.open(root / 'preview/reopened' / (name + '.png')) as image:
            tile = image.convert('RGB').resize((400, 400), Image.Resampling.NEAREST)
        horizontal = index % 4 * 400
        vertical = index // 4 * 430
        sheet.paste(tile, (horizontal, vertical + 25))
        drawing.text((horizontal + 12, vertical + 8), name, fill='#e8dfc9')
    sheet.save(root / 'preview/model-contact-sheet.png')
    with Image.open(root / 'preview/reopened/hero.png') as image:
        image.save(root / 'preview/动态底座_成品.png')
    displays = ['gui', 'ground', 'fixed', 'head', 'firstperson_righthand', 'firstperson_lefthand', 'thirdperson_righthand', 'thirdperson_lefthand']
    sheet = Image.new('RGB', (1600, 860), '#303232')
    drawing = ImageDraw.Draw(sheet)
    for index, name in enumerate(displays):
        with Image.open(root / 'preview/reopened' / ('display_' + name + '.png')) as image:
            tile = image.convert('RGB').resize((400, 400), Image.Resampling.NEAREST)
        horizontal = index % 4 * 400
        vertical = index // 4 * 430
        sheet.paste(tile, (horizontal, vertical + 25))
        drawing.text((horizontal + 10, vertical + 8), name, fill='#e8dfc9')
    sheet.save(root / 'preview/display-contact-sheet.png')
    report = {'passed': True, 'reference': str(arguments.reference), 'retained_components': len(retained),
              'removed_fixed_goods_components': len(removed), 'retained_geometry_and_uv_exact': True,
              'newly_exposed_deleted_faces': exposed, 'texture_embedded_matches_runtime': True,
              'reopened_native_java_export_identical': True, 'strict_reopen_comparisons': comparisons,
              'game_client_tested': False, 'dynamic_item_render_tested': False}
    (root / 'verification/native-roundtrip.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({'passed': True, 'strict_views': len(comparisons), 'components': len(retained)}))


if __name__ == '__main__':
    main()
