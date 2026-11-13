"""Copy original Paper Kite assets into the independent animation mod."""
from pathlib import Path
from copy import deepcopy
import hashlib,io,json,zipfile
from PIL import Image
from prepare_cocktail_profiles import corners,dump,READ,ROOT,ASSETS,PACK
from liquid_textures import liquid_atlas
from container_assets import face_piece,subtract,exterior_box
from papercraft_repairs import coffee_interior, chocolate_bite, coconut_interior

NS='papercraft_magic_decoration'
def spec(side,top,remove,lo,hi,radius,grip,groups=(),yaw=0,**kw):
    return dict(side=side,top=top,remove=remove,lo=lo,hi=hi,radius=radius,grip=grip,groups=groups,yaw=yaw,**kw)
SPECS={
 'kaleidoscope_whiskey_sour':spec(6,4,[4,6],[5.05,2.89,5.05],[10.95,7.015,10.95],3.2,2.2,[('FRUIT',[0]),('ICE',[1])],22.5),
 'ferry_whiskey':spec(1,2,[1,2],[5.05,.25,5.05],[10.95,5.99,10.95],3.45,2.8,[('STICK',[4])],serving='PINEAPPLE',prop=[3]),
 'blaze_whiskey':spec(11,8,list(range(8,14)),[6.02,1.04,6.02],[9.98,6.515,9.98],2.6,2.8,[('ICE',[1]),('LEAF',[14])],-22.5,ground=[0]),
 'land_no1':spec(5,6,[5,6],[6.02,7.03,5.695],[9.98,15.96,9.655],.5,3.7,yaw=-22.5,shift=[0,0,.325]),
 'devil_margarita':spec(6,4,list(range(2,7)),[5.03,6.03,5.03],[10.97,9.98,10.97],.5,3.3,[('FRUIT',[9,10])],22.5),
 'dionysus':spec(12,11,list(range(8,13)),[5.03,4.53,5.03],[10.97,9.48,10.97],.5,2.6,[('LEAF',[0,1]),('FRUIT',[2,5]),('STICK',[3,4])],22.5,top_face='down'),
 'long_island_popsicle_tea':spec(2,3,[2,3,4],[4.55,4.28,4.55],[11.45,7.98,11.45],.5,2.6,[('FRUIT',[9]),('ICE',[10])],-22.5,serving='POPSICLE',prop=[0,1]),
 'poison_rum':spec(10,8,list(range(6,12)),[3.52,.54,7.52],[8.48,7.015,12.48],3,2.8,[('STICK',[5])],-22.5,ground=list(range(5)),shift=[2,0,-2]),
 'lucky_cactus':spec(6,4,list(range(1,7)),[6.02,1.52,6.02],[9.98,8.515,9.98],2.5,3.2,[('FRUIT',[7])],-22.5,ground=[0]),
 'fly_whiskey':spec(4,5,list(range(2,8)),[5.52,1.08,5.52],[10.48,8.04,10.48],3,3.2,[('LEAF',[0])],22.5,ground=[1]),
 'bloody_mary':spec(2,2,[1,2],[6.02,.55,6.02],[9.98,11.49,9.98],2.6,3.5,[('FRUIT',[6]),('STICK',[7,8]),('LEAF',[9])],22.5),
 'diplomatico_coffee':spec(0,1,[0,1,2],[5.03,4.28,5.03],[10.97,7.98,10.97],.5,2.6,yaw=-22.5,serving='CHOCOLATE',prop=[7,8,9,10,11]),
 'guang_s':spec(2,2,[],[5.15,1.04,5.15],[10.85,10.9,10.85],3,3.5,yaw=22.5,can=True),
}
DISABLED_COFFEES={NS+':'+name for name in ('gold_medal_coffee','white_rabbit_mocha','nocturnal_cat_coffee')}

