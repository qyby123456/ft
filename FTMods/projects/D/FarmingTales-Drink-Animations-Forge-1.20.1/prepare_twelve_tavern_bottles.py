"""Add twelve source-shaped opaque bottles, including two explicitly screw-open lids."""
from copy import deepcopy
from pathlib import Path
import hashlib,io,json,zipfile
from PIL import Image
from container_assets import bounds,face_piece,subtract,FACE_AXIS
from letsdo_bottles import fixed_bottle,cut_y
from prepare_cocktail_profiles import corners,dump,READ

ROOT=Path(__file__).resolve().parent
PACK=ROOT.parents[2]
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
SPECS={
    'kaleidoscope_tavern:carignan':(5,9),
    'kaleidoscope_tavern:mother_snow':(1,12.5),
    'kaleidoscope_tavern:miners_star':(0,13),
    'kaleidoscope_tavern:rum':(0,12),
    'kaleidoscope_tavern:riesling_dry_white':(0,13),
    'kaleidoscope_tavern:madame_shexiang':(4,14),
    'kaleidoscope_world_liquor:bamboo_leaf_green_liquor':(2,11),
    'kaleidoscope_tavern:sunset_glow':(1,11),
    'kaleidoscope_tavern:sweet_berry_wine':(0,13),
    'kaleidoscope_tavern:vodka':(3,13),
    'kaleidoscope_world_liquor:maotai':(2,11),
    'kaleidoscope_tavern:brandy':(6,10),
}
SCREW={'kaleidoscope_tavern:carignan','kaleidoscope_tavern:mother_snow'}

def clear_cavity(body,cut):
    """Remove horizontal faces crossing the open shaft, including its floor plane."""
    floor=next(e for e in body if e.get('name')=='mouth_interior_floor')
    a,b=bounds(floor);opening=(a[0],a[2],b[0],b[2]);result=[]
    for e in body:
        if e is floor or e.get('rotation',{}).get('angle',0):
            result.append(e);continue
        lo,hi=bounds(e);pieces=[]
        for face,height in [('up',hi[1]),('down',lo[1])]:
            if face not in e['faces'] or not a[1]-1e-7<=height<=cut+1e-7:continue
            rect=(lo[0],lo[2],hi[0],hi[2]);remaining=subtract(rect,opening)
            if remaining==[rect]:continue
            source=deepcopy(e);e['faces'].pop(face)
            pieces.extend(face_piece(source,face,r,f'_cavity_{i}') for i,r in enumerate(remaining))
        if e['faces']:result.append(e)
        result.extend(pieces)
    return result

