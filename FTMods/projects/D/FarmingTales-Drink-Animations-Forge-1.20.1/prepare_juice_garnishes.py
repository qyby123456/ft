"""Move the five original juice slices with the cocktail garnish spring."""
from copy import deepcopy
from prepare_cocktail_profiles import ROOT, ASSETS, READ, dump, corners
from container_assets import bounds

JUICES = ('orange', 'lemon', 'strawberry', 'apple', 'watermelon')


def prepare(profiles, masks, decorations):
    for fruit in JUICES:
        item = f'farmingtales:{fruit}_juice_1'
        profile = next(p for p in profiles if p['id'] == item)
        path = ASSETS / f"models/{profile['bodyModel']}.json"
        body = READ(path)
        part_model = profile['bodyModel'] + '_fruit'
        part_path = ASSETS / f'models/{part_model}.json'
        # The fruit is the last source cuboid; its Z rotation distinguishes it
        # from the glass's Y rotation. Reading an existing split is idempotent.
        slices = [e for e in body['elements'] if e.get('rotation', {}).get('axis') == 'z']
        if slices:
            body['elements'] = [e for e in body['elements'] if e not in slices]
            part = dict(textures=deepcopy(body['textures']), render_type='minecraft:cutout', elements=slices)
            dump(path, body)
            dump(part_path, part)
        else:
            slices = READ(part_path)['elements']
        assert len(slices) == 1, item
        points = list(corners(slices[0]))
        low = [min(v[j] for v in points) for j in range(3)]
        high = [max(v[j] for v in points) for j in range(3)]
        pivot = [(a+b)/2 for a, b in zip(low, high)]
        pivot[1] = max(low[1], min(profile['mouthY']-.35, high[1]))
        extent = [max(abs(v[j]-pivot[j]) for v in points) for j in range(3)]
        decorations[item] = [dict(name='fruit_slice', model=part_model, kind='FRUIT', pivot=pivot, extent=extent)]
        boxes = []
        for e in body['elements']:
            a, b = bounds(e)
            rot = e.get('rotation', {})
            if all(b[j]-a[j] > .001 for j in range(3)):
                boxes.append(dict(min=a, max=b, origin=rot.get('origin', [0,0,0]), axis=rot.get('axis','y'),
                                  angle=rot.get('angle',0), rescale=rot.get('rescale',False), shiftY=0))
        masks[item] = boxes


if __name__ == '__main__':
    profiles = READ(ASSETS/'drink_profiles.json')
    masks = READ(ASSETS/'hand_volumes.json')
    decorations = READ(ASSETS/'decorations.json')
    prepare(profiles, masks, decorations)
    dump(ASSETS/'hand_volumes.json', masks)
    dump(ASSETS/'decorations.json', decorations)
    print('Prepared five independently moving source fruit slices.')
