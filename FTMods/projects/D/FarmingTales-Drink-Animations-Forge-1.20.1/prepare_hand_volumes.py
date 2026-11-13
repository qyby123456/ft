"""Read the original container cuboids; write only our hand occlusion masks."""
from pathlib import Path
import json,hashlib

ROOT=Path(__file__).resolve().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
sources=json.loads((ROOT/'drink-profile-sources.json').read_text(encoding='utf-8-sig'))
models={Path(s['path']).stem:s for s in sources if s['path'].endswith('.json')}
result={}
previous=json.loads((ASSETS/'hand_volumes.json').read_text(encoding='utf-8-sig'))
for p in json.loads((ASSETS/'drink_profiles.json').read_text()):
    name=p['id'].split(':')[1];source=models.get(name)
    if source is None:
        # Geometry authored inside this project instead of lifted from a pack model has no
        # upstream cuboid to derive a mask from; keep the mask that ships with the profile.
        assert p['id'] in previous,'No hand mask source for '+p['id']
        result[p['id']]=previous[p['id']];continue
    raw=Path(source['path']).read_bytes()
    assert hashlib.sha256(raw).hexdigest()==source['sha256']
    model=json.loads(raw.decode('utf-8-sig'));boxes=[]
    cap={'BRANDY':5,'CAN':3,'PINK_CAN':3,'WATER':0,'SPORTS':0,'JUICE':0}.get(p['style'])
    for i,e in enumerate(model['elements']):
        if i==cap:continue
        lo=[min(a,b) for a,b in zip(e['from'],e['to'])];hi=[max(a,b) for a,b in zip(e['from'],e['to'])]
        if any(b-a<.001 for a,b in zip(lo,hi)):continue
        rotation=e.get('rotation',{})
        # Closed cork geometry also describes the glass exposed after uncorking.
        cap_index=0 if name in ['rum','coconut_wine'] else 1
        shift=p['neckJoin']*16 if p['style'] in ['WINE','BEER'] and i==cap_index else 0
        boxes.append(dict(min=lo,max=hi,origin=rotation.get('origin',[0,0,0]),axis=rotation.get('axis','y'),
                          angle=rotation.get('angle',0),rescale=rotation.get('rescale',False),shiftY=shift))
    assert boxes,p['id']
    result[p['id']]=boxes
(ASSETS/'hand_volumes.json').write_text(json.dumps(result,separators=(',',':'))+'\n',encoding='utf-8')
print('Prepared',len(result),'container hand masks from unchanged source models.')
