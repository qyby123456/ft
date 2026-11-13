import hashlib
import json
import shutil
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent
target=root/'qa/reproduction-storage-fix'
target.mkdir(exist_ok=False)
runner='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs'
digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
for name in ['tools.mjs','assemble.mjs']:shutil.copyfile(root/name,target/name)
for number in range(11,21):
    identifier=f'S5-{number}'
    if number!=17:
        old=root/'qa/reproduction-final'/identifier
        shutil.copytree(old,target/identifier)
        assert digest(old/(identifier.lower()+'.nbt'))==digest(root/identifier/(identifier.lower()+'.nbt'))
        continue
    folder=target/identifier;folder.mkdir();shutil.copyfile(root/identifier/'recipe.mjs',folder/'recipe.mjs')
    process=subprocess.run(['node',runner,str(folder/'recipe.mjs'),'--out-dir',str(folder)],capture_output=True)
    (folder/'build.log').write_bytes(process.stdout+process.stderr);assert process.returncode==0
    sha256=digest(folder/'s5-17.nbt');assert sha256==digest(root/identifier/'s5-17.nbt')
    (folder/'qa').mkdir();(folder/'qa/self-check.json').write_text(json.dumps({'sha256':sha256,'self_check_passed':True,'scope':'Byte reproduction only'}),encoding='utf-8')
process=subprocess.run(['node',runner,str(target/'assemble.mjs'),'--out-dir',str(target/'merged')],capture_output=True)
(target/'merged-build.log').write_bytes(process.stdout+process.stderr);assert process.returncode==0
assert digest(target/'merged/merged.nbt')==digest(root/'merged/merged.nbt')
report={'passed':True,'scope':'Only changed source17 recipe and merged rebuilt independently in new output folder. Nine unchanged sources inherited from previous independently reproduced outputs, not claimed rerun.','results':[{'id':identifier,'sha256':digest(root/identifier/filename),'byte_equal':True} for identifier,filename in [('S5-17','s5-17.nbt'),('merged','merged.nbt')]],'inherited_unchanged_sources':[f'S5-{number}' for number in range(11,21) if number!=17]}
(root/'qa/reproduction-storage-fix-report.json').write_text(json.dumps(report,indent=2),encoding='utf-8');print(json.dumps(report))
