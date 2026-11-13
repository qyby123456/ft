"""Seat the two decorated wine necks on their shoulders without moving source pack models."""
from pathlib import Path
import json,zipfile
ROOT=Path(__file__).absolute().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
BASE=ROOT/'backups/1.7.2-before-cup-expansion/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.7.2.jar'
PREFIX='assets/farmingtales_drink_anim/'
profiles=json.loads((ASSETS/'drink_profiles.json').read_text())
masks=json.loads((ASSETS/'hand_volumes.json').read_text())
with zipfile.ZipFile(BASE) as archive:
    old_masks=json.loads(archive.read(PREFIX+'hand_volumes.json'))
    for name in ['mysticfruit_wine','cherry_liqueur']:
        p=next(p for p in profiles if p['id']=='farmingtales:'+name)
        p['neckJoin']=-.25/16
        path='models/'+p['bodyModel']+'.json';body=json.loads(archive.read(PREFIX+path))
        for i in [5,6,7]:
            e=body['elements'][i]
            for key in ['from','to']:e[key][1]-=.25
            e['rotation']['origin'][1]-=.25
        (ASSETS/path).write_text(json.dumps(body,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        path='models/'+p['neckModel']+'.json';neck=json.loads(archive.read(PREFIX+path))
        for e in neck['elements']:
            for key in ['from','to']:
                if abs(e[key][1]-13.1)<.0001:e[key][1]-=.025
        (ASSETS/path).write_text(json.dumps(neck,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        masks[p['id']]=old_masks[p['id']]
        for box in masks[p['id']]:
            if box['min'][1]>=11.1:box['shiftY']=-.25
(ASSETS/'drink_profiles.json').write_text(json.dumps(profiles,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
(ASSETS/'hand_volumes.json').write_text(json.dumps(masks,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Seated mysticfruit/cherry neck and ribbon assemblies by 0.25 model pixel.')
