import argparse
import base64
import copy
import hashlib
import io
import json
import re
import uuid
import zipfile
from pathlib import Path

from PIL import Image, ImageEnhance


PROJECT = Path(__file__).resolve().parent.parent
ROOT = PROJECT.parent
RESOURCES = PROJECT / 'src/main/resources'
NAMESPACE = 'ftdungeonprops'
REVISION_IDS = {
    'masonry': {'prison_brick_slab', 'prison_fluted_pillar', 'prison_cornice'},
    'furnishings': {'prison_supply_crate', 'prison_ironbound_barrel', 'prison_shackled_post',
                    'prison_weapon_rack', 'prison_alchemy_shelf', 'prison_ossuary_niche', 'prison_warden_lectern'},
    'lighting': {'prison_alchemical_lamp', 'prison_rune_beacon'},
}
FACES = ('north', 'south', 'west', 'east', 'up', 'down')
NORMALS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0),
           'east': (1, 0, 0), 'up': (0, 1, 0), 'down': (0, -1, 0)}
INGREDIENTS = {
    'prison_damp_bricks': 'vine', 'prison_fractured_bricks': 'flint',
    'prison_inlaid_tiles': 'calcite', 'prison_fluted_pillar': 'chiseled_stone_bricks',
    'prison_cornice': 'stone_brick_slab', 'prison_supply_crate': 'chest',
    'prison_ironbound_barrel': 'barrel', 'prison_shackled_post': 'chain',
    'prison_weapon_rack': 'iron_sword', 'prison_alchemy_shelf': 'glass_bottle',
    'prison_torn_banner': 'red_banner', 'prison_ossuary_niche': 'bone_block',
    'prison_warden_lectern': 'book', 'prison_cell_lantern': 'soul_lantern',
    'prison_wall_lantern': 'lantern', 'prison_iron_chandelier': 'iron_bars',
    'prison_candle_cluster': 'candle', 'prison_alchemical_lamp': 'brewing_stand',
    'prison_wall_crystal': 'amethyst_cluster', 'prison_rune_beacon': 'ender_pearl',
    'prison_barred_window': 'iron_ingot',
}
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.63, 0.63, 0.63]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.3, 0.3, 0.3]},
    'fixed': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [0.65, 0.65, 0.65]},
    'head': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.35, 0.35, 0.35]},
    'thirdperson_lefthand': {'rotation': [75, 225, 0], 'translation': [0, 2.5, 0], 'scale': [0.35, 0.35, 0.35]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [0.4, 0.4, 0.4]},
}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def native_asset(archive, path):
    return json.loads(archive.read('assets/minecraft/' + path))


def default_uv(bounds, face):
    lower_x, lower_y, lower_z, upper_x, upper_y, upper_z = bounds
    return {
        'north': [16 - upper_x, 16 - upper_y, 16 - lower_x, 16 - lower_y],
        'south': [lower_x, 16 - upper_y, upper_x, 16 - lower_y],
        'west': [lower_z, 16 - upper_y, upper_z, 16 - lower_y],
        'east': [16 - upper_z, 16 - upper_y, 16 - lower_z, 16 - lower_y],
        'up': [lower_x, lower_z, upper_x, upper_z],
        'down': [lower_x, 16 - upper_z, upper_x, 16 - lower_z],
    }[face]


def cells(box):
    return {(across, height, depth)
            for across in range(box['from'][0], box['to'][0])
            for height in range(box['from'][1], box['to'][1])
            for depth in range(box['from'][2], box['to'][2])}


def face_visible(box, face, occupied):
    axis = next(index for index, value in enumerate(NORMALS[face]) if value)
    positive = NORMALS[face][axis] > 0
    outward = box['to'][axis] if positive else box['from'][axis] - 1
    other_axes = [index for index in range(3) if index != axis]
    for first in range(box['from'][other_axes[0]], box['to'][other_axes[0]]):
        for second in range(box['from'][other_axes[1]], box['to'][other_axes[1]]):
            point = [0, 0, 0]
            point[axis] = outward
            point[other_axes[0]], point[other_axes[1]] = first, second
            if tuple(point) not in occupied:
                return True
    return False


