"""Animation-owned copies of Brewery/Vinery containers; never edits the source mods."""
from pathlib import Path
from copy import deepcopy
import json,zipfile,hashlib,io
from PIL import Image
from research_letsdo import ROOT,TARGETS,JARS,model,source
from prepare_cocktail_profiles import corners,dump,READ
from container_assets import bounds
from letsdo_bottles import fixed_bottle
from letsdo_liquids import make_dynamic

ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
LIQUIDS={'clark_wine':[2],'jellie_wine':[2,3],'jo_special_mixture':[3],
 'strad_wine':[3,4],'stal_wine':[3,4],'magnetic_wine':[3,4],'chenet_wine':[3,4],
 'lilitu_wine':[9,10,11,12],'whiskey_lilitusinglemalt':[5,6,7,8],'apple_wine':[5],
 'mead':[1],'apple_cider':[0,1]}
NECKS={'whiskey_highland_hearth':(1,15),'whiskey_jamesons_malt':(2,12),
 'whiskey_maggoallan':(2,11),'whiskey_smokey_reverie':(0,13),
 'whiskey_carrasconlabel':(2,15),'whiskey_ak':(1,13),'whiskey_jojannik':(0,14),
 'whiskey_lilitusinglemalt':(0,15.5),'whiskey_cristelwalker':(1,13),
 'clark_wine':(0,14),'creepers_crush':(0,12),'jellie_wine':(4,15),
 'jo_special_mixture':(4,9),'mellohi_wine':(0,9),'strad_wine':(0,15),'stal_wine':(0,17),
 'lilitu_wine':(1,15.5),'kelp_cider':(0,11),'mead':(0,14),'glowing_wine':(1,15),
 'magnetic_wine':(0,13),'eiswein':(0,14),'apple_wine':(1,12.2),'apple_cider':(5,15),
 'aegis_wine':(2,15),'chenet_wine':(0,13),'cristel_wine':(0,17.9),
 'solaris_wine':(0,13),'chorus_wine':(1,12)}
SCREW={'jellie_wine','jo_special_mixture'}
STOPPERS=set(('lilitu_wine stal_wine strad_wine mellohi_wine magnetic_wine apple_wine creepers_crush mead '
 'whiskey_maggoallan whiskey_highland_hearth chenet_wine aegis_wine cristel_wine chorus_wine solaris_wine '
 'kelp_cider apple_cider eiswein').split())
FOAM={'beer_wheat','beer_barley','beer_hops','beer_nettle','beer_haley','beer_oat'}

