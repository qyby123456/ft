import hashlib
import json
import shutil
import subprocess
from pathlib import Path

from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
destination=workspace/'s8-independent-recipes-20261107-1335'
destination.mkdir()
for filename in root.glob('*.mjs'):
    shutil.copyfile(filename,destination/filename.name)
ready=load(root/'ready-for-review.json')
results=[]
for room in ready['rooms']:
    identifier=room['id'];folder=destination/identifier;folder.mkdir()
    shutil.copyfile(root/identifier/'recipe.mjs',folder/'recipe.mjs')
    command=[r'C:\Program Files\nodejs\node.exe',r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs',str(folder/'recipe.mjs'),'--out-dir',str(folder),'--json']
    process=subprocess.run(command,capture_output=True,timeout=180)
    (destination/(identifier+'.log')).write_bytes(process.stdout+process.stderr)
    assert process.returncode==0,process.stderr.decode('utf-8',errors='replace')
    candidate=folder/Path(room['source']['path']).name
    assert hashlib.sha256(candidate.read_bytes()).hexdigest()==room['source']['sha256'],identifier
    for filename in ('inventory.json','SELF-QA.json'):
        shutil.copyfile(root/identifier/filename,folder/filename)
    results.append({'id':identifier,'sha256':room['source']['sha256'],'binary_equal':True})
    print(identifier,'EXACT',flush=True)
(destination/'assembly').mkdir()
shutil.copyfile(root/'assembly/build.mjs',destination/'assembly/build.mjs')
process=subprocess.run([r'C:\Program Files\nodejs\node.exe',str(destination/'assembly/build.mjs')],cwd=destination,capture_output=True,timeout=180)
(destination/'assembly.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0,process.stderr.decode('utf-8',errors='replace')
shutil.copyfile(root/'assembly/verify.mjs',destination/'assembly/verify.mjs')
process=subprocess.run([r'C:\Program Files\nodejs\node.exe',str(destination/'assembly/verify.mjs')],cwd=destination,capture_output=True,timeout=180)
(destination/'parts.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
outputs=[root/'assembly/s8-functional-combined.nbt',*sorted((root/'assembly/parts').glob('*.nbt'))]
assert len(outputs)==21
for original in outputs:
    assert original.read_bytes()==(destination/original.relative_to(root)).read_bytes(),str(original)
for name in ('submission-snapshot.json','supplemental-snapshot.json'):
    for item in load(root/name)['files']:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT31NBT_RECIPE_OUTPUTS_BINARY_EXACT','rooms':results,'merged_and_parts':21,'all31_binary_equal':True,'candidate_nbt_used_as_recipe_input':False,'directory':str(destination)}
(root/'independent-reproduction.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False),flush=True)
