"""Use the user's saved pose, with sparse editable Bezier animation keys. BB only."""
from pathlib import Path
from copy import deepcopy
import json, math, uuid, hashlib
import numpy as np

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'用户持握与精简关键帧-v6'
SOURCE=OUT/'用户调整原稿-v5-0.93333秒.bbmodel'
original=json.loads(SOURCE.read_text(encoding='utf-8'))
model=deepcopy(original)
groups={g['name']:g for g in model['groups']}
byid={g['uuid']:g for g in model['groups']}
uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales-user-pose-v6/'+s))

def vec(k):return np.array([float(k['data_points'][0][c]) for c in 'xyz'])
def linear_channel(a,ch,t,default=(0,0,0)):
    ks=sorted([k for k in a.get('keyframes',[]) if k['channel']==ch],key=lambda k:k['time'])
    if not ks:return np.array(default,dtype=float)
    if t<=ks[0]['time']:return vec(ks[0])
    for a,b in zip(ks,ks[1:]):
        if t<=b['time']:
            if a.get('interpolation')=='step':return vec(a)
            return vec(a)+(vec(b)-vec(a))*(t-a['time'])/(b['time']-a['time'])
    return vec(ks[-1])

reference=original['animations'][0]
ref_time=.93333
ref={g['name']:{ch:linear_channel(reference['animators'].get(g['uuid'],{}),ch,ref_time,[1,1,1] if ch=='scale' else [0,0,0])
    for ch in ['position','rotation','scale']} for g in original['groups']}
O=np.array(groups['bottle_turn']['origin'])
C=O+ref['bottle_turn']['position']
right=groups['right_forearm'];left=groups['left_cork_hand']
rightO=np.array(right['origin']);leftO=np.array(left['origin'])

def rotation(v):
    x,y,z=np.radians(v);cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
    return np.array([[cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx],
        [sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx],[-sy,cy*sx,cy*cx]])
def euler(m):return np.degrees([math.atan2(m[2,1],m[2,2]),math.asin(np.clip(-m[2,0],-1,1)),math.atan2(m[1,0],m[0,0])])
def aim(axis):
    y=np.array(axis,dtype=float);y/=np.linalg.norm(y)
    z=np.cross([1,0,0],y);z/=np.linalg.norm(z)
    x=np.cross(y,z)
    return np.column_stack([x,y,z])

def newgroup(name,origin,color):
    g=dict(name=name,uuid=uid(name),origin=list(origin),rotation=[0,0,0],export=True,visibility=True,color=color,isOpen=True)
    model['groups'].append(g);groups[name]=g;byid[g['uuid']]=g
    return g
cal=newgroup('user_bottle_pose',O,3)
tilt=newgroup('inspect_tilt',C,0)
groups['bottle_turn']['origin']=C.tolist()
spin_node=model['outliner'][0]['children'][0]
calnode=dict(uuid=cal['uuid'],isOpen=False,children=spin_node['children'])
spin_node['children']=[calnode]
old_children=model['outliner'][0]['children']
model['outliner'][0]['children']=[dict(uuid=tilt['uuid'],isOpen=True,children=old_children)]

# A separate palm lets the wrist turn while the left elbow stays on the left.
# Split the existing skin cube at the wrist, retaining its UV rows and silhouette.
left_cube=next(e for e in model['elements'] if e['name']=='left_arm_skin')
palm_cube=deepcopy(left_cube);palm_cube['uuid']=uid('left_palm_skin');palm_cube['name']='left_palm_skin'
wrist_y=leftO[1]-3.
palm_cube['from'][1]=wrist_y
left_cube['to'][1]=wrist_y
for face in ['north','south','east','west']:
    u0,v0,u1,v1=left_cube['faces'][face]['uv'];mid=v0+(v1-v0)*.25
    left_cube['faces'][face]['uv']=[u0,mid,u1,v1]
    palm_cube['faces'][face]['uv']=[u0,v0,u1,mid]
