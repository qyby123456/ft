"""Export the actual game rig into sparse editable BB tracks, retaining all source cubes."""
from pathlib import Path
import base64, copy, json, math, uuid, warnings
import numpy as np

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'舒适持握与完整双手-v10'
OUT.mkdir(exist_ok=True)
runtime=json.loads((ROOT.parent/'build/blockbench/runtime-motion.json').read_text())
uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales/comfort-v10/'+s))
report={}

def continuous_euler(quaternions):
    q=np.array(quaternions,dtype=float);q/=np.linalg.norm(q,axis=1)[:,None]
    x,y,z,w=q.T
    raw=np.degrees(np.stack([np.arctan2(2*(w*x+y*z),1-2*(x*x+y*y)),np.arcsin(np.clip(2*(w*y-z*x),-1,1)),np.arctan2(2*(w*z+x*y),1-2*(y*y+z*z))],axis=1))
    result=[raw[0]]
    for value in raw[1:]:
        alternate=np.array([value[0]+180,180-value[1],value[2]+180])
        candidates=[v+360*np.round((result[-1]-v)/360) for v in (value,alternate)]
        result.append(min(candidates,key=lambda v:np.linalg.norm(v-result[-1])))
    return np.array(result)

def fitted_keys(times,values,channel,tolerance,tag,step=False):
    values=np.asarray(values,dtype=float)
    if step:
        indices=[0]+[i for i in range(1,len(times)) if np.max(np.abs(values[i]-values[i-1]))>1e-8]
        return [dict(uuid=uid(tag+str(i)),channel=channel,time=float(times[i]),interpolation='step',data_points=[dict(zip('xyz',values[i].tolist()))]) for i in indices]
    slopes=np.gradient(values,times,axis=0)
    chosen={0,len(times)-1}
    def segment(a,b):
        t=(times[a:b+1]-times[a])/(times[b]-times[a]);t=t[:,None]
        span=times[b]-times[a]
        return (2*t**3-3*t*t+1)*values[a]+(t**3-2*t*t+t)*slopes[a]*span+(-2*t**3+3*t*t)*values[b]+(t**3-t*t)*slopes[b]*span
    while True:
        worst_error=0;worst=None
        ordered=sorted(chosen)
        for a,b in zip(ordered,ordered[1:]):
            errors=np.max(np.abs(segment(a,b)-values[a:b+1]),axis=1)
            i=int(np.argmax(errors))
            if errors[i]>worst_error:worst_error=float(errors[i]);worst=a+i
        if worst_error<=tolerance:break
        chosen.add(worst)
    keys=[];ordered=sorted(chosen)
    for n,i in enumerate(ordered):
        left=(times[i]-times[ordered[n-1]])/3 if n else 0
        right=(times[ordered[n+1]]-times[i])/3 if n+1<len(ordered) else 0
        keys.append(dict(uuid=uid(tag+str(i)),channel=channel,time=float(times[i]),interpolation='bezier',
            data_points=[dict(zip('xyz',values[i].tolist()))],bezier_linked=False,
            bezier_left_time=[-float(left)]*3,bezier_right_time=[float(right)]*3,
            bezier_left_value=(-slopes[i]*left).tolist(),bezier_right_value=(slopes[i]*right).tolist()))
    return keys

def subclip(clip,start,end):
    frames=[copy.deepcopy(f) for f in clip['frames'] if start-1e-7<=f['time']<=end+1e-7]
    first=frames[0]['time']
    for f in frames:f['time']-=first
    return dict(length=frames[-1]['time'],frames=frames)

def combine(parts):
    frames=[];offset=0
    for clip in parts:
        for old in clip['frames']:
            f=copy.deepcopy(old);f['time']+=offset
            if frames and abs(f['time']-frames[-1]['time'])<1e-7:frames[-1]=f
            else:frames.append(f)
        offset+=clip['length']
    return dict(length=offset,frames=frames)

