"""Extend the approved jam rig using the pack's actual single-jar registrations."""
from pathlib import Path
from PIL import Image
import collections, copy, hashlib, json, re, shutil

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
SOURCE = PACK / 'kubejs/assets/farmingtales'
SCRIPTS = PACK / 'kubejs/startup_scripts'
PROTOTYPE = 'farmingtales:ancient_fruit_jam_1'

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def discover():
    jam_script = (SCRIPTS / 'winemaking/jam.js').read_text(encoding='utf-8')
    mayo_script = (SCRIPTS / 'winemaking/mayonnaise.js').read_text(encoding='utf-8')
    roe_script = (SCRIPTS / 'content/fish_roe.js').read_text(encoding='utf-8')
    jams = re.findall(r"event.create\('farmingtales:([^']+_jam_1)'", jam_script)
    mayos = re.findall(r'\["([^"\n]+mayonnaise)",\s*"([^"]+)"\]', mayo_script)
    roe = re.findall(r'roe:\s*"([^"]+)",\s*name:\s*"([^"]+)"', roe_script)
    entries = [(x, 'jam', 'bakerys/' + x[:-2]) for x in jams]
    assert "event.create('farmingtales:chocolate_sauce'" in jam_script
    entries.append(('chocolate_sauce', 'chocolate_sauce', 'bakerys/chocolate_sauce'))
    entries += [(x, 'mayonnaise', 'bakerys/' + x) for x, _ in mayos]
    for source, _ in roe:
        name = ('salmon_roe' if source == 'youkaisfeasts:roe' else source.split(':')[1]) + '_sauce'
        entries.append((name, 'roe_sauce', 'fish_roe_sauce/' + name))
    entries.append(('crab_roe_sauce', 'roe_sauce', 'fish_roe_sauce/crab_roe_sauce'))
    assert len(entries) == len({x[0] for x in entries}) == 130
    return entries

def main(only=None):
    profiles_path = ASSETS / 'drink_profiles.json'
    profiles = read(profiles_path)
    prototype = next(p for p in profiles if p['id'] == PROTOTYPE)
    original = read(SOURCE / 'models/block/bakerys/ancient_fruit_jam.json')
    mask_path = ASSETS / 'hand_volumes.json'
    masks = read(mask_path)
    atlas_path = ROOT / 'src/main/resources/assets/minecraft/atlases/blocks.json'
    atlas = read(atlas_path)
    sprites = {s.get('resource') for s in atlas['sources']}
    templates = {part: read(ASSETS / f'models/animation/jam/{part}.json')
                 for part in ('body', 'open_body', 'stopper')}
    manifest, sources = [], set()
    if only:
        previous = read(ROOT / 'jar-family-sources.json')
        manifest = [e for e in previous['entries'] if e['id'].split(':')[1] not in only]
        sources = {Path(e['path']) for e in previous['sources']}
    for name, category, model_location in discover():
        if only and name not in only:
            continue
        item_id = 'farmingtales:' + name
        model_path = SOURCE / f'models/block/{model_location}.json'
        model = read(model_path)
        geometry_path = (PACK / ('kubejs/assets/' + model['parent'].replace(':', '/models/') + '.json')
                         if 'parent' in model else model_path)
        geometry = read(geometry_path)
        assert geometry['elements'][:3] == original['elements'][:3], name
        assert geometry['elements'][6:8] == original['elements'][6:8], name
        texture_path = PACK / ('kubejs/assets/' + model['textures']['4'].replace(':', '/textures/') + '.png')
        sources.update([model_path, geometry_path, texture_path])
        image = Image.open(texture_path).convert('RGBA')
        assert image.size == (32, 32)
        if item_id == PROTOTYPE:
            profile = prototype
        else:
            texture_root = 'farmingtales_drink_anim:jam/' + name
            model_root = 'animation/jam/' + name
            out = ASSETS / 'textures/jam'
            shutil.copy2(texture_path, out / (name + '.png'))
            sealed = image.copy()
            # Continue each jar's own upper-glass pixels underneath the plug.
            for x in range(5):
                for y in (4, 5):
                    sealed.putpixel((x, y), image.getpixel((x, 3)))
            sealed.save(out / (name + '_sealed.png'))
            image.crop((0, 2, 4, 3)).resize((4, 4), Image.Resampling.NEAREST).save(out / (name + '_rim.png'))
            content = Image.new('RGBA', (8, 4))
            content.paste(image.crop((10, 10, 14, 14)), (0, 0))
            content.paste(image.crop((10, 14, 14, 18)), (4, 0))
            assert content.getchannel('A').getextrema() == (255, 255), name
            content.save(out / (name + '_content.png'))
            for part, template in templates.items():
                data = copy.deepcopy(template)
                data['textures'] = {k: (v if k == 'lip' else v.replace(
                    'farmingtales_drink_anim:jam/ancient_fruit_jam', texture_root))
                    for k, v in data['textures'].items()}
                write(ASSETS / f'models/{model_root}/{part}.json', data)
            profile = copy.deepcopy(prototype)
            profile.update(id=item_id, bodyModel=model_root + '/body',
                           capModel=model_root + '/stopper', neckModel=model_root + '/open_body')
            profile['liquid'][0]['texture'] = texture_root.replace(':', ':textures/') + '_content.png'
            profiles = [p for p in profiles if p['id'] != item_id]
            profiles.append(profile)
            masks[item_id] = copy.deepcopy(masks[PROTOTYPE])
            for suffix in ('', '_sealed', '_rim', '_content'):
                sprite = texture_root + suffix
                if sprite not in sprites:
                    atlas['sources'].append({'type': 'minecraft:single', 'resource': sprite})
                    sprites.add(sprite)
        # These fixtures stay outside the release JAR; use original 2D item icons.
        fixture = ROOT / 'src/smoke/resources/assets/farmingtales'
        if item_id != PROTOTYPE:
            icon_folder = {'jam': 'bakerys/jam', 'chocolate_sauce': 'bakerys/jam', 'mayonnaise': 'bakerys/mayonnaise', 'roe_sauce': 'fish_roe_sauce'}[category]
            icon_name = name[:-2] if category == 'jam' else name
            icon = SOURCE / f'textures/item/{icon_folder}/{icon_name}.png'
            sources.add(icon)
            target = fixture / f'textures/item/{icon_folder}/{icon_name}.png'
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(icon, target)
            write(fixture / f'models/item/{name}.json', {'parent': 'minecraft:item/generated',
                  'textures': {'layer0': f'farmingtales:item/{icon_folder}/{icon_name}'}})
            write(fixture / f'blockstates/{name}.json', {'variants': {'': {
                  'model': 'farmingtales_drink_anim:' + profile['bodyModel']}}})
        manifest.append({'id': item_id, 'category': category, 'model': str(model_path),
                         'geometry': str(geometry_path), 'texture': str(texture_path)})
    for relative in ['winemaking/jam.js', 'winemaking/mayonnaise.js', 'content/fish_roe.js', 'content/fish_roe_sauces.js']:
        sources.add(SCRIPTS / relative)
    write(profiles_path, profiles)
    write(mask_path, masks)
    write(atlas_path, atlas)
    write(ROOT / 'jar-family-sources.json', {'entries': manifest, 'sources': [
        {'path': str(p), 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in sorted(sources)]})
    print('Prepared', len(manifest), 'jar profiles:', dict(collections.Counter(e['category'] for e in manifest)))

if __name__ == '__main__':
    import sys
    main(set(sys.argv[1:]) or None)
