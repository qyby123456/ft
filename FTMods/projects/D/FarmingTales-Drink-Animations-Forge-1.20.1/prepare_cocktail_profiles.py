"""Independent animated cocktail copies; source JARs stay intact."""
from pathlib import Path
from copy import deepcopy
from PIL import Image
import hashlib, io, itertools, json, math, zipfile
from liquid_textures import liquid_atlas

ROOT=Path(__file__).absolute().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
PACK=ROOT.parents[2]
READ=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
def dump(p,d):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def fix_handheld_surfaces(item,body):
    body=deepcopy(body)
    if item=='kaleidoscope_world_liquor:jerk':
        # The source has two identical outward bottom faces at Y=6.05.
        body['elements']=[e for e in body['elements'] if e['name']!='original_5']
        for e in body['elements']:
            if e['name']=='original_0':
                e['faces'].pop('up',None);e['faces'].pop('down',None)
            if e['name']=='original_1':
                e['faces']={k:v for k,v in e['faces'].items() if k in ['up','down']}
    if item=='beachparty:coconut_cocktail':
        # Keep the brown outside bottom and add inward flesh below the dynamic liquid.
        bottom=next(e for e in body['elements'] if e['name']=='original_4')
        bottom['to'][1]=.95
        bottom['faces']['up']={'uv':[0,13,8,16],'texture':'#2'}
    return body

# Liquid side, liquid top, replaced liquid meshes, rim, grip, grip radius, moving groups.
def spec(side,top,remove,rim,grip,radius,groups,**kw):
    return dict(side=side,top=top,remove=remove,rim=rim,grip=grip,radius=radius,groups=groups,**kw)
T={
 'signature_cocktail':spec(0,1,[0,1,2,13,14],10.3,2.6,.5,[("STICK",[12])],tint=1),
 'godfather':spec(0,2,[0,1,2],8,3.2,2.9,[("ICE",[9]),("ICE",[10]),("ICE",[11]),("FRUIT",[12]),("LEAF",[13])]),
 'mystery_cocktail':spec(8,10,[8,9,10],10.3,2.6,.5,[("STICK",[12])]),
 'allium_garden':spec(9,10,[9,10,11],10.3,2.6,.5,[("LEAF",[12,13])]),
 'bloody_mary':spec(3,3,[3],8.95,3.6,.5,[("FRUIT",[10]),("FRUIT",[11]),("LEAF",[12]),("LEAF",[13])]),
 'mojito':spec(7,9,[7,8,9],8.89,3.4,2.35,[("STICK",[0]),("FLOATING_FRUIT",[10]),("FLOATING_FRUIT",[11]),("LEAF",[12,13]),("ICE",[14])]),
 'screwdriver':spec(6,6,[6],8.28,3.4,2.35,[("LEAF",[7]),("STICK",[8]),("FRUIT",[9])]),
 'grasshopper':spec(7,7,[7],8.95,3.6,.5,[("LEAF",[0]),("LEAF",[1]),("LEAF",[2]),("STICK",[13])]),
 'sculk_special':spec(8,10,[8,9,10],10.3,2.6,.5,[("LEAF",[13]),("LEAF",[14])]),
 'depth_charge':spec(0,5,[0,1,4],8.5,3.4,3.35,[("ICE",[7]),("LEAF",[8,9]),("LEAF",[10,11])],remove_faces={5:['up','down']}),
 'white_lady':spec(5,5,[2,5],8.95,3.6,.5,[("LEAF",[11]),("LEAF",[12]),("LEAF",[13])]),
 'emerald':spec(0,4,[0,1],8,3.2,2.9,[("ICE",[5]),("FRUIT",[6]),("LEAF",[7])],remove_faces={4:['up','down']}),
 'nether_special':spec(8,10,[8,9,10],10.3,2.6,.5,[("FRUIT",[12,13,14]),("STICK",[15])]),
 'brass_heart':spec(8,9,[8,9,10],10.3,2.6,.5,[("LEAF",[11])]),
}
W={
 'highball':spec(7,9,[7,8,9],8.89,3.4,2.35,[("FRUIT",[6]),("ICE",[10]),("LEAF",[11])]),
 'gin_tonic':spec(6,8,[6,7,8],8.89,3.4,2.35,[("ICE",[9]),("ICE",[10]),("ICE",[11]),("ICE",[12]),("FLOATING_FRUIT",[13]),("ICE",[14]),("LEAF",[15])]),
 'pine_colada':spec(8,8,[8],10.3,2.6,.5,[("LEAF",[10]),("FRUIT",[11])]),
 'long_island_iced_tea':spec(0,2,[0,1,2],8,3.2,2.9,[("ICE",[i]) for i in range(9,15)]+[("FRUIT",[15])]),
 'jerk':spec(4,4,[4],8.95,3.6,.5,[("LEAF",[10])]),
 'shrimp_cocktail':spec(4,4,[4],8.95,3.6,.5,[("FRUIT",[10,11,12])]),
 'around_the_world':spec(6,6,[6],8,3.4,2.35,[("LEAF",[7]),("STICK",[8]),("ICE",[9])]),
}
B={
 'melon_cocktail':spec(6,6,[6],12,3.9,1,[("FRUIT",[4,5])]),
 'pumpkin_cocktail':spec(5,5,[5],10.01,4,3.9,[("STICK",[2]),("FRUIT",[3,4])],tint=1),
 'cocoa_cocktail':spec(4,4,[4],12,3.9,1,[("FLOATING_FRUIT",[5])]),
 'honey_cocktail':spec(6,6,[6],13,2.6,1,[("STICK",[4]),("FRUIT",[5])],tint=1),
 'sweetberries_cocktail':spec(0,0,[0,1],12,2.7,1.9,[("LEAF",[4])],tint=1,lower=(1,[6.3,.3,6.3],[9.2,5,9.2])),
 'coconut_cocktail':spec(4,4,[],8,3.8,3.9,[("STICK",[5,6]),("STICK",[7]),("FRUIT",[8])],liquid_min=[5.03,1,5.03],liquid_max=[10.97,8,10.97],side_face='up',remove_faces={4:['up']}),
}
SPECS={**{'kaleidoscope_tavern:'+k:v for k,v in T.items()},**{'kaleidoscope_world_liquor:'+k:v for k,v in W.items()},**{'beachparty:'+k:v for k,v in B.items()}}

