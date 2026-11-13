"""Run after the bottle generators to enable the four requested rigid ornaments."""
from copy import deepcopy
from pathlib import Path
import math
from prepare_cocktail_profiles import READ, dump, corners

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
GAINS = {'bamboo_leaf_green_liquor': .65, 'maotai': .55, 'dassai': .65, 'strongbow': .7}

def prepare():
    decorations = READ(ASSETS / 'decorations.json')
    body_path = ASSETS / 'models/animation/tavern_bottles/maotai_body.json'
    ribbon_path = ASSETS / 'models/animation/tavern_bottles/maotai_ribbon.json'
    body = READ(body_path)
    ribbon = next((e for e in body['elements'] if e.get('name') == 'source_3'), None)
    if ribbon is not None:
        body['elements'].remove(ribbon)
        moving = deepcopy(body)
        moving['elements'] = [ribbon]
        dump(ribbon_path, moving)
        dump(body_path, body)
    else:
        ribbon = READ(ribbon_path)['elements'][0]
    anchor = deepcopy(ribbon)
    anchor['from'] = anchor['to'] = [ribbon['to'][0], ribbon['to'][1], (ribbon['from'][2]+ribbon['to'][2])/2]
    pivot = next(corners(anchor))
    points = list(corners(ribbon))
    decorations['kaleidoscope_world_liquor:maotai'] = [dict(
        name='ribbon', model='animation/tavern_bottles/maotai_ribbon', kind='RIBBON', pivot=pivot,
        extent=[max(abs(v[j]-pivot[j]) for v in points) for j in range(3)], capAttached=False)]
    for name, gain in GAINS.items():
        part = decorations['kaleidoscope_world_liquor:'+name][0]
        model = READ(ASSETS / ('models/'+part['model']+'.json'))
        points = [v for e in model['elements'] for v in corners(e)]
        if name in ('bamboo_leaf_green_liquor', 'strongbow'):
            # Both original leaf planes run diagonally through the square cap. Yawing about
            # its centre preserves height and keeps their exposed portions outside the cap.
            part['pivot'][0] = part['pivot'][2] = 8.0
        part['extent'] = [max(abs(v[j]-part['pivot'][j]) for v in points) for j in range(3)]
        length = max(math.dist(v, part['pivot']) for v in points)
        axis = ([math.sin(math.pi/8),0,math.cos(math.pi/8)] if name=='dassai'
                else [0,1,0] if name in ('bamboo_leaf_green_liquor', 'strongbow') else None)
        part['ribbon'] = dict(length=length, gain=gain, outward=[-1,0,0] if name=='maotai' else None,
                              rigid=True, swingAxis=axis, maxAngle=8)
    dump(ASSETS / 'decorations.json', decorations)
    print('Prepared four rigid ornaments; Maotai ribbon separated from its bottle body.')

if __name__ == '__main__':
    prepare()