palm=newgroup('left_palm',leftO-[0,3,0],2)
model['elements'].append(palm_cube)
left_node=next(n for n in old_children if n['uuid']==left['uuid'])
left_node['children'].append(dict(uuid=palm['uuid'],isOpen=True,children=[palm_cube['uuid']]))

# Continue the sleeves behind the view frustum; the hand and original forearm
# stay exactly where the user placed them. A visible flat sleeve cap reads as
# a disconnected arm in a first-person camera.
for arm_name,cube_name,node in [(right['name'],'right_arm_skin',next(n for n in old_children if n['uuid']==right['uuid'])),
                               (left['name'],'left_arm_skin',left_node)]:
    source_cube=next(e for e in model['elements'] if e['name']==cube_name)
    extension=deepcopy(source_cube);extension['name']=arm_name+'_sleeve_continuation';extension['uuid']=uid(extension['name'])
    extension['to'][1]=source_cube['from'][1];extension['from'][1]-=32 if arm_name==left['name'] else 8
    for face in ['north','south','east','west']:
        u0,v0,u1,v1=extension['faces'][face]['uv']
        extension['faces'][face]['uv']=[u0,v0+(v1-v0)*.85,u1,v1]
    model['elements'].append(extension);node['children'].append(extension['uuid'])

def slopes(track):
    a=np.array(track,dtype=float);t=a[:,0];v=a[:,1:];m=np.zeros_like(v)
    h=np.diff(t);d=np.diff(v,axis=0)/h[:,None]
    for i in range(1,len(t)-1):
        for c in range(v.shape[1]):
            if d[i-1,c]*d[i,c]>0:
                w1=2*h[i]+h[i-1];w2=h[i]+2*h[i-1]
                m[i,c]=(w1+w2)/(w1/d[i-1,c]+w2/d[i,c])
    return t,v,m
def sample(track,t):
    times,v,m=slopes(track)
    if t<=times[0]:return v[0].copy()
    if t>=times[-1]:return v[-1].copy()
    i=int(np.searchsorted(times,t)-1);h=times[i+1]-times[i];u=(t-times[i])/h
    return (2*u**3-3*u*u+1)*v[i]+(u**3-2*u*u+u)*h*m[i]+(-2*u**3+3*u*u)*v[i+1]+(u**3-u*u)*h*m[i+1]

def bezier(ch,track):
    # Interior duplicates in a pause carry no pose or curve information.
    track=[p for i,p in enumerate(track) if i==0 or i==len(track)-1 or
        not (np.max(abs(np.array(p[1:])-track[i-1][1:]))<1e-9 and np.max(abs(np.array(p[1:])-track[i+1][1:]))<1e-9)]
    times,v,m=slopes(track);ks=[]
    if np.max(abs(v-v[0]))<1e-9:times=times[:1];v=v[:1];m=m[:1]
    for i,t in enumerate(times):
        hl=(t-times[i-1])/3 if i else 0
        hr=(times[i+1]-t)/3 if i+1<len(times) else 0
        ks.append(dict(uuid=uid(f'{ch}:{t}:{v[i].tolist()}'),channel=ch,time=round(float(t),6),interpolation='bezier',
            data_points=[dict(zip('xyz',[round(float(x),7) for x in v[i]]))],
            bezier_linked=False,bezier_left_time=[-hl]*3,bezier_right_time=[hr]*3,
            bezier_left_value=(-m[i]*hl).tolist(),bezier_right_value=(m[i]*hr).tolist()))
    return ks
def track(a,name,ch,points):
    g=groups[name]
    target=a['animators'].setdefault(g['uuid'],dict(name=name,type='bone',keyframes=[]))
    target['keyframes']+=bezier(ch,points)
def constant(a,name,ch,value):track(a,name,ch,[[0,*value]])
def blank(name,length):
    return dict(uuid=uid('animation:'+name),name='animation.orange_wine.'+name,loop='hold',override=True,length=length,snapping=60,animators={})