def main():
    jar=next(p for p in (PACK/'mods').glob('*.jar') if 'Paper_Kite' in p.name)
    profiles=[p for p in READ(ASSETS/'drink_profiles.json') if not p['id'].startswith(NS+':')]
    decorations=READ(ASSETS/'decorations.json');masks=READ(ASSETS/'hand_volumes.json');servings={};sources=[];items=[]
    for item in DISABLED_COFFEES:
        decorations.pop(item,None);masks.pop(item,None)
    with zipfile.ZipFile(jar) as archive:
        def asset(path):
            data=archive.read(path);sources.append(dict(entry=path,sha256=hashlib.sha256(data).hexdigest()));return data
        for name,s in SPECS.items():
            item=NS+':'+name;base='animation/papercraft/'+name;folder='papercraft/'+name
            source=f'assets/{NS}/models/block/drink/{name}/count1.json';m=json.loads(asset(source));els=deepcopy(m['elements'])
            textures={};images={}
            for key,location in m['textures'].items():
                if key=='particle':continue
                ns,rel=location.split(':');path=f'assets/{ns}/textures/{rel}.png';raw=asset(path)
                target=ASSETS/f'textures/{folder}_{key}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
                images[key]=Image.open(io.BytesIO(raw)).convert('RGBA');textures[key]=f'farmingtales_drink_anim:{folder}_{key}'
                if path+'.mcmeta' in archive.namelist():Path(str(target)+'.mcmeta').write_bytes(asset(path+'.mcmeta'))
            textures['particle']=next(iter(textures.values()))
            shift=s.get('shift',[0,0,0])
            for i,e in enumerate(els):
                e['name']='original_'+str(i)
                for field in ['from','to']:e[field]=[e[field][j]+shift[j] for j in range(3)]
                if 'rotation' in e:e['rotation']['origin']=[e['rotation']['origin'][j]+shift[j] for j in range(3)]
                for f in e['faces'].values():f.pop('cullface',None)
            def model(elements):return dict(textures=textures,render_type='minecraft:cutout',elements=elements)
            lo=[s['lo'][j]+shift[j] for j in range(3)];hi=[s['hi'][j]+shift[j] for j in range(3)]
            sf=els[s['side']]['faces'];side=sf.get('north',sf.get('south',next(iter(sf.values()))));top=els[s['top']]['faces'][s.get('top_face','up')]
            if s.get('can'):
                side=top={'texture':'#0','uv':[0,1,1,2]}
            atlas,suv,tuv=liquid_atlas(images[side['texture'][1:]],side['uv'],images[top['texture'][1:]],top['uv'])
            atlas.save(ASSETS/f'textures/{folder}_liquid.png')
            region=dict(min=[(lo[0]-8)/16,lo[1]/16,(lo[2]-8)/16],max=[(hi[0]-8)/16,hi[1]/16,(hi[2]-8)/16],
                sideUV=suv,surfaceUV=tuv,texture=f'farmingtales_drink_anim:textures/{folder}_liquid.png')
            removed=set(s['remove'])|set(s.get('ground',[]))|set(s.get('prop',[]))|{i for _,indices in s['groups'] for i in indices}
            body=[e for i,e in enumerate(els) if i not in removed];can_lid=None;cap_model='animation/cups/empty';cap_pivot=[8,hi[1],8]
            if name in ('gold_medal_coffee','white_rabbit_mocha','nocturnal_cat_coffee'):
                body.extend(coffee_interior())
            if name=='land_no1':
                # The bottom pixel of the negative liquid cube also formed the bowl-to-stem joint.
                original=exterior_box(els[5]);joint=deepcopy(original);joint['name']='restored_bowl_joint';joint['to'][1]=7
                for face in ['north','east','south','west']:
                    a,b,c,d=original['faces'][face]['uv'];joint['faces'][face]['uv']=[a,d+(b-d)/8,c,d]
                joint['faces']['up']=deepcopy(joint['faces']['down'])
                body.append(joint)
            if name=='ferry_whiskey':
                body.extend(coconut_interior())
            if s.get('can'):
                lid=els[0];hole=[6.8,8.25,8.5,9.85];body=[deepcopy(els[2]),els[3]];body[0]['faces'].pop('up',None)
                body.extend(face_piece(els[2],'up',p,'_shoulder'+str(i)) for i,p in enumerate(subtract((5,5,11,11),(5.5,5.5,10.5,10.5))))
                for face in lid['faces']:
                    if face=='up':body.extend(face_piece(lid,face,p,'_rim'+str(i)) for i,p in enumerate(subtract((5.5,5.5,10.5,10.5),hole)))
                    else:
                        e=deepcopy(lid);e['faces']={face:e['faces'][face]};body.append(e)
                flap=face_piece(lid,'up',hole,'flap');flap.pop('rotation',None);flap['from'][1]=flap['to'][1]=11
                flap['faces']['down']=deepcopy(flap['faces']['up'])
                dump(ASSETS/f'models/{base}_flap.json',model([flap]))
                cap_model=base+'_tab';dump(ASSETS/f'models/{cap_model}.json',model([els[1]]));cap_pivot=[6.775,11,7.9]
                can_lid=dict(origin=[8,0,8],yaw=22.5,hinge=[7.65,11,9.85],hole=hole,flapModel=base+'_flap')
                metal={'texture':'#0','uv':[12,8.5,12.5,9]}
                body.append(dict(name='can_inner_bottom',**{'from':[5.1,1.025,5.1],'to':[10.9,1.025,10.9]},
                    rotation={'angle':22.5,'axis':'y','origin':[8,0,8]},faces={'up':metal}))
                body.append(dict(name='can_inner_walls',**{'from':[10.9,10,10.9],'to':[5.1,1.025,5.1]},
                    rotation={'angle':22.5,'axis':'y','origin':[8,0,8]},faces={f:deepcopy(metal) for f in ['north','east','south','west']}))
                body.append(dict(name='can_inner_neck',**{'from':[10.4,10.95,10.4],'to':[5.6,10,5.6]},
                    rotation={'angle':22.5,'axis':'y','origin':[8,0,8]},faces={f:deepcopy(metal) for f in ['north','east','south','west']}))
                upper_region=deepcopy(region)
                region['max'][1]=10/16
                upper_region['min']=[(5.65-8)/16,10/16,(5.65-8)/16]
                upper_region['max']=[(10.35-8)/16,10.85/16,(10.35-8)/16]
            if s.get('serving'):
                prop=[deepcopy(els[i]) for i in s['prop']];cap_model=base+'_serving';cap_pivot=[0,0,0]
                if s['serving']=='SPOON':
                    # Retain the original bowl/handle geometry, normalized around the grasp.
                    anchor=[13+shift[0],1.5+shift[1],2.75+shift[2]]
                    for e in prop:
                        e.pop('rotation',None)
                        for field in ['from','to']:e[field]=[e[field][j]-anchor[j] for j in range(3)]
                    rest=[9.5,8,8];tip=[-5.25,0,0]
                    # Both sides of the ceramic floor remain visible as the coffee empties.
                    face={'texture':'#0','uv':[.5,8,.75,8.25]}
                    body.append(dict(name='ceramic_floor',**{'from':[lo[0]-.4,.10,lo[2]-.4],'to':[hi[0]+.4,.12,hi[2]+.4]},faces={'up':face,'down':deepcopy(face)}))
                elif s['serving']=='PINEAPPLE':
                    import math
                    a=math.radians(22.5)
                    anchor=[9+2*math.cos(a),6.05-2*math.sin(a),8.5]
                    for e in prop:
                        for field in ['from','to']:e[field]=[e[field][j]-anchor[j] for j in range(3)]
                        e['rotation']['origin']=[e['rotation']['origin'][j]-anchor[j] for j in range(3)]
                    rest=anchor;tip=[4*math.sin(a),4*math.cos(a),0]
                elif s['serving']=='POPSICLE':
                    anchor=[7.375,12.925,7.475]
                    # Preserve the original tilted popsicle and grip at the exposed wooden stick.
                    import math
                    y,z=anchor[1]-4.425,anchor[2]-8.475;a=math.radians(22.5)
                    anchor=[anchor[0],4.425+y*math.cos(a)-z*math.sin(a),8.475+y*math.sin(a)+z*math.cos(a)]
                    for e in prop:
                        for field in ['from','to']:e[field]=[e[field][j]-anchor[j] for j in range(3)]
                        if 'rotation' in e:e['rotation']['origin']=[e['rotation']['origin'][j]-anchor[j] for j in range(3)]
                    rest=anchor;tip=[0,-8.5*math.cos(a),-8.5*math.sin(a)]
                    prop[0]['faces']['down']=deepcopy(prop[0]['faces']['up'])
                    # The placed model only supplied the exposed handle. Continue
                    # that same wood through the ice so bites cannot consume it.
                    prop[0]['from'][1]-=6
                else:
                    anchor=[8.5,9.025,8]
                    for e in prop:
                        for field in ['from','to']:e[field]=[e[field][j]-anchor[j] for j in range(3)]
                        if 'rotation' in e:e['rotation']['origin']=[e['rotation']['origin'][j]-anchor[j] for j in range(3)]
                    rest=anchor;tip=[-2.5,0,0]
                dump(ASSETS/f'models/{cap_model}.json',model(prop))
                stages=[cap_model]
                if s['serving'] in ['CHOCOLATE','POPSICLE']:
                    if s['serving']=='POPSICLE':
                        # Three bites: retain the stick until the final spit, then clear it.
                        stage_elements=[]
                        for fraction in (2/3,1/3):
                            food=prop[1];pieces=[deepcopy(prop[0])]
                            x0,y0,z0=food['from'];x1,y1,z1=food['to']
                            for j,(a,b) in enumerate(((0,.33),(.33,.67),(.67,1))):
                                piece=deepcopy(food);piece['name']+='bite_edge_'+str(j)
                                piece['from'][0]=x0+(x1-x0)*a;piece['to'][0]=x0+(x1-x0)*b
                                piece['from'][1]=y1-(y1-y0)*fraction+(.45 if j==1 else 0)
                                kept=(y1-piece['from'][1])/(y1-y0)
                                for f in ('north','east','south','west'):
                                    u,v,w,q=food['faces'][f]['uv'];piece['faces'][f]['uv']=[u,v,w,v+(q-v)*kept]
                                for f in ('north','south','up','down'):
                                    u,v,w,q=piece['faces'][f]['uv'];piece['faces'][f]['uv']=[u+(w-u)*a,v,u+(w-u)*b,q]
                                piece['faces']['down']=deepcopy(piece['faces']['up'])
                                pieces.append(piece)
                            stage_elements.append(pieces)
                        stage_elements.append([prop[0]])
                    else:
                        stage_elements=[chocolate_bite(prop)]
                    for k,e in enumerate(stage_elements,1):
                        path=cap_model+'_bite'+str(k);dump(ASSETS/f'models/{path}.json',model(e));stages.append(path)
                servings[item]=dict(kind=s['serving'],rest=rest,tip=tip,stages=stages)
            dump(ASSETS/f'models/{base}.json',model(body))
            parts=[]
            for k,(kind,indices) in enumerate(s['groups']):
                group=[els[i] for i in indices];points=[v for e in group for v in corners(e)]
                a=[min(v[j] for v in points) for j in range(3)];b=[max(v[j] for v in points) for j in range(3)]
                pivot=[(a[j]+b[j])/2 for j in range(3)]
                if kind not in ['ICE','FLOATING_FRUIT']:pivot[1]=min(hi[1]-.35,max(a[1],pivot[1]))
                extent=[max(abs(v[j]-pivot[j]) for v in points) for j in range(3)];path=base+f'_{kind.lower()}_{1 if name=="ferry_whiskey" and kind=="STICK" else k}'
                dump(ASSETS/f'models/{path}.json',model(group));parts.append(dict(name=f'{kind.lower()}_{1 if name=="ferry_whiskey" and kind=="STICK" else k}',kind=kind,model=path,pivot=pivot,extent=extent,layered=True))
            decorations[item]=parts
            masks[item]=[]
            for e in body:
                # The shell already supplies the approved hand mask; inward flesh faces are visual only.
                if name=='ferry_whiskey' and e.get('name','').startswith('coconut_flesh_'):continue
                a=[min(x,y) for x,y in zip(e['from'],e['to'])];b=[max(x,y) for x,y in zip(e['from'],e['to'])];rot=e.get('rotation',{})
                if all(b[j]-a[j]>.001 for j in range(3)):masks[item].append(dict(min=a,max=b,origin=rot.get('origin',[0,0,0]),axis=rot.get('axis','y'),angle=rot.get('angle',0),rescale=rot.get('rescale',False),shiftY=0))
            if not masks[item]:masks[item]=[dict(min=lo,max=hi,origin=[8,0,8],axis='y',angle=s['yaw'],rescale=False,shiftY=0)]
            points=[v for e in body for v in corners(e)];scale=.86 if s['radius']<=1 else .90
            p=dict(id=item,style='CAN' if s.get('can') else 'CUP',scale=scale,width=s['radius']*2,depth=s['radius']*2,capPivot=cap_pivot,
                neckJoin=0,neckMin=hi[1]/16,neckMax=hi[1]/16,mouthY=hi[1]+.01,bodyModel=base,capModel=cap_model,neckModel='animation/cups/empty',
                boundsMin=[min(v[j] for v in points) for j in range(3)],boundsMax=[max(v[j] for v in points) for j in range(3)],liquid=[region,upper_region] if s.get('can') else [region],canLid=can_lid,
                cupShape=None if s.get('can') else dict(gripY=s['grip']+shift[1],radius=s['radius'],liquidYaw=s['yaw']))
            profiles.append(p)
            fixture=ROOT/f'src/smoke/resources/assets/{NS}'
            dump(fixture/f'models/item/{name}.json',{'parent':'farmingtales_drink_anim:'+base})
            dump(fixture/f'blockstates/{name}.json',{'variants':{'':{'model':'farmingtales_drink_anim:'+base}}})
            items.append(dict(id=item,source=source,liquid=s['remove'],ground=s.get('ground',[]),serving=s.get('serving'),props=s.get('prop',[]),moving_parts=parts))
    for name,data in [('drink_profiles',profiles),('decorations',decorations),('hand_volumes',masks),('servings',servings)]:dump(ASSETS/(name+'.json'),data)
    path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=READ(path)
    atlas['sources']=[s for s in atlas['sources'] if ':papercraft/' not in s.get('resource','')]
    for p in sorted((ASSETS/'textures/papercraft').glob('*.png')):atlas['sources'].append(dict(type='minecraft:single',resource='farmingtales_drink_anim:'+p.relative_to(ASSETS/'textures').with_suffix('').as_posix()))
    dump(path,atlas);dump(ROOT/'papercraft-sources.json',dict(jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),assets=sources,items=items))
    # Keep the separate four-serving bottle reproducible after the broad cup import.
    from prepare_land_no1_bottle import main as prepare_land_no1_bottle
    prepare_land_no1_bottle(str(jar))
    print('Prepared',len(SPECS)+1,'Paper Kite drinks including the detailed Land No.1 bottle;',len(profiles)+1,'total profiles;',len(servings),'edible garnishes.')

if __name__=='__main__':main()


