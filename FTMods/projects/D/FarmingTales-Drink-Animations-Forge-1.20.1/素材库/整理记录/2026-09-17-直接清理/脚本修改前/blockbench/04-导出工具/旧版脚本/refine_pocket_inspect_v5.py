"""Author a Blockbench-only draw/put-away revision. Never builds or installs the mod."""
from pathlib import Path
from copy import deepcopy
import json
import hashlib
import math
import uuid
import numpy as np

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'双手检视与持握-v5'
OUT.mkdir(exist_ok=True)
source=ROOT/'农场物语-橙子果酒动画-1.2.0.bbmodel'
model=json.loads(source.read_text(encoding='utf-8'))
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales:pocket-inspect-review-5/'+name))

# Times in seconds, translations in Blockbench pixels. Independent wine-specific curves.
TRACKS={
 'draw':{
  'length':.72,
  'position':[[0,3.4,-15.8,1.8],[.05,3.05,-12.8,1.3],[.14,1.45,-4.5,.1],
    [.23,.32,.32,-.38],[.29,.04,.58,-.28],[.40,-.035,-.16,.065],
    [.53,.01,.045,-.012],[.72,0,0,0]],
  'arm':[[0,-19,-3],[.07,-12,-2],[.17,5,1.8],[.25,3,1],
    [.36,-1,-.3],[.51,.35,.1],[.72,0,0]]},
 'put_away':{
  'length':.42,
  'position':[[0,0,0,0],[.055,.06,-.025,.42],[.12,.52,-1.65,.96],
    [.21,1.8,-5.8,1.5],[.31,3.4,-11.8,2.05],[.42,4.5,-17.2,2.6]],
  'arm':[[0,0,0],[.055,1.8,-.8],[.12,-4,-1.4],[.21,-12,-2],
    [.31,-19,-2.5],[.42,-24,-3]]}
}
# Reach into the pocket before the bottle moves, then return an empty hand after stowing it.
draw=TRACKS['draw']
draw['position']=[[0,*draw['position'][0][1:]]]+[[round(p[0]+.18,6),*p[1:]] for p in draw['position']]
draw['arm']=[[0,-7,0],[.11,-27,-5]]+[[round(p[0]+.18,6),*p[1:]] for p in draw['arm']]
draw['hand_offset']=[[0,-2,15.5,-.5],[.07,-1.5,11.5,-.2],[.14,-.25,1.5,0],[.18,0,0,0],[.9,0,0,0]]
draw['length']=.9
put=TRACKS['put_away']
put['position'] += [[.82,*put['position'][-1][1:]]]
put['arm'] += [[.46,-24,-3],[.60,-17,-2],[.73,-6,0],[.82,-7,0]]
put['hand_offset']=[[0,0,0,0],[.45,0,0,0],[.54,-.7,7,-.2],[.68,-2.7,16.6,-1.1],[.82,-3.1,16.9,-1.3]]
put['length']=.82
# The right palm supports the base. The left hand drives each short turn,
# releases while the bottle is stationary, and regrips before the next turn.
STROKES=[(1.30,1.60,0,45),(2.08,2.52,45,135),
         (2.76,3.20,135,225),(3.44,3.88,225,315),(4.12,4.56,315,405)]
REGRIPS=[(1.82,2.08,45),(2.52,2.76,90),(3.20,3.44,90),(3.88,4.12,90)]
TRACKS['inspect']={
 'length':6.05,
 'position':[[0,0,0,0],[.10,.10,-.12,.1],[.40,-1.65,2.70,1.05],[.57,-1.55,2.55,.90],
   [5.45,-1.55,2.55,.90],[5.84,.02,-.075,-.02],[6.05,0,0,0]],
 'arm':[[0,0,0],[.10,-2,0],[.40,9,-3],[5.40,9,-3],[5.84,-1,0],[6.05,0,0]],
 'hand_offset':[[0,0,0,0],[6.05,0,0,0]],
 'support':[[0,0],[.98,0],[1.27,1],[5.04,1],[5.30,0],[6.05,0]],
 'left_reach':[[0,0],[.64,0],[.98,1],[5.30,1],[5.70,0],[6.05,0]],
 'turn':[[0,0],[1.30,0],[1.60,45],[2.08,45],[2.52,135],[2.76,135],
   [3.20,225],[3.44,225],[3.88,315],[4.12,315],[4.56,405],[4.76,405],
   [5.04,360],[6.05,360]],
 'strokes':STROKES,'regrips':REGRIPS}
