"""Fourteen original opaque bottles, using the existing Vinery cork/cavity path."""
from copy import deepcopy
from pathlib import Path
import hashlib
import io
import json
import zipfile
from PIL import Image
from container_assets import bounds, face_piece, subtract
from math import floor, ceil
from letsdo_bottles import fixed_bottle
from prepare_cocktail_profiles import corners, dump, READ

ROOT=Path(__file__).resolve().parent
PACK=ROOT.parents[2]
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
# Source neck element and the boundary below its two-pixel closure.
SPECS={
    'kaleidoscope_tavern:sauvignon_blanc_dry_white':(0,14),
    'kaleidoscope_tavern:champagne':(1,14),
    'kaleidoscope_tavern:whiskey':(1,14),
    'kaleidoscope_tavern:sakura_wine':(1,14),
    'kaleidoscope_tavern:plum_wine':(1,10),
    'kaleidoscope_tavern:wine':(1,14),
    'kaleidoscope_tavern:sherry':(0,14),
    'kaleidoscope_tavern:red_queen':(0,14),
    'kaleidoscope_world_liquor:pina_colada':(0,13),
    'kaleidoscope_tavern:polaris_sweet_white':(0,14),
    'kaleidoscope_world_liquor:lafite_1982':(1,14),
    'kaleidoscope_tavern:honey_wine':(0,14),
    'kaleidoscope_tavern:vinegar':(1,14),
    'kaleidoscope_tavern:ice_wine':(1,14),
}

MATCHED_MOUTHS={'kaleidoscope_tavern:wine','kaleidoscope_world_liquor:lafite_1982'}
MOUTH_GLASS_SAMPLES={
    # Original upper body glass, outside the label and closure UVs.
    'kaleidoscope_tavern:wine':((0,0),(1,0)),
    'kaleidoscope_world_liquor:lafite_1982':((1,0),(0,0)),
}
CAVITY_FLOOR_FIXES={'kaleidoscope_tavern:ice_wine','kaleidoscope_tavern:champagne'}


def match_mouth_glass(item, neck, cut, images):
    """Give the entire exposed neck the bottle body's own glass palette.

    Both the outer neck and its tiled inner walls use these animation texels.
    The removable closure and label have separate UV regions.
    """
    if item not in MATCHED_MOUTHS:return set()
    lo,hi=bounds(neck);changed=set()
    for direction in ['north','east','south','west']:
        face=neck['faces'][direction];key=face['texture'][1:];im=images[key]
        u,v,s,t=face['uv']
        top=v+(t-v)*(hi[1]-cut)/(hi[1]-lo[1])
        step=(t-v)/(hi[1]-lo[1])
        assert step>0 and abs(step*im.height/16-1)<1e-7,item
        row=floor(top*im.height/16+1e-7)
        left=floor(min(u,s)*im.width/16)
        right=ceil(max(u,s)*im.width/16)
        colors=[im.getpixel(point) for point in MOUTH_GLASS_SAMPLES[item]]
        assert right-left==len(colors),item
        for y in range(row,ceil(t*im.height/16)):
            for x,color in zip(range(left,right),colors):im.putpixel((x,y),color)
        changed.add(key)
    return changed


def separate_cavity_floor(item,body):
    """Cut the shoulder around a cavity whose floor lands exactly on it."""
    if item not in CAVITY_FLOOR_FIXES:return body
    floor_element=next(e for e in body if e.get('name')=='mouth_interior_floor')
    a,b=bounds(floor_element);plane=a[1];opening=(a[0],a[2],b[0],b[2])
    rings=[]
    for e in body:
        if e is floor_element:continue
        lo,hi=bounds(e)
        if abs(hi[1]-plane)<1e-7 and 'up' in e['faces']:
            source=deepcopy(e)
            e['faces'].pop('up')
            for i,rect in enumerate(subtract((lo[0],lo[2],hi[0],hi[2]),opening)):
                rings.append(face_piece(source,'up',rect,f'_cavity_{i}'))
        # This hidden neck bottom coincides with the same floor from below.
        if abs(lo[1]-plane)<1e-7 and (lo[0],lo[2],hi[0],hi[2])==opening:
            e['faces'].pop('down',None)
    return body+rings


