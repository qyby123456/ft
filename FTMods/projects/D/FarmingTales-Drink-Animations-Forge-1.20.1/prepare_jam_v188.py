from pathlib import Path
from PIL import Image
import json,copy,shutil
R=Path(__file__).resolve().parent;A=R/'src/main/resources/assets/farmingtales_drink_anim';P=R.parents[2]
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def write(p,d):p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
body=read(A/'models/animation/jam/body.json')
original=read(P/'kubejs/assets/farmingtales/models/block/bakerys/ancient_fruit_jam.json')
residue=[e for e in body['elements'] if e.get('name','').startswith('jam_wall')]
body['elements']=copy.deepcopy(original['elements'][:3])+residue
body['textures']['4']='farmingtales_drink_anim:jam/ancient_fruit_jam_sealed'
im=Image.open(A/'textures/jam/ancient_fruit_jam.png').convert('RGBA');sealed=im.copy()
# The closed jar keeps its inward shell; the pale mouth stripe appears only after uncorking.
for x in range(5):
 for y in (4,5):sealed.putpixel((x,y),im.getpixel((x,3)))
sealed.save(A/'textures/jam/ancient_fruit_jam_sealed.png')
write(A/'models/animation/jam/body.json',body)
opened=copy.deepcopy(body);opened['textures']['4']='farmingtales_drink_anim:jam/ancient_fruit_jam'
rot=copy.deepcopy(original['elements'][2]['rotation'])
for i,(a,b) in enumerate([
 ([5.35,5.94,5.35],[10.65,6.08,5.47]),([5.35,5.94,10.53],[10.65,6.08,10.65]),
 ([5.35,5.94,5.47],[5.47,6.08,10.53]),([10.53,5.94,5.47],[10.65,6.08,10.53])]):
 e=dict(name='wide_thin_open_rim_'+str(i),to=b,rotation=rot,faces={f:dict(uv=[0,0,16,16],texture='#rim') for f in ['north','south','east','west','up','down']});e['from']=a;opened['elements'].append(e)
write(A/'models/animation/jam/open_body.json',opened)
profiles=read(A/'drink_profiles.json');jam=next(p for p in profiles if p['style']=='JAM');jam['neckModel']='animation/jam/open_body';write(A/'drink_profiles.json',profiles)
atlas=R/'src/main/resources/assets/minecraft/atlases/blocks.json';data=read(atlas);sprite='farmingtales_drink_anim:jam/ancient_fruit_jam_sealed'
if not any(s.get('resource')==sprite for s in data['sources']):data['sources'].append(dict(type='single',resource=sprite,sprite=sprite))
write(atlas,data)
assets=P.parents[1]/'assets';index=next(p for p in (assets/'indexes').glob('*.json') if 'minecraft/sounds/item/bottle/drink_honey1.ogg' in read(p)['objects']);objects=read(index)['objects']
sounds=read(A/'sounds.json');samples=[]
for i in range(1,4):
 key=f'minecraft/sounds/item/bottle/drink_honey{i}.ogg';digest=objects[key]['hash'];shutil.copy2(assets/'objects'/digest[:2]/digest,A/f'sounds/jam/honey{i}.ogg')
 samples.append(dict(name=f'farmingtales_drink_anim:jam/honey{i}',volume=.65))
sounds['jam_eat']['sounds']=samples
sounds['jam_pat']=dict(subtitle='subtitles.farmingtales_drink_anim.jam_pat',sounds=[dict(name='farmingtales_drink_anim:balanced/glass_touch',volume=.55)])
write(A/'sounds.json',sounds)
for lang,label in [('zh_cn','轻拍果酱罐'),('en_us','Jam jar pat')]:
 p=A/f'lang/{lang}.json';d=read(p);d['subtitles.farmingtales_drink_anim.jam_pat']=label;write(p,d)
for p in [R/'build.gradle',R/'src/main/resources/META-INF/mods.toml']:
 p.write_text(p.read_text(encoding='utf-8').replace('1.8.7','1.8.8'),encoding='utf-8')
print('Prepared sealed/open jar variants, wider thin mouth, and local vanilla honey samples.')
