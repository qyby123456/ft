"""Add independent hand-held copies of the pack's 29 cup models and liquid atlases."""
from pathlib import Path
from copy import deepcopy
import hashlib, json, math, re
from PIL import Image
from liquid_textures import liquid_atlas

ROOT=Path(__file__).absolute().parent
PACK=ROOT.parents[2]/'kubejs'
DEST=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
NAMES='orange_juice_1 red_grape_juice_1 ancient_fruit_wine_cup_1 blueberry_wine_cup_1 pomegranate_wine_cup_1 coconut_wine_cup_1 light_beer_cup_1 mysticfruit_wine_cup_1 orange_wine_cup_1 pumpkin_wine_cup red_apple_wine_cup_1 wheatbeer_cup_1 watermelon_wine_cup sweet_grape_wine_cup_1 sweet_berry_wine_cup_1 strawberry_wine_cup_1 starfruit_wine_cup_1 sakura_wine_cup_1 rum_cup_1 brandy_xo_cup_1 apple_juice_1 strawberry_juice_1 blueberry_juice_1 sweet_berry_juice_1 white_grape_juice_1 ancient_fruit_juice_1 lemon_juice_1 watermelon_juice_1 cherry_liqueur_cup_1'.split()
SPECIAL={
 'orange_juice_1':(6,[6],9,2.2,.5), 'red_grape_juice_1':(7,[7],10,2.2,.5),
 'apple_juice_1':(2,[2],8,3.8,3), 'strawberry_juice_1':(3,[3],10,4.5,2.5),
 'brandy_xo_cup_1':(4,[4],10,2.1,1), 'red_apple_wine_cup_1':(6,[6],8,3.8,3),
 'coconut_wine_cup_1':(6,[6,7,8],9,4,3), 'mysticfruit_wine_cup_1':(3,[3,5,8],7.5,3.6,2.5),
 'rum_cup_1':(3,[3,5,7],7.7,3.6,2.9),
 'light_beer_cup_1':(2,[2],7.92,3.7,2.8), 'wheatbeer_cup_1':(2,[2],7.92,3.7,2.8),
 'sakura_wine_cup_1':(0,[0],5,2.4,2.5),
 'pumpkin_wine_cup':(2,[2,3,5],6.4,3.4,3.2),
 'watermelon_wine_cup':(2,[2,3,4],6.4,3.4,3.2),
}
sources=[]; details=[]
def read(path):
    raw=path.read_bytes();sources.append({'path':str(path),'sha256':hashlib.sha256(raw).hexdigest()});return raw
