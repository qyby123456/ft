"""Fixed, inward glass necks using the same construction as the v20 screw-cap drinks."""
from pathlib import Path
from copy import deepcopy
import json, math, zipfile
from PIL import Image
from container_assets import bounds, face_piece, subtract
from prepare_matching_mouths import ROOT, ASSETS, PREFIX

PRIOR=ROOT/'backups/1.6.7-before-fixed-mouth-and-inertia/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.6.7.jar'

def opaque(face,textures):
    image=textures[face['texture'][1:]];a,b,c,d=face['uv']
    for y in range(math.floor(min(b,d)*image.height/16),math.ceil(max(b,d)*image.height/16)):
        for x in range(math.floor(min(a,c)*image.width/16),math.ceil(max(a,c)*image.width/16)):
            if image.getpixel((min(image.width-1,x),min(image.height-1,y)))[3]!=255:return False
    return True

def generate():
    report=[]
    strips={r['item']:r['glass_strip'] for r in json.loads((ROOT/'matching-mouth-sources.json').read_text())}
    with zipfile.ZipFile(PRIOR) as archive:
        profiles=json.loads(archive.read(PREFIX+'drink_profiles.json'))
        for p in profiles:
            path=PREFIX+'models/'+p['neckModel']+'.json';model=json.loads(archive.read(path))
            if not model['elements']:continue
            old=model['elements'];lo=[min(bounds(e)[0][i] for e in old) for i in range(3)];hi=[max(bounds(e)[1][i] for e in old) for i in range(3)]
            original_bounds=[lo[:],hi[:]]
            # The opaque closure sits outside this shell and hides it with ordinary depth testing.
            for i in [0,2]:lo[i]+=.02;hi[i]-=.02
            hi[1]-=.02
            name=p['id'].split(':')[1]
            source=json.loads((ROOT.parents[2]/'kubejs/assets/farmingtales/models/block/drinks'/f'{name}.json').read_text(encoding='utf-8-sig'))
            candidates=[e for e in source['elements'] if any(a>b for a,b in zip(e['from'],e['to'])) and all(a!=b for a,b in zip(e['from'],e['to']))]
            donor=max(candidates,key=lambda e:bounds(e)[1][1]) if candidates else source['elements'][0];dl,dh=bounds(donor)
            textures={k:Image.open(ASSETS/('textures/'+v.split(':')[1]+'.png')).convert('RGBA') for k,v in model['textures'].items() if k!='particle'}
            shell=dict(name='fixed_glass',from_=hi,to=lo,rotation=deepcopy(old[0].get('rotation',dict(angle=0,axis='y',origin=[8,8,8]))),faces={})
            shell['from']=shell.pop('from_');pieces=[];used=[]
            for direction in ['north','east','south','west']:
                face=deepcopy(donor['faces'][direction]);a,b,c,d=face['uv'];axis=0 if direction in ['north','south'] else 2
                fraction=min(1,(hi[axis]-lo[axis])/(dh[axis]-dl[axis]));center=(a+c)/2
                face['uv']=[center+(a-c)*fraction/2,b,center+(c-a)*fraction/2,d]
                if not opaque(face,textures):
                    # Restrict to an actual glass row if the original face contains a cutout.
                    row=deepcopy(strips[name])
                    face=row;dh_y=16/textures[face['texture'][1:]].height
                else:dh_y=dh[1]-dl[1]
                a,b,c,d=face['uv'];height=hi[1]-lo[1];bands=max(1,math.ceil(height/dh_y))
                for index in range(bands):
                    top=hi[1]-index*dh_y;bottom=max(lo[1],top-dh_y);ratio=(top-bottom)/dh_y
                    uv=[a,b+(d-b)*ratio,c,b] if donor['from'][1]<donor['to'][1] else [a,d+(b-d)*ratio,c,d]
                    part=deepcopy(shell);part['from'][1]=top;part['to'][1]=bottom;part['faces']={direction:dict(face,uv=uv)}
                    rect=(lo[0],bottom,hi[0],top) if axis==0 else (bottom,lo[2],top,hi[2])
                    part=face_piece(part,direction,rect,'_'+str(index));pieces.append(part);used.append(deepcopy(part['faces'][direction]))
                    if p['style']=='BEER':
                        outer=deepcopy(part);opposite={'north':'south','south':'north','east':'west','west':'east'}[direction]
                        f=outer['faces'].pop(direction);u,v,w,z=f['uv'];f['uv']=[w,v,u,z];outer['faces'][opposite]=f;outer['name']+='_outer';pieces.append(outer)
            # Double-sided lip, like the water/juice top viewing faces; no cap across its centre.
            rim_source=deepcopy(shell);rim_source['faces']={'down':deepcopy(used[0])}
            for index,rect in enumerate(subtract((lo[0],lo[2],hi[0],hi[2]),(lo[0]+.14,lo[2]+.14,hi[0]-.14,hi[2]-.14))):
                part=face_piece(rim_source,'down',rect,'_lip'+str(index));f=deepcopy(part['faces']['down']);a,b,c,d=f['uv'];f['uv']=[a,d,c,b];part['faces']['up']=f;pieces.append(part)
            model['elements']=pieces;model['render_type']='minecraft:cutout'
            (ROOT/'src/main/resources'/path).write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
            report.append(dict(item=name,neck=p['neckModel'],closure_bounds=original_bounds,shell_bounds=[lo,hi],donor=donor,faces=used,elements=len(pieces)))
    (ROOT/'fixed-mouth-sources.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Prepared',len(report),'fixed inset necks with original glass panels and open viewing lips.')

if __name__=='__main__':generate()
