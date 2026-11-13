from pathlib import Path
from PIL import Image
import json,copy,shutil
ROOT=Path(__file__).resolve().parent
PACK=ROOT.parents[2]
A=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def write(p,v):p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
profiles=read(A/'drink_profiles.json');jam=next(p for p in profiles if p['style']=='JAM')
jam['scale']=1.0
jam['liquid'][0].update(sideUV=[0,0,8,16],surfaceUV=[8,0,16,16])
write(A/'drink_profiles.json',profiles)
original=read(PACK/'kubejs/assets/farmingtales/models/block/bakerys/ancient_fruit_jam.json')
shell=copy.deepcopy(original['elements'][2])
body=read(A/'models/animation/jam/body.json')
body['elements']=copy.deepcopy(original['elements'][:3])
body['textures']['rim']='farmingtales_drink_anim:jam/ancient_fruit_jam_rim'
# A recessed rim follows the original inward shell; its top stays below the original lip.
for i,(a,b) in enumerate([
 ([5.51,5.86,5.51],[10.49,6.08,5.74]),([5.51,5.86,10.26],[10.49,6.08,10.49]),
 ([5.51,5.86,5.74],[5.74,6.08,10.26]),([10.26,5.86,5.74],[10.49,6.08,10.26])]):
 body['elements'].append(dict(name='thin_recessed_rim_'+str(i),from_=a,to=b,
  rotation=copy.deepcopy(shell['rotation']),faces={f:dict(uv=[0,0,16,16],texture='#rim') for f in ['north','south','east','west','up','down']}))
 body['elements'][-1]['from']=body['elements'][-1].pop('from_')
# The inner wall extends down, never above the unmodified glass shell. Only its inside face is drawn.
for i,(a,b,face) in enumerate([
 ([5.74,5.08,5.74],[10.26,5.86,5.74],'south'),([5.74,5.08,10.26],[10.26,5.86,10.26],'north'),
 ([5.74,5.08,5.74],[5.74,5.86,10.26],'east'),([10.26,5.08,5.74],[10.26,5.86,10.26],'west')]):
 e=dict(name='recessed_inner_wall_'+str(i),to=b,rotation=copy.deepcopy(shell['rotation']),faces={face:dict(uv=[0,0,16,16],texture='#rim')})
 e['from']=a;body['elements'].append(e)
body['textures']['content']='farmingtales_drink_anim:jam/ancient_fruit_jam_content'
# Tiny wall residue sits inside the full volume and is exposed gradually as jam is eaten.
for i,(a,b) in enumerate([([6.015,.42,6.9],[6.045,2.4,7.5]),([8.4,.42,9.955],[9,1.9,9.985])]):
 e=dict(name='jam_wall_residue_'+str(i),to=b,rotation=copy.deepcopy(shell['rotation']),
  faces={f:dict(uv=[0,0,16,16],texture='#content') for f in ['north','south','east','west','up','down']})
 e['from']=a;body['elements'].append(e)
write(A/'models/animation/jam/body.json',body)
im=Image.open(A/'textures/jam/ancient_fruit_jam.png').convert('RGBA')
content=Image.new('RGBA',(8,4));content.paste(im.crop((10,10,14,14)),(0,0));content.paste(im.crop((10,14,14,18)),(4,0))
content.save(A/'textures/jam/ancient_fruit_jam_content.png')
rim=im.crop((0,4,5,6));rim.putalpha(255);rim.save(A/'textures/jam/ancient_fruit_jam_rim.png')
atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=read(atlas_path)
sprite='farmingtales_drink_anim:jam/ancient_fruit_jam_rim'
if not any(s.get('resource')==sprite for s in atlas['sources']):atlas['sources'].append(dict(type='single',resource=sprite,sprite=sprite))
write(atlas_path,atlas)
# Keep vanilla resources local to this mod's custom gesture; no minecraft sound overrides.
asset_root=PACK.parents[1]/'assets'
indexes=sorted((asset_root/'indexes').glob('*.json'))
sounds=read(A/'sounds.json');sound_dir=A/'sounds/jam';sound_dir.mkdir(exist_ok=True)
for event,key,output in [('jam_eat','minecraft/sounds/random/eat1.ogg','eat'),('jam_tap','minecraft/sounds/step/wood1.ogg','tap')]:
 entry=next(read(p)['objects'][key] for p in indexes if key in read(p)['objects'])
 digest=entry['hash'];shutil.copy2(asset_root/'objects'/digest[:2]/digest,sound_dir/(output+'.ogg'))
 sounds[event]=dict(subtitle='subtitles.farmingtales_drink_anim.'+event,sounds=[dict(name='farmingtales_drink_anim:jam/'+output,volume=1.0)])
write(A/'sounds.json',sounds)
for name,labels in [('zh_cn',{'jam_eat':'食用果酱','jam_tap':'轻敲木塞'}),('en_us',{'jam_eat':'Eating jam','jam_tap':'Wooden stopper taps'})]:
 path=A/'lang'/f'{name}.json';data=read(path)
 data.update({'subtitles.farmingtales_drink_anim.'+k:v for k,v in labels.items()});write(path,data)
for path in [ROOT/'build.gradle',ROOT/'src/main/resources/META-INF/mods.toml']:
 path.write_text(path.read_text(encoding='utf-8').replace('1.8.6','1.8.7'),encoding='utf-8')
print('Prepared original inward shell, recessed thin rim, separate pixel jam surface/side UV and local sound samples.')
