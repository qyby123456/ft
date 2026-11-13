"""Author a Blockbench-only draw/put-away revision. Never builds or installs the mod."""
from pathlib import Path
from copy import deepcopy
import json
import hashlib
import math
import uuid
import numpy as np

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'口袋拿取与检视-v3'
OUT.mkdir(exist_ok=True)
source=ROOT/'农场物语-橙子果酒动画-1.2.0.bbmodel'
model=json.loads(source.read_text(encoding='utf-8'))
uid=lambda name:str(uuid.uuid5(uuid.NAMESPACE_URL,'farmingtales:pocket-inspect-review-3/'+name))

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
TRACKS['inspect']={
 'length':3.9,
 'position':[[0,0,0,0],[.10,.10,-.12,.1],[.40,-1.35,2.0,1.2],[.57,-1.25,1.85,1.08],
   [1.02,-1.25,1.85,1.08],[1.45,-1.10,1.82,1.02],[1.68,-1.10,1.82,1.02],
   [2.90,-1.10,1.82,1.02],[3.12,-1.10,1.82,1.02],[3.64,.02,-.075,-.02],[3.90,0,0,0]],
 'arm':[[0,0,0],[.10,-2,0],[.36,11,-3],[.57,9,-3],[1.02,9,-3],
   [1.45,8,-5],[1.68,8,-5],[2.1,7,-3],[2.50,9,-4],[2.90,8,-5],
   [3.12,8,-5],[3.64,-1,0],[3.90,0,0]],
 'hand_offset':[[0,0,0,0],[3.9,0,0,0]],
 'turn':[[0,0],[1.02,0],[1.45,45],[1.68,45],[1.87,60],[2.16,158],
   [2.53,324],[2.90,405],[3.12,405],[3.62,360],[3.9,360]]}
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

def arm_angles(lift,sweep):
    axis=euler_matrix([0,sweep,0])@euler_matrix([lift,0,0])@np.array([-.40,.20,-.894])
    axis/=np.linalg.norm(axis)
    forward=np.cross([1,0,0],axis);forward/=np.linalg.norm(forward)
    right=np.cross(axis,forward);right/=np.linalg.norm(right)
    return matrix_euler(np.column_stack([right,axis,forward]))

def groups_in(nodes):
    for g in nodes:
        if isinstance(g,dict):yield g;yield from groups_in(g.get('children',[]))
groups={g['name']:g for g in groups_in(model['outliner'])}
arm=groups['right_forearm']
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
        if t<5.6:return pose('inspect',t-1.7)
        return pose('put_away',t-5.6)
    # One complete take-out / hold / put-away sequence for easy review.
    if t<.20:return pose('draw',0)
    if t<1.10:return pose('draw',t-.20)
    if t<2.05:return np.zeros(3),np.zeros(2),np.zeros(3)
    return pose('put_away',t-2.05)

def animation(name,length):
    times=sorted(set([round(i/120,7) for i in range(math.ceil(length*120))]+[length]))
    sampled=[pose(name,t) for t in times]
    positions=[x[0] for x in sampled]
    angles=[arm_angles(*x[1])-np.array(arm['rotation']) for x in sampled]
    # The arm rotates about its hand endpoint, while the shared root preserves bottle contact.
    animators={root_group['uuid']:dict(name=root_group['name'],type='bone',
        keyframes=compact('position',times,positions,.006))}
    offsets=[x[2] for x in sampled]
    animators[arm['uuid']]=dict(name=arm['name'],type='bone',keyframes=compact('rotation',times,angles,.05)+compact('position',times,offsets,.006))
    turn=[]
    for t in times:
        angle=sample(TRACKS['inspect']['turn'],t)[0] if name=='inspect' else (
            sample(TRACKS['inspect']['turn'],t-1.7)[0] if name=='preview_complete' else 0)
        turn.append([0,angle,0])
    animators[spin['uuid']]=dict(name=spin['name'],type='bone',keyframes=compact('rotation',times,turn,.08))
    for n in ['left_cork_hand','recolored_original_neck']:
        animators[groups[n]['uuid']]=dict(name=n,type='bone',keyframes=[key('scale',0,[0,0,0])])
    animators[groups['wooden_stopper']['uuid']]=dict(name='wooden_stopper',type='bone',keyframes=[key('scale',0,[1,1,1])])
    animators[groups['wine_fill_preview']['uuid']]=dict(name='wine_fill_preview',type='bone',keyframes=[key('scale',0,[1,1,1])])
    return dict(uuid=uid(name),name='animation.orange_wine.'+name,loop='hold',override=True,
        length=length,snapping=60,animators=animators)

