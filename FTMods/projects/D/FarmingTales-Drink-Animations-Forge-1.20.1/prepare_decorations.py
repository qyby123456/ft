"""Separate existing ornament meshes; never edit the source pack or redraw its textures."""
from pathlib import Path
from copy import deepcopy
import json,math,zipfile
ROOT=Path(__file__).absolute().parent
A=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
BASE=ROOT/'backups/1.8.0-before-motion-refinement/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.0.jar'
TARGETS={n:(6,'LEAF') for n in ['red_grape_juice_1','blueberry_juice_1','sweet_berry_juice_1','white_grape_juice_1']}
TARGETS.update({n:(7,'RIBBON') for n in ['mysticfruit_wine','cherry_liqueur']})
TARGETS.update({'pumpkin_wine_cup':(2,'STICK'),'coconut_wine_cup_1':(5,'ICE'),'mysticfruit_wine_cup_1':(4,'ICE'),'rum_cup_1':(4,'ICE')})
def dump(path,v):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def rotated(e,p):
 r=e.get('rotation',{});o=r.get('origin',[0,0,0]);v=[p[i]-o[i] for i in range(3)];j,k={'x':(1,2),'y':(2,0),'z':(0,1)}[r.get('axis','y')];a=math.radians(r.get('angle',0))
 v[j],v[k]=v[j]*math.cos(a)-v[k]*math.sin(a),v[j]*math.sin(a)+v[k]*math.cos(a)
 return [round(v[i]+o[i],6) for i in range(3)]
catalog={}
with zipfile.ZipFile(BASE) as z:
 for p in json.loads((A/'drink_profiles.json').read_text()):
  name=p['id'].split(':')[1]
  if name not in TARGETS:continue
  idx,kind=TARGETS[name];path='models/'+p['bodyModel']+'.json';body=json.loads(z.read('assets/farmingtales_drink_anim/'+path))
  if kind=='RIBBON':
   for i,dy in [(5,.15),(6,.15),(7,.25)]:
    e=body['elements'][i]
    for key in ['from','to']:e[key][1]-=dy
    e['rotation']['origin'][1]-=dy
  e=body['elements'].pop(idx);a,b=e['from'],e['to'];center=[(a[i]+b[i])/2 for i in range(3)]
  if kind=='LEAF':pivot=rotated(e,[b[0]-.8,a[1],a[2]])
  elif kind=='RIBBON':pivot=rotated(e,[b[0],b[1]-.15,a[2]])
  elif kind=='STICK':pivot=rotated(e,[a[0]+.35,center[1],center[2]])
  else:pivot=rotated(e,center)
  extent=[0,0,0]
  for x in [a[0],b[0]]:
   for y in [a[1],b[1]]:
    for zz in [a[2],b[2]]:
     point=rotated(e,[x,y,zz]);extent=[max(extent[j],abs(point[j]-pivot[j])) for j in range(3)]
  model='animation/decorations/'+name+'_'+kind.lower();part=deepcopy(body);part['elements']=[e]
  dump(A/('models/'+model+'.json'),part);dump(A/path,body)
  catalog[p['id']]=[dict(name=kind.lower(),model=model,kind=kind,pivot=pivot,extent=extent)]
dump(A/'decorations.json',catalog)
print('Separated',len(catalog),'ornaments with original UVs and fixed attachment pivots.')