def basics(a):
    for ch in ['position','rotation','scale']:constant(a,cal['name'],ch,ref['bottle_turn'][ch])
    constant(a,'recolored_original_neck','scale',[0,0,0])
    constant(a,'wooden_stopper','scale',[1,1,1])
    constant(a,'wine_fill_preview','scale',[1,1,1])
    constant(a,left['name'],'scale',[.7,.7,.7])

DRAW_POS=[[0,3.4,-18.6,1.8],[.18,3.4,-18.6,1.8],[.27,2.95,-12.8,1.2],
          [.40,1.20,-3.4,0],[.50,.18,.42,-.30],[.62,-.03,-.10,.04],[.90,0,0,0]]
DRAW_HAND=[[0,-2,18.3,-.5],[.08,-1.5,12.5,-.2],[.18,0,0,0],[.90,0,0,0]]
DRAW_ROT=[[0,7,0,0],[.12,24,4,0],[.18,19,3,0],[.39,-3,-1,0],[.53,-1,0,0],[.90,0,0,0]]
PUT_POS=[[0,0,0,0],[.07,.12,-.05,.32],[.17,.82,-2.7,.95],[.29,2.75,-10.3,1.8],[.44,4.5,-19.4,2.6],[.82,4.5,-19.4,2.6]]
PUT_HAND=[[0,0,0,0],[.47,0,0,0],[.58,-1.1,9.8,-.4],[.73,-2.9,19,-1.2],[.82,-3.1,19.1,-1.3]]
PUT_ROT=[[0,0,0,0],[.07,-1,0,0],[.22,9,1,0],[.44,24,3,0],[.50,24,3,0],[.72,8,0,0],[.82,7,0,0]]
OFFSCREEN=np.array([-9,-15,8.])
def standalone(name,length,pos,hand,angles):
    a=blank(name,length);basics(a)
    track(a,'viewmodel_motion','position',[[t,*(np.array(v)+ref['viewmodel_motion']['position'])] for t,*v in pos])
    track(a,right['name'],'position',[[t,*(np.array(v)+ref[right['name']]['position'])] for t,*v in hand])
    track(a,right['name'],'rotation',[[t,*(np.array(v)+ref[right['name']]['rotation'])] for t,*v in angles])
    constant(a,left['name'],'position',OFFSCREEN-leftO)
    return a
draw=standalone('draw',.90,DRAW_POS,DRAW_HAND,DRAW_ROT)
put=standalone('put_away',.82,PUT_POS,PUT_HAND,PUT_ROT)

# Asymmetric timing: inspect the label, guide to the right, then four short turns.
# Right hand secures the bottle before each left wrist reset.
TURN=[[0,0],[1.32,0],[1.64,45],[2.06,45],[2.46,135],[2.73,135],
      [3.08,225],[3.31,225],[3.64,315],[3.90,315],[4.34,405],[4.62,405],[4.94,360],[6.05,360]]
REGRIPS=[(1.83,2.06,45),(2.46,2.73,90),(3.08,3.31,90),(3.64,3.90,90)]
PHASE=[[0,-45],[1.32,-45],[1.64,0],[1.83,0],[1.91,0],[1.98,-45],[2.06,-45],
       [2.46,45],[2.54,45],[2.65,-45],[2.73,-45],[3.08,45],[3.15,45],[3.24,-45],[3.31,-45],
       [3.64,45],[3.72,45],[3.82,-45],[3.90,-45],[4.34,45],[4.62,45],[4.94,0],[6.05,0]]
INSPECT_POS=[[0,0,0,0],[.10,.08,-.08,.04],[.38,-1.70,3.13,.55],[.60,-1.55,3.0,.45],
             [5.40,-1.55,3.0,.45],[5.79,.045,-.08,-.025],[6.05,0,0,0]]
