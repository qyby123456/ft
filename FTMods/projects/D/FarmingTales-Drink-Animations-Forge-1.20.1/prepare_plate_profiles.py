"""Senluo Cookery's shareable rice-cake plate, held as a presentation dish.

`sticky_rice_cake_plate` is a `PlateBlockItem`: it has no use animation and no food
properties, so the hotkey must never call `useItem` or it would place the block.
The per-portion models the source mod already ships (`_0` empty .. `_5` full) become
the bite stages, so no geometry is authored here at all. Geometry and both sheets are
reused verbatim apart from the coordinate shift that puts the plate's own centre on
the renderer pivot, which is fixed at model (8, 4.8, 8).

The source JAR is never modified.
"""
from pathlib import Path
import hashlib
import json
import zipfile

from prepare_cookery_profiles import READ, dump, dump_like, read_png, rect_pixels, assert_opaque

ROOT = Path(__file__).absolute().parent
RESOURCES = ROOT / 'src/main/resources'
ASSETS = RESOURCES / 'assets/farmingtales_drink_anim'
FIXTURE = ROOT / 'src/smoke/resources/assets'
JAR = ROOT / 'compat-libs/kaleidoscope-cookery-1.4.1.jar'

ITEM = 'kaleidoscope_cookery:sticky_rice_cake_plate'
SOURCE = 'assets/kaleidoscope_cookery/models/block/plate/sticky_rice_cake_plate/sticky_rice_cake_plate_{}.json'
DISH_SOURCE = 'assets/kaleidoscope_cookery/textures/block/plate/sticky_rice_cake_plate.png'
UNDER_SOURCE = 'assets/kaleidoscope_cookery/textures/block/plate.png'
DISH_SPRITE = 'farmingtales_drink_anim:snacks/sticky_rice_cake'
UNDER_SPRITE = 'farmingtales_drink_anim:plates/plate'
FOLDER = 'plates/sticky_rice_cake_plate'
PORTIONS = 5
# The renderer pivots at model (8, 4.8, 8); the plate's own bounds centre is (8, 4.05, 8).
PIVOT_SHIFT = 4.8 - 4.05


def stage_model(source):
    """Verbatim source geometry, re-centred on the render pivot and pointed at our sheets."""
    elements = json.loads(json.dumps(source['elements']))
    # Keep the source's own texture key names so the face references stay untouched.
    textures = {'1': DISH_SPRITE, 'particle': UNDER_SPRITE}
    faces = []
    for element in elements:
        element['from'][1] += PIVOT_SHIFT
        element['to'][1] += PIVOT_SHIFT
        if 'rotation' in element:
            element['rotation']['origin'][1] += PIVOT_SHIFT
        for face in element['faces'].values():
            reference = face.get('texture', '#1')
            faces.append((textures[reference[1:]] if reference.startswith('#') else reference, face['uv']))
    return {
        'render_type': 'minecraft:cutout',
        'textures': textures,
        'elements': elements,
    }, faces


