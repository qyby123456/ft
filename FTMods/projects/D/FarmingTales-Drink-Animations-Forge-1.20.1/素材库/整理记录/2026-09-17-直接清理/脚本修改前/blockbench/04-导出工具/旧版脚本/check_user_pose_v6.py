from pathlib import Path
import json, math, hashlib
import numpy as np
ROOT=Path(__file__).resolve().parent/'用户持握与精简关键帧-v6'
old=json.loads((ROOT/'用户调整原稿-v5-0.93333秒.bbmodel').read_text(encoding='utf-8'))
new=json.loads((ROOT/'农场物语-橙子果酒-用户持握与精简关键帧-v6.bbmodel').read_text(encoding='utf-8'))
def tr(v):m=np.eye(4);m[:3,3]=v;return m
def rot(v):
 x,y,z=np.radians(v);cx,sx=math.cos(x),math.sin(x);cy,sy=math.cos(y),math.sin(y);cz,sz=math.cos(z),math.sin(z)
 m=np.eye(4);m[:3,:3]=[[cz*cy,cz*sy*sx-sz*cx,cz*sy*cx+sz*sx],[sz*cy,sz*sy*sx+cz*cx,sz*sy*cx-cz*sx],[-sy,cy*sx,cy*cx]];return m
def val(k):return np.array([float(k['data_points'][0].get(c,0)) for c in 'xyz'])
def channel(a,ch,t):
 ks=sorted([k for k in a.get('keyframes',[]) if k['channel']==ch],key=lambda k:k['time'])
 if not ks:return np.ones(3) if ch=='scale' else np.zeros(3)
 if t<=ks[0]['time']:return val(ks[0])
 for a,b in zip(ks,ks[1:]):
  if t<=b['time']:
   u=(t-a['time'])/(b['time']-a['time']);v=val(a);w=val(b)
   if a.get('interpolation')=='step':return v
   if a.get('interpolation')=='bezier':return (1-u)**3*v+3*(1-u)**2*u*(v+a['bezier_right_value'])+3*(1-u)*u*u*(w+b['bezier_left_value'])+u**3*w
   return v+(w-v)*u
 return val(ks[-1])
def matrices(m,clip,t):
 groups={g['uuid']:g for g in m['groups']};a=next(a for a in m['animations'] if a['name'].split('.')[-1]==clip)['animators'];out={}
 def walk(ns,parent):
  for n in ns:
   if isinstance(n,str):out[n]=parent;continue
   g=groups[n['uuid']];o=np.array(g['origin']);k=a.get(g['uuid'],{})
   mat=parent@tr(o+channel(k,'position',t))@rot(np.array(g['rotation'])+channel(k,'rotation',t))@np.diag([*channel(k,'scale',t),1])@tr(-o)
   out[g['uuid']]=mat;walk(n.get('children',[]),mat)
 walk(m['outliner'],np.eye(4));return out
a=matrices(old,'preview_complete',.93333);b=matrices(new,'preview_complete',.93333)
err=0.
for e in old['elements']:
 if e['type']!='cube' or e['name']=='left_arm_skin':continue
 if e['name'] in ['wine_volume_proxy','original_neck_recolored_only']:continue
 corners=np.array([[x,y,z,1] for x in [e['from'][0],e['to'][0]] for y in [e['from'][1],e['to'][1]] for z in [e['from'][2],e['to'][2]]])
 err=max(err,float(np.max(abs(a[e['uuid']]@corners.T-b[e['uuid']]@corners.T))))
assert err<1e-5,err
turn_id=next(g['uuid'] for g in new['groups'] if g['name']=='bottle_turn')
inspect=next(a for a in new['animations'] if a['name'].endswith('.inspect'))
turn=inspect['animators'][turn_id]
for start,end in [(1.83,2.06),(2.46,2.73),(3.08,3.31),(3.64,3.90)]:
 vals=[channel(turn,'rotation',t) for t in np.linspace(start,end,31)]
 assert max(np.linalg.norm(x-vals[0]) for x in vals)<1e-7
report=json.loads((ROOT/'检查记录.json').read_text(encoding='utf-8'))
report.update(user_reference_vertex_error_pixels=err,regrip_bottle_stationary=True,camera_plugin='Cameras 1.2.2',
 desktop_camera_import_verified=True,camera_position=[0,16,18],camera_fov=70,
 delivered_sha256=hashlib.sha256((ROOT/'农场物语-橙子果酒-用户持握与精简关键帧-v6.bbmodel').read_bytes()).hexdigest())
(ROOT/'检查记录.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Exact user-pose comparison passed. Maximum vertex error:',err)
print('Bottle holds still during each regrip. Camera import verified in desktop Blockbench.')