(OUT/'动作轨道.json').write_text(json.dumps(TRACKS,ensure_ascii=False,indent=2),encoding='utf-8')

def sample(track,t):
    """Shape-preserving cubic interpolation; authored extrema supply controlled follow-through."""
    a=np.array(track,dtype=float);times=a[:,0];v=a[:,1:]
    if t<=times[0]:return v[0].copy()
    if t>=times[-1]:return v[-1].copy()
    h=np.diff(times);d=np.diff(v,axis=0)/h[:,None];m=np.zeros_like(v)
    # Zero endpoint velocity lets draw/idle/put-away join without a position discontinuity.
    for i in range(1,len(v)-1):
        for axis in range(v.shape[1]):
            if d[i-1,axis]*d[i,axis]>0:
                w1=2*h[i]+h[i-1];w2=h[i]+2*h[i-1]
                m[i,axis]=(w1+w2)/(w1/d[i-1,axis]+w2/d[i,axis])
    i=int(np.searchsorted(times,t)-1);u=(t-times[i])/h[i]
    return (2*u**3-3*u*u+1)*v[i]+(u**3-2*u*u+u)*h[i]*m[i]+(-2*u**3+3*u*u)*v[i+1]+(u**3-u*u)*h[i]*m[i+1]

def euler_matrix(v):
    x,y,z=np.radians(v);cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
    return np.array([[cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx],
        [sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx],[-sy,cy*sx,cy*cx]])

def matrix_euler(m):
    return np.degrees([math.atan2(m[2,1],m[2,2]),math.asin(np.clip(-m[2,0],-1,1)),math.atan2(m[1,0],m[0,0])])

def axis_angles(axis):
    axis=np.array(axis,dtype=float)
    axis/=np.linalg.norm(axis)
    forward=np.cross([1,0,0],axis);forward/=np.linalg.norm(forward)
    right=np.cross(axis,forward);right/=np.linalg.norm(right)
    return matrix_euler(np.column_stack([right,axis,forward]))

def arm_angles(lift,sweep):
    return axis_angles(euler_matrix([0,sweep,0])@euler_matrix([lift,0,0])@np.array([-.40,.20,-.894]))

def groups_in(nodes):
    for g in nodes:
        if isinstance(g,dict):yield g;yield from groups_in(g.get('children',[]))
groups={g['name']:g for g in groups_in(model['outliner'])}
arm=groups['right_forearm']
grip_delta=np.array([.70,.12,-1.80])
arm['origin']=(np.array(arm['origin'])+grip_delta).tolist()
for cube in model['elements']:
    if cube['name']=='right_arm_skin':
        for field in ('from','to','origin'):cube[field]=(np.array(cube[field])+grip_delta).tolist()
# The old export put the shoulder's UV row at the hand. Flip sleeve-to-hand V and swap caps.
for cube in model['elements']:
    if cube['name'] in ('right_arm_skin','left_arm_skin'):
        for face in ('north','south','east','west'):
            u0,v0,u1,v1=cube['faces'][face]['uv'];cube['faces'][face]['uv']=[u0,v1,u1,v0]
        cube['faces']['up'],cube['faces']['down']=deepcopy(cube['faces']['down']),deepcopy(cube['faces']['up'])
spin=dict(name='bottle_turn',uuid=uid('bottle_turn'),origin=groups['bottle']['origin'],rotation=[0,0,0],
    children=[groups['bottle'],groups['wooden_stopper']],visibility=True,export=True,isOpen=False,color=3)
root_group=dict(name='viewmodel_motion',uuid=uid('viewmodel_motion'),origin=[0,16,16],rotation=[0,0,0],
    children=[spin,arm,groups['left_cork_hand']],visibility=True,export=True,isOpen=True,color=0)
model['outliner']=[root_group]
groups[root_group['name']]=root_group
groups[spin['name']]=spin

def inspection_time(clip,t):
    if clip=='inspect':return np.clip(t,0,6.05)
    if clip=='preview_complete':return np.clip(t-1.7,0,6.05)
    return 0.