def export_model(entry):
    occupied = set().union(*(cells(box) for box in entry['boxes']))
    texture_names = {}
    elements = []
    removed_faces = 0
    for box in entry['boxes']:
        bounds = box['from'] + box['to']
        faces = {}
        for face in FACES:
            if not face_visible(box, face, occupied):
                removed_faces += 1
                continue
            tile = box.get('faces', {}).get(face, box['tile'])
            key = 'tile' + str(tile)
            texture_names[key] = f'{NAMESPACE}:block/{entry.get("material_set", entry["lane"])}_{tile:02d}'
            detail = {'uv': default_uv(bounds, face), 'texture': '#' + key}
            axis = next(index for index, value in enumerate(NORMALS[face]) if value)
            endpoint = box['to'][axis] if NORMALS[face][axis] > 0 else box['from'][axis]
            if endpoint == (16 if NORMALS[face][axis] > 0 else 0):
                detail['cullface'] = face
            faces[face] = detail
        if faces:
            elements.append({'name': box['name'], 'from': box['from'], 'to': box['to'], 'faces': faces})
    if not elements:
        raise ValueError('No visible geometry: ' + entry['id'])
    texture_names['particle'] = next(iter(texture_names.values()))
    return {'ambientocclusion': True, 'textures': texture_names, 'elements': elements}, removed_faces


def validate_entry(entry, lane, seen):
    identifier = entry['id']
    if not re.fullmatch(r'prison_[a-z0-9_]+', identifier) or identifier in seen:
        raise ValueError('Invalid/duplicate ID ' + identifier)
    seen.add(identifier)
    if entry['kind'] not in ('solid', 'decor', 'stairs', 'slab'):
        raise ValueError('Invalid kind: ' + identifier)
    if entry['mount'] not in ('any', 'floor', 'wall', 'ceiling'):
        raise ValueError('Invalid mounting: ' + identifier)
    limit = 12 if lane == 'masonry' else 28
    if not 0 < len(entry['boxes']) <= limit:
        raise ValueError('Geometry budget: ' + identifier)
    occupied = set()
    for box in entry['boxes']:
        bounds = box['from'] + box['to']
        if any(type(value) is not int or value < 0 or value > 16 for value in bounds):
            raise ValueError('Noninteger/out-of-cell bounds: ' + identifier)
        if any(bounds[axis] >= bounds[axis + 3] for axis in range(3)):
            raise ValueError('Zero/inverted box: ' + identifier)
        if any(type(tile) is not int or not 0 <= tile <= 15
               for tile in [box['tile'], *box.get('faces', {}).values()]):
            raise ValueError('Invalid material: ' + identifier)
        box_cells = cells(box)
        if occupied & box_cells:
            raise ValueError('Intersecting structural boxes: ' + identifier + ' / ' + box['name'])
        occupied.update(box_cells)
    if entry['mount'] == 'floor' and min(box['from'][1] for box in entry['boxes']) != 0:
        raise ValueError('Floating floor item: ' + identifier)
    if entry['mount'] == 'wall' and max(box['to'][2] for box in entry['boxes']) != 16:
        raise ValueError('Missing wall attachment: ' + identifier)
    if entry['mount'] == 'ceiling' and max(box['to'][1] for box in entry['boxes']) != 16:
        raise ValueError('Missing ceiling attachment: ' + identifier)
    if not 0 <= entry['light'] <= 15:
        raise ValueError('Light level: ' + identifier)
    if entry['kind'] == 'solid' and len(occupied) != 4096:
        raise ValueError('Solid does not fill cube: ' + identifier)


