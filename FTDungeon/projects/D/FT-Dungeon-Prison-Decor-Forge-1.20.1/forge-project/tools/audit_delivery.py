import argparse
import base64
import hashlib
import io
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageEnhance

from export_kit import FACES, PROJECT, RESOURCES, ROOT, resolve_native_model, texture_file, texture_reference


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def element_cells(element):
    return {(across, height, depth)
            for across in range(element['from'][0], element['to'][0])
            for height in range(element['from'][1], element['to'][1])
            for depth in range(element['from'][2], element['to'][2])}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    parser.add_argument('--jar', type=Path)
    arguments = parser.parse_args()
    catalog = read(ROOT / 'catalog.json')
    require(len(catalog) == 24, '24 definitions')
    require(len({entry['id'] for entry in catalog}) == 24, 'Unique identifiers')
    for filename, expected in read(ROOT / 'qa/authoring-hashes.json').items():
        require(hashlib.sha256((ROOT / filename).read_bytes()).hexdigest() == expected,
                'Frozen authoring input: ' + filename)
    assets = RESOURCES / 'assets/ftdungeonprops'
    english, chinese = read(assets / 'lang/en_us.json'), read(assets / 'lang/zh_cn.json')
    pickaxe = read(RESOURCES / 'data/minecraft/tags/blocks/mineable/pickaxe.json')
    seen_recipes, used_textures, checked = set(), set(), []
    with zipfile.ZipFile(arguments.minecraft_jar) as vanilla:
        for entry in catalog:
            identifier = entry['id']
            raw = read(assets / f'models/block/{identifier}.json')
            model = resolve_native_model(raw, vanilla)
            source = read(ROOT / f'model_sources/{identifier}.bbmodel')
            require(source['meta']['model_format'] == 'java_block', identifier + ': Java source format')
            require(len(source['elements']) == len(model['elements']), identifier + ': source/runtime box count')
            require(source['resolution'] == {'width': 32, 'height': 32}, identifier + ': source texture resolution')
            require(len(model['elements']) <= (12 if entry['lane'] == 'masonry' else 28), identifier + ': budget')
            occupied = set()
            for source_box, element in zip(source['elements'], model['elements']):
                require(element['from'] == source_box['from'] and element['to'] == source_box['to'], identifier + ': source bounds')
                require(not element.get('rotation'), identifier + ': no element rotation')
                bounds = element['from'] + element['to']
                require(all(type(value) is int and 0 <= value <= 16 for value in bounds), identifier + ': integer bounded geometry')
                require(all(bounds[axis] < bounds[axis + 3] for axis in range(3)), identifier + ': positive volume')
                active_cells = element_cells(element)
                require(not occupied.intersection(active_cells), identifier + ': no intersecting-volume boxes')
                occupied.update(active_cells)
                require(element['faces'], identifier + ': no contributor-free boxes')
                for face in FACES:
                    original = source_box['faces'][face]
                    if face not in element['faces']:
                        require(original['texture'] is None, identifier + ': hidden face persistence')
                        continue
                    exported = element['faces'][face]
                    require(original['uv'] == [value * 2 for value in exported['uv']], identifier + ': source/runtime UV')
                    require(all(0 <= value <= 16 for value in exported['uv']), identifier + ': UV bounds')
                    reference = texture_reference(model, exported['texture'])
                    image_path = texture_file(reference)
                    require(image_path.is_file(), identifier + ': resolved PNG ' + reference)
                    used_textures.add(image_path)
                    texture = source['textures'][original['texture']]
                    data = base64.b64decode(texture['source'].split(',', 1)[1])
                    require(data == image_path.read_bytes(), identifier + ': embedded PNG exact equality')
            if entry['light']:
                off = read(assets / f'models/block/{identifier}_off.json')
                require(raw['elements'] == off['elements'], identifier + ': exact on/off geometry equality')
                require(raw['textures'] != off['textures'], identifier + ': distinct off materials')
                for reference in off['textures'].values():
                    if not reference.startswith('#'):
                        used_textures.add(texture_file(reference))
            locale_key = 'block.ftdungeonprops.' + identifier
            require(chinese.get(locale_key) == entry['name_zh'] and english.get(locale_key) == entry['name_en'], identifier + ': translations')
            require('ftdungeonprops:' + identifier in pickaxe['values'], identifier + ': harvest tag')
            item = read(assets / f'models/item/{identifier}.json')
            require(len(item['display']) == 8, identifier + ': eight Display slots')
            require(item['parent'] == 'ftdungeonprops:block/' + identifier, identifier + ': item model parent')
            recipe = read(RESOURCES / f'data/ftdungeonprops/recipes/{identifier}.json')
            require(recipe['result']['item'] == 'ftdungeonprops:' + identifier, identifier + ': recipe result')
            if 'ingredients' in recipe:
                signature = tuple(sorted(json.dumps(part, sort_keys=True) for part in recipe['ingredients']))
                require(signature not in seen_recipes, identifier + ': nonambiguous shapeless recipe')
                seen_recipes.add(signature)
            loot = read(RESOURCES / f'data/ftdungeonprops/loot_tables/blocks/{identifier}.json')
            require(loot['pools'][0]['entries'][0]['name'] == 'ftdungeonprops:' + identifier, identifier + ': loot result')
            if entry['kind'] == 'slab':
                functions = loot['pools'][0]['entries'][0]['functions']
                require(any(function.get('count') == 2 for function in functions), 'Double slab drops two')
            checked.append({'id': identifier, 'source_boxes': len(source['elements']),
                            'runtime_faces': sum(len(element['faces']) for element in model['elements'])})
    for path in used_textures:
        image = Image.open(path).convert('RGBA')
        require(image.size == (32, 32), '32px sprite: ' + str(path))
        require(image.getextrema()[3] == (255, 255), 'Opaque sprite: ' + str(path))
        lane, number, *off = path.stem.split('_')
        tile = int(number)
        folder = ROOT / 'revisions' / lane.removeprefix('remake-') if lane.startswith('remake-') else ROOT / 'lanes' / lane
        atlas = Image.open(folder / 'atlas.png').convert('RGBA')
        across, height = tile % 4 * 32, tile // 4 * 32
        expected = atlas.crop((across, height, across + 32, height + 32))
        if off:
            expected = ImageEnhance.Brightness(ImageEnhance.Color(expected).enhance(0.35)).enhance(0.32)
        require(image.tobytes() == expected.tobytes(), 'Final sprite/authoring atlas pixel equality: ' + str(path))
    for family, identifier in (('stairs', 'prison_brick_stairs'), ('slabs', 'prison_brick_slab')):
        for category in ('blocks', 'items'):
            tag = read(RESOURCES / f'data/minecraft/tags/{category}/{family}.json')
            require('ftdungeonprops:' + identifier in tag['values'], 'Exact vanilla family tag: ' + family)
    jar_hash = None
    if arguments.jar:
        with zipfile.ZipFile(arguments.jar) as archive:
            require(archive.testzip() is None, 'ZIP integrity')
            names = archive.namelist()
            require(not any('/verification/' in name for name in names), 'No verifier classes in runtime JAR')
            for path in RESOURCES.rglob('*'):
                if path.is_file():
                    relative = path.relative_to(RESOURCES).as_posix()
                    require(archive.read(relative) == path.read_bytes(), 'JAR/resource equality: ' + relative)
            require('net/farmingtales/ftdungeonprops/FTDungeonProps.class' in names, 'Runtime entry point')
        jar_hash = hashlib.sha256(arguments.jar.read_bytes()).hexdigest()
    report = {'status': 'pass', 'models': checked, 'opaque_sprites_checked': len(used_textures),
              'editable_source_count': 24, 'source_runtime_texture_equality': True,
              'source_runtime_uv_equality': True, 'on_off_geometry_equality': True,
              'jar_sha256': jar_hash,
              'limitations': ['Static asset and packaged-file audit; not Minecraft client/lighting propagation/FPS validation.',
                              'Blockbench sources parsed independently as JSON; shared active editor was not interrupted.']}
    (ROOT / 'qa/delivery-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'pass', 'models': 24, 'sprites': len(used_textures), 'jar_sha256': jar_hash}))


if __name__ == '__main__':
    main()
