"""Create a portable Blockbench 4.10+ project from the actual Java rig samples."""
from pathlib import Path
from copy import deepcopy
import base64
import io
import json
import math
import uuid
import zipfile
import numpy as np
from PIL import Image

root = Path(__file__).resolve().parent
pack = root.parents[2]
assets = root / 'src/main/resources/assets/farmingtales_drink_anim'
out = root / 'blockbench'
out.mkdir(exist_ok=True)
clips = json.loads((root / 'build/blockbench/runtime-motion.json').read_text())
uid = lambda name: str(uuid.uuid5(uuid.NAMESPACE_URL, 'farmingtales:orange-wine-1.2/' + name))
elements, groups, textures = [], [], []
shift = np.array([0, 16, 16])

def euler(q):
    x, y, z, w = np.array(q) / np.linalg.norm(q)
    return np.degrees([math.atan2(2*(y*z+w*x),1-2*(x*x+y*y)),
                       math.asin(np.clip(2*(w*y-z*x),-1,1)),
                       math.atan2(2*(x*y+w*z),1-2*(y*y+z*z))])

def texture(name, data):
    im = Image.open(io.BytesIO(data))
    textures.append(dict(name=name, id=str(len(textures)), uuid=uid(name), mode='bitmap',
        source='data:image/png;base64,'+base64.b64encode(data).decode(), internal=True,
        width=im.width,height=im.height,uv_width=im.width,uv_height=im.height,render_mode='default',render_sides='front'))
    return len(textures)-1

wine_tex=texture('orange_wine.png',(assets/'textures/orange_wine.png').read_bytes())
open_tex=texture('orange_wine_open.png',(assets/'textures/orange_wine_open.png').read_bytes())
with zipfile.ZipFile(pack/'.gradle-user/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar') as jar:
    skin_tex=texture('steve_preview.png',jar.read('assets/minecraft/textures/entity/player/wide/steve.png'))

rest=clips['idle']['frames'][0]
def group(name, transform=None, origin=None):
    pos=np.array(transform['position'])+shift if transform else np.array(origin)
    rotation=euler(transform['quaternion']) if transform else np.zeros(3)
    g=dict(name=name,uuid=uid(name),origin=pos.tolist(),rotation=rotation.tolist(),children=[],
           export=True,visibility=True,isOpen=False,color=len(groups)%8)
    groups.append(g)
    return g

bottle=group('bottle',rest['bottle'])
hand=group('right_forearm',rest['hand'])
helper=group('left_cork_hand',rest['helper'])
cork=group('wooden_stopper',rest['cork'])
neck=group('recolored_original_neck',origin=bottle['origin'])
liquid_origin=np.array(bottle['origin'])+[0,(1-4.8)*.61,0]
liquid=group('wine_fill_preview',origin=liquid_origin)
bottle['children'] += [neck,liquid]

def cube_from_original(el,name,parent,tex,pivot,scale=.61):
    origin=np.array(parent['origin'])
    def point(p):return (origin+(np.array(p)-pivot)*scale).tolist()
    rotation=[0,0,0]
    r=el.get('rotation',{})
    if r:rotation['xyz'.index(r['axis'])]=r['angle']
    faces={key:dict(uv=value['uv'],texture=tex,rotation=value.get('rotation',0)) for key,value in el['faces'].items()}
    cube=dict(name=name,uuid=uid(name),type='cube',from_=point(el['from']),to=point(el['to']),
              origin=point(r.get('origin',pivot)),rotation=rotation,faces=faces,box_uv=False,
              autouv=0,shade=True,rescale=False,visibility=True,export=True)
    cube['from']=cube.pop('from_')
    elements.append(cube);parent['children'].append(cube['uuid'])

body=json.loads((assets/'models/animation/orange_bottle.json').read_text())
for i,el in enumerate(body['elements']):cube_from_original(el,f'bottle_original_{i}',bottle,wine_tex,[8,4.8,8])
stopper=json.loads((assets/'models/animation/orange_cork.json').read_text())['elements'][0]
cube_from_original(stopper,'original_yellow_stopper',cork,wine_tex,[8,16,8])
cube_from_original(stopper,'original_neck_recolored_only',neck,open_tex,[8,4.8,8])

def arm(parent,name,scale,left=False):
    # Vanilla wide arm around the hand endpoint. In game the player's own skin is used.
    u,v=(32,48) if left else (40,16)
    uv={'east':[u,v+4,u+4,v+16],'north':[u+4,v+4,u+8,v+16],
        'west':[u+8,v+4,u+12,v+16],'south':[u+12,v+4,u+16,v+16],
        'up':[u+4,v,u+8,v+4],'down':[u+8,v+4,u+12,v]}
    source={'from':[-2,-12,-2],'to':[2,0,2],'faces':{k:{'uv':x} for k,x in uv.items()}}
    cube_from_original(source,name,parent,skin_tex,[0,0,0],scale)
arm(hand,'right_arm_skin',.70)
arm(helper,'left_arm_skin',1,True)
fluid={'from':[6.25,1,6.25],'to':[9.75,8.68,9.75],
       'faces':{k:{'uv':[4,0,7,8]} for k in ['north','south','east','west','up','down']}}
fluid['faces']['up']['uv']=[7,12,10,15]
cube_from_original(fluid,'wine_volume_proxy',liquid,wine_tex,[8,1,8])

