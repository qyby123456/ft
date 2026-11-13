"""Restore the named v20 drink models and reuse bottle pixels for open necks."""
from pathlib import Path
from copy import deepcopy
import json,zipfile
from PIL import Image
from container_assets import bounds,face_piece,subtract
from prepare_wine_surfaces import V20,PREFIX

ROOT=Path(__file__).absolute().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
PRIOR=ROOT/'backups/1.6.6-before-matching-mouths/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.6.6.jar'
DRINKS=('purified_water','vitamin_drink','sweet_orange_juice')

def top_views(model):
    model=deepcopy(model)
    for e in model['elements']:
        if not e.get('name','').startswith('cube_outline_down_') or e['from'][1]!=e['to'][1] or 'up' in e['faces']:continue
        f=deepcopy(e['faces']['down']);a,b,c,d=f['uv']
        f['uv']=[c,b,a,d] if f.get('rotation',0) in (90,270) else [a,d,c,b]
        e['faces']['up']=f
    return model

def glass_strip(source,textures):
    candidates=[e for e in source['elements'] if any(a>b for a,b in zip(e['from'],e['to']))
                and all(a!=b for a,b in zip(e['from'],e['to']))]
    e=max(candidates,key=lambda e:bounds(e)[1][1]) if candidates else source['elements'][0]
    f=deepcopy(e['faces']['north']);im=textures[f['texture'][1:]]
    u0,v0,u1,v1=f['uv'];step=16/im.height
    # Select the row nearest the mouth that contains actual glass pixels, not a label hole.
    rows=range(int(min(v0,v1)/step),max(int(min(v0,v1)/step)+1,int(max(v0,v1)/step)))
    if e['from'][1]>e['to'][1]:rows=reversed(list(rows))
    x0,x1=sorted((round(u0*im.width/16),round(u1*im.width/16)))
    for row in rows:
        if all(im.getpixel((x,row))[3]==255 for x in range(x0,max(x0+1,x1))):
            f['uv']=[u0,row*step,u1,(row+1)*step];f.pop('rotation',None)
            return f
    raise ValueError('No opaque original glass row near mouth')

def open_neck(old,strip,body_textures,beer=False):
    model=deepcopy(old);model['textures']=deepcopy(body_textures);model['render_type']='minecraft:cutout'
    output=[]
    for source in old['elements']:
        lo,hi=bounds(source);e=deepcopy(source);e['from']=hi;e['to']=lo
        e['name']='open_glass';e['faces']={f:deepcopy(strip) for f in ['north','east','south','west','down']}
        for face in ['north','east','south','west']:
            output.append(face_piece(e,face,(lo[0],lo[1],hi[0],hi[1]) if face in ['north','south'] else (lo[1],lo[2],hi[1],hi[2])))
        ring=subtract((lo[0],lo[2],hi[0],hi[2]),(lo[0]+.14,lo[2]+.14,hi[0]-.14,hi[2]-.14))
        for i,rect in enumerate(ring):
            p=face_piece(e,'down',rect,'_rim'+str(i));f=deepcopy(p['faces']['down']);a,b,c,d=f['uv'];f['uv']=[a,d,c,b]
            p['faces']['up']=f;output.append(p)
        if beer:
            # Beer mouths remain opaque on the outside as requested, with a real center opening.
            for face in ['north','east','south','west']:
                p=deepcopy(next(p for p in output if p['name']=='open_glass_'+face))
                opposite={'north':'south','south':'north','east':'west','west':'east'}[face]
                f=p['faces'].pop(face);a,b,c,d=f['uv'];f['uv']=[c,b,a,d];p['faces'][opposite]=f;p['name']+='_outer';output.append(p)
    model['elements']=output
    model.pop('groups',None)
    return model

def generate():
    report=[]
    with zipfile.ZipFile(PRIOR) as previous,zipfile.ZipFile(V20) as v20:
        profiles=json.loads(previous.read(PREFIX+'drink_profiles.json'))
        for p in profiles:
            name=p['id'].split(':')[1]
            if name in DRINKS:
                path=PREFIX+'models/'+p['bodyModel']+'.json'
                m=top_views(json.loads(v20.read(path)))
                (ROOT/'src/main/resources'/path).write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
            path=PREFIX+'models/'+p['neckModel']+'.json';old=json.loads(previous.read(path))
            if not old['elements']:continue
            source=json.loads((Path('Z:/kubejs/assets/farmingtales/models/block/drinks')/(name+'.json')).read_text(encoding='utf-8-sig'))
            body=json.loads(previous.read(PREFIX+'models/'+p['bodyModel']+'.json'))
            textures={k:Image.open(ASSETS/('textures/'+v.split(':')[1]+'.png')).convert('RGBA') for k,v in body['textures'].items() if k!='particle'}
            strip=glass_strip(source,textures)
            m=open_neck(old,strip,body['textures'],p['style']=='BEER')
            (ROOT/'src/main/resources'/path).write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
            report.append(dict(item=name,neck=p['neckModel'],glass_strip=strip,texture=body['textures'][strip['texture'][1:]],elements=len(m['elements'])))
    (ROOT/'matching-mouth-sources.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Restored three v20 drink bodies with top viewing faces; matched',len(report),'open necks to original glass pixels.')

if __name__=='__main__':generate()
