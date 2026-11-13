"""Animation profiles for the requested Tavern/world-liquor bottles.

Only animation-owned copies are generated; installed source mods stay unchanged.
Run this script directly: older profile generators overwrite the expanded catalog.
"""
from copy import deepcopy
from pathlib import Path
import hashlib
import io
import json
import zipfile
from PIL import Image
from container_assets import bounds, face_piece, subtract, FACE_AXIS
from liquid_textures import liquid_atlas, patch, complete
from prepare_cocktail_profiles import corners

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
ASSETS = ROOT/'src/main/resources/assets/farmingtales_drink_anim'
READ = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))


def spec(cap, liquid, shell, width=4, depth=None, scale=1, **kwargs):
    return dict(cap=cap, liquid=liquid, shell=shell, width=width,
                depth=width if depth is None else depth, scale=scale, **kwargs)


SPECS = {
    'kaleidoscope_tavern:luminous_bride': spec([5], [2, 4], [0], yaw=45, top=2),
    'kaleidoscope_world_liquor:smirnoff_red_vodka': spec([2], [3], [0, 1]),
    'kaleidoscope_world_liquor:johnnie_walker': spec([1], [2], [0]),
    'kaleidoscope_world_liquor:cool_tea': spec([2, 3], [5, 6], [0, 1], 8, 6, .84, source='cool_ice_tea'),
    'kaleidoscope_world_liquor:skyy_vodka': spec([2], [3], [0, 1]),
    'kaleidoscope_world_liquor:sour_plum': spec([2, 3], [5, 6], [0, 1], 8, 6, .84),
    'smc:ice_tea': spec([2, 3], [5, 6], [0, 1], 8, 6, .84),
    'kaleidoscope_world_liquor:absolut_vodka': spec([0], [3, 7], [4, 2, 1], 6, scale=.92),
    'kaleidoscope_world_liquor:jack_daniel': spec([2], [1], [0], 7, scale=.88),
    'kaleidoscope_world_liquor:bacardi_carta_blanca': spec([2], [3], [0, 1]),
    'kaleidoscope_world_liquor:kwas_chlebowy': spec([1], [2], [0], 6, scale=.92),
    'kaleidoscope_world_liquor:bombay_sapphire_gin': spec([3], [1], [2, 0], 6, scale=.92),
    'kaleidoscope_world_liquor:strongbow': spec([0, 6], [2, 4], [1, 3]),
    'kaleidoscope_world_liquor:spiryt_vodka': spec([2], [3], [0, 1]),
    'kaleidoscope_tavern:glowflower_brew': spec([5], [2, 4], [0], yaw=45, top=2),
    'kaleidoscope_world_liquor:dassai': spec([0], [2, 4], [1, 3]),
}

# Original ornament geometry stays attached to its closure during opening.
ORNAMENTS = {
    'kaleidoscope_world_liquor:strongbow': (6, 'LEAF', [6.58649, 15.05, 7.41492]),
    'kaleidoscope_world_liquor:dassai': (6, 'RIBBON', [7.5, 14.55, 7]),
}


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')


def opening_bounds(cap, donor, join=0):
    """One model pixel above the original neck, with no wider collar."""
    ca, cb = bounds(cap)
    da, db = bounds(donor)
    # Some original models use a single closure above a broad shoulder.
    # In that case its footprint supplies the narrow neck, not the shoulder.
    return ([max(ca[0], da[0]), db[1]-.025-join*16, max(ca[2], da[2])],
            [min(cb[0], db[0]), db[1]+1-join*16, min(cb[2], db[2])])


def fixed_neck(cap, donor, images, textures, folder, join=0):
    """Short open glass and a narrow lip, using the bottle's own pixels."""
    lo, hi = opening_bounds(cap, donor, join)
    face = donor['faces']['north']
    tile = patch(images[face['texture'][1:]], face['uv'])
    row = max(range(tile.height), key=lambda y: sum(tile.getpixel((x,y))[3] >= 128 for x in range(tile.width)))
    strip = tile.crop((0,row,tile.width,row+1))
    strip = complete(strip, strip).resize((16,16), Image.Resampling.NEAREST)
    strip.save(ASSETS/f'textures/{folder}_neck.png')
    textures['neck'] = 'farmingtales_drink_anim:'+folder+'_neck'
    f = {'uv':[0,0,16,16], 'texture':'#neck'}
    result=[]
    for direction, axis, plane in [('east',0,lo[0]),('west',0,hi[0]),('south',2,lo[2]),('north',2,hi[2])]:
        a,b=lo[:],hi[:]; a[axis]=b[axis]=plane
        result.append({'name':'open_glass_'+direction,'from':a,'to':b,'faces':{direction:deepcopy(f)}})
    rim={'name':'open_rim','from':lo,'to':hi,'faces':{'up':deepcopy(f)}}
    for i, rect in enumerate(subtract((lo[0],lo[2],hi[0],hi[2]),(lo[0]+.14,lo[2]+.14,hi[0]-.14,hi[2]-.14))):
        piece=face_piece(rim,'up',rect,str(i));piece['faces']['down']=deepcopy(piece['faces']['up']);result.append(piece)
    return result