def resolve_native_model(model, archive):
    result = {}
    parent_reference = model.get('parent', '')
    if parent_reference and (parent_reference.startswith('minecraft:') or ':' not in parent_reference):
        parent = parent_reference.split(':', 1)[-1]
        result = resolve_native_model(native_asset(archive, 'models/' + parent + '.json'), archive)
    textures = dict(result.get('textures', {}))
    textures.update(model.get('textures', {}))
    result.update(copy.deepcopy(model))
    result['textures'] = textures
    for element in result.get('elements', []):
        for face, details in element['faces'].items():
            details.setdefault('uv', default_uv(element['from'] + element['to'], face))
    return result


def texture_file(reference):
    namespace, location = reference.split(':', 1)
    return RESOURCES / 'assets' / namespace / 'textures' / (location + '.png')


def texture_reference(model, key):
    seen = set()
    while key.startswith('#'):
        if key in seen:
            raise ValueError('Circular material alias')
        seen.add(key)
        key = model['textures'][key[1:]]
    return key


def export_bbmodel(entry, model):
    references = sorted({texture_reference(model, face['texture'])
                         for element in model['elements'] for face in element['faces'].values()})
    textures = []
    for index, reference in enumerate(references):
        image_path = texture_file(reference)
        image = Image.open(image_path)
        textures.append({'name': image_path.name, 'id': str(index), 'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, reference)),
                         'mode': 'bitmap', 'width': image.width, 'height': image.height,
                         'uv_width': 32, 'uv_height': 32,
                         'source': 'data:image/png;base64,' + base64.b64encode(image_path.read_bytes()).decode()})
    cubes = []
    for index, element in enumerate(model['elements']):
        faces = {}
        for face in FACES:
            if face in element['faces']:
                details = element['faces'][face]
                faces[face] = {'uv': [value * 2 for value in details['uv']],
                               'texture': references.index(texture_reference(model, details['texture']))}
                if 'rotation' in details:
                    faces[face]['rotation'] = details['rotation']
                if 'cullface' in details:
                    faces[face]['cullface'] = details['cullface']
            else:
                faces[face] = {'uv': [0, 0, 0, 0], 'texture': None}
        cubes.append({'name': element.get('name', 'Structure ' + str(index + 1)), 'type': 'cube',
                      'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, entry['id'] + '/' + str(index))),
                      'from': element['from'], 'to': element['to'], 'origin': [8, 8, 8],
                      'rotation': [0, 0, 0], 'autouv': 0, 'box_uv': False, 'faces': faces})
    document = {'meta': {'format_version': '4.10', 'model_format': 'java_block', 'box_uv': False},
                'name': entry['name_zh'], 'model_identifier': entry['id'],
                'resolution': {'width': 32, 'height': 32}, 'elements': cubes,
                'outliner': [cube['uuid'] for cube in cubes], 'textures': textures, 'display': DISPLAY}
    write_json(ROOT / 'model_sources' / (entry['id'] + '.bbmodel'), document)


def export_native(entry, archive, assets, base_tile):
    identifier = entry['id']
    textures = {key: base_tile for key in ('top', 'bottom', 'side')}
    if 'native_tiles' in entry:
        textures = {key: f'{NAMESPACE}:block/{entry["material_set"]}_{entry["native_tiles"][key]:02d}'
                    for key in ('top', 'bottom', 'side')}
    families = [('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')]
    source_name = 'stone_brick_stairs'
    if entry['kind'] == 'slab':
        families = [('', 'slab'), ('_top', 'slab_top')]
        if 'native_tiles' in entry:
            families.append(('_double', 'cube_bottom_top'))
        source_name = 'stone_brick_slab'
    for suffix, parent in families:
        write_json(assets / 'models/block' / (identifier + suffix + '.json'),
                   {'parent': 'minecraft:block/' + parent, 'textures': textures})
    states = native_asset(archive, 'blockstates/' + source_name + '.json')
    for variant in states['variants'].values():
        model_name = variant['model'].split('/')[-1]
        suffix = model_name.removeprefix(source_name)
        replacement = identifier + suffix if model_name.startswith(source_name) else entry['base']
        if entry['kind'] == 'slab' and 'native_tiles' in entry and not model_name.startswith(source_name):
            replacement = identifier + '_double'
        variant['model'] = f'{NAMESPACE}:block/' + replacement
    write_json(assets / 'blockstates' / (identifier + '.json'), states)