def simplify(times,values,tolerance):
    values=np.asarray(values);times=np.asarray(times)
    selected={0,len(times)-1}
    def split(a,b):
        if b<=a+1:return
        t=(times[a+1:b]-times[a])/(times[b]-times[a])
        delta=values[a+1:b]-(values[a]+t[:,None]*(values[b]-values[a]))
        error=np.max(np.abs(delta),axis=1)
        index=int(np.argmax(error))+a+1
        if max(error)>tolerance:selected.add(index);split(a,index);split(index,b)
    split(0,len(times)-1)
    return sorted(selected)

def keys(channel,times,values,tolerance):
    result=[]
    for i in simplify(times,values,tolerance):
        xyz=np.array(values[i],dtype=float)
        # 4.x coordinates are converted automatically on load by Blockbench 5.x.
        if channel in ('rotation','position'):xyz[0]*=-1
        if channel=='rotation':xyz[1]*=-1
        result.append(dict(channel=channel,time=round(times[i],6),interpolation='linear',
            data_points=[{k:round(float(v),7) for k,v in zip('xyz',xyz)}]))
    return result

def visibility_keys(frames,predicate):
    result=[];previous=None
    for f in frames:
        value=1 if predicate(f) else 0
        if value!=previous:
            result.append(dict(channel='scale',time=round(f['time'],6),interpolation='step',
                data_points=[dict(x=value,y=value,z=value)]));previous=value
    return result

animations=[]
for name,clip in clips.items():
    frames=clip['frames'];times=[f['time'] for f in frames];animators={}
    for field,g in [('bottle',bottle),('hand',hand),('helper',helper),('cork',cork)]:
        positions=[np.array(f[field]['position'])+shift-np.array(g['origin']) for f in frames]
        angles=np.degrees(np.unwrap(np.radians([euler(f[field]['quaternion']) for f in frames]),axis=0))-np.array(g['rotation'])
        k=keys('position',times,positions,.006)+keys('rotation',times,angles,.065)
        if field=='helper':k+=visibility_keys(frames,lambda f:f['helperVisible'])
        if field=='cork':k+=visibility_keys(frames,lambda f:f['corkVisible'])
        animators[g['uuid']]=dict(name=g['name'],type='bone',keyframes=k)
    animators[neck['uuid']]=dict(name=neck['name'],type='bone',keyframes=visibility_keys(frames,lambda f:f['open']))
    animators[liquid['uuid']]=dict(name=liquid['name'],type='bone',
        keyframes=keys('scale',times,[[1,f['fill']/.8,1] for f in frames],.001))
    animations.append(dict(uuid=uid('clip/'+name),name='animation.orange_wine.'+name,
        loop='loop' if name=='idle' else 'once',override=True,length=clip['length'],snapping=60,animators=animators,
        markers=([dict(time=1.35,color=1),dict(time=2.4,color=2),dict(time=4.4,color=3)] if name=='drink' else [])))

model=dict(meta=dict(format_version='4.10',model_format='free',box_uv=False),
           name='农场物语-橙子果酒动画 1.2.0',model_identifier='orange_wine',
           credit='FarmingTales original bottle model; animation exported from BottleRig 1.2.0',
           resolution=dict(width=16,height=16),elements=elements,textures=textures,
           outliner=[bottle,hand,helper,cork],animations=animations)
path=out/'农场物语-橙子果酒动画-1.2.0.bbmodel'
path.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
(out/'使用说明.txt').write_text('''农场物语-橙子果酒动画 1.2.0

用 Blockbench 4.10 或更新版本：文件 → 打开模型，选择同目录 .bbmodel。
切换右上角“动画”模式，在左侧选择动作，按空格播放。

idle：正常持握。
inspect：正常拿出来，停顿，再收回；不转腕、不翻瓶。
drink：握塞、向上拔塞、前臂抬瓶饮用、放下。木塞在 1.35 秒拔出，2.4 秒开始慢饮。
walk / sprint：行走/跑步进入、移动、停下。
jump：起跳与落地惯性。

包含原酒瓶几何、原贴图及仅换色的瓶口贴图、左右手和木塞；贴图已内嵌。
手臂预览使用 Minecraft 默认 Steve 皮肤，游戏中跟随玩家自己的皮肤。
动作位置与旋转由模组实际 BottleRig 以 60 Hz 采样导出，并精简为可编辑关键帧。
这是动画编辑工程，不是另一个模组，也不能直接把 bbmodel 放进 mods。
修改工程后不会自动改变 Java 模组，需要同步修改并重新构建。
酒液在此工程中仅提供酒量下降的简化预览；游戏中的实时液面裁切、流动和运动阻尼由模组代码计算。
音效在游戏中播放，预览视频含同步声音；bbmodel 本身未嵌入音轨。
''',encoding='utf-8')

all_ids={e['uuid'] for e in elements}|{g['uuid'] for g in groups}
assert len(all_ids)==len(elements)+len(groups)
for anim in animations:
    assert set(anim['animators']).issubset(all_ids)
    for animator in anim['animators'].values():
        assert all(0<=k['time']<=anim['length']+1e-5 for k in animator['keyframes'])
        for k in animator['keyframes']:
            assert all(math.isfinite(v) for v in k['data_points'][0].values())
assert len(animations)==6 and len(textures)==3
print(json.dumps(dict(file=str(path),bytes=path.stat().st_size,clips=[a['name'] for a in animations],
    elements=len(elements),bones=len(groups),keyframes=sum(len(t['keyframes']) for a in animations for t in a['animators'].values())),ensure_ascii=False))
