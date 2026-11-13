"""Check only the twelve additions, original artwork, and unobstructed cavity geometry."""
from itertools import combinations
from copy import deepcopy
import hashlib,json,zipfile
from prepare_twelve_tavern_bottles import ROOT,PACK,ASSETS,SPECS,SCREW,READ
from container_assets import bounds,FACE_AXIS

BACKUP=ROOT/'backups/1.8.51-before-twelve-tavern-bottles'
PREFIX='src/main/resources/assets/farmingtales_drink_anim/'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()

def verify():
    profiles=READ(ASSETS/'drink_profiles.json');catalog={p['id']:p for p in profiles}
    assert len(profiles)==len(catalog)==305
    aggregates={PREFIX+f for f in ('drink_profiles.json','hand_volumes.json','letsdo_closures.json','decorations.json')}
    atlas='src/main/resources/assets/minecraft/atlases/blocks.json'
    allowed=aggregates|{atlas,'src/main/resources/META-INF/mods.toml',
        'src/test/java/com/farmingtales/drinkanim/math/DrinkProfilesTest.java','build.gradle'}
    with zipfile.ZipFile(BACKUP/'source.zip') as old:
        for path in old.namelist():
            if path not in allowed:assert old.read(path)==(ROOT/path).read_bytes(),path
        previous=json.loads(old.read(PREFIX+'drink_profiles.json'))
        assert profiles[:len(previous)]==previous
        assert {p['id'] for p in profiles[len(previous):]}==SPECS.keys()
        for file in ('hand_volumes.json','letsdo_closures.json','decorations.json'):
            before=json.loads(old.read(PREFIX+file));after=READ(ASSETS/file)
            assert after.keys()==before.keys()|SPECS.keys(),file
            for key,value in before.items():assert after[key]==value,(file,key)
        before=json.loads(old.read(atlas))['sources'];after=READ(ROOT/atlas)['sources']
        assert after[:len(before)]==before and len(after)==len(before)+12
    details=READ(ROOT/'twelve-tavern-bottle-sources.json');sources={s['entry']:s for s in details['sources']}
    closures=READ(ASSETS/'letsdo_closures.json');report={}
    for item,(index,cut) in SPECS.items():
        p=catalog[item];name=item.split(':')[1];body=READ(ASSETS/f"models/{p['bodyModel']}.json");cap=READ(ASSETS/f"models/{p['capModel']}.json")
        entry=next(s['source'] for s in details['items'] if s['id']==item)
        with zipfile.ZipFile(sources[entry]['jar']) as z:original=json.loads(z.read(entry))
        for key,location in original['textures'].items():
            if key=='particle':continue
            ns,path=location.split(':');texture=f'assets/{ns}/textures/{path}.png'
            assert sha(ASSETS/f'textures/tavern_bottles/{name}_{key}.png')==sources[texture]['sha256'],item
        assert p['liquid']==[] and p['mouthY']==cut and p['style']=='WINE'
        assert closures[item]==('screw' if item in SCREW else 'wine')
        assert len(cap['elements'])==(1 if item in SCREW else 3)
        source=original['elements'][index];a,b=bounds(source)
        assert p['capPivot']==[(a[0]+b[0])/2,b[1],(a[2]+b[2])/2]
        if a[1]==cut:
            head=cap['elements'][0]
            assert head['from']==source['from'] and head['to']==source['to'] and head.get('rotation')==source.get('rotation'),item
            for direction in ('north','east','south','west','up'):
                assert head['faces'][direction]==source['faces'][direction],(item,direction)
            if name=='vodka':assert head['faces']['down']==source['faces']['down'],'Wide cap underside must close the overhang'
        floor=next(e for e in body['elements'] if e.get('name')=='mouth_interior_floor')
        lo,hi=bounds(floor);floor_y=lo[1];opening=(lo[0],lo[2],hi[0],hi[2]);faces=[]
        for e in body['elements']:
            if e.get('rotation',{}).get('angle',0):continue
            a,b=bounds(e)
            for direction in e['faces']:
                axis,end=FACE_AXIS[direction];other=[i for i in range(3) if i!=axis]
                rect=tuple(a[i] for i in other)+tuple(b[i] for i in other);plane=(a,b)[end][axis]
                if rect[2]-rect[0]<1e-7 or rect[3]-rect[1]<1e-7:continue
                faces.append((e['name'],direction,plane,rect))
                if axis==1 and floor_y-1e-7<=plane<=cut+1e-7 and e is not floor:
                    assert not all(min(rect[i+2],opening[i+2])-max(rect[i],opening[i])>1e-7 for i in (0,1)),(item,e['name'],direction)
        for a,b in combinations(faces,2):
            if a[1]!=b[1] or abs(a[2]-b[2])>1e-7:continue
            assert not all(min(a[3][i+2],b[3][i+2])-max(a[3][i],b[3][i])>1e-7 for i in (0,1)),(item,a[:2],b[:2])
        for model in (body,cap):
            for e in model['elements']:
                for f in e['faces'].values():
                    assert 'cullface' not in f
                    ns,path=model['textures'][f['texture'][1:]].split(':')
                    assert (ROOT/f'src/main/resources/assets/{ns}/textures/{path}.png').is_file()
        report[item]=dict(opening=closures[item],mouth_y=cut,source_texture_preserved=True,cavity_clear=True,coplanar_overlaps=0)
    for s in sources.values():
        with zipfile.ZipFile(s['jar']) as z:assert hashlib.sha256(z.read(s['entry'])).hexdigest()==s['sha256']
    print('Verified 12 source textures, closure geometry, clear cavities, and all previous resources.');return report

if __name__=='__main__':verify()
