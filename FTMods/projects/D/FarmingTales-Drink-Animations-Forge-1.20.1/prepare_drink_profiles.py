"""Copy the pack's actual container parts and describe their attachment/liquid bounds."""
from pathlib import Path
from copy import deepcopy
import hashlib, json, math
from PIL import Image
from container_assets import hollow_shell,can_parts,split_beer_cap
from liquid_textures import liquid_atlas

ROOT=Path(__file__).absolute().parent
PACK=Path('Z:/kubejs/assets/farmingtales')
DEST=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
NAMES='orange_wine mysticfruit_wine sweet_grape_wine pumpkin_wine ancient_fruit_wine sweet_berry_wine watermelon_wine sakura_wine wheatbeer cherry_liqueur light_beer red_apple_wine starfruit_wine blueberry_wine pomegranate_wine strawberry_wine rum coconut_wine brandy_xo mozhao_p mozhao purified_water vitamin_drink sweet_orange_juice'.split()
SPECIAL={
 'rum':dict(cap=0,liquid=[1,2],scale=.92,width=6,depth=5),
 'coconut_wine':dict(cap=0,liquid=[1,2,3],scale=.98,width=6,depth=4),
 'brandy_xo':dict(cap=5,liquid=[0,1,2,3],scale=.80,width=10,depth=5,style='BRANDY'),
 'mozhao':dict(cap=3,liquid=[],scale=.90,width=6.3,depth=6.3,style='CAN'),
 'mozhao_p':dict(cap=3,liquid=[],scale=.90,width=6.3,depth=6.3,style='PINK_CAN'),
 'purified_water':dict(cap=0,liquid=[2,3,4,5],scale=1.04,width=4,depth=4,style='WATER'),
 'vitamin_drink':dict(cap=0,liquid=list(range(1,9)),scale=.91,width=6,depth=6,style='SPORTS'),
 'sweet_orange_juice':dict(cap=0,liquid=[2,3,4],scale=.91,width=6,depth=6,style='JUICE'),
}
profiles=[];sources=[]

def read(path):
 data=path.read_bytes();sources.append(dict(path=str(path),sha256=hashlib.sha256(data).hexdigest()));return data

