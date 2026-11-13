"""Use the user's named v20 projects as the reference, without rebuilding their faces."""
from pathlib import Path
import json,zipfile,hashlib,copy,math
from prepare_wine_surfaces import supplement

ROOT=Path(__file__).absolute().parent
PREFIX='assets/farmingtales_drink_anim/'
RESTORED=('brandy_xo','rum','coconut_wine')
GLASS_BODIES={PREFIX+'models/animation/drinks/'+n+'_body.json' for n in RESTORED}
V20=ROOT/'backups/1.6.1-before-can-and-xo-corrections/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.6.1.jar'
PRIOR=ROOT/'backups/1.6.5-before-label-backs-and-tops/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.6.5.jar'

def verify(archive):
    with zipfile.ZipFile(V20) as reference,zipfile.ZipFile(PRIOR) as previous:
        profiles=json.loads(archive.read(PREFIX+'drink_profiles.json'))
        wine=[p for p in profiles if p['style'] in ('WINE','BEER','BRANDY')]
        assert len(wine)==19
        for p in wine:
            n=PREFIX+'models/'+p['bodyModel']+'.json'
            item=p['id'].split(':')[1]
            if item in RESTORED:assert json.loads(archive.read(n))==supplement(item,json.loads(reference.read(n))),item
            else:assert archive.read(n)==reference.read(n),item
        old_names={n for n in previous.namelist() if n.startswith('assets/') and not n.endswith('/')}
        new_names={n for n in archive.namelist() if n.startswith('assets/') and not n.endswith('/')}
        assert old_names==new_names
        for n in old_names-GLASS_BODIES:
            assert archive.read(n)==previous.read(n),'Unrelated resource changed: '+n
        for n in previous.namelist():
            if n.endswith('.class'):
                assert archive.read(n)==previous.read(n),'This correction must preserve all runtime animation/rendering code: '+n
    print('v20 original faces preserved; only top reverse faces and two XO label backs added. All runtime classes, textures, closures, liquids and sounds preserved.')

def verify_projects(profiles):
    def signature(bb):
        result=[]
        for e in bb['elements']:
            if not e.get('name','').startswith('bottle_'):continue
            e={k:v for k,v in e.items() if k!='uuid'}
            e['faces']={k:dict(v) for k,v in e['faces'].items()}
            for face in e['faces'].values():
                if face['texture'] is not None:
                    texture=bb['textures'][int(face['texture'])]
                    face['texture']={k:texture[k] for k in ['name','source','render_mode','render_sides','width','height','uv_width','uv_height']}
            result.append(e)
        return result
    report=[]
    for p in profiles:
        if p['style'] not in ('WINE','BEER','BRANDY'):continue
        item=p['id'].split(':')[1]
        for arm in ['Steve','纤细手臂']:
            old=ROOT/f'blockbench/酒水扩展-v20/{item}-{arm}-v20.bbmodel'
            new=ROOT/f'blockbench/酒水扩展-v25/{item}-{arm}-v25.bbmodel'
            a=signature(json.loads(old.read_text(encoding='utf-8')))
            b=signature(json.loads(new.read_text(encoding='utf-8')))
            expected=copy.deepcopy(a)
            if item in RESTORED:
                with zipfile.ZipFile(V20) as reference:
                    source=json.loads(reference.read(PREFIX+'models/'+p['bodyModel']+'.json'))
                assert len(source['elements'])==len(expected)
                for original,element in zip(source['elements'],expected):
                    if original.get('name','').startswith('cube_outline_down_') and original['from'][1]==original['to'][1] and 'up' not in original['faces']:
                        down=element['faces']['down'];up=element['faces']['up']
                        assert down['texture'] is not None and up['texture'] is None
                        face=copy.deepcopy(down);u0,v0,u1,v1=face['uv'];face['uv']=[u0,v1,u1,v0];element['faces']['up']=face
            if item=='brandy_xo':
                scale=.7625*p['scale']
                for i,axis,old_face,new_face in [(35,2,'north','south'),(38,0,'west','east')]:
                    e=copy.deepcopy(a[i]);e['name']='bottle_'+str(len(expected))
                    f=copy.deepcopy(e['faces'][old_face]);u0,v0,u1,v1=f['uv'];f['uv']=[u1,v0,u0,v1]
                    e['faces']={d:dict(uv=[0,0,0,0],texture=None) for d in ['north','east','south','west','up','down']};e['faces'][new_face]=f
                    e['from'][1]-=.1*scale;e['from'][axis]+=.01*scale;e['to'][axis]+=.01*scale
                    expected.append(e)
            def same(a,b):
                if isinstance(a,(int,float)) and isinstance(b,(int,float)):return math.isclose(a,b,rel_tol=0,abs_tol=1e-8)
                if isinstance(a,dict):return isinstance(b,dict) and a.keys()==b.keys() and all(same(a[k],b[k]) for k in a)
                if isinstance(a,list):return isinstance(b,list) and len(a)==len(b) and all(same(x,y) for x,y in zip(a,b))
                return a==b
            assert same(expected,b),(item,arm,'Only documented added viewing sides may differ from v20')
            report.append(dict(item=item,arm=arm,reference=str(old),reference_sha256=hashlib.sha256(old.read_bytes()).hexdigest(),body_elements=len(b),v20_original_faces_preserved=True,supplemented_viewing_faces=item in RESTORED))
    assert len(report)==38
    (ROOT/'model-reference-v20.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('38 wine Blockbench bodies preserve v20 geometry and textures, with verified top viewing faces and two XO label backs.')
    return report
