from pathlib import Path
import copy,hashlib,json,math,shutil,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parent
PACK=ROOT.parents[2]
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
backup=ROOT/'backups/1.8.5-before-jam';backup.mkdir(exist_ok=True)
for p in [PACK/'mods/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.5.jar',PACK/'config/farmingtales_drink_anim-client.toml']+[ROOT/n for n in ['build.gradle','README.md','verification.json','installation-1.8.5.json','install_release.ps1']]:
    if not (backup/p.name).exists():shutil.copy2(p,backup/p.name)
if not (backup/'source.zip').exists():
    with zipfile.ZipFile(backup/'source.zip','w',zipfile.ZIP_DEFLATED) as z:
        for p in (ROOT/'src').rglob('*'):
            if p.is_file():z.write(p,p.relative_to(ROOT))
source=PACK/'kubejs/assets/farmingtales/models/block/bakerys/ancient_fruit_jam.json'
texture=PACK/'kubejs/assets/farmingtales/textures/block/bakerys/jam/ancient_fruit_jam.png'
model=json.loads(source.read_text());elements=model['elements']
models=ASSETS/'models/animation/jam';models.mkdir(parents=True,exist_ok=True)
textures=ASSETS/'textures/jam';textures.mkdir(exist_ok=True)
atlas_path=ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json';atlas=json.loads(atlas_path.read_text())
for sprite in ['farmingtales_drink_anim:jam/ancient_fruit_jam','farmingtales_drink_anim:jam/ancient_fruit_jam_content']:
    if not any(s.get('resource')==sprite for s in atlas['sources']):atlas['sources'].append(dict(type='single',resource=sprite,sprite=sprite))
atlas_path.write_text(json.dumps(atlas,ensure_ascii=False,indent=2)+'\n')
shutil.copy2(texture,textures/'ancient_fruit_jam.png')
def write(name,els):
    data={'textures':{'4':'farmingtales_drink_anim:jam/ancient_fruit_jam','particle':'farmingtales_drink_anim:jam/ancient_fruit_jam'},'elements':els}
    (models/(name+'.json')).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
shell=copy.deepcopy(elements[2]);shell['faces']={k:v for k,v in shell['faces'].items() if k not in ['up','down']}
write('body',[copy.deepcopy(elements[0]),copy.deepcopy(elements[1]),shell])
write('stopper',copy.deepcopy(elements[6:8]));write('empty',[])
im=Image.open(texture).convert('RGBA');liquid=im.crop((10,10,14,14));liquid.putalpha(255);liquid.save(textures/'ancient_fruit_jam_content.png')
profiles_path=ASSETS/'drink_profiles.json';profiles=json.loads(profiles_path.read_text())
yaw=math.radians(-22.5)
profile=dict(id='farmingtales:ancient_fruit_jam_1',style='JAM',scale=1.35,width=5,depth=5,
    capPivot=[8-2.25*math.cos(yaw),5.1,8+2.25*math.sin(yaw)],neckJoin=0,neckMin=5.1/16,neckMax=7.1/16,mouthY=6.1,
    bodyModel='animation/jam/body',capModel='animation/jam/stopper',neckModel='animation/jam/empty',
    boundsMin=[4.7,.1,4.7],boundsMax=[11.3,7.1,11.3],
    liquid=[dict(min=[-2/16,.4/16,-2/16],max=[2/16,5.4/16,2/16],sideUV=[0,0,1,1],surfaceUV=[0,0,1,1],texture='farmingtales_drink_anim:textures/jam/ancient_fruit_jam_content.png')],
    cupShape=dict(gripY=2.6,radius=2.5,liquidYaw=-22.5))
profiles=[p for p in profiles if p['id']!=profile['id']]+[profile]
profiles_path.write_text(json.dumps(profiles,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
mask_path=ASSETS/'hand_volumes.json';masks=json.loads(mask_path.read_text())
masks[profile['id']]=[dict(min=[5.5,.1,5.5],max=[10.5,6.1,10.5],origin=[8,3.35,8],axis='y',angle=-22.5,rescale=False)]
mask_path.write_text(json.dumps(masks,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
fixture=ROOT/'src/smoke/resources/assets/farmingtales'
for path,data in [('blockstates/ancient_fruit_jam_1.json',{'variants':{'':{'model':'farmingtales_drink_anim:animation/jam/body'}}}),('models/item/ancient_fruit_jam_1.json',{'parent':'farmingtales_drink_anim:animation/jam/body'})]:
    p=fixture/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data))
(ROOT/'jam-source-assets.json').write_text(json.dumps([dict(path=str(p),sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in [source,texture]],ensure_ascii=False,indent=2),encoding='utf-8')
print('Prepared original jar shell, detachable wooden stopper, viscous jam texture and isolated fixture.')
