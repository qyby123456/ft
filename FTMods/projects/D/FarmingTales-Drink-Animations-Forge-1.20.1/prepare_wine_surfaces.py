"""Add the missing viewing sides to v20 tops and XO label backs, preserving inward glass."""
from pathlib import Path
from copy import deepcopy
import json,zipfile

ROOT=Path(__file__).absolute().parent
PREFIX='assets/farmingtales_drink_anim/'
V20=ROOT/'backups/1.6.1-before-can-and-xo-corrections/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.6.1.jar'
ITEMS=('brandy_xo','rum','coconut_wine')

def supplement(name,original):
    model=deepcopy(original)
    for e in model['elements']:
        if e.get('name','').startswith('cube_outline_down_') and e['from'][1]==e['to'][1] and 'up' not in e['faces']:
            face=deepcopy(e['faces']['down']);u0,v0,u1,v1=face['uv']
            face['uv']=[u0,v1,u1,v0];e['faces']['up']=face
    if name=='brandy_xo':
        for index,axis,opposite in [(35,2,'south'),(38,0,'east')]:
            e=deepcopy(original['elements'][index]);e['name']='label_inner_back_'+opposite
            face=deepcopy(next(iter(e['faces'].values())));u0,v0,u1,v1=face['uv']
            face['uv']=[u1,v0,u0,v1];e['faces']={opposite:face}
            # Both source glass windows extend 0.1px below the label's outside edge.
            e['from'][1]=3.2
            # A visible inner backing must not occupy the glass's coplanar pixel boundary.
            e['from'][axis]+=.01;e['to'][axis]+=.01
            model['elements'].append(e)
    return model

if __name__=='__main__':
    with zipfile.ZipFile(V20) as z:
        for name in ITEMS:
            path=PREFIX+'models/animation/drinks/'+name+'_body.json'
            model=supplement(name,json.loads(z.read(path)))
            (ROOT/'src/main/resources'/path).write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
            print(name,len(model['elements']),'elements')