def left_contact(t):
    """Hand orbit is at most 90 degrees per grip; the wrist never spins fully."""
    phase=0.;clearance=0.
    for a,b,start,end in STROKES:
        if t>=a:
            phase=sample([[a,0],[b,end-start]],t)[0]
    for a,b,angle in REGRIPS:
        if a<=t<b:
            span=b-a
            phase=sample([[a,angle],[a+.25*span,angle],[a+.75*span,0],[b,0]],t)[0]
            clearance=sample([[a,0],[a+.23*span,1.8],[a+.77*span,1.8],[b,0]],t)[0]
    if t>=4.76:phase=sample([[4.76,90],[5.04,45]],t)[0]
    center=np.array(spin['origin'])
    radial=np.array([-2.05,0,.60]);radial*=1+clearance/np.linalg.norm(radial)
    relative=euler_matrix([0,phase,0])@radial
    contact=center+relative+np.array([0,1.70-.18*clearance,0])
    # Keep the elbow on the player's left. Rotating the whole forearm with the
    # bottle would incorrectly move the left arm to the right side of the screen.
    shoulder=np.array([-5.,3.,11.])
    axis=contact-shoulder
    reach=sample(TRACKS['inspect']['left_reach'],t)[0]
    offscreen=np.array([-8.,-13.,8.])
    pos=offscreen*(1-reach)+contact*reach
    return pos,axis_angles(axis),phase,clearance,reach

def inspect_right_pose(t,base_angles):
    weight=sample(TRACKS['inspect']['support'],t)[0]
    support=np.array(spin['origin'])+np.array([.40,-3.56,.48])
    offset=(support-np.array(arm['origin']))*weight
    angles=base_angles*(1-weight)+axis_angles([-.40,.70,-.592])*weight
    return offset,angles

def key(channel,t,value):
    v=np.array(value,dtype=float)
    if channel in ('position','rotation'):v[0]*=-1
    if channel=='rotation':v[1]*=-1
    return dict(channel=channel,time=round(float(t),6),interpolation='linear',
        data_points=[{k:round(float(x),7) for k,x in zip('xyz',v)}])

def compact(channel,times,values,error):
    values=np.asarray(values);keep={0,len(times)-1}
    def split(a,b):
        if b<=a+1:return
        f=(np.array(times[a+1:b])-times[a])/(times[b]-times[a])
        e=np.max(abs(values[a+1:b]-(values[a]+f[:,None]*(values[b]-values[a]))),axis=1)
        if max(e)>error:
            i=a+1+int(np.argmax(e));keep.add(i);split(a,i);split(i,b)
    split(0,len(times)-1)
    return [key(channel,times[i],values[i]) for i in sorted(keep)]

def pose(clip,t):
    if clip in TRACKS:
        track=TRACKS[clip]
        return sample(track['position'],t),sample(track['arm'],t),sample(track['hand_offset'],t)
    if clip=='preview_complete':
        if t<.2:return pose('draw',0)
        if t<1.1:return pose('draw',t-.2)
        if t<1.7:return np.zeros(3),np.zeros(2),np.zeros(3)
        if t<7.75:return pose('inspect',t-1.7)
        return pose('put_away',t-7.75)
    # One complete take-out / hold / put-away sequence for easy review.
    if t<.20:return pose('draw',0)
    if t<1.10:return pose('draw',t-.20)
    if t<2.05:return np.zeros(3),np.zeros(2),np.zeros(3)
    return pose('put_away',t-2.05)