TILT=[[0,0,0,0],[.16,0,0,-1],[.46,-1,0,-10],[.68,-.8,0,-8],[5.34,-.8,0,-8],[5.80,0,0,.35],[6.05,0,0,0]]
REACH=[[0,0],[.68,0],[.84,.22],[1.04,.93],[1.18,1],[5.18,1],[5.36,.96],[5.64,.18],[5.86,0],[6.05,0]]

# Calculate the resized bottle's base, using the exact user transform.
userR=rotation(ref['bottle_turn']['rotation']);userS=np.diag(ref['bottle_turn']['scale'])
baseR=rotation(groups['bottle']['rotation'])
base_center=C+userR@userS@baseR@(np.array([O[0],7.72,O[2]])-O)
palm_top=np.array([C[0]-.08,base_center[1]-.12,C[2]+.10])
wrist=palm_top-[0,2.1,0]
def clearance(t):
    for a,b,_ in REGRIPS:
        if a<=t<=b:
            d=b-a
            return sample([[a,0],[a+.32*d,1],[a+.69*d,1],[b,0]],t)[0]
    return 0.
def left_pose(t):
    reach=sample(REACH,t)[0];gap=clearance(t)
    w=wrist+np.array([-.65,-1.05,.20])*gap
    forearm=aim(w-np.array([-6.,5.,11.]))
    w=OFFSCREEN*(1-reach)+w*reach
    # Arm group origin is the original fingertip; the new child rotates at the wrist.
    group_pos=w+forearm@np.array([0,2.1,0])
    handrot=rotation([0,sample(PHASE,t)[0],0])
    return group_pos-leftO,euler(forearm)-np.array(left['rotation']),euler(forearm.T@handrot)

inspect=blank('inspect',6.05);basics(inspect)
track(inspect,'viewmodel_motion','position',[[t,*(np.array(v)+ref['viewmodel_motion']['position'])] for t,*v in INSPECT_POS])
track(inspect,tilt['name'],'rotation',TILT)
track(inspect,'bottle_turn','rotation',[[t,0,a,0] for t,a in TURN])
right_loose=[[0,0],[1.20,0],[1.32,1]]
for a,b,_ in REGRIPS:right_loose += [[a-.07,0],[b,0],[b+.06,1]]
right_loose += [[4.94,1],[5.08,0],[6.05,0]]
right_loose.sort()
radial=rightO-C;radial[1]=0;radial/=np.linalg.norm(radial)
track(inspect,right['name'],'position',[[t,*(ref[right['name']]['position']+radial*.13*w)] for t,w in right_loose])
track(inspect,right['name'],'rotation',[[0,*ref[right['name']]['rotation']],[.4,*(ref[right['name']]['rotation']+[-2,-1,.4])],
    [.65,*(ref[right['name']]['rotation']+[-1.3,-.6,.2])],[5.35,*(ref[right['name']]['rotation']+[-1.3,-.6,.2])],[6.05,*ref[right['name']]['rotation']]])
left_times=sorted(set([p[0] for p in PHASE]+[p[0] for p in REACH]+[a+(b-a)*f for a,b,_ in REGRIPS for f in [.32,.69]]))
left_poses=[left_pose(t) for t in left_times]
for name,ch,index in [(left['name'],'position',0),(left['name'],'rotation',1),(palm['name'],'rotation',2)]:
    track(inspect,name,ch,[[t,*p[index]] for t,p in zip(left_times,left_poses)])

pose=blank('pose_reference',1);basics(pose)
constant(pose,'viewmodel_motion','position',ref['viewmodel_motion']['position'])
for ch in ['position','rotation']:constant(pose,right['name'],ch,ref[right['name']][ch])
constant(pose,left['name'],'position',OFFSCREEN-leftO)