def main():
    jars={ns:next(p for p in (PACK/'mods').glob('*.jar') if token in p.name)
          for ns,token in [('kaleidoscope_tavern','kaleidoscopetavern-'),
                           ('kaleidoscope_world_liquor','kaleidoscope_world_liquor-')]}
    profiles=[p for p in READ(ASSETS/'drink_profiles.json') if p['id'] not in SPECS]
    masks=READ(ASSETS/'hand_volumes.json')
    closures=READ(ASSETS/'letsdo_closures.json')
    details=[];sources={}
    for item,(cap_index,cut) in SPECS.items():
        ns,name=item.split(':')
        folder=f'tavern_bottles/{name}'
        base='animation/'+folder
        with zipfile.ZipFile(jars[ns]) as archive:
            def read_asset(entry):
                raw=archive.read(entry)
                sources[entry]=dict(jar=str(jars[ns]),entry=entry,sha256=hashlib.sha256(raw).hexdigest())
                return raw
            source=f'assets/{ns}/models/block/brew/drink/{name}/count1.json'
            original=json.loads(read_asset(source))
            elements=deepcopy(original['elements'])
            textures={};images={}
            for key,location in original['textures'].items():
                if key=='particle':continue
                tns,path=location.split(':');entry=f'assets/{tns}/textures/{path}.png'
                raw=read_asset(entry)
                target=ASSETS/f'textures/{folder}_{key}.png'
                target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
                textures[key]='farmingtales_drink_anim:'+folder+'_'+key
                images[key]=Image.open(io.BytesIO(raw)).convert('RGBA')
                if entry+'.mcmeta' in archive.namelist():
                    Path(str(target)+'.mcmeta').write_bytes(read_asset(entry+'.mcmeta'))
            textures['particle']=next(iter(textures.values()))
        for i,e in enumerate(elements):
            e['name']=f'source_{i}'
            for face in e['faces'].values():face.pop('cullface',None)
        a,b=bounds(elements[cap_index])
        assert a[1]<cut<b[1] and b[1]-cut==2,item
        for key in match_mouth_glass(item,elements[cap_index],cut,images):
            images[key].save(ASSETS/f'textures/{folder}_{key}.png')
        body,cap=fixed_bottle(elements,cap_index,cut,False,images)
        body=separate_cavity_floor(item,body)
        def model(part):return dict(textures=textures,render_type='minecraft:cutout',elements=part)
        for part,mesh in [('body',body),('cap',cap),('neck',[])]:
            dump(ASSETS/f'models/{base}_{part}.json',model(mesh))
        points=[v for e in elements for v in corners(e)]
        height=max(v[1] for v in points)
        grip=[bounds(e) for e in elements if bounds(e)[0][1]<=4.8<=bounds(e)[1][1]]
        width=max(hi[0]-lo[0] for lo,hi in grip)
        depth=max(hi[2]-lo[2] for lo,hi in grip)
        scale=round(min(1.04,15.5/height,7.1/max(width,depth)),3)
        profiles.append(dict(id=item,style='WINE',scale=scale,width=width,depth=depth,
            capPivot=[(a[0]+b[0])/2,b[1],(a[2]+b[2])/2],neckJoin=0,
            neckMin=cut/16,neckMax=cut/16,mouthY=cut,bodyModel=base+'_body',
            capModel=base+'_cap',neckModel=base+'_neck',
            boundsMin=[min(v[j] for v in points) for j in range(3)],
            boundsMax=[max(v[j] for v in points) for j in range(3)],
            liquid=[],canLid=None,cupShape=None))
        closures[item]='wine'
        masks[item]=[]
        for e in body:
            lo,hi=bounds(e);rotation=e.get('rotation',{})
            if all(y-x>.01 for x,y in zip(lo,hi)):
                masks[item].append(dict(min=lo,max=hi,origin=rotation.get('origin',[0,0,0]),
                    axis=rotation.get('axis','y'),angle=rotation.get('angle',0),
                    rescale=rotation.get('rescale',False),shiftY=0))
        fixture=ROOT/f'src/smoke/resources/assets/{ns}'
        dump(fixture/f'models/block/{name}.json',model(elements))
        dump(fixture/f'models/item/{name}.json',dict(parent=ns+':block/'+name))
        dump(fixture/f'blockstates/{name}.json',{'variants':{'':{'model':ns+':block/'+name}}})
        details.append(dict(id=item,source=source,cap_element=cap_index,cut=cut,
                            opening='wine',dynamic_liquid=False))
    dump(ASSETS/'drink_profiles.json',profiles)
    dump(ASSETS/'hand_volumes.json',masks)
    dump(ASSETS/'letsdo_closures.json',closures)
    atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json'
    atlas=READ(atlas_path)
    atlas['sources']=[s for s in atlas['sources'] if ':tavern_bottles/' not in s.get('resource','')]
    for path in sorted((ASSETS/'textures/tavern_bottles').glob('*.png')):
        atlas['sources'].append(dict(type='minecraft:single',resource='farmingtales_drink_anim:'+path.relative_to(ASSETS/'textures').with_suffix('').as_posix()))
    dump(atlas_path,atlas)
    dump(ROOT/'fixed-tavern-bottle-sources.json',dict(items=details,sources=list(sources.values())))
    print(f'Prepared {len(SPECS)} bottles without dynamic liquid; {len(profiles)} total profiles.')


if __name__=='__main__':main()