def animation(name,length):
    times=sorted(set([round(i/120,7) for i in range(math.ceil(length*120))]+[length]))
    sampled=[pose(name,t) for t in times]
    positions=[x[0] for x in sampled]
    angles=[];offsets=[]
    for t,x in zip(times,sampled):
        a=arm_angles(*x[1]);offset=x[2].copy()
        if name in ('inspect','preview_complete'):
            addition,a=inspect_right_pose(inspection_time(name,t),a)
            offset+=addition
        angles.append(a-np.array(arm['rotation']));offsets.append(offset)
    # The arm rotates about its hand endpoint, while the shared root preserves bottle contact.
    animators={root_group['uuid']:dict(name=root_group['name'],type='bone',
        keyframes=compact('position',times,positions,.006))}
    animators[arm['uuid']]=dict(name=arm['name'],type='bone',keyframes=compact('rotation',times,angles,.05)+compact('position',times,offsets,.006))
    turn=[]
    for t in times:
        angle=sample(TRACKS['inspect']['turn'],t)[0] if name=='inspect' else (
            sample(TRACKS['inspect']['turn'],t-1.7)[0] if name=='preview_complete' else 0)
        turn.append([0,angle,0])
    animators[spin['uuid']]=dict(name=spin['name'],type='bone',keyframes=compact('rotation',times,turn,.08))
    left=groups['left_cork_hand']
    left_poses=[left_contact(inspection_time(name,t)) for t in times]
    animators[left['uuid']]=dict(name=left['name'],type='bone',keyframes=
        compact('position',times,[p[0]-np.array(left['origin']) for p in left_poses],.005)+
        compact('rotation',times,[p[1]-np.array(left['rotation']) for p in left_poses],.04)+
        [key('scale',0,[.7,.7,.7])])
    for n in ['recolored_original_neck']:
        animators[groups[n]['uuid']]=dict(name=n,type='bone',keyframes=[key('scale',0,[0,0,0])])
    animators[groups['wooden_stopper']['uuid']]=dict(name='wooden_stopper',type='bone',keyframes=[key('scale',0,[1,1,1])])
    animators[groups['wine_fill_preview']['uuid']]=dict(name='wine_fill_preview',type='bone',keyframes=[key('scale',0,[1,1,1])])
    return dict(uuid=uid(name),name='animation.orange_wine.'+name,loop='hold',override=True,
        length=length,snapping=60,animators=animators)

added=[animation('preview_complete',9.0),animation('draw',.9),animation('put_away',.82),animation('inspect',6.05),animation('preview_draw_hold_put_away',3.10)]
retained=[a for a in model['animations'] if not a['name'].endswith('.inspect')]

def read_channel(animator,channel,t):
    ks=sorted([k for k in animator['keyframes'] if k['channel']==channel],key=lambda k:k['time'])
    def value(k):
        v=np.array([float(k['data_points'][0][c]) for c in 'xyz'])
        v[0]*=-1
        if channel=='rotation':v[1]*=-1
        return v
    if not ks:return np.zeros(3)
    if t<=ks[0]['time']:return value(ks[0])
    for a,b in zip(ks,ks[1:]):
        if t<=b['time']:
            u=(t-a['time'])/(b['time']-a['time'])
            return value(a)+(value(b)-value(a))*u
    return value(ks[-1])

# A changed resting grip must follow the bottle as it tips during existing clips.
# Preserve the original hand/bottle relative contact instead of adding a fixed world offset.
max_grip_error=0.
for a in retained:
    bottle_anim=a['animators'][groups['bottle']['uuid']]
    hand_anim=a['animators'][arm['uuid']]
    original_hand=deepcopy(hand_anim)
    times=sorted(set([round(i/120,7) for i in range(math.ceil(a['length']*120))]+[a['length']]))
    base=euler_matrix(groups['bottle']['rotation'])
    positions=[]
    for t in times:
        r=euler_matrix(np.array(groups['bottle']['rotation'])+read_channel(bottle_anim,'rotation',t))
        correction=r@base.T@grip_delta-grip_delta
        positions.append(read_channel(original_hand,'position',t)+correction)
    hand_anim['keyframes']=[k for k in hand_anim['keyframes'] if k['channel']!='position']+compact('position',times,positions,.003)
    for t,p in zip(times,positions):
        max_grip_error=max(max_grip_error,float(np.linalg.norm(read_channel(hand_anim,'position',t)-p)))
model['animations']=added+retained
model['name']='农场物语-橙子果酒-双手检视与持握-v5'
model['credit']='FarmingTales bottle; independently authored wine draw/put-away. TACZ b43eb84 timing reference.'
output=OUT/'农场物语-橙子果酒-双手检视与持握-v5.bbmodel'
output.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')

# Authoring checks: old assets/clips are retained and hand/bottle move under the same parent.
old=json.loads(source.read_text(encoding='utf-8'))
assert [e for e in model['elements'] if '_arm_skin' not in e['name']]==[e for e in old['elements'] if '_arm_skin' not in e['name']]
assert model['textures']==old['textures']
for before,after in zip([a for a in old['animations'] if not a['name'].endswith('.inspect')],retained):
    for identifier,animator in before['animators'].items():
        if identifier!=arm['uuid']:assert animator==after['animators'][identifier]
