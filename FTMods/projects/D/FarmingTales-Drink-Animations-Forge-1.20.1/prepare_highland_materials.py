"""Rebuild only Highland Hearth's glass/whisky layers from its installed source model."""
from pathlib import Path
from copy import deepcopy
import io
import json
import zipfile
from PIL import Image
from letsdo_liquids import make_dynamic

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
NAME = 'whiskey_highland_hearth'
FOLDER = 'letsdo/brewery/' + NAME

def prepare():
    jar = next((ROOT.parents[2] / 'mods').glob('*letsdo-brewery-forge*.jar'))
    with zipfile.ZipFile(jar) as z:
        source = json.loads(z.read(f'assets/brewery/models/block/{NAME}.json'))
        images = {}
        for key, ref in source['textures'].items():
            namespace, path = ref.split(':')
            images[key] = Image.open(io.BytesIO(z.read(f'assets/{namespace}/textures/{path}.png'))).convert('RGBA')
    path = ASSETS / f'models/animation/{FOLDER}_body.json'
    body = json.loads(path.read_text(encoding='utf-8'))
    original_body = deepcopy(body)
    shell = deepcopy(source['elements'][0]);shell['name'] = 'source_0'
    body['elements'] = [shell if e.get('name') == 'source_0' else e for e in body['elements']]
    boxes, atlas, side, top, windows = make_dynamic(NAME, source['elements'], body['elements'], 1, 15,
                                                  images, body['textures'], FOLDER, ASSETS)
    assert body == original_body, 'This correction must only change pixels, not the model geometry.'
    profile = next(p for p in json.loads((ASSETS / 'drink_profiles.json').read_text(encoding='utf-8'))
                   if p['id'] == 'brewery:' + NAME)
    regions = [dict(min=[(a[0]-8)/16,a[1]/16,(a[2]-8)/16],max=[(b[0]-8)/16,b[1]/16,(b[2]-8)/16],
                    sideUV=side,surfaceUV=top,texture='farmingtales_drink_anim:textures/'+FOLDER+'_liquid.png') for a,b in boxes]
    assert regions == profile['liquid'], 'The existing dynamic cavity must remain intact.'
    atlas.save(ASSETS / f'textures/{FOLDER}_liquid.png')
    print(f'Rebuilt six Highland Hearth glass faces and its dynamic whisky palette; {windows} liquid texels separated.')

if __name__ == '__main__':
    prepare()