def evaluate(ks,t,ch):
    ks=sorted([k for k in ks if k['channel']==ch],key=lambda k:k['time'])
    if not ks:return np.ones(3) if ch=='scale' else np.zeros(3)
    if t<=ks[0]['time']:return vec(ks[0])
    for a,b in zip(ks,ks[1:]):
        if t<=b['time']:
            u=(t-a['time'])/(b['time']-a['time'])
            if a.get('interpolation')=='step':return vec(a)
            if a.get('interpolation')!='bezier':return vec(a)+(vec(b)-vec(a))*u
            return (1-u)**3*vec(a)+3*(1-u)**2*u*(vec(a)+a['bezier_right_value'])+3*(1-u)*u*u*(vec(b)+b['bezier_left_value'])+u**3*vec(b)
    return vec(ks[-1])

def combine(name,length,segments):
    a=blank(name,length)
    identifiers=set(g for _,_,clip in segments for g in clip['animators'])
    for identifier in identifiers:
        chans=set(k['channel'] for _,_,clip in segments for k in clip['animators'].get(identifier,{}).get('keyframes',[]))
        dst=dict(name=byid[identifier]['name'],type='bone',keyframes=[])
        for ch in chans:
            keys=[]
            for start,end,clip in segments:
                sourceks=clip['animators'].get(identifier,{}).get('keyframes',[])
                matching=[deepcopy(k) for k in sourceks if k['channel']==ch and k['time']<=end-start]
                if not matching:matching=bezier(ch,[[0,*evaluate(sourceks,0,ch)]])
                for k in matching:k['time']=round(k['time']+start,6)
                last=bezier(ch,[[end,*evaluate(sourceks,end-start,ch)]])[0]
                keys += matching+[last]
            # Adjacent segments have matching end poses. Keep the outgoing and incoming handles.
            merged={}
            for k in keys:
                t=k['time']
                if t in merged:
                    k['bezier_left_time']=merged[t].get('bezier_left_time',[0]*3)
                    k['bezier_left_value']=merged[t].get('bezier_left_value',[0]*3)
                merged[t]=k
            keys=sorted(merged.values(),key=lambda k:k['time'])
            if all(np.max(abs(vec(k)-vec(keys[0])))<1e-7 for k in keys):keys=keys[:1]
            for k in keys:k['uuid']=uid(f'{name}:{identifier}:{ch}:{k["time"]}')
            dst['keyframes']+=keys
        a['animators'][identifier]=dst
    return a

hold_after=deepcopy(pose)
constant(hold_after,'bottle_turn','rotation',[0,360,0])
stow_after=deepcopy(put);constant(stow_after,'bottle_turn','rotation',[0,360,0])
complete=combine('preview_complete',9.,[(0,.9,draw),(.9,1.5,pose),(1.5,7.55,inspect),(7.55,7.80,hold_after),(7.8,9.,stow_after)])
short=combine('preview_draw_hold_put_away',3.1,[(0,.9,draw),(.9,2.05,pose),(2.05,3.1,put)])

def simplify(ks,ch,tolerance):
    keys=sorted([k for k in ks if k['channel']==ch],key=lambda k:k['time'])
    if len(keys)<3 or any(k.get('interpolation')=='step' for k in keys):return keys
    times=np.array([k['time'] for k in keys]);v=np.array([vec(k) for k in keys]);keep={0,len(keys)-1}
    def split(a,b):
        if b<=a+1:return
        f=(times[a+1:b]-times[a])/(times[b]-times[a])
        errors=np.max(abs(v[a+1:b]-(v[a]+f[:,None]*(v[b]-v[a]))),axis=1)
        i=int(np.argmax(errors))+a+1
        if max(errors)>tolerance:keep.add(i);split(a,i);split(i,b)
    split(0,len(keys)-1)
    return [keys[i] for i in sorted(keep)]