def export_recipe(entry):
    identifier = entry['id']
    base = NAMESPACE + ':prison_blue_bricks'
    result = {'item': NAMESPACE + ':' + identifier}
    if identifier == 'prison_blue_bricks':
        result['count'] = 4
        recipe = {'type': 'minecraft:crafting_shapeless',
                  'ingredients': [{'item': 'minecraft:stone_bricks'}, {'item': 'minecraft:deepslate_bricks'}], 'result': result}
    elif entry['kind'] in ('stairs', 'slab'):
        result['count'] = 4 if entry['kind'] == 'stairs' else 6
        pattern = ['A  ', 'AA ', 'AAA'] if entry['kind'] == 'stairs' else ['AAA']
        recipe = {'type': 'minecraft:crafting_shaped', 'pattern': pattern, 'key': {'A': {'item': base}}, 'result': result}
    else:
        recipe = {'type': 'minecraft:crafting_shapeless',
                  'ingredients': [{'item': base}, {'item': 'minecraft:' + INGREDIENTS[identifier]}], 'result': result}
    write_json(RESOURCES / f'data/{NAMESPACE}/recipes/{identifier}.json', recipe)
    trigger = 'minecraft:stone_bricks' if identifier == 'prison_blue_bricks' else base
    write_json(RESOURCES / f'data/{NAMESPACE}/advancements/recipes/{identifier}.json',
               {'parent': 'minecraft:recipes/root', 'criteria': {'has_base':
                {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': [trigger]}]}},
                'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': NAMESPACE + ':' + identifier}}},
                'requirements': [['has_base', 'has_the_recipe']], 'rewards': {'recipes': [NAMESPACE + ':' + identifier]}})
    functions = []
    if entry['kind'] == 'slab':
        functions.append({'function': 'minecraft:set_count', 'count': 2, 'conditions':
                          [{'condition': 'minecraft:block_state_property', 'block': NAMESPACE + ':' + identifier,
                            'properties': {'type': 'double'}}]})
    functions.append({'function': 'minecraft:explosion_decay'})
    write_json(RESOURCES / f'data/{NAMESPACE}/loot_tables/blocks/{identifier}.json',
               {'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries':
                [{'type': 'minecraft:item', 'name': NAMESPACE + ':' + identifier, 'functions': functions}]}]})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    arguments = parser.parse_args()
    assets = RESOURCES / f'assets/{NAMESPACE}'
    texture_folder = assets / 'textures/block'
    texture_folder.mkdir(parents=True, exist_ok=True)
    entries = []
    material_folders = {}
    for lane in ('masonry', 'furnishings', 'lighting'):
        folder = ROOT / 'lanes' / lane
        definitions = json.loads((folder / 'definitions.json').read_text(encoding='utf-8-sig'))
        if len(definitions) != 8:
            raise ValueError('Expected 8 entries in ' + lane)
        for entry in definitions:
            entry['lane'] = lane
        entries.extend(definitions)
        material_folders[lane] = folder
    original = {entry['id']: entry for entry in entries}
    replacements = {}
    for lane, identifiers in REVISION_IDS.items():
        folder = ROOT / 'revisions' / lane
        definitions = json.loads((folder / 'definitions.json').read_text(encoding='utf-8-sig'))
        if len(definitions) != len(identifiers) or {entry['id'] for entry in definitions} != identifiers:
            raise ValueError('Exact selected remake IDs required in ' + lane)
        material_set = 'remake-' + lane
        material_folders[material_set] = folder
        for entry in definitions:
            previous = original[entry['id']]
            if any(entry[key] != previous[key] for key in ('kind', 'mount', 'light')):
                raise ValueError('Runtime identity changed: ' + entry['id'])
            if entry['kind'] in ('stairs', 'slab') and entry.get('base') != previous['base']:
                raise ValueError('Native base changed: ' + entry['id'])
            entry['lane'] = lane
            entry['material_set'] = material_set
            replacements[entry['id']] = entry
    entries = [replacements.get(entry['id'], entry) for entry in entries]
    seen = set()
    sprites = []
    for entry in entries:
        validate_entry(entry, entry['lane'], seen)
        if 'native_tiles' in entry:
            if set(entry['native_tiles']) != {'top', 'bottom', 'side'} or any(
                    type(tile) is not int or not 0 <= tile <= 15 for tile in entry['native_tiles'].values()):
                raise ValueError('Invalid native materials: ' + entry['id'])
    for material_set, folder in material_folders.items():
        atlas = Image.open(folder / 'atlas.png').convert('RGBA')
        if atlas.size != (128, 128) or atlas.getextrema()[3] != (255, 255):
            raise ValueError('Expected opaque 128x128 atlas in ' + material_set)
        glow_tiles, ordinary_tiles = set(), set()
        for entry in entries:
            if entry.get('material_set', entry['lane']) != material_set:
                continue
            ordinary_tiles.update(entry.get('native_tiles', {}).values())
            for box in entry['boxes']:
                tiles = {box['tile'], *box.get('faces', {}).values()}
                (glow_tiles if box.get('glow', False) else ordinary_tiles).update(tiles)
        if glow_tiles & ordinary_tiles:
            raise ValueError('Shared glowing and ordinary tile in ' + material_set)
        for tile in sorted(glow_tiles | ordinary_tiles):
            across, height = tile % 4 * 32, tile // 4 * 32
            sprite = atlas.crop((across, height, across + 32, height + 32))
            filename = f'{material_set}_{tile:02d}.png'
            sprite.save(texture_folder / filename)
            sprites.append({'file': filename, 'width': 32, 'height': 32})
            if tile in glow_tiles:
                dark = ImageEnhance.Color(sprite).enhance(0.35)
                dark = ImageEnhance.Brightness(dark).enhance(0.32)
                dark.save(texture_folder / f'{material_set}_{tile:02d}_off.png')
                sprites.append({'file': f'{material_set}_{tile:02d}_off.png', 'width': 32, 'height': 32})
    expected_sprites = {entry['file'] for entry in sprites}
    for path in texture_folder.glob('*.png'):
        if path.name not in expected_sprites:
            path.unlink()
    catalog, costs = [], []
    language_zh = {'itemGroup.ftdungeonprops': 'FT地牢装饰 · 囚狱套件'}
    language_en = {'itemGroup.ftdungeonprops': 'FT Dungeon Decor · Prison Kit'}
    base_entry = next(entry for entry in entries if entry['id'] == 'prison_blue_bricks')
    base_tile = f'{NAMESPACE}:block/masonry_{base_entry["boxes"][0]["tile"]:02d}'
    with zipfile.ZipFile(arguments.minecraft_jar) as archive:
        for entry in entries:
            identifier = entry['id']
            native = entry['kind'] in ('stairs', 'slab')
            removed_faces = 0
            if native:
                export_native(entry, archive, assets, base_tile)
                model = resolve_native_model(json.loads((assets / f'models/block/{identifier}.json').read_text()), archive)
            else:
                model, removed_faces = export_model(entry)
                write_json(assets / f'models/block/{identifier}.json', model)
                if entry['light'] > 0:
                    off = copy.deepcopy(model)
                    glowing = {tile for box in entry['boxes'] if box.get('glow', False)
                               for tile in [box['tile'], *box.get('faces', {}).values()]}
                    for key, reference in off['textures'].items():
                        if any(reference.endswith(f'_{tile:02d}') for tile in glowing):
                            off['textures'][key] = reference + '_off'
                    write_json(assets / f'models/block/{identifier}_off.json', off)
                variants = {}
                if entry['kind'] == 'solid':
                    variants[''] = {'model': NAMESPACE + ':block/' + identifier}
                else:
                    for facing, rotation in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                        for lit in ([True, False] if entry['light'] else [None]):
                            state = 'facing=' + facing + (',lit=' + str(lit).lower() if lit is not None else '')
                            variants[state] = {'model': NAMESPACE + ':block/' + identifier + ('_off' if lit is False else ''),
                                               'y': rotation}
                write_json(assets / f'blockstates/{identifier}.json', {'variants': variants})
            write_json(assets / f'models/item/{identifier}.json',
                       {'parent': NAMESPACE + ':block/' + identifier, 'display': DISPLAY})
            export_bbmodel(entry, model)
            export_recipe(entry)
            language_zh['block.' + NAMESPACE + '.' + identifier] = entry['name_zh']
            language_en['block.' + NAMESPACE + '.' + identifier] = entry['name_en']
            runtime = {key: entry[key] for key in ('id', 'kind', 'mount', 'light')}
            runtime['boxes'] = [box['from'] + box['to'] for box in entry['boxes']]
            if native:
                runtime['base'] = entry['base']
            catalog.append(runtime)
            costs.append({'id': identifier, 'lane': entry['lane'], 'native': native,
                          'editor_boxes': len(model['elements']),
                          'default_selected_boxes': len(model['elements']),
                          'default_selected_faces': sum(len(element['faces']) for element in model['elements']),
                          'fully_occluded_faces_removed': removed_faces})
    write_json(RESOURCES / f'data/{NAMESPACE}/kit_catalog.json', catalog)
    write_json(assets / 'lang/zh_cn.json', language_zh)
    write_json(assets / 'lang/en_us.json', language_en)
    write_json(RESOURCES / 'data/minecraft/tags/blocks/mineable/pickaxe.json',
               {'replace': False, 'values': [NAMESPACE + ':' + entry['id'] for entry in entries]})
    for kind in ('stairs', 'slab'):
        identifier = next(entry['id'] for entry in entries if entry['kind'] == kind)
        plural = 'stairs' if kind == 'stairs' else 'slabs'
        for category in ('blocks', 'items'):
            write_json(RESOURCES / f'data/minecraft/tags/{category}/{plural}.json',
                       {'replace': False, 'values': [NAMESPACE + ':' + identifier]})
    write_json(ROOT / 'catalog.json', entries)
    write_json(ROOT / 'qa/export-costs.json', {'blocks': costs, 'sprites': sprites,
               'rgba_base_bytes': len(sprites) * 32 * 32 * 4,
               'notes': ['Default selected state only; native stairs/slab alternatives counted separately by native QA.',
                         'PNG sprites use the vanilla block atlas; sprite count is not a measured draw-call count.',
                         'No animation frames, transparent textures, block entities or ticking effects.',
                         'No Minecraft client, player world or FPS test has been performed.']})
    resource_files = sorted(path for path in RESOURCES.rglob('*') if path.is_file())
    write_json(ROOT / 'qa/resource-hashes.json', {path.relative_to(RESOURCES).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
                                               for path in resource_files})
    authoring_files = [ROOT / 'lanes' / lane / filename
                      for lane in ('masonry', 'furnishings', 'lighting')
                      for filename in ('definitions.json', 'atlas.png', 'tilemap.json', 'make_assets.py', 'DESIGN.md', 'contact-sheet.png')]
    authoring_files.extend(ROOT / 'revisions' / lane / filename for lane in REVISION_IDS
                           for filename in ('definitions.json', 'atlas.png', 'tilemap.json', 'make_assets.py',
                                            'DESIGN.md', 'contact-sheet.png', 'qa.json'))
    write_json(ROOT / 'qa/authoring-hashes.json', {path.relative_to(ROOT).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
                                                for path in authoring_files})
    print(json.dumps({'exported_blocks': len(entries), 'sprites': len(sprites), 'sources': len(entries),
                      'default_boxes': sum(item['default_selected_boxes'] for item in costs),
                      'default_faces': sum(item['default_selected_faces'] for item in costs)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
