"""Export every container from game samples with native arm meshes and a linked camera."""
from pathlib import Path
import ast,base64,copy,io,json,math,shutil,uuid
import numpy as np
from PIL import Image

ROOT=Path(__file__).absolute().parent
ASSETS=ROOT.parent/'src/main/resources/assets/farmingtales_drink_anim'
OUT=ROOT/'纸鸢酒水动画-v60';OUT.mkdir(exist_ok=True)
BASE=np.array([0,16,15.5])
uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales/drinks-v60/'+s))
# Reuse the v14 fitting and quaternion functions without running its historical exporter.
tree=ast.parse((ROOT/'export_elbow_v14.py').read_text(encoding='utf-8'))
functions=[n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name in ['multiply_quat','inverse_quat','rotate_vector','continuous_euler','fitted_keys','subclip','combine']]
exec(compile(ast.Module(body=functions,type_ignores=[]),'v14_export_helpers','exec'),globals())
report=[]

for source in sorted((ROOT.parent/'build/blockbench/drinks').glob('*.json')):
    data=json.loads(source.read_text());profile=data['profile'];item=profile['id'].split(':')[1];scale=.7625*profile['scale']
    if not profile['id'].startswith('papercraft_magic_decoration:'):continue
    for shape,label in [('slim','纤细手臂'),('wide','Steve')]:
        template=json.loads((ROOT/f'肘部联动与原创音效-v14/农场物语-橙子果酒-{label}-肘部联动-v14.bbmodel').read_text(encoding='utf-8'))
        elements=[];groups=[];textures=[];texture_ids={}
        def tex(location,opacity=255,tint=0xFFFFFF):
            key=location+str(opacity)+str(tint)
            if key in texture_ids:return texture_ids[key]
            path=ASSETS/('textures/'+location.split(':')[1]+'.png');raw=path.read_bytes();im=Image.open(io.BytesIO(raw))
            meta_path=Path(str(path)+'.mcmeta')
            if meta_path.exists():
                meta=json.loads(meta_path.read_text())['animation']
                frame_height=meta.get('height',im.width)
                im=im.crop((0,0,im.width,frame_height))
            im=im.convert('RGBA')
            if tint!=0xFFFFFF:
                channels=list(im.split())
                for channel,shift in enumerate([16,8,0]):
                    gain=(tint>>shift)&255
                    channels[channel]=channels[channel].point(lambda v:round(v*gain/255))
                im=Image.merge('RGBA',channels)
            encoded=io.BytesIO();im.save(encoded,format='PNG');raw=encoded.getvalue()
            if opacity<255:
                im=im.convert('RGBA');im.putalpha(im.getchannel('A').point(lambda a:round(a*opacity/255)))
                encoded=io.BytesIO();im.save(encoded,format='PNG');raw=encoded.getvalue()
            index=len(textures);texture_ids[key]=index
            textures.append(dict(name=path.name,id=str(index),uuid=uid(item+key),mode='bitmap',source='data:image/png;base64,'+base64.b64encode(raw).decode(),internal=True,width=im.width,height=im.height,uv_width=im.width,uv_height=im.height,render_mode='default',render_sides='front'))
            return index
        def group(name,origin=BASE):
            g=dict(name=name,uuid=uid(item+shape+name),origin=list(origin),rotation=[0,0,0],visibility=True,export=True,color=len(groups)%8)
            groups.append(g);return g
        bottle=group('bottle');cap=group('closure');hand=group('right_forearm');helper=group('left_hand')
        neck=group('open_neck',BASE+[0,(profile['neckMin']-.30)*scale*16,0])
        children={g['name']:[] for g in groups}
        def cube(e,name,parent,texture_map,pivot=(8,4.8,8),shift=(0,0,0),opacity=255):
            e=copy.deepcopy(e)
            r=e.get('rotation',{});rotation=[0,0,0]
            if r.get('rescale'):
                origin=r.get('origin',[0,0,0]);axis='xyz'.index(r['axis'])
                for field in ['from','to']:
                    e[field]=[origin[j]+(e[field][j]-origin[j])/(math.cos(math.radians(r['angle'])) if j!=axis else 1) for j in range(3)]
            if r:rotation['xyz'.index(r['axis'])]=r['angle']
            point=lambda a:(BASE+(np.array(a)-pivot+shift)*scale).tolist()
            faces={f:dict(uv=[0,0,0,0],texture=None) for f in ['north','east','south','west','up','down']}
            for key,value in e['faces'].items():
                index=tex(texture_map[value['texture'][1:]],opacity,0x5555FF if item=='signature_cocktail' and value.get('tintindex')==1 else 0xFFFFFF)
                im=textures[index];uv=[v*(im['width'] if i%2==0 else im['height'])/16 for i,v in enumerate(value['uv'])]
                faces[key]=dict(uv=uv,texture=index,rotation=value.get('rotation',0))
            element=dict(name=name,uuid=uid(item+shape+name),type='cube',origin=point(r.get('origin',pivot)),rotation=rotation,
                faces=faces,from_=point(e['from']),to=point(e['to']),box_uv=False,autouv=0,visibility=True,export=True)
            element['from']=element.pop('from_');elements.append(element);children[parent].append(element['uuid'])
        for part,path,pivot,shift in [('bottle',profile['bodyModel'],[8,4.8,8],[0,0,0]),('closure',profile['capModel'],profile['capPivot'],[0,0,0]),('open_neck',profile['neckModel'],[8,4.8,8],[0,profile['neckJoin']*16,0])]:
            model=json.loads((ASSETS/f'models/{path}.json').read_text())
            for i,e in enumerate(model['elements']):cube(e,part+'_'+str(i),part,model['textures'],pivot,shift)
        decorations=[]
        for part in data.get('decorations',[]):
            g=group(part['name'],BASE+(np.array(part['pivot'])-[8,4.8,8])*scale)
            children[g['name']]=[];decorations.append((g,part))
            model=json.loads((ASSETS/f"models/{part['model']}.json").read_text(encoding='utf-8'))
            for i,e in enumerate(model['elements']):cube(e,part['name']+'_'+str(i),part['name'],model['textures'],opacity=173 if part['kind']=='ICE' else 255)
        flap=None;liquid_space=None
        if profile.get('canLid'):
            lid=profile['canLid'];flap=group('can_opening_flap');children[flap['name']]=[]
            model=json.loads((ASSETS/f"models/{lid['flapModel']}.json").read_text(encoding='utf-8'))
            for i,e in enumerate(model['elements']):cube(e,'can_flap_'+str(i),flap['name'],model['textures'],lid['hinge'])
            liquid_space=group('can_liquid_space',BASE+(np.array(lid['origin'])-[8,4.8,8])*scale)
            liquid_space['rotation']=[0,lid['yaw'],0]
        if profile.get('cupShape'):
            liquid_space=group('cup_liquid_space',BASE+[0,-4.8*scale,0])
            liquid_space['rotation']=[0,profile['cupShape']['liquidYaw'],0]
        liquids=[];liquid_cells=[]
        for i,r in enumerate(profile['liquid']):
            location=r['texture'].replace(':textures/',':')[:-4]
            for x in range(6):
                for z in range(6):
                    name=f'liquid_{i}_{x}_{z}'
                    a=(np.array(r['min'])*16+[8,0,8]);b=(np.array(r['max'])*16+[8,0,8])
                    lo=a.copy();hi=b.copy()
                    for axis,n in [(0,x),(2,z)]:
                        lo[axis]=a[axis]+(b[axis]-a[axis])*n/6
                        hi[axis]=a[axis]+(b[axis]-a[axis])*(n+1)/6
                    center=(lo+hi)*.5
                    g=group(name,BASE+np.array([center[0]-8,lo[1]-4.8,center[2]-8])*scale)
                    liquids.append(g);children[name]=[]
                    faces={f:dict(uv=r['surfaceUV'] if f in ['up','down'] else r['sideUV'],texture='#0',tintindex=1 if item=='signature_cocktail' else -1) for f in ['north','east','south','west','up','down']}
                    cube({'from':lo.tolist(),'to':hi.tolist(),'faces':faces},name+'_volume',name,{'0':location},opacity=184)
                    liquid_cells.append((g,i,(center[0]-8)/16,(center[2]-8)/16))
        skin=copy.deepcopy(template['textures'][2]);skin_index=len(textures);skin['id']=str(skin_index);textures.append(skin)
        for prefix,parent in [('right','right_forearm'),('left','left_hand')]:
            for layer in ['skin','sleeve']:
                e=copy.deepcopy(next(e for e in template['elements'] if e['name']==prefix+'_arm_'+layer));origin=np.array(e['origin'])
                for field in ['from','to']:e[field]=(np.array(e[field])-origin+BASE).tolist()
                e['origin']=BASE.tolist();e.pop('rotation',None);e['uuid']=uid(item+shape+e['name'])
                for f in e['faces'].values():f['texture']=skin_index
                elements.append(e);children[parent].append(e['uuid'])
        camera=copy.deepcopy(next(e for e in template['elements'] if e.get('type')=='camera'));camera['uuid']=uid(item+shape+'camera');elements.append(camera)
        node=lambda g,kids:dict(uuid=g['uuid'],children=kids)
        liquid_nodes=[node(g,children[g['name']]) for g in liquids]
        extra_nodes=[node(g,children[g['name']]) for g,part in decorations]
        if flap is not None:extra_nodes.append(node(flap,children[flap['name']]))
        if liquid_space:liquid_nodes=[node(liquid_space,liquid_nodes)]
        outliner=[node(bottle,children['bottle']+[node(cap,children['closure']),node(neck,children['open_neck'])]+liquid_nodes+extra_nodes),node(hand,children['right_forearm']),node(helper,children['left_hand']),camera['uuid']]
        clips={name:copy.deepcopy(data['clips'][shape+'/'+name]) for name in ['idle','draw','put_away','inspect','drink','drink_variant_2','drink_variant_3','drink_variant_4','walk','sprint','jump','place']}
        clips['preview_complete']=copy.deepcopy(data['clips'][shape+'/preview_complete'])
        animations=[]
        for name,clip in clips.items():
            frames=clip['frames'];times=np.array([f['time'] for f in frames]);count=len(frames);animators={}
            def add(g,channel,values,tolerance=.015,step=False):
                track=animators.setdefault(g['uuid'],dict(name=g['name'],type='camera' if g is camera else 'bone',keyframes=[]))
                track['keyframes']+=fitted_keys(times,values,channel,tolerance,item+shape+name+g['name']+channel,step)
            offset=np.array([f['offset'] for f in frames])*16
            for g,field in [(bottle,'bottle'),(hand,'hand'),(helper,'helper')]:
                add(g,'position',np.array([f[field]['position'] for f in frames])+offset)
                add(g,'rotation',continuous_euler([f[field]['quaternion'] for f in frames]),.08)
            add(helper,'scale',[[int(f['helperVisible'])]*3 for f in frames],step=True)
            add(bottle,'scale',[[int(f['bottleVisible'])]*3 for f in frames],step=True)
            relative=[]
            for f in frames:
                inv=inverse_quat(f['bottle']['quaternion']);p=rotate_vector(inv,np.array(f['cap']['position'])-f['bottle']['position'])
                relative.append((p,multiply_quat(inv,f['cap']['quaternion'])))
            add(cap,'position',[p for p,q in relative],.006)
            add(cap,'rotation',continuous_euler([q for p,q in relative]),.10)
            add(cap,'scale',[[int(f['capVisible'])]*3 for f in frames],step=True)
            if flap:
                yaw=math.radians(lid['yaw']);q_yaw=[0,math.sin(yaw/2),0,math.cos(yaw/2)]
                pivot=np.array(lid['origin'])+rotate_vector(q_yaw,np.array(lid['hinge'])-lid['origin'])
                add(flap,'position',[(pivot-[8,4.8,8])*scale]*count,.006)
                rotations=[]
                for f in frames:
                    angle=math.radians(-80*f['canOpening'])
                    rotations.append(multiply_quat(q_yaw,[math.sin(angle/2),0,0,math.cos(angle/2)]))
                add(flap,'rotation',continuous_euler(rotations),.1)
            add(neck,'scale',[[1,f['neckReveal'],1] for f in frames],.015)
            for g,part in decorations:
                add(g,'position',np.array([f['decorations'][part['name']]['offset'] for f in frames])*scale,.015)
                add(g,'rotation',continuous_euler([f['decorations'][part['name']]['quaternion'] for f in frames]),.15)
            for g,i,x,z in liquid_cells:
                r=profile['liquid'][i];heights=[]
                for f in frames:
                    nx,ny,nz=f['liquidNormal']
                    y=(f['liquidLevel']-nx*x-nz*z)/max(.001,ny)
                    heights.append([1,max(0,min(1,(y-r['min'][1])/(r['max'][1]-r['min'][1]))),1])
                add(g,'scale',heights,.015)
            quats=np.array([f['camera'] for f in frames]);quats[:,:3]*=-1
            add(camera,'rotation',continuous_euler(quats),.10);add(camera,'position',np.zeros((count,3)))
            animations.append(dict(uuid=uid(item+shape+name),name='animation.'+item+'.'+name,loop='loop' if name in ['idle','walk','sprint'] else 'hold',override=True,length=clip['length'],snapping=60,animators=animators))
        model=dict(meta=dict(format_version='5.0',model_format='free',box_uv=False),name=f'{item}-{label}-v60',model_identifier=item,credit='Original cocktail assets; 紫菜 / ARR / FarmingTales 1.8.30',resolution=dict(width=16,height=16),textures=textures,elements=elements,groups=groups,outliner=outliner,animations=animations,editor_state=copy.deepcopy(template['editor_state']))
        path=OUT/(model['name']+'.bbmodel');path.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
        report.append(dict(item=profile['id'],arm=shape,path=str(path),animations=len(animations),textures=len(textures),geometry_scale=scale))
shutil.copy2(ROOT/'camera-source/cameras.js',OUT/'cameras.js')
(OUT/'导出检查.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
shutil.copy2(ROOT/'使用说明-v60.txt',OUT/'使用说明.txt')
print('Exported',len(report),'portable Blockbench projects with animated first-person cameras.')

import zipfile
with zipfile.ZipFile(OUT.with_suffix('.zip'),'w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(OUT.iterdir()):
        if path.is_file():archive.write(path,path.name)