def dump(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def bounds(e):return [min(a,b) for a,b in zip(e['from'],e['to'])],[max(a,b) for a,b in zip(e['from'],e['to'])]
script='\n'.join(read(p).decode('utf-8-sig') for p in (PACK/'startup_scripts/winemaking').glob('*.js'))
profiles=[p for p in json.loads((DEST/'drink_profiles.json').read_text()) if p['id'].split(':')[1] not in NAMES]
masks=json.loads((DEST/'hand_volumes.json').read_text())
for name in NAMES:
    start=re.search(r'event.create\([\'\"]farmingtales:'+name+r'[\'\"]',script).start()
    block=script[start:].split('event.create(',2)[1]
    model_id=re.search(r'\.model\([\'\"]farmingtales:([^\'\"]+)',block)[1]
    model=json.loads(read(PACK/'assets/farmingtales/models'/f'{model_id}.json').decode('utf-8-sig'))
    juice='_juice_' in name
    source,remove,rim,grip,radius=SPECIAL.get(name,(6,[6],10,2.2,.5) if juice else (7,[7],10,2.6,.5))
    e=model['elements'][source];lo,hi=bounds(e);yaw=e.get('rotation',{}).get('angle',0)
    if hi[1]==lo[1]:lo[1]=1 if name!='sakura_wine_cup_1' else 1.1
    hi[1]=min(rim-.06,lo[1]+(hi[1]-lo[1])/.8)
    lo[0]+=.025;lo[2]+=.025;hi[0]-=.025;hi[2]-=.025
    images={};textures={}
    for key,tex in model['textures'].items():
        if key=='particle':continue
        raw=read(PACK/'assets/farmingtales/textures'/(tex.split(':')[1]+'.png'))
        out=DEST/f'textures/cups/{name}_{key}.png';out.parent.mkdir(parents=True,exist_ok=True);out.write_bytes(raw)
        images[key]=Image.open(out).convert('RGBA');textures[key]=f'farmingtales_drink_anim:cups/{name}_{key}'
    side=e['faces']['north'];top=e['faces']['up']
    if e['from'][1]==e['to'][1]:side=top
    atlas,suv,tuv=liquid_atlas(images[side['texture'][1:]],side['uv'],images[top['texture'][1:]],top['uv'])
    atlas.save(DEST/f'textures/cups/{name}_liquid.png')
    body={k:deepcopy(v) for k,v in model.items() if k in ['ambientocclusion','elements']}
    body['textures']=dict(textures,particle=next(iter(textures.values())));body['render_type']='minecraft:cutout'
    body['elements']=[deepcopy(e) for i,e in enumerate(model['elements']) if i not in remove]
    if name in ['light_beer_cup_1','wheatbeer_cup_1']:
        shell=body['elements'][1];face=shell['faces']['down'];im=images[face['texture'][1:]]
        u0,v0,u1,v1=face['uv'];x0,x1=sorted((round(u0*im.width/16),round(u1*im.width/16)));y0,y1=sorted((round(v0*im.height/16),round(v1*im.height/16)))
        base_image=im.crop((x0,y0,x1,y1)).convert('RGBA')
        opaque=[(x,y,base_image.getpixel((x,y))) for y in range(base_image.height) for x in range(base_image.width) if base_image.getpixel((x,y))[3]==255]
        assert opaque
        for y in range(base_image.height):
            for x in range(base_image.width):
                if base_image.getpixel((x,y))[3]<255:base_image.putpixel((x,y),min(opaque,key=lambda p:(p[0]-x)**2+(p[1]-y)**2)[2])
        base_image.save(DEST/f'textures/cups/{name}_bottom.png')
        body['textures']['cup_bottom']=f'farmingtales_drink_anim:cups/{name}_bottom'
        shell['faces']['down']=dict(texture='#cup_bottom',uv=[0,0,16,16])
        body['elements'].append(dict(name='closed_cup_bottom',from_=[5.48,.82,5.48],to=[10.52,.9,10.52],faces={'up':dict(texture='#cup_bottom',uv=[0,0,16,16])}))
        body['elements'][-1]['from']=body['elements'][-1].pop('from_')
        inner=dict(name='inner_cup_walls',from_=[10.52,7.20,10.52],to=[5.48,.90,5.48],
            faces={side:dict(texture=face['texture'],uv=[.5,.5,4,4.875]) for side in ['north','east','south','west']})
        inner['from']=inner.pop('from_');body['elements'].append(inner)
    for i,e in enumerate(body['elements']):e['name']=e.get('name',f'cup_part_{i}')
    base=f'animation/cups/{name}'
    dump(DEST/f'models/{base}.json',body)
    points=[];boxes=[]
    for e in body['elements']:
        a,b=bounds(e);rot=e.get('rotation',{});origin=rot.get('origin',[8,0,8]);axis=rot.get('axis','y');angle=math.radians(rot.get('angle',0))
        if all(b[j]-a[j]>.001 for j in range(3)):
            boxes.append(dict(min=a,max=b,origin=origin,axis=axis,angle=rot.get('angle',0),rescale=False,shiftY=0))
        for x in [a[0],b[0]]:
            for y in [a[1],b[1]]:
                for z in [a[2],b[2]]:
                    v=[x-origin[0],y-origin[1],z-origin[2]];j,k={'x':(1,2),'y':(2,0),'z':(0,1)}[axis]
                    v[j],v[k]=v[j]*math.cos(angle)-v[k]*math.sin(angle),v[j]*math.sin(angle)+v[k]*math.cos(angle)
                    points.append([v[j]+origin[j] for j in range(3)])
    scale=.86 if radius<=1 else .94
    profiles.append(dict(id='farmingtales:'+name,style='CUP',scale=scale,width=radius*2,depth=radius*2,
        capPivot=[8,rim,8],neckJoin=0,neckMin=rim/16,neckMax=rim/16,mouthY=rim,
        bodyModel=base,capModel='animation/cups/empty',neckModel='animation/cups/empty',
        boundsMin=[min(v[j] for v in points) for j in range(3)],boundsMax=[max(v[j] for v in points) for j in range(3)],
        liquid=[dict(min=[(lo[0]-8)/16,lo[1]/16,(lo[2]-8)/16],max=[(hi[0]-8)/16,hi[1]/16,(hi[2]-8)/16],sideUV=suv,surfaceUV=tuv,texture=f'farmingtales_drink_anim:textures/cups/{name}_liquid.png')],
        canLid=None,cupShape=dict(gripY=grip,radius=radius,liquidYaw=yaw)))
    masks['farmingtales:'+name]=boxes
    details.append(dict(id='farmingtales:'+name,source=model_id,removed=remove,liquid_source=source,gripY=grip,rim=rim))
dump(DEST/'models/animation/cups/empty.json',dict(textures={},elements=[]))
from prepare_juice_garnishes import prepare as prepare_garnishes
decorations=json.loads((DEST/'decorations.json').read_text())
prepare_garnishes(profiles,masks,decorations)
dump(DEST/'decorations.json',decorations)
dump(DEST/'drink_profiles.json',profiles);dump(DEST/'hand_volumes.json',masks)
atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=json.loads(atlas_path.read_text())
atlas['sources']=[s for s in atlas['sources'] if ':cups/' not in s.get('resource','')]
for path in sorted((DEST/'textures/cups').glob('*.png')):
    sprite='farmingtales_drink_anim:cups/'+path.stem;atlas['sources'].append(dict(type='single',resource=sprite,sprite=sprite))
dump(atlas_path,atlas);dump(ROOT/'cup-profile-sources.json',list({s['path']:s for s in sources}.values()));dump(ROOT/'preview/cup-model-details.json',details)
print('Prepared',len(NAMES),'cup profiles; total',len(profiles))