added=[animation('preview_complete',6.85),animation('draw',.9),animation('put_away',.82),animation('inspect',3.9),animation('preview_draw_hold_put_away',3.10)]
retained=[a for a in model['animations'] if not a['name'].endswith('.inspect')]
model['animations']=added+retained
model['name']='农场物语-橙子果酒-口袋拿取与检视-v3'
model['credit']='FarmingTales bottle; independently authored wine draw/put-away. TACZ b43eb84 timing reference.'
output=OUT/'农场物语-橙子果酒-口袋拿取与检视-v3.bbmodel'
output.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')

# Authoring checks: old assets/clips are retained and hand/bottle move under the same parent.
old=json.loads(source.read_text(encoding='utf-8'))
assert [e for e in model['elements'] if '_arm_skin' not in e['name']]==[e for e in old['elements'] if '_arm_skin' not in e['name']]
assert model['textures']==old['textures']
assert model['animations'][5:]==[a for a in old['animations'] if not a['name'].endswith('.inspect')]
assert np.linalg.norm(pose('draw',.9)[0])<1e-9
assert np.linalg.norm(pose('draw',.9)[1])<1e-9
assert np.linalg.norm(pose('put_away',0)[0])<1e-9
assert not any(k['channel']=='rotation' for k in added[0]['animators'][root_group['uuid']]['keyframes'])
check=dict(bottle_assets_unchanged=True,arm_uv_direction_corrected=True,shared_root=True,draw_seconds=.9,put_away_seconds=.82,inspect_seconds=3.9,
    jar_sha256=hashlib.sha256((ROOT.parent.parent.parent.parent/'mods/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.2.0.jar').read_bytes()).hexdigest(),
    model_sha256=hashlib.sha256(output.read_bytes()).hexdigest())
(OUT/'检查记录.json').write_text(json.dumps(check,indent=2),encoding='utf-8')
(OUT/'说明.txt').write_text('''口袋拿取与检视 v3 — 仅 Blockbench 动画设计稿

打开 bbmodel，切换动画模式，优先播放 preview_complete。
draw：0.90 秒，空手先探向右侧口袋，0.18 秒握住瓶子后掏出，快速抬起后收稳。
put_away：0.82 秒，先往身侧回带，0.42 秒将酒瓶收入口袋方向，随后松手、空手返回。
preview_draw_hold_put_away：拿出、持握、收回的连续预览。
inspect：先拿近查看，向右转 45 度停顿，再顺向转完整 360 度，回正并放回持握位置。
preview_complete：掏出口袋、持握、检视、收入口袋、空手回位的完整连续预览。

拿出与收回不翻瓶或拧腕。检视按最新要求转动瓶身；前臂轻微配合，围绕握持端改变方向。
抓握和松开均发生在画面外的口袋位置；没有用缩放或隐藏酒瓶制造突然出现、消失。
原贴图、瓶身和木塞形状保留；手臂为默认 Steve 皮肤预览，修正手掌/袖口方向颠倒的 UV。
原有 idle、drink、walk、sprint、jump 五段保留在工程中。
可直接修改 viewmodel_motion 的位置轨道和 right_forearm 的旋转轨道。
“动作轨道.json”记录本次作者关键姿态，时间单位秒，位移单位 Blockbench 像素。

参考：TACZ 1.20.1 b43eb84，pistol_default、Glock 17、M1911 的 draw / put_away。
参考快入、缓停、回稳和独立收回的节奏；本文件的轨迹按酒瓶重新设计，未复制枪械资源。
新视频完整按每秒一帧检查 0–185 秒；对 9.5–12.5 秒及 44.7–46.2 秒的拿出收回进一步密集拆帧。
视频参考物品沿画面边缘连续进出、手臂先后跟随和速度衔接；抓握位置参考用户最新 Blockbench 截图。
当前只优化工程，未修改、构建或安装新模组。
酒液仍为简化酒量预览；实时液面和音效由游戏中的模组控制。
''',encoding='utf-8')
print('Created Blockbench pocket/inspect v3:',output.name)
print('Original bottle retained; arm UV corrected; pocket draw, stow and new inspection authored.')
