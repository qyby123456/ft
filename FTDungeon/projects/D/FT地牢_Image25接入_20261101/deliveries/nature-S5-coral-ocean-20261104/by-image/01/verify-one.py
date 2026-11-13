import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
from urllib.request import urlopen

ROOT = Path(__file__).resolve().parent
BATCH = ROOT.parents[1]
spec = importlib.util.spec_from_file_location('s5_verifier', BATCH / 'verify.py')
verifier = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verifier)
folder = ROOT / 'rooms/NAT-S5-01'
metadata, structure = verifier.inspect(folder)
support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
classpath = os.pathsep.join([str(BATCH / 'qa/classes'),'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar',*dependencies])
result = subprocess.run(['C:/Program Files/Common Files/Oracle/Java/javapath/java.exe','-Djava.awt.headless=true','-classpath',classpath,'NativeS5',str(folder)],capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=150,creationflags=subprocess.CREATE_NO_WINDOW,cwd=ROOT)
(ROOT / 'native.log').write_text(result.stdout+result.stderr,encoding='utf-8')
if result.returncode:
    raise RuntimeError(result.stdout[-1500:]+result.stderr[-1500:])
for direction, title, camera in verifier.views.VIEWS:
    verifier.views.render(structure,metadata,camera,title,False,size=(1100,850)).save(folder / f'interior-{direction}.png')
verifier.views.render(structure,metadata,(-.7,-1),'完整外壳',True,size=(1100,850)).save(folder / 'exterior.png')
verifier.Image.open(folder / 'interior-north.png').save(folder / 'preview.png')
qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
qa.update(native_registry='PASS',native_scope='Actual Minecraft registry, collision-path and StructureTemplate roundtrip; not world placement.',reference_review='Single central tiered coral island and four-sided circulation; wall arches and sea lanterns. Explicit adaptations in IMAGE-ANALYSIS.md.',world_placement='NOT_RUN')
verifier.write(folder / 'qa.json',qa)
with urlopen('http://127.0.0.1:5216/__minecraft_builder__/build.nbt',timeout=20) as response:
    if hashlib.sha256(response.read()).hexdigest()!=metadata['sha256']:
        raise ValueError('Lodestone endpoint differs from final single-image NBT')
verifier.write(ROOT / 'delivery.json',dict(status='SINGLE_IMAGE_CANDIDATE_STATIC_NATIVE_PASS',reference=metadata['reference'],reference_sha256=metadata['reference_sha256'],room=str(folder / 'room.nbt'),sha256=metadata['sha256'],size=metadata['size'],previous_batch='Not overwritten or validated by this run; old bulk scheme is not the current image-based deliverable.',next_image='NOT_STARTED',lodestone_url='http://127.0.0.1:5216/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S5-image01',analysis=str(ROOT / 'IMAGE-ANALYSIS.md')))
print('S5 image 01 only: static, native and final-NBT four views complete.')