for profile,label in [('wide','Steve'),('slim','纤细手臂')]:
    source=ROOT/f'纤细手臂与玻璃瓶颈-v9/农场物语-橙子果酒-{label}-v9.bbmodel'
    model=json.loads(source.read_text(encoding='utf-8'))
    groups={g['name']:g for g in model['groups']}
    elements={e['name']:e for e in model['elements']}
    # Keep the same cubes, UVs and pivots. Put the animated attachments under one rig root.
    used=['viewmodel_motion','bottle','recolored_original_neck','wine_fill_preview','wooden_stopper','right_forearm','left_cork_hand']
    model['groups']=[groups[n] for n in used]
    node=lambda name,children:dict(uuid=groups[name]['uuid'],children=children)
    cube=lambda name:elements[name]['uuid']
    model['outliner']=[node('viewmodel_motion',[
        node('bottle',[node('recolored_original_neck',[cube('original_neck_recolored_only')]),
                       node('wine_fill_preview',[cube('wine_volume_proxy')])]+[cube(n) for n in elements if n.startswith('bottle_original_')]),
        node('wooden_stopper',[cube('original_yellow_stopper')]),
        node('right_forearm',[cube('right_arm_skin')]),node('left_cork_hand',[cube('left_arm_skin')])]),cube('camera_first_person')]
    camera=elements['camera_first_person'];camera['position']=[0,16,15.5];camera['rotation']=[0,0,0]
    model['editor_state']['previews']['main']['position']=[0,16,15.5]
    # Scale preview glass around its bottom; the game clips baked polygons instead of stretching UVs.
    groups['recolored_original_neck']['origin'][1]=14.491
    if profile=='slim':
        skin=ROOT.parent/'preview/user-slim-skin.png'
        model['textures'][2]['source']='data:image/png;base64,'+base64.b64encode(skin.read_bytes()).decode()
        model['textures'][2]['name']='玩家当前纤细皮肤.png'
    clips={name:copy.deepcopy(runtime[profile+'/'+name]) for name in ('idle','draw','put_away','inspect','drink','walk','sprint','jump')}
    clips['pose_reference']=copy.deepcopy(clips['idle'])
    clips['drink_stow_empty_bottle']=subclip(clips['drink'],4.4,5.6)
    clips['drink_empty_hand_recover']=subclip(clips['drink'],5.6,6.25)
    clips['new_bottle_offscreen_pause']=subclip(clips['drink'],6.25,6.45)
    clips['drink_draw_new_bottle']=subclip(clips['drink'],6.45,7.35)
    clips['empty_hand_reference']=copy.deepcopy(clips['new_bottle_offscreen_pause'])
    clips['preview_draw_hold_put_away']=combine([clips['draw'],clips['idle'],clips['put_away']])
    clips['preview_complete']=combine([clips['draw'],subclip(clips['idle'],0,.5),clips['inspect'],subclip(clips['idle'],0,.5),clips['put_away']])
    model['animations']=[];statistics={}
    for name,clip in clips.items():
        frames=clip['frames'];times=np.array([f['time'] for f in frames]);count=len(frames)
        animation=dict(uuid=uid(profile+name),name='animation.orange_wine.'+name,loop='loop' if name in ('idle','walk','sprint') else 'hold',override=True,length=clip['length'],snapping=60,animators={})
        def add(group,channel,values,tolerance=.05,step=False):
            data=camera if group=='camera_first_person' else groups[group]
            animator=animation['animators'].setdefault(data['uuid'],dict(name=group,type='camera' if group=='camera_first_person' else 'bone',keyframes=[]))
            animator['keyframes']+=fitted_keys(times,values,channel,tolerance,profile+name+group+channel,step)
        offset=np.array([f['offset'] for f in frames])*16+np.array([0,16,15.5])
        for bone,field,scale in [('bottle','bottle',[1.25]*3),('wooden_stopper','cork',[1.25]*3),('right_forearm','hand',[1]*3),('left_cork_hand','helper',None)]:
            add(bone,'position',np.array([f[field]['position'] for f in frames])+offset-np.array(groups[bone]['origin']))
            angles=continuous_euler([f[field]['quaternion'] for f in frames])-np.array(groups[bone].get('rotation',[0]*3))
            add(bone,'rotation',angles,.3)
            values=np.tile(scale,(count,1)) if scale else np.array([f['helperScale'] for f in frames])
            if bone=='wooden_stopper':values*=np.array([f['corkVisible'] for f in frames])[:,None]
            if bone=='left_cork_hand':values*=np.array([f['helperVisible'] for f in frames])[:,None]
            add(bone,'scale',values,step=True)
        add('recolored_original_neck','scale',[[1,f['mouthReveal'],1] for f in frames],.015)
        add('wine_fill_preview','scale',[[1,f['fill']/.8,1] for f in frames],.005)
        camera_quats=np.array([f['camera'] for f in frames]);camera_quats[:,:3]*=-1
        add('camera_first_person','rotation',continuous_euler(camera_quats),.12)
        add('camera_first_person','position',np.zeros((count,3)))
        model['animations'].append(animation)
        statistics[name]=sum(len(a['keyframes']) for a in animation['animators'].values())
    model['name']=f'农场物语-橙子果酒-{label}-舒适持握-v10'
    path=OUT/(model['name']+'.bbmodel')
    path.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    report[profile]={'path':str(path),'clips':len(clips),'key_counts':statistics,'position_fit_tolerance_pixels':.05,'rotation_fit_tolerance_degrees':.3}
(OUT/'导出检查.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=True))