def main():
    jars={ns:next(p for p in (PACK/'mods').glob('*.jar') if token in p.name)
          for ns,token in [('kaleidoscope_tavern','kaleidoscopetavern-'),
                          ('kaleidoscope_world_liquor','kaleidoscope_world_liquor-')]}
    profiles=[p for p in READ(ASSETS/'drink_profiles.json') if p['id'] not in SPECS]
    masks=READ(ASSETS/'hand_volumes.json');closures=READ(ASSETS/'letsdo_closures.json')
    decorations=READ(ASSETS/'decorations.json');details=[];sources={};atlas_additions=[]
    for item,(index,cut) in SPECS.items():
        ns,name=item.split(':');folder=f'tavern_bottles/{name}';base='animation/'+folder
        source=f'assets/{ns}/models/block/brew/drink/{name}/count1.json'
        with zipfile.ZipFile(jars[ns]) as z:
            def read(entry):
                raw=z.read(entry);sources[entry]=dict(jar=str(jars[ns]),entry=entry,sha256=hashlib.sha256(raw).hexdigest());return raw
            original=json.loads(read(source));elements=deepcopy(original['elements']);textures={};images={}
            for key,ref in original['textures'].items():
                if key=='particle':continue
                tns,path=ref.split(':');entry=f'assets/{tns}/textures/{path}.png';raw=read(entry)
                target=ASSETS/f'textures/{folder}_{key}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
                images[key]=Image.open(io.BytesIO(raw)).convert('RGBA');textures[key]='farmingtales_drink_anim:'+folder+'_'+key
                atlas_additions.append(dict(type='minecraft:single',resource=textures[key]))
                if entry+'.mcmeta' in z.namelist():Path(str(target)+'.mcmeta').write_bytes(read(entry+'.mcmeta'))
            textures['particle']=next(iter(textures.values()))
        for i,e in enumerate(elements):
            e['name']=f'source_{i}'
            for face in e['faces'].values():face.pop('cullface',None)
        fixture=deepcopy(elements);cap_source=deepcopy(elements[index]);a,b=bounds(cap_source)
        assert a[1]<=cut<b[1],item
        # Source ribbons include zero-area edge faces; they are not surfaces.
        for e in elements:
            lo,hi=bounds(e)
            e['faces']={f:s for f,s in e['faces'].items()
                        if all(hi[j]-lo[j]>1e-7 for j in range(3) if j!=FACE_AXIS[f][0])}
        # Snow Mother's narrow glass neck projects 0.5 into its larger cap.
        # Only that covered portion is trimmed; the two original collars stay.
        if name=='mother_snow':elements[5]=cut_y(elements[5],7,cut)
        # These underside faces are buried inside the adjacent shoulder, and
        # overlap an existing underside at exactly the same depth.
        if name=='madame_shexiang':elements[3]['faces'].pop('down',None)
        if name=='sunset_glow':elements[2]['faces'].pop('down',None)
        # Sunset Glow's decorative head is rotated 45 degrees. Its buried shank
        # and the opening fit the upright 2x2 neck, not the rotated head's box.
        if name=='sunset_glow':
            elements[index]['from']=[7,cut,7];elements[index]['to']=[9,b[1],9]
            elements[index].pop('rotation',None)
        body,cap=fixed_bottle(elements,index,cut,item in SCREW,images)
        if name=='sunset_glow':
            cap[0]=deepcopy(cap_source);cap[0]['name']='removable_cork'
        # Separate overhanging stoppers have a narrower shank inside the neck.
        if name=='vodka':
            # The wider head overhangs the neck, so its original underside is
            # visible from below even while seated. Keep that closing face.
            cap[0]['faces']['down']=deepcopy(cap_source['faces']['down'])
            for e in cap[1:]:
                e['from'][0]=e['from'][2]=6.75;e['to'][0]=e['to'][2]=9.25
        body=clear_cavity(body,cut)
        decorations[item]=[]
        def model(part):return dict(textures=textures,render_type='minecraft:cutout',elements=part)
        if name=='bamboo_leaf_green_liquor':
            leaf=next(e for e in body if e['name']=='source_0');body.remove(leaf)
            # Ignore the source plane's degenerate edges, retain both visible sides.
            leaf['faces']={f:s for f,s in leaf['faces'].items() if f in ('north','south')}
            leaf_model=base+'_leaf';dump(ASSETS/f'models/{leaf_model}.json',model([leaf]))
            points=list(corners(leaf));anchor=deepcopy(leaf);anchor['from']=anchor['to']=[8.29289,14,9.70711]
            pivot=next(corners(anchor));extent=[(max(v[j] for v in points)-min(v[j] for v in points))/2 for j in range(3)]
            decorations[item]=[dict(name='leaf',model=leaf_model,kind='LEAF',pivot=pivot,extent=extent,capAttached=True)]
        for part,mesh in [('body',body),('cap',cap),('neck',[])]:dump(ASSETS/f'models/{base}_{part}.json',model(mesh))
        points=[v for e in fixture for v in corners(e)];height=max(v[1] for v in points)
        grips=[bounds(e) for e in body if bounds(e)[0][1]<=4.8<=bounds(e)[1][1] and all(y-x>.01 for x,y in zip(*bounds(e)))]
        width=max(hi[0] for lo,hi in grips)-min(lo[0] for lo,hi in grips)
        depth=max(hi[2] for lo,hi in grips)-min(lo[2] for lo,hi in grips)
        scale=round(min(1.04,15.5/height,7.1/max(width,depth)),3)
        profiles.append(dict(id=item,style='WINE',scale=scale,width=width,depth=depth,
            capPivot=[(a[0]+b[0])/2,b[1],(a[2]+b[2])/2],neckJoin=0,neckMin=cut/16,neckMax=cut/16,mouthY=cut,
            bodyModel=base+'_body',capModel=base+'_cap',neckModel=base+'_neck',
            boundsMin=[min(v[j] for v in points) for j in range(3)],boundsMax=[max(v[j] for v in points) for j in range(3)],
            liquid=[],canLid=None,cupShape=None))
        closures[item]='screw' if item in SCREW else 'wine';masks[item]=[]
        for e in body:
            lo,hi=bounds(e);rotation=e.get('rotation',{})
            if all(y-x>.01 for x,y in zip(lo,hi)):
                masks[item].append(dict(min=lo,max=hi,origin=rotation.get('origin',[0,0,0]),axis=rotation.get('axis','y'),
                    angle=rotation.get('angle',0),rescale=rotation.get('rescale',False),shiftY=0))
        fixture_path=ROOT/f'src/smoke/resources/assets/{ns}'
        dump(fixture_path/f'models/block/{name}.json',model(fixture))
        dump(fixture_path/f'models/item/{name}.json',dict(parent=ns+':block/'+name))
        dump(fixture_path/f'blockstates/{name}.json',{'variants':{'':{'model':ns+':block/'+name}}})
        details.append(dict(id=item,source=source,cap_element=index,cut=cut,opening=closures[item],dynamic_liquid=False))
        print(item,closures[item],len(body),len(cap),'scale',scale)
    for file,value in [('drink_profiles.json',profiles),('hand_volumes.json',masks),('letsdo_closures.json',closures),('decorations.json',decorations)]:dump(ASSETS/file,value)
    atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=READ(atlas_path)
    for source in atlas_additions:
        if source not in atlas['sources']:atlas['sources'].append(source)
    dump(atlas_path,atlas);dump(ROOT/'twelve-tavern-bottle-sources.json',dict(items=details,sources=list(sources.values())))
    print('Total profiles:',len(profiles))

if __name__=='__main__':main()