def main():
    jars={}
    for ns, token in [('kaleidoscope_tavern','kaleidoscopetavern-'),('kaleidoscope_world_liquor','kaleidoscope_world_liquor-')]:
        jars[ns]=next(p for p in (PACK/'mods').glob('*.jar') if token in p.name)
    jars['smc']=jars['kaleidoscope_world_liquor']
    archives={ns:zipfile.ZipFile(p) for ns,p in jars.items()}
    sources={}; details=[]

    def read_asset(path):
        ns=path.split('/')[1]; raw=archives[ns].read(path)
        sources[path]={'jar':str(jars[ns]),'entry':path,'sha256':hashlib.sha256(raw).hexdigest()}
        return raw

    profiles=[p for p in READ(ASSETS/'drink_profiles.json') if p['id'] not in SPECS]
    masks=READ(ASSETS/'hand_volumes.json')
    decorations=READ(ASSETS/'decorations.json')
    for item,s in SPECS.items():
        ns,name=item.split(':');folder=f'world_bottles/{name}'
        source=f"assets/{ns}/models/block/brew/drink/{s.get('source',name)}/count1.json"
        original=json.loads(read_asset(source)); elements=deepcopy(original['elements'])
        textures={};images={}
        for key,location in original['textures'].items():
            if key=='particle':continue
            tns,rel=location.split(':');path=f'assets/{tns}/textures/{rel}.png';raw=read_asset(path)
            target=ASSETS/f'textures/{folder}_{key}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
            textures[key]='farmingtales_drink_anim:'+folder+'_'+key
            images[key]=Image.open(io.BytesIO(raw)).convert('RGBA')
            if path+'.mcmeta' in archives[tns].namelist():
                Path(str(target)+'.mcmeta').write_bytes(read_asset(path+'.mcmeta'))
        textures['particle']=next(iter(textures.values()))
        for i,e in enumerate(elements):
            e['name']=f'source_{i}'
            # Degenerate side faces on labels are not visible geometry.
            e['faces']={direction:f for direction,f in e['faces'].items()
                        if all(abs(e['to'][a]-e['from'][a])>.00001 for a in range(3) if a!=FACE_AXIS[direction][0])}
            for f in e['faces'].values():f.pop('cullface',None)
        ornament=ORNAMENTS.get(item)
        ornament_indices=[ornament[0]] if ornament else []
        cap=[deepcopy(elements[i]) for i in s['cap'] if i not in ornament_indices]
        ca,cb=bounds(cap[0]);join=-.1/16 if ns=='kaleidoscope_tavern' else 0
        body=[deepcopy(e) for i,e in enumerate(elements) if i not in s['cap']+s['liquid']+ornament_indices]
        donor=elements[s['shell'][-1]]
        na,nb=opening_bounds(cap[0],donor,join)

        # Remove only horizontal partitions covered by the adjoining bottle section.
        # Keep original inward glass and outward label winding unchanged.
        rings=[]
        for e in body:
            lo,hi=bounds(e)
            for direction in ['up','down']:
                if direction not in e['faces']:continue
                plane=e['to' if direction=='up' else 'from'][1]
                if abs(plane-hi[1])>.0001 or plane<1:continue
                rects=[(lo[0],lo[2],hi[0],hi[2])]
                for idx in s['shell']:
                    a,b=bounds(elements[idx])
                    if a[1]-.11<=plane<b[1]-.01:
                        rects=[r for rect in rects for r in subtract(rect,(a[0],a[2],b[0],b[2]))]
                if plane>=ca[1]-.15:
                    # The rotated Tavern shoulder needs an inscribed opening.
                    inset=.35 if s.get('yaw') else 0
                    hole=(na[0]+inset,na[2]+inset,nb[0]-inset,nb[2]-inset)
                    rects=[r for rect in rects for r in subtract(rect,hole)]
                if rects!=[(lo[0],lo[2],hi[0],hi[2])]:
                    rings.extend(face_piece(e,direction,rect,'_joint'+str(i)) for i,rect in enumerate(rects))
                    del e['faces'][direction]
        body.extend(rings)
        neck=fixed_neck(cap[0],donor,images,textures,folder,join)
        regions=[]
        liquids=[elements[i] for i in s['liquid'] if all(b-a>.001 for a,b in zip(*bounds(elements[i])))]
        for i,e in enumerate(liquids):
            a,b=bounds(e); side=e['faces']['north'];top=elements[s['top']]['faces']['up'] if 'top' in s else e['faces']['up']
            atlas,suv,tuv=liquid_atlas(images[side['texture'][1:]],side['uv'],images[top['texture'][1:]],top['uv'])
            atlas.save(ASSETS/f'textures/{folder}_liquid_{i}.png')
            # Shared bands keep one liquid surface; capacity extends to the glass shoulder.
            if i+1<len(liquids):b[1]=bounds(liquids[i+1])[0][1]
            else:
                enclosing=[bounds(elements[j])[1][1] for j in s['shell'] if bounds(elements[j])[0][1]<=a[1]+.01]
                b[1]=max(b[1],max(enclosing,default=b[1])-.18)
            regions.append(dict(min=[(a[0]-8)/16,a[1]/16,(a[2]-8)/16],max=[(b[0]-8)/16,b[1]/16,(b[2]-8)/16],
                                sideUV=suv,surfaceUV=tuv,texture=f'farmingtales_drink_anim:textures/{folder}_liquid_{i}.png'))
        base='animation/'+folder
        def model(els):
            # A fully removed partition must not leave a zero-face model element.
            return dict(textures=textures,render_type='minecraft:cutout',elements=[e for e in els if e['faces']])
        for suffix,part in [('body',body),('cap',cap),('neck',neck)]:dump(ASSETS/f'models/{base}_{suffix}.json',model(part))
        if ornament:
            idx,kind,root=ornament
            element=elements[idx]
            pivot=next(corners(dict(element,**{'from':root,'to':root})))
            points=list(corners(element))
            extent=[max(abs(v[j]-pivot[j]) for v in points) for j in range(3)]
            part_model=base+'_'+kind.lower()
            dump(ASSETS/f'models/{part_model}.json',model([element]))
            decorations[item]=[dict(name=kind.lower(),model=part_model,kind=kind,
                                    pivot=pivot,extent=extent,capAttached=True)]
        pts=[v for e in elements for v in corners(e)]
        profiles.append(dict(id=item,style='WINE',scale=s['scale'],width=s['width'],depth=s['depth'],
            capPivot=[(ca[0]+cb[0])/2,cb[1],(ca[2]+cb[2])/2],neckJoin=join,neckMin=na[1]/16,neckMax=nb[1]/16,mouthY=nb[1]+join*16,
            bodyModel=base+'_body',capModel=base+'_cap',neckModel=base+'_neck',boundsMin=[min(v[j] for v in pts) for j in range(3)],
            boundsMax=[max(v[j] for v in pts) for j in range(3)],liquid=regions,canLid=None,
            cupShape=dict(gripY=0,radius=0,liquidYaw=s['yaw']) if s.get('yaw') else None))
        masks[item]=[]
        for idx in s['shell']:
            e=elements[idx];a,b=bounds(e);r=e.get('rotation',{})
            masks[item].append(dict(min=a,max=b,origin=r.get('origin',[0,0,0]),axis=r.get('axis','y'),angle=r.get('angle',0),rescale=r.get('rescale',False),shiftY=0))
        fixture=ROOT/'src/smoke/resources/assets'/ns
        dump(fixture/f'models/block/{name}.json',model(elements))
        dump(fixture/f'models/item/{name}.json',{'parent':ns+':block/'+name})
        dump(fixture/f'blockstates/{name}.json',{'variants':{'':{'model':ns+':block/'+name}}})
        details.append(dict(id=item,source=source,cap=s['cap'],liquid=s['liquid'],shell=s['shell']))
    dump(ASSETS/'drink_profiles.json',profiles);dump(ASSETS/'hand_volumes.json',masks)
    dump(ASSETS/'decorations.json',decorations)
    atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=READ(atlas_path)
    atlas['sources']=[s for s in atlas['sources'] if ':world_bottles/' not in s.get('resource','')]
    for path in sorted((ASSETS/'textures/world_bottles').rglob('*.png')):
        atlas['sources'].append(dict(type='minecraft:single',resource='farmingtales_drink_anim:'+path.relative_to(ASSETS/'textures').with_suffix('').as_posix()))
    dump(atlas_path,atlas);dump(ROOT/'world-bottle-sources.json',dict(items=details,sources=list(sources.values())))
    print(f'Prepared {len(SPECS)} bottles; {len(profiles)} total profiles (tea lids are selected in BottleClosures).')


if __name__=='__main__':main()