def dump(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def bounds(el):return [min(a,b) for a,b in zip(el['from'],el['to'])],[max(a,b) for a,b in zip(el['from'],el['to'])]

def paint_region(im,uv,color,alpha):
 x0,x1=sorted([round(uv[0]*im.width/16),round(uv[2]*im.width/16)])
 y0,y1=sorted([round(uv[1]*im.height/16),round(uv[3]*im.height/16)])
 for y in range(max(0,y0),min(im.height,y1)):
  for x in range(max(0,x0),min(im.width,x1)):
   original=im.getpixel((x,y));shade=.88+.12*((x+y)%3)/2
   im.putpixel((x,y),tuple(round(c*shade) for c in color)+(alpha if original[3]>0 else 0,))

for name in NAMES:
 model=json.loads(read(PACK/f'models/block/drinks/{name}.json').decode('utf-8-sig'))
 info=SPECIAL.get(name,dict(cap=1,liquid=([4,5] if name in ['mysticfruit_wine','cherry_liqueur'] else [4,6] if name in ['orange_wine','red_apple_wine'] else [] if name in ['light_beer','wheatbeer'] else [4,5]),scale=1,width=4,depth=4))
 style='BEER' if name in ['light_beer','wheatbeer'] else info.get('style','WINE');wine=style=='WINE';can=style in ['CAN','PINK_CAN']
 cap=deepcopy(model['elements'][info['cap']]);fixed_neck=None
 if style=='BEER':fixed_neck,cap=split_beer_cap(cap)
 lo,hi=bounds(cap)
 texture_map={};images={}
 for key,value in model['textures'].items():
  if key=='particle':continue
  texture=value.split(':')[1];data=read(PACK/('textures/'+texture+'.png'))
  target=f'textures/drinks/{name}_{key}.png';(DEST/target).parent.mkdir(parents=True,exist_ok=True);(DEST/target).write_bytes(data)
  texture_map[key]='farmingtales_drink_anim:'+target[9:-4]
  images[key]=Image.open(PACK/('textures/'+texture+'.png')).convert('RGBA')
 template={k:deepcopy(v) for k,v in model.items() if k in ['textures','elements','ambientocclusion']}
 template['textures']=dict(texture_map,particle=next(iter(texture_map.values())))
 template['render_type']='minecraft:cutout'
 body=deepcopy(template);body['elements']=[e for i,e in enumerate(model['elements']) if i not in info['liquid']+[info['cap']]]
 body=hollow_shell(name,model,body)
 if fixed_neck:body['elements'].append(fixed_neck)
 can_lid=None
 if can:
  body,flap,can_lid=can_parts(template,model)
  can_lid['flapModel']=f'animation/drinks/{name}_flap'
  dump(DEST/f"models/{can_lid['flapModel']}.json",flap)
 closure=deepcopy(template);closure['elements']=[cap]
 neck=deepcopy(closure);neck['elements']=[cap] if wine or style=='BEER' else [];neck['render_type']='minecraft:cutout' if style=='BEER' else 'minecraft:translucent'
 join=-.075/16 if wine and lo[1] in [10.1,11.1] else 0
 # The tab pivots at the rivet before the helper hand removes it.
 cap_pivot=[8,16,8] if name=='orange_wine' else [(lo[0]+hi[0])/2,hi[1],(lo[2]+hi[2])/2]
 if can:cap_pivot=[(lo[0]+hi[0])/2,lo[1],hi[2]]
 if wine or style=='BEER':
  capkey=next(iter(cap['faces'].values()))['texture'][1:]
  im=images[capkey].copy()
  # Sample an existing glass side near the mouth, keeping each drink's own color.
  neck_candidates=[e for i,e in enumerate(model['elements']) if i!=info['cap'] and i not in info['liquid'] and 'outline' in e.get('name','')]
  sample_el=fixed_neck if fixed_neck else max(neck_candidates,key=lambda e:max(e['from'][1],e['to'][1])) if neck_candidates else model['elements'][0]
  uv=sample_el['faces']['north']['uv'];src=images[sample_el['faces']['north']['texture'][1:]]
  color=src.getpixel((min(src.width-1,int((uv[0]+uv[2])/2*src.width/16)),min(src.height-1,int((uv[1]+uv[3])/2*src.height/16))))[:3]
  for face in cap['faces'].values():paint_region(im,face['uv'],color,255 if style=='BEER' else 217)
  im.save(DEST/f'textures/drinks/{name}_open.png');neck['textures'][capkey]=f'farmingtales_drink_anim:drinks/{name}_open'
 # Keep the original orange assets byte-for-byte; all previous visual adjustments remain valid.
 base=f'animation/drinks/{name}'
 body_id,cap_id,neck_id=(base+'_body',base+'_cap',base+'_neck')
 if name=='orange_wine':body_id,cap_id,neck_id='animation/orange_bottle','animation/orange_cork','animation/orange_open_neck'
 else:
  dump(DEST/f'models/{body_id}.json',body);dump(DEST/f'models/{cap_id}.json',closure);dump(DEST/f'models/{neck_id}.json',neck)
 liquid=[]
 if can:
  color=(193,143,57) if name=='mozhao' else (201,85,132)
  liquid_image=Image.new('RGBA',(16,16))
  for y in range(16):
   for x in range(16):
    shade=.92+.08*((x//4+y//4)%3)/2
    liquid_image.putpixel((x,y),tuple(round(c*shade) for c in color)+(255,))
  liquid_image.save(DEST/f'textures/drinks/{name}_liquid.png')
  liquid.append(dict(min=[(4.06-8)/16,.90/16,(6.85-8)/16],max=[(9.70-8)/16,9.91/16,(12.49-8)/16],sideUV=[0,0,16,16],surfaceUV=[0,0,16,16],texture=f'farmingtales_drink_anim:textures/drinks/{name}_liquid.png'))
 for i in info['liquid']:
  e=model['elements'][i];a,b=bounds(e)
  if any(b[j]-a[j]<.0001 for j in range(3)):continue
  uv=e['faces']['north']['uv'];top=e['faces']['up']['uv'];key=e['faces']['north']['texture'][1:]
  topkey=e['faces']['up']['texture'][1:]
  atlas,uv,top=liquid_atlas(images[key],uv,images[topkey],top)
  liquid_path=f'textures/drinks/{name}_liquid_{i}.png';atlas.save(DEST/liquid_path)
  liquid.append(dict(min=[(a[0]-8)/16,a[1]/16,(a[2]-8)/16],max=[(b[0]-8)/16,b[1]/16,(b[2]-8)/16],sideUV=uv,surfaceUV=top,texture='farmingtales_drink_anim:'+liquid_path))
 if name not in SPECIAL and liquid:
  liquid=liquid[:1];liquid[0]['max'][1]=(10.7 if name not in ['mysticfruit_wine','cherry_liqueur'] else 10.725)/16
 # Adjacent sections share a horizontal interface and a single world-level liquid plane.
 liquid.sort(key=lambda r:r['min'][1])
 for a,b in zip(liquid,liquid[1:]):
  if abs(a['max'][1]-b['min'][1])<.08:a['max'][1]=b['min'][1]
 points=[]
 for e in model['elements']:
  a,b=bounds(e);rot=e.get('rotation',{});r=math.radians(rot.get('angle',0));axis=rot.get('axis','y');origin=rot.get('origin',[8,0,8])
  for x in [a[0],b[0]]:
   for y in [a[1],b[1]]:
    for z in [a[2],b[2]]:
     v=[x-origin[0],y-origin[1],z-origin[2]];j,k={'x':(1,2),'y':(2,0),'z':(0,1)}[axis];v[j],v[k]=v[j]*math.cos(r)-v[k]*math.sin(r),v[j]*math.sin(r)+v[k]*math.cos(r);points.append([v[t]+origin[t] for t in range(3)])
 profiles.append(dict(id='farmingtales:'+name,style=style,scale=info['scale'],width=info['width'],depth=info['depth'],capPivot=cap_pivot,neckJoin=join,neckMin=lo[1]/16,neckMax=hi[1]/16,mouthY=(hi[1] if wine or style=='BEER' else lo[1]),bodyModel=body_id,capModel=cap_id,neckModel=neck_id,boundsMin=[min(v[i] for v in points) for i in range(3)],boundsMax=[max(v[i] for v in points) for i in range(3)],liquid=liquid,canLid=can_lid))
dump(DEST/'drink_profiles.json',profiles)
dump(ROOT/'drink-profile-sources.json',list({s['path']:s for s in sources}.values()))
# These models render through the vanilla block atlas, so every embedded sprite
# must be explicitly stitched. Direct liquid rendering uses the same resource
# image with its full `textures/` prefix and `.png` extension.
sprites={
    'farmingtales_drink_anim:orange_wine', 'farmingtales_drink_anim:orange_wine_open'
}
for path in (DEST/'textures/drinks').glob('*.png'):
    sprites.add('farmingtales_drink_anim:drinks/'+path.stem)
atlas=[dict(type='single',resource=sprite,sprite=sprite) for sprite in sorted(sprites)]
(ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json').write_text(
    json.dumps(dict(sources=atlas),ensure_ascii=False,separators=(',',':'))+'\n',encoding='utf-8')
print('Prepared',len(profiles),'container profiles from unchanged pack resources.')
