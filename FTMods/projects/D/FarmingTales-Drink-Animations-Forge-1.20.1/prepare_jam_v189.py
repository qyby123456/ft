from pathlib import Path
from PIL import Image
import copy,json
R=Path(__file__).resolve().parent; A=R/'src/main/resources/assets/farmingtales_drink_anim'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
original=read(R.parents[2]/'kubejs/assets/farmingtales/models/block/bakerys/ancient_fruit_jam.json')
body=read(A/'models/animation/jam/body.json')
body['elements']=copy.deepcopy(original['elements'][:3])
write(A/'models/animation/jam/body.json',body)
opened=copy.deepcopy(body);opened['textures']['4']='farmingtales_drink_anim:jam/ancient_fruit_jam_sealed'
rot=copy.deepcopy(original['elements'][2]['rotation'])
for i,(a,b) in enumerate([
 ([5.5,6.0,5.5],[10.5,6.1,5.70]),([5.5,6.0,10.30],[10.5,6.1,10.5]),
 ([5.5,6.0,5.70],[5.70,6.1,10.30]),([10.30,6.0,5.70],[10.5,6.1,10.30])]):
 e=dict(name='fitted_open_rim_'+str(i),to=b,rotation=rot,faces={f:dict(uv=[0,0,16,16],texture='#rim') for f in ['north','south','east','west','up','down']});e['from']=a;opened['elements'].append(e)
write(A/'models/animation/jam/open_body.json',opened)
im=Image.open(A/'textures/jam/ancient_fruit_jam.png').convert('RGBA')
im.crop((0,2,4,3)).resize((4,4),Image.Resampling.NEAREST).save(A/'textures/jam/ancient_fruit_jam_rim.png')
profiles=read(A/'drink_profiles.json');p=next(p for p in profiles if p['style']=='JAM')
p['liquid'][0]['min']=[-2.28/16,.32/16,-2.28/16]
p['liquid'][0]['max']=[2.28/16,5.42/16,2.28/16]
write(A/'drink_profiles.json',profiles)
for f in [R/'build.gradle',R/'src/main/resources/META-INF/mods.toml']:
 f.write_text(f.read_text(encoding='utf-8').replace('1.8.8','1.8.9'),encoding='utf-8')
print('Jar shell preserved; jam approaches inner wall with 0.22px clearance; rim fits 5px shell.')
