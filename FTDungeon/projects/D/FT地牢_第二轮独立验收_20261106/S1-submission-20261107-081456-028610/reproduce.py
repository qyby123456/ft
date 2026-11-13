import hashlib
import json
import pathlib
import shutil
import subprocess
import sys

ROOT=pathlib.Path(__file__).parent
OUTPUT=ROOT/'reproduction'
OUTPUT.mkdir(exist_ok=True)
SKILL=pathlib.Path(r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill')
for name in ['common.mjs','enhance.mjs','equip.py']:
    shutil.copyfile(ROOT/name,OUTPUT/name)
results=[]
for folder in sorted(ROOT.glob('[12][0-9]-*')):
    destination=OUTPUT/folder.name
    destination.mkdir(exist_ok=True)
    for name in ['recipe.mjs','design.json']:
        shutil.copyfile(folder/name,destination/name)
    command=['node',str(SKILL/'scripts'/'run-build.mjs'),str(destination/'recipe.mjs'),'--out-dir',str(destination),'--data-version','3465']
    build=subprocess.run(command,cwd=SKILL,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    (destination/'rebuild.log').write_bytes(build.stdout)
    assert build.returncode==0,build.stdout
    equipment=subprocess.run([sys.executable,str(OUTPUT/'equip.py'),str(destination)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    assert equipment.returncode==0,equipment.stdout
    original=next(folder.glob('*.nbt'))
    rebuilt=destination/original.name
    digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
    assert digest(original)==digest(rebuilt),folder.name
    results.append({'room':folder.name,'original':str(original),'rebuilt':str(rebuilt),'sha256':digest(original),'byte_identical':True,'recipe_sha256':digest(folder/'recipe.mjs'),'design_sha256':digest(folder/'design.json')})
report={'rooms':results,'all_ten_byte_identical':True,'common_sha256':digest(ROOT/'common.mjs'),'enhance_sha256':digest(ROOT/'enhance.mjs'),'equip_sha256':digest(ROOT/'equip.py'),'runner':str(SKILL/'scripts'/'run-build.mjs'),'runner_sha256':digest(SKILL/'scripts'/'run-build.mjs')}
(ROOT/'reproduction-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Ten final NBT files rebuilt byte-identically from current recipes and equipment payload step.')