def main():
 ids={ns+':'+n for ns,names in TARGETS.items() for n in names}
 profiles=[p for p in READ(ASSETS/'drink_profiles.json') if p['id'] not in ids]
 closures={};foam_definitions={}
 masks=READ(ASSETS/'hand_volumes.json');decorations=READ(ASSETS/'decorations.json');details=[]
 for ns,names in TARGETS.items():
  with zipfile.ZipFile(JARS[ns]) as z:
   for name in names:
    item=ns+':'+name;print('Preparing',item);loc=source(z,ns,name);m=model(z,loc);els=deepcopy(m['elements'])
    folder=f'letsdo/{ns}/{name}';base='animation/'+folder;textures={};images={}
    for key,ref in m['textures'].items():
     if ref.startswith('#'):ref=m['textures'][ref[1:]]
     tns,rel=ref.split(':')
     if tns=='minecraft':textures[key]=ref;continue
     raw=z.read(f'assets/{tns}/textures/{rel}.png');images[key]=Image.open(io.BytesIO(raw)).convert('RGBA')
     p=ASSETS/f'textures/{folder}_{key}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(raw)
     textures[key]='farmingtales_drink_anim:'+folder+'_'+key
    textures.setdefault('particle',next(iter(textures.values())))
    for i,e in enumerate(els):
     e['name']='source_'+str(i)
     # A few source models include deliberately absent, covered underside faces.
     e['faces']={f:v for f,v in e['faces'].items() if v['texture'][1:] in textures}
     for f in e['faces'].values():f.pop('cullface',None)
    def outmodel(elements):return dict(textures=textures,render_type='minecraft:cutout',elements=elements)
    beer=name.startswith('beer_');removed=LIQUIDS.get(name,[]);cap=[];neck=[]
    if beer:
     removed=[4]+([5] if name=='beer_hops' else [])
     handles=[i for i,e in enumerate(els) if e['from'][2]==e['to'][2] and min(e['from'][0],e['to'][0])>=11]
     assert len(handles)==1,(item,'Expected the source mug handle')
     body=[e for i,e in enumerate(els) if i not in removed+handles]
     floor=deepcopy(els[4]);floor['name']='mug_inner_floor';floor['to'][1]=.45
     floor['faces']['up']=deepcopy(els[1]['faces']['down']);body.append(floor)
     cap_pivot=[8,8,8];mouth=8;neck_min=neck_max=8
     boxes=[([6.025,.46,6.025],[9.975,7.99,9.975])]
     # Keep the original image and face UVs: resampling a cropped tile changes
     # its pixel boundaries, and whitening it loses the beer's source colors.
     top=els[4]['faces']['up']
     atlas=images[top['texture'][1:]].copy();suv=tuv=top['uv']
     scale=.94;width=depth=6;cup=dict(gripY=3.5,radius=2.9,liquidYaw=0)
     if name in FOAM:
      head=els[5] if name=='beer_hops' else els[4]
      foam_top=head['faces']['up'];foam_side=head['faces']['north'] if name=='beer_hops' else foam_top
      fa=images[foam_top['texture'][1:]].copy();fs=foam_side['uv'];ft=foam_top['uv']
      fa.save(ASSETS/f'textures/{folder}_foam.png')
      foam_definitions[item]=dict(texture='farmingtales_drink_anim:textures/'+folder+'_foam.png',sideUV=fs,surfaceUV=ft)
    else:
     cap_index,cut=NECKS.get(name,(0,14));a,b=bounds(els[cap_index])
     if name in SCREW:cut=a[1]
     closures[item]='screw' if name in SCREW else 'stopper' if name in STOPPERS else 'wine'
     assert a[1]<=cut<b[1],(item,a,b,cut)
     body,cap=fixed_bottle(els,cap_index,cut,name in SCREW,images)
     cap_pivot=[(a[0]+b[0])/2,b[1],(a[2]+b[2])/2]
     mouth=neck_min=neck_max=cut
     boxes=[];removed=[]
     if name=='whiskey_highland_hearth':
      # This broad, clear bottle gets the requested moving source-colored whisky.
      boxes,atlas,suv,tuv,_=make_dynamic(name,els,body,cap_index,cut,images,textures,folder,ASSETS)
     pts=[v for q in body for v in corners(q)];height=max(v[1] for v in pts+[cap_pivot])
     grips=[bounds(q) for q in body if bounds(q)[0][1]<=4.8<=bounds(q)[1][1] and all(y-x>.1 for x,y in zip(*bounds(q)))]
     width=max((b[0]-a[0] for a,b in grips),default=4);depth=max((b[2]-a[2] for a,b in grips),default=4)
     scale=round(min(1.04,15.5/height,7.1/max(width,depth)),3);cup=None
    if boxes:atlas.save(ASSETS/f'textures/{folder}_liquid.png')
    regions=[dict(min=[(a[0]-8)/16,a[1]/16,(a[2]-8)/16],max=[(b[0]-8)/16,b[1]/16,(b[2]-8)/16],sideUV=suv,surfaceUV=tuv,texture='farmingtales_drink_anim:textures/'+folder+'_liquid.png') for a,b in boxes]
    for suffix,part in [('_body',body),('_cap',cap),('_neck',neck)]:dump(ASSETS/f'models/{base+suffix}.json',outmodel(part))
    pts=[v for q in body+cap for v in corners(q)]
    p=dict(id=item,style='CUP' if beer else 'WINE',scale=scale,width=width,depth=depth,capPivot=cap_pivot,neckJoin=0,neckMin=neck_min/16,neckMax=neck_max/16,mouthY=mouth,
     bodyModel=base+'_body',capModel=base+'_cap',neckModel=base+'_neck',boundsMin=[min(v[j] for v in pts) for j in range(3)],boundsMax=[max(v[j] for v in pts) for j in range(3)],liquid=regions,canLid=None,cupShape=cup)
    profiles.append(p);decorations[item]=[];masks[item]=[]
    if beer:
     # End at the actual rim; the old extra height erased the visible wrist.
     masks[item].append(dict(min=[5,0,5],max=[11,8,11],origin=[0,0,0],axis='y',angle=0,rescale=False,shiftY=0))
    for q in body:
     a,b=bounds(q);r=q.get('rotation',{})
     if all(b[i]-a[i]>.01 for i in range(3)):masks[item].append(dict(min=a,max=b,origin=r.get('origin',[0,0,0]),axis=r.get('axis','y'),angle=r.get('angle',0),rescale=r.get('rescale',False),shiftY=0))
    fixture=ROOT/'src/smoke/resources/assets'/ns
    fixture_model='farmingtales_drink_anim:'+base+'_body'
    if beer:
     # The placed test mug is the untouched source geometry, for a real visual
     # comparison with the animated held mug rather than another generated body.
     original=deepcopy(m);original['textures']=textures;original['render_type']='minecraft:cutout'
     dump(fixture/f'models/block/{name}.json',original);fixture_model=ns+':block/'+name
    dump(fixture/f'models/item/{name}.json',{'parent':fixture_model})
    dump(fixture/f'blockstates/{name}.json',{'variants':{'':{'model':fixture_model}}})
    details.append(dict(id=item,source=loc,removed_liquid=removed,cap=None if beer else dict(element=cap_index,cut=cut,opening=closures[item]),foam=name in FOAM))
 dump(ASSETS/'beer_foam.json',dict(items=list(foam_definitions),variants=foam_definitions))
 dump(ASSETS/'letsdo_closures.json',closures)
 dump(ASSETS/'drink_profiles.json',profiles);dump(ASSETS/'hand_volumes.json',masks);dump(ASSETS/'decorations.json',decorations)
 atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=READ(atlas_path)
 atlas['sources']=[s for s in atlas['sources'] if ':letsdo/' not in s.get('resource','')]
 for p in sorted((ASSETS/'textures/letsdo').rglob('*.png')):atlas['sources'].append(dict(type='minecraft:single',resource='farmingtales_drink_anim:'+p.relative_to(ASSETS/'textures').with_suffix('').as_posix()))
 dump(atlas_path,atlas);dump(ROOT/'letsdo-sources.json',dict(jars=[dict(path=str(p),sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in JARS.values()],items=details))
 from prepare_beer_overflow import paint
 paint()
 print('Added',len(details),'containers; total',len(profiles))
if __name__=='__main__':main()



