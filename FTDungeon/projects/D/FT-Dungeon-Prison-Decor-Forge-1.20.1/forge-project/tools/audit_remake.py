import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from export_kit import NAMESPACE, RESOURCES, REVISION_IDS, ROOT, resolve_native_model, texture_reference, write_json


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def references(model):
    return {texture_reference(model, details['texture'])
            for element in model['elements'] for details in element['faces'].values()}


def texture_path(resources, reference):
    namespace, location = reference.split(':', 1)
    return resources / 'assets' / namespace / 'textures' / (location + '.png')


def geometry(model):
    return {(across, height, depth)
            for element in model['elements']
            for across in range(element['from'][0], element['to'][0])
            for height in range(element['from'][1], element['to'][1])
            for depth in range(element['from'][2], element['to'][2])}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    arguments = parser.parse_args()
    baseline = arguments.baseline.resolve()
    original_resources = baseline / 'forge-project/src/main/resources'
    selected = set().union(*REVISION_IDS.values())
    previous = read(baseline / 'catalog.json')
    current = read(ROOT / 'catalog.json')
    checks = []

    def require(condition, label):
        if not condition:
            raise AssertionError(label)
        checks.append(label)

    require([entry['id'] for entry in previous] == [entry['id'] for entry in current], 'All 24 IDs and numbering preserved')
    require([index + 1 for index, entry in enumerate(current) if entry['id'] in selected] == [6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 21, 23],
            'Exactly requested twelve models selected')
    frozen_files = read(baseline / 'CONTENTS-SHA256.json')
    for filename, expected in frozen_files.items():
        require(digest(baseline / filename) == expected, 'Original release unchanged: ' + filename)
    old_release = read(baseline / 'RELEASE.json')
    require(digest(baseline / old_release['archive']) == old_release['archive_sha256'], 'Original ZIP preserved')
    originals = {entry['id']: entry for entry in previous}
    untouched, remade = [], []
    with zipfile.ZipFile(arguments.minecraft_jar) as vanilla:
        for entry in current:
            identifier = entry['id']
            old_entry = originals[identifier]
            old_path = original_resources / f'assets/{NAMESPACE}/models/block/{identifier}.json'
            new_path = RESOURCES / old_path.relative_to(original_resources)
            old_model = resolve_native_model(read(old_path), vanilla)
            new_model = resolve_native_model(read(new_path), vanilla)
            for key in ('id', 'kind', 'mount', 'light'):
                require(entry[key] == old_entry[key], identifier + ': runtime identity ' + key)
            for category in ('recipes', 'loot_tables/blocks', 'advancements/recipes'):
                relative = Path(f'data/{NAMESPACE}/{category}/{identifier}.json')
                require((RESOURCES / relative).read_bytes() == (original_resources / relative).read_bytes(),
                        identifier + ': original gameplay resource ' + category)
            if identifier not in selected:
                require(entry == old_entry, identifier + ': catalog byte-equivalent content')
                paths = [Path('model_sources') / (identifier + '.bbmodel')]
                for relative in paths:
                    require((ROOT / relative).read_bytes() == (baseline / relative).read_bytes(),
                            identifier + ': editable source unchanged')
                family = [old_path, original_resources / f'assets/{NAMESPACE}/models/item/{identifier}.json',
                          original_resources / f'assets/{NAMESPACE}/blockstates/{identifier}.json']
                family.extend(old_path.parent.glob(identifier + '_*.json'))
                for path in family:
                    relative = path.relative_to(original_resources)
                    require((RESOURCES / relative).read_bytes() == path.read_bytes(), identifier + ': unchanged ' + relative.as_posix())
                    if path.parent.name == 'block' and path.suffix == '.json':
                        resolved = resolve_native_model(read(path), vanilla)
                        for reference in references(resolved):
                            require(texture_path(RESOURCES, reference).read_bytes() == texture_path(original_resources, reference).read_bytes(),
                                    identifier + ': referenced sprite unchanged ' + reference)
                untouched.append(identifier)
            else:
                old_geometry, new_geometry = geometry(old_model), geometry(new_model)
                same_geometry = old_geometry == new_geometry
                require(not same_geometry or entry['kind'] == 'slab', identifier + ': genuine structural remake (native slab excepted)')
                previous_pixels = {texture_path(original_resources, reference).read_bytes() for reference in references(old_model)}
                new_pixels = {texture_path(RESOURCES, reference).read_bytes() for reference in references(new_model)}
                require(previous_pixels != new_pixels, identifier + ': genuinely redesigned materials')
                remade.append({'number': current.index(entry) + 1, 'id': identifier,
                               'old_boxes': len(old_model['elements']), 'new_boxes': len(new_model['elements']),
                               'geometry_changed': not same_geometry, 'materials_changed': True,
                               'changed_unit_cells': len(old_geometry.symmetric_difference(new_geometry)),
                               'old_model_sha256': digest(old_path), 'new_model_sha256': digest(new_path)})
        slab = next(entry for entry in current if entry['kind'] == 'slab')
        slab_states = read(RESOURCES / f'assets/{NAMESPACE}/blockstates/{slab["id"]}.json')['variants']
        require(slab_states['type=double']['model'] == f'{NAMESPACE}:block/{slab["id"]}_double', 'Revised slab double has its own matching material')
        for suffix, parent in (('', 'slab'), ('_top', 'slab_top'), ('_double', 'cube_bottom_top')):
            model = read(RESOURCES / f'assets/{NAMESPACE}/models/block/{slab["id"]}{suffix}.json')
            require(model['parent'] == 'minecraft:block/' + parent, 'Vanilla slab shape parent: ' + suffix)
            require(len(set(model['textures'].values())) == 3, 'Slab top, bottom and edge materials distinguished: ' + suffix)
            require(all(texture_path(RESOURCES, reference).is_file() for reference in model['textures'].values()), 'Slab textures resolve: ' + suffix)
    for locale in ('zh_cn', 'en_us'):
        old_language = read(original_resources / f'assets/{NAMESPACE}/lang/{locale}.json')
        new_language = read(RESOURCES / f'assets/{NAMESPACE}/lang/{locale}.json')
        for key, value in old_language.items():
            if key.rsplit('.', 1)[-1] not in selected:
                require(new_language[key] == value, 'Unchanged locale: ' + locale + '/' + key)
    require(len(untouched) == 12 and len(remade) == 12, 'Twelve remade and twelve preserved')
    report = {'status': 'pass', 'baseline': str(baseline), 'remade': remade, 'unchanged_models': untouched,
              'unchanged_models_count': 12, 'assertions': len(checks), 'checks': checks,
              'native_slab_note': '06 retains native half/full slab geometry; its top, bottom and edge materials are remade.',
              'runtime_ids_preserved': True, 'original_release_preserved': True,
              'limitations': ['Offline geometry, bytes and native resource checks; not a Minecraft client test.']}
    write_json(ROOT / 'qa/remake-scope.json', report)
    print(json.dumps({'status': 'pass', 'remade': 12, 'preserved': 12, 'assertions': len(checks)}))


if __name__ == '__main__':
    main()