def main():
    with zipfile.ZipFile(JAR) as archive:
        stages = [(level, json.loads(archive.read(SOURCE.format(level)).decode('utf-8-sig')))
                  for level in range(PORTIONS + 1)]
        dish_raw = archive.read(DISH_SOURCE)
        under_raw = archive.read(UNDER_SOURCE)

    # The dish sheet is the one the single cake already ships; only the plate underside is new.
    dish = ASSETS / 'textures/snacks/sticky_rice_cake.png'
    assert dish.exists(), 'the rice-cake sheet is shared with the single cake'
    scratch = ROOT / 'build/cookery/plate_dish_source.png'
    scratch.parent.mkdir(parents=True, exist_ok=True)
    scratch.write_bytes(dish_raw)
    upstream, shipped = read_png(scratch), read_png(dish)
    assert upstream[:2] == shipped[:2], 'the shared sheet keeps the source dimensions'
    painted = [index for index in range(0, len(upstream[2]), 4) if upstream[2][index + 3] == 255]
    assert all(upstream[2][i:i + 4] == shipped[2][i:i + 4] for i in painted), \
        'the shared sheet keeps every source pixel and only completes transparent ones'
    under = ASSETS / 'textures/plates/plate.png'
    under.parent.mkdir(parents=True, exist_ok=True)
    under.write_bytes(under_raw)

    sheets = {DISH_SPRITE: read_png(dish), UNDER_SPRITE: read_png(under)}
    bounds = None
    for level, source in stages:
        model, faces = stage_model(source)
        # Source art is intentionally sparse (the plate silhouette and the scattered sesame
        # pixels rely on cutout), so the invariant here is verbatim geometry, not opacity.
        expected = json.loads(json.dumps(source['elements']))
        for element in expected:
            element['from'][1] += PIVOT_SHIFT
            element['to'][1] += PIVOT_SHIFT
            if 'rotation' in element:
                element['rotation']['origin'][1] += PIVOT_SHIFT
        assert model['elements'] == expected, f'level{level} geometry must stay verbatim'
        for sprite, _ in faces:
            assert sprite in sheets, f'level{level} references an unknown sheet {sprite}'
        points = [corner for element in model['elements'] for corner in (element['from'], element['to'])]
        low = [min(p[i] for p in points) for i in range(3)]
        high = [max(p[i] for p in points) for i in range(3)]
        bounds = low, high
        dump(ASSETS / f'models/animation/{FOLDER}/level{level}.json', model)

    low, high = bounds
    profile = dict(
        id=ITEM, style='FOOD', scale=.9, width=14.0, depth=14.0,
        capPivot=[8.0, high[1], 8.0], neckJoin=0.0, neckMin=0.0, neckMax=0.0, mouthY=high[1],
        bodyModel=f'animation/{FOLDER}/level{PORTIONS}',
        capModel='animation/cups/empty', neckModel='animation/cups/empty',
        boundsMin=low, boundsMax=high,
        liquid=[], canLid=None, cupShape=None)
    profiles = [p for p in READ(ASSETS / 'drink_profiles.json') if p['id'] != ITEM]
    profiles.append(profile)
    dump(ASSETS / 'drink_profiles.json', profiles)

    # Stage count drives the whole performance, so the next plate is one more line here.
    plates = READ(ASSETS / 'plates.json') if (ASSETS / 'plates.json').exists() else {}
    plates[ITEM] = PORTIONS + 1
    dump(ASSETS / 'plates.json', plates)

    # Every profile needs a hand mask, and the whole dish is the occluder.
    masks = READ(ASSETS / 'hand_volumes.json')
    masks[ITEM] = [dict(min=list(low), max=list(high), origin=list(low),
                        axis='y', angle=0, rescale=False, shiftY=0)]
    dump_like(ASSETS / 'hand_volumes.json', masks)

    atlas_path = RESOURCES / 'assets/minecraft/atlases/blocks.json'
    atlas = READ(atlas_path)
    registered = {source.get('resource') for source in atlas['sources']}
    for sprite in (UNDER_SPRITE,):
        if sprite not in registered:
            atlas['sources'].append(dict(type='minecraft:single', resource=sprite))
    dump(atlas_path, atlas)

    # The upstream inventory sprite is deliberately left alone: no item model is written, so the
    # original `item/generated` icon stays in the inventory, the GUI and on the ground.
    dump(FIXTURE / 'kaleidoscope_cookery/blockstates/sticky_rice_cake_plate.json',
         {'variants': {'': {'model': f'farmingtales_drink_anim:animation/{FOLDER}/level{PORTIONS}'}}})
    dump(FIXTURE / 'kaleidoscope_cookery/models/item/sticky_rice_cake_plate.json',
         {'parent': f'farmingtales_drink_anim:animation/{FOLDER}/level{PORTIONS}'})

    sources = READ(ROOT / 'cookery-sources.json')
    entry = dict(id=ITEM, source=SOURCE.format(PORTIONS),
                 sha256=hashlib.sha256(dish_raw).hexdigest(), texture=DISH_SPRITE,
                 underside=UNDER_SPRITE, underside_sha256=hashlib.sha256(under_raw).hexdigest(),
                 stages=[f'animation/{FOLDER}/level{level}' for level in range(PORTIONS, -1, -1)])
    sources['items'] = [item for item in sources['items'] if item['id'] != ITEM] + [entry]
    dump(ROOT / 'cookery-sources.json', sources)
    print(f'Prepared {ITEM}: {PORTIONS + 1} stages ({PORTIONS} portions), {len(profiles)} profiles, bounds {low} .. {high}')


if __name__ == '__main__':
    main()