def corners(e):
    rot=e.get('rotation',{});origin=rot.get('origin',[0,0,0]);angle=math.radians(rot.get('angle',0))
    axis=rot.get('axis','y');j,k={'x':(1,2),'y':(2,0),'z':(0,1)}[axis]
    for point in itertools.product(*zip(e['from'],e['to'])):
        v=[point[i]-origin[i] for i in range(3)]
        if rot.get('rescale'):
            v[j]/=math.cos(angle);v[k]/=math.cos(angle)
        v[j],v[k]=v[j]*math.cos(angle)-v[k]*math.sin(angle),v[j]*math.sin(angle)+v[k]*math.cos(angle)
        yield [v[i]+origin[i] for i in range(3)]

def main(selected=None):
    specs=SPECS if selected is None else {item:SPECS[item] for item in selected}
    jars={}
    for ns,token in [('kaleidoscope_tavern','kaleidoscopetavern-'),('kaleidoscope_world_liquor','kaleidoscope_world_liquor-'),('beachparty','beachparty-forge-')]:
        jars[ns]=next(p for p in (PACK/'mods').glob('*.jar') if token in p.name)
    archives={ns:zipfile.ZipFile(p) for ns,p in jars.items()}
    sources={};details=[]
    def read_asset(path):
        ns=path.split('/')[1];raw=archives[ns].read(path)
        sources[(ns,path)]={'jar':str(jars[ns]),'entry':path,'sha256':hashlib.sha256(raw).hexdigest()}
        return raw
    profiles=[p for p in READ(ASSETS/'drink_profiles.json') if p['id'] not in specs]
    masks=READ(ASSETS/'hand_volumes.json');decorations=READ(ASSETS/'decorations.json')
    tints={k:v for k,v in READ(ASSETS/'liquid_tints.json').items() if k not in specs}
    for item,s in specs.items():
        ns,name=item.split(':');folder=f'cocktails/{ns}/{name}'
        original=f'assets/{ns}/models/block/'+(name+'_3' if ns=='beachparty' else 'mixology/'+name)+'.json'
        source=json.loads(read_asset(original));elements=source['elements'];images={};metadata={};textures={}
        for key,location in source['textures'].items():
            if key=='particle':continue
            tns,rel=location.split(':');path=f'assets/{tns}/textures/{rel}.png';raw=read_asset(path)
            target=ASSETS/f'textures/{folder}_{key}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
            images[key]=Image.open(io.BytesIO(raw)).convert('RGBA')
            textures[key]=f'farmingtales_drink_anim:{folder}_{key}'
            if path+'.mcmeta' in archives[tns].namelist():
                meta=read_asset(path+'.mcmeta');metadata[key]=json.loads(meta);Path(str(target)+'.mcmeta').write_bytes(meta)
        textures['particle']=next(iter(textures.values()))
        side=elements[s['side']]['faces'][s.get('side_face','north')]
        top=elements[s['top']]['faces'][s.get('top_face','up')]
        def liquid_texture(side,top,suffix):
            sk=side['texture'][1:];tk=top['texture'][1:];im=images[sk];tm=images[tk]
            n=im.height//im.width if sk in metadata else 1
            nt=tm.height//tm.width if tk in metadata else 1
            frames=[]
            for i in range(max(n,nt)):
                a=im.crop((0,(i%n)*im.width,im.width,(i%n+1)*im.width))
                b=tm.crop((0,(i%nt)*tm.width,tm.width,(i%nt+1)*tm.width))
                frame,suv,tuv=liquid_atlas(a,side['uv'],b,top['uv']);frames.append(frame)
            atlas=Image.new('RGBA',(32,16*len(frames)))
            for i,frame in enumerate(frames):atlas.paste(frame,(0,i*16))
            target=ASSETS/f'textures/{folder}_{suffix}.png';atlas.save(target)
            if len(frames)>1:
                meta=deepcopy(metadata.get(sk,metadata.get(tk)));meta['animation'].update(width=32,height=16)
                dump(Path(str(target)+'.mcmeta'),meta)
            return suv,tuv,f'farmingtales_drink_anim:textures/{folder}_{suffix}.png'
        suv,tuv,liquid_path=liquid_texture(side,top,'liquid')
        lo=[min(a,b) for a,b in zip(elements[s['side']]['from'],elements[s['side']]['to'])]
        hi=[max(a,b) for a,b in zip(elements[s['side']]['from'],elements[s['side']]['to'])]
        if hi[1]==lo[1]:lo[1]=1
        lo=[lo[0]+.02,lo[1],lo[2]+.02];hi=[hi[0]-.02,min(s['rim']-.01,lo[1]+(hi[1]-lo[1])/.8),hi[2]-.02]
        lo=s.get('liquid_min',lo);hi=s.get('liquid_max',hi)
        def region(lo,hi,uv1,uv2,texture):
            return dict(min=[(lo[0]-8)/16,lo[1]/16,(lo[2]-8)/16],max=[(hi[0]-8)/16,hi[1]/16,(hi[2]-8)/16],sideUV=uv1,surfaceUV=uv2,texture=texture)
        liquid=[region(lo,hi,suv,tuv,liquid_path)]
        if 'lower' in s:
            idx,a,b=s['lower'];f=elements[idx]['faces']['north'];uv1,uv2,tex=liquid_texture(f,f,'liquid_lower');liquid.insert(0,region(a,b,uv1,uv2,tex))
        moving={i for _,indices in s['groups'] for i in indices}
        spills={i for i,e in enumerate(elements) if abs(e['from'][1]-e['to'][1])<.001 and e['from'][1]<.2}
        removed=set(s['remove'])|spills
        body={'textures':textures,'render_type':'minecraft:cutout','elements':[]}
        seen=set();clean={}
        for i,original_element in enumerate(elements):
            e=deepcopy(original_element);e['name']=f'original_{i}'
            for face in s.get('remove_faces',{}).get(i,[]):e['faces'].pop(face,None)
            # Absent source faces were never visible; avoid missing-texture squares.
            e['faces']={k:f for k,f in e['faces'].items() if f['texture'][1:] in textures}
            for f in e['faces'].values():
                f.pop('cullface',None)
                if item=='kaleidoscope_tavern:signature_cocktail' and f.get('tintindex')==0:f['tintindex']=1
            clean[i]=e
            if i in removed or i in moving:continue
            signature=json.dumps({k:v for k,v in e.items() if k not in ['name','rotation']},sort_keys=True)
            if signature in seen:continue
            seen.add(signature);body['elements'].append(e)
        base='animation/'+folder;dump(ASSETS/f'models/{base}.json',fix_handheld_surfaces(item,body))
        parts=[]
        for number,(kind,indices) in enumerate(s['groups']):
            group=[clean[i] for i in indices];points=[v for e in group for v in corners(e)]
            low=[min(v[j] for v in points) for j in range(3)];high=[max(v[j] for v in points) for j in range(3)]
            pivot=[(a+b)/2 for a,b in zip(low,high)]
            if kind not in ['ICE','FLOATING_FRUIT']:
                pivot[1]=max(low[1],min(s['rim']-.35,high[1]))
            extent=[max(abs(v[j]-pivot[j]) for v in points) for j in range(3)]
            part_model=base+f'_{kind.lower()}_{number}'
            dump(ASSETS/f'models/{part_model}.json',dict(textures=textures,render_type='minecraft:cutout',elements=group))
            parts.append(dict(name=f'{kind.lower()}_{number}',model=part_model,kind=kind,pivot=pivot,extent=extent,layered=True))
        decorations[item]=parts
        # Hollow glass must not become a solid hand-clipping cube through its ornaments.
        boxes=[]
        for e in body['elements']:
            a=[min(x,y) for x,y in zip(e['from'],e['to'])];b=[max(x,y) for x,y in zip(e['from'],e['to'])];rot=e.get('rotation',{})
            if all(b[j]-a[j]>.001 for j in range(3)):
                boxes.append(dict(min=a,max=b,origin=rot.get('origin',[0,0,0]),axis=rot.get('axis','y'),angle=rot.get('angle',0),rescale=rot.get('rescale',False),shiftY=0))
        masks[item]=boxes
        points=[v for i,e in clean.items() if i not in removed for v in corners(e)]
        scale=.86 if s['radius']<=1 else .94
        if name in ['coconut_cocktail','pumpkin_cocktail']:scale=.83
        profile=dict(id=item,style='CUP',scale=scale,width=s['radius']*2,depth=s['radius']*2,capPivot=[8,s['rim'],8],neckJoin=0,
            neckMin=s['rim']/16,neckMax=s['rim']/16,mouthY=s['rim'],bodyModel=base,capModel='animation/cups/empty',neckModel='animation/cups/empty',
            boundsMin=[min(v[j] for v in points) for j in range(3)],boundsMax=[max(v[j] for v in points) for j in range(3)],liquid=liquid,canLid=None,
            cupShape=dict(gripY=s['grip'],radius=s['radius'],liquidYaw=0))
        profiles.append(profile)
        if 'tint' in s:tints[item]=s['tint']
        fixture=ROOT/'src/smoke/resources/assets'/ns
        dump(fixture/f'models/item/{name}.json',{'parent':'farmingtales_drink_anim:'+base})
        dump(fixture/f'blockstates/{name}.json',{'variants':{'':{'model':'farmingtales_drink_anim:'+base}}})
        details.append(dict(id=item,source=original,liquid=s['remove'],ground_decorations=sorted(spills),moving_parts=parts))
    dump(ASSETS/'drink_profiles.json',profiles);dump(ASSETS/'hand_volumes.json',masks);dump(ASSETS/'decorations.json',decorations)
    dump(ASSETS/'liquid_tints.json',tints)
    atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=READ(atlas_path)
    registered={s.get('resource') for s in atlas['sources']}
    for path in sorted((ASSETS/'textures/cocktails').rglob('*.png')):
        sprite='farmingtales_drink_anim:'+path.relative_to(ASSETS/'textures').with_suffix('').as_posix()
        if sprite not in registered:atlas['sources'].append(dict(type='minecraft:single',resource=sprite))
    dump(atlas_path,atlas)
    if selected is not None:
        previous=READ(ROOT/'cocktail-sources.json')
        entries={s['entry'] for s in sources.values()}
        sources_list=[s for s in previous['assets'] if s['entry'] not in entries]+list(sources.values())
        details=[d for d in previous['items'] if d['id'] not in specs]+details
    else:sources_list=list(sources.values())
    dump(ROOT/'cocktail-sources.json',dict(jars=[dict(path=str(p),sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in jars.values()],assets=sources_list,items=details))
    print('Prepared',len(specs),'cocktails;',len(profiles),'profiles.')

if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser()
    parser.add_argument('--item',action='append',choices=sorted(SPECS))
    main(parser.parse_args().item)