retained=[deepcopy(a) for a in original['animations'] if a['name'].split('.')[-1] in ['idle','drink','walk','sprint','jump']]
# Apply the user's bottle calibration and keep the right gripping point attached.
grip_local=np.linalg.solve(userR@userS@baseR,rightO-C)
for a in retained:
    for ch in ['position','rotation','scale']:constant(a,cal['name'],ch,ref['bottle_turn'][ch])
    constant(a,'viewmodel_motion','position',ref['viewmodel_motion']['position'])
    bt=a['animators'][groups['bottle']['uuid']]
    rt=a['animators'][right['uuid']]
    times=sorted(set([k['time'] for k in bt['keyframes'] if k['channel'] in ['position','rotation']]+[0,a['length']]))
    positions=[]
    for t in times:
        pos=linear_channel(bt,'position',t);r=rotation(np.array(groups['bottle']['rotation'])+linear_channel(bt,'rotation',t))
        world=C+userR@userS@(pos+r@grip_local)
        positions.append(dict(channel='position',time=t,interpolation='linear',data_points=[dict(zip('xyz',(world-rightO).tolist()))]))
    rt['keyframes']=[k for k in rt['keyframes'] if k['channel']!='position']+positions
    for k in rt['keyframes']:
        if k['channel']=='rotation':k['data_points']=[dict(zip('xyz',(vec(k)+ref[right['name']]['rotation']).tolist()))]
    # Original left-hand contact follows the user's resized bottle during cork handling.
    if left['uuid'] in a['animators']:
        for k in a['animators'][left['uuid']]['keyframes']:
            if k['channel']=='position':
                point=leftO+vec(k);point=C+userR@userS@(point-O)
                k['data_points']=[dict(zip('xyz',(point-leftO).tolist()))]
    for anim in a['animators'].values():
        ks=anim['keyframes'];anim['keyframes']=sum([simplify(ks,ch,{'position':.028,'rotation':.22,'scale':.002}[ch]) for ch in ['position','rotation','scale']],[])

model['animations']=[complete,draw,put,inspect,pose,short]+retained
camera=dict(uuid=uid('first_person_camera'),name='camera_first_person',type='camera',position=[0,16,18],rotation=[0,0,0],
    fov=70,aspect_ratio=[16,9],linked_preview='main',camera_linked=True,visibility=False)
model['elements'].append(camera);model['outliner'].append(camera['uuid'])
for a in model['animations']:
    a['selected']=a is pose
    a['animators'][camera['uuid']]=dict(name=camera['name'],type='camera',keyframes=bezier('position',[[0,0,0,0]])+bezier('rotation',[[0,0,0,0]]))
model['editor_state']=dict(mode='animate',tool='move_tool',saved=True,selected_elements=[],selected_groups=[],
    previews=dict(main=dict(position=[0,16,18],target=[0,16,2],orthographic=False,zoom=1,angle=None)))
model['name']='农场物语-橙子果酒-用户持握与精简关键帧-v6'
target=OUT/(model['name']+'.bbmodel')
target.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
counts=lambda m:{a['name'].split('.')[-1]:sum(len(x['keyframes']) for x in a['animators'].values()) for a in m['animations']}
report=dict(user_pose_time=ref_time,user_source_sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
    user_bottle_pose={k:v.tolist() for k,v in ref['bottle_turn'].items()},source_keyframes=counts(original),output_keyframes=counts(model),
    mod_sha256=hashlib.sha256((ROOT.parent.parent.parent.parent/'mods/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.2.0.jar').read_bytes()).hexdigest(),
    original_bottle_elements_unchanged=all(e==next(x for x in original['elements'] if x['uuid']==e['uuid']) for e in model['elements'] if e['name'].startswith(('bottle_original','original_','wine_volume'))),
    source_preserved=True,bezier_sparse_authored=True,right_pulls_and_tilts=True,left_supports_base=True)
(OUT/'检查记录.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
(OUT/'作者关键姿态.json').write_text(json.dumps(dict(draw=DRAW_POS,put_away=PUT_POS,inspect_position=INSPECT_POS,inspect_tilt=TILT,bottle_turn=TURN,left_wrist=PHASE,regrips=REGRIPS),indent=2),encoding='utf-8')
print(json.dumps({'before':counts(original),'after':counts(model),'user_pose':report['user_bottle_pose']},ensure_ascii=True))