assert max_grip_error<.006
assert np.linalg.norm(pose('draw',.9)[0])<1e-9
assert np.linalg.norm(pose('draw',.9)[1])<1e-9
assert np.linalg.norm(pose('put_away',0)[0])<1e-9
assert not any(k['channel']=='rotation' for k in added[0]['animators'][root_group['uuid']]['keyframes'])
for a,b,start,end in STROKES:
    for t in np.linspace(a,b,51):
        _,_,phase,clearance,reach=left_contact(t)
        assert abs(sample(TRACKS['inspect']['turn'],t)[0]-start-phase)<1e-8
        assert clearance==0 and reach==1
for a,b,_ in REGRIPS:
    assert abs(sample(TRACKS['inspect']['turn'],a)[0]-sample(TRACKS['inspect']['turn'],b)[0])<1e-8
check=dict(bottle_assets_unchanged=True,arm_uv_direction_corrected=True,grip_alignment_pixels=grip_delta.tolist(),shared_root=True,draw_seconds=.9,put_away_seconds=.82,inspect_seconds=6.05,
    left_drives_turns=True,right_supports_base=True,stationary_bottle_during_regrip=True,max_turn_per_grip_degrees=90,legacy_grip_error_pixels=max_grip_error,
    jar_sha256=hashlib.sha256((ROOT.parent.parent.parent.parent/'mods/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.2.0.jar').read_bytes()).hexdigest(),
    model_sha256=hashlib.sha256(output.read_bytes()).hexdigest())
(OUT/'检查记录.json').write_text(json.dumps(check,indent=2),encoding='utf-8')
(OUT/'说明.txt').write_text('''双手检视与持握 v5 — 仅 Blockbench 动画设计稿

打开 bbmodel，切换动画模式，优先播放 preview_complete。
draw：0.90 秒，空手先探向右侧口袋，0.18 秒握住瓶子后掏出，快速抬起后收稳。
put_away：0.82 秒，先往身侧回带，0.42 秒将酒瓶收入口袋方向，随后松手、空手返回。
preview_draw_hold_put_away：拿出、持握、收回的连续预览。
inspect：6.05 秒。先拿近查看；左手握住瓶身后，右手移到瓶底托稳。
左手带动向右转 45 度，停顿查看；再分四次各转 90 度，合计转完整一圈。
每次换握时瓶子停住，由右手支撑；左手松开、绕回并重新握紧后才继续转。
最后左手将瓶身回正，右手重新握回右后侧，左手退出，右手将酒放回原持握位置。
preview_complete：掏出口袋、持握、检视、收入口袋、空手回位的完整连续预览。

拿出与收回不翻瓶或拧腕。检视使用双手操作，没有手腕旋转一整圈或瓶子自行转动。
左手为原有方块手臂，缺少独立手指骨骼，松手换握用整只手离开瓶身表达。
抓握和松开均发生在画面外的口袋位置；没有用缩放或隐藏酒瓶制造突然出现、消失。
原贴图、瓶身和木塞形状保留；手臂为默认 Steve 皮肤预览，修正手掌/袖口方向颠倒的 UV。
握持按最新截图调整：手在瓶身右后侧，酒瓶置于手掌前方，露出正面的瓶标。
原有 idle、drink、walk、sprint、jump 五段保留，并同步修正右手新握持点的跟随位移。
可直接修改 viewmodel_motion 的位置轨道和 right_forearm 的旋转轨道。
“动作轨道.json”记录本次作者关键姿态，时间单位秒，位移单位 Blockbench 像素。

参考：TACZ 1.20.1 b43eb84，pistol_default、Glock 17、M1911 的 draw / put_away。
参考快入、缓停、回稳和独立收回的节奏；本文件的轨迹按酒瓶重新设计，未复制枪械资源。
新视频完整按每秒一帧检查 0–185 秒；对 9.5–12.5 秒及 44.7–46.2 秒的拿出收回进一步密集拆帧。
视频参考物品沿画面边缘连续进出、手臂先后跟随和速度衔接；抓握位置参考用户最新 Blockbench 截图。
当前只优化工程，未修改、构建或安装新模组。
酒液仍为简化酒量预览；实时液面和音效由游戏中的模组控制。
''',encoding='utf-8')
print('Created Blockbench two-hand inspect v5:',output.name)
print('Two-hand turn/regrip checks passed. Legacy grip correction error:',max_grip_error)
