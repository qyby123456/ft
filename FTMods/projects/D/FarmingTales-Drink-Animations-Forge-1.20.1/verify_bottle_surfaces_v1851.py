"""Audit real UV colors, coplanar faces, preserved exterior mapping, and scope."""
from pathlib import Path
from itertools import combinations
import io,json,zipfile
from PIL import Image
from container_assets import bounds,FACE_AXIS,FACE_UV

ROOT=Path(__file__).resolve().parent
PREFIX='src/main/resources/assets/farmingtales_drink_anim/'
BACKUP=ROOT/'backups/1.8.50-before-targeted-bottle-fixes'
COLOR={'wine':('0',7,7,11,((0,0),(1,0))),
       'lafite_1982':('2',10,2,5,((1,0),(0,0)))}
MODELS=('ice_wine','champagne')
CHANGED={PREFIX+f'textures/tavern_bottles/{name}_{spec[0]}.png' for name,spec in COLOR.items()}
CHANGED|={PREFIX+f'models/animation/tavern_bottles/{name}_body.json' for name in MODELS}

def overlaps(model):
    faces=[]
    for e in model['elements']:
        assert e.get('rotation',{}).get('angle',0)==0
        lo,hi=bounds(e)
        for direction in e['faces']:
            axis,end=FACE_AXIS[direction];other=[i for i in range(3) if i!=axis]
            rect=tuple(lo[i] for i in other)+tuple(hi[i] for i in other)
            faces.append((e['name'],direction,(lo,hi)[end][axis],rect))
    result=[]
    for a,b in combinations(faces,2):
        if a[1]!=b[1] or abs(a[2]-b[2])>1e-7:continue
        if all(min(a[3][i+2],b[3][i+2])-max(a[3][i],b[3][i])>1e-7 for i in (0,1)):
            result.append((a[0],b[0],a[1]))
    return result

def uv_at(element,face,point):
    ua,ur,va,vr=FACE_UV[face];spec=element['faces'][face]
    assert spec.get('rotation',0)==0
    u=(point[ua]-element['from'][ua])/(element['to'][ua]-element['from'][ua])
    v=(point[va]-element['from'][va])/(element['to'][va]-element['from'][va])
    if ur:u=1-u
    if vr:v=1-v
    a,b,c,d=spec['uv'];return a+(c-a)*u,b+(d-b)*v

def verify():
    report={}
    with zipfile.ZipFile(BACKUP/'source.zip') as old:
        allowed=CHANGED|{'src/main/resources/META-INF/mods.toml',
                        'src/smoke/java/com/farmingtales/drinkanim/smoke/CatalogSmoke.java',
                        'build.gradle','prepare_fixed_tavern_bottles.py','README.md'}
        changed={name for name in old.namelist() if old.read(name)!=(ROOT/name).read_bytes()}
        assert changed<=allowed,changed-allowed
        assert changed&set(n for n in old.namelist() if n.startswith(PREFIX))==CHANGED
        for name,(key,left,top,bottom,samples) in COLOR.items():
            path=PREFIX+f'textures/tavern_bottles/{name}_{key}.png'
            before=Image.open(io.BytesIO(old.read(path))).convert('RGBA')
            after=Image.open(ROOT/path).convert('RGBA')
            expected={(x,y) for x in range(left,left+2) for y in range(top,bottom)}
            actual={(x,y) for x in range(16) for y in range(16) if before.getpixel((x,y))!=after.getpixel((x,y))}
            assert actual and actual<=expected,(name,actual,expected)
            for x,y in expected:assert after.getpixel((x,y))==before.getpixel(samples[x-left])
            model=json.loads((ROOT/(PREFIX+f'models/animation/tavern_bottles/{name}_body.json')).read_text())
            colors={before.getpixel(s) for s in samples}
            # Sample UV interiors on every remaining neck and cavity face.
            count=0
            for e in model['elements']:
                if e['name']!='source_1' and not e['name'].startswith('mouth_inside_'):continue
                for direction,spec in e['faces'].items():
                    if direction in ('up','down'):continue
                    a,b,c,d=spec['uv']
                    for u in (.125,.375,.625,.875):
                        for v in (.125,.375,.625,.875):
                            color=after.getpixel((int(a+(c-a)*u),int(b+(d-b)*v)))
                            assert color in colors,(name,e['name'],color)
                            assert color[2]>=color[0] and color[3]==255
                            count+=1
            report[name]={'recolored_texels':len(actual),'neck_and_inner_wall_samples':count}
        for name in MODELS:
            path=PREFIX+f'models/animation/tavern_bottles/{name}_body.json'
            before=json.loads(old.read(path));after=json.loads((ROOT/path).read_text())
            previous=overlaps(before);remaining=overlaps(after)
            assert previous==[('source_0','mouth_interior_floor','up')],previous
            assert not remaining,remaining
            original={e['name']:e for e in before['elements']}
            revised={e['name']:e for e in after['elements']}
            for ename,e in original.items():
                other=revised[ename]
                assert {k:v for k,v in e.items() if k!='faces'}=={k:v for k,v in other.items() if k!='faces'}
                removed={'up'} if ename=='source_0' else {'down'} if ename=='source_1' else set()
                assert other['faces']=={f:s for f,s in e['faces'].items() if f not in removed}
            shell=original['source_0'];floor=revised['mouth_interior_floor'];a,b=bounds(floor)
            rings=[e for e in after['elements'] if e['name'].startswith('source_0_up_cavity_')]
            assert len(rings)==4
            area=(b[0]-a[0])*(b[2]-a[2])
            for ring in rings:
                lo,hi=bounds(ring);area+=(hi[0]-lo[0])*(hi[2]-lo[2])
                for x in (lo[0],hi[0]):
                    for z in (lo[2],hi[2]):
                        p=[x,lo[1],z]
                        assert all(abs(u-v)<1e-7 for u,v in zip(uv_at(shell,'up',p),uv_at(ring,'up',p)))
            lo,hi=bounds(shell);assert abs(area-(hi[0]-lo[0])*(hi[2]-lo[2]))<1e-7
            report[name]={'coplanar_overlaps_before':previous,'coplanar_overlaps_after':remaining,'shoulder_uvs_preserved':True}
    print(json.dumps(report,indent=2));return report

if __name__=='__main__':verify()
