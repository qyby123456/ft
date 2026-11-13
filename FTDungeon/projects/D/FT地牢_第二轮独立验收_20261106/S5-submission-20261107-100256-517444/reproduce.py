import hashlib
import json
import shutil
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent
target=root/'qa/reproduction-final'
target.mkdir(exist_ok=False)
runner=Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs')
for name in ['tools.mjs','assemble.mjs']:
    shutil.copy2(root/name,target/name)
reports=[]
for number in range(11,21):
    identifier=f'S5-{number}'
    folder=target/identifier
    folder.mkdir()
    shutil.copy2(root/identifier/'recipe.mjs',folder/'recipe.mjs')
    result=subprocess.run(['node',str(runner),str(folder/'recipe.mjs'),'--out-dir',str(folder)],capture_output=True)
    (folder/'build.log').write_bytes(result.stdout+result.stderr)
    assert result.returncode==0
    candidate=folder/(identifier.lower()+'.nbt')
    expected=root/identifier/candidate.name
    actual_hash=hashlib.sha256(candidate.read_bytes()).hexdigest()
    assert actual_hash==hashlib.sha256(expected.read_bytes()).hexdigest()
    (folder/'qa').mkdir()
    (folder/'qa/self-check.json').write_text(json.dumps({'self_check_passed':True,'sha256':actual_hash,'scope':'Reproduction byte equality only; original bound QA checked separately.'}),encoding='utf-8')
    reports.append({'id':identifier,'sha256':actual_hash,'byte_equal':True,'output':str(candidate)})
result=subprocess.run(['node',str(runner),str(target/'assemble.mjs'),'--out-dir',str(target/'merged')],capture_output=True)
(target/'merged-build.log').write_bytes(result.stdout+result.stderr)
assert result.returncode==0
actual=hashlib.sha256((target/'merged/merged.nbt').read_bytes()).hexdigest()
assert actual==hashlib.sha256((root/'merged/merged.nbt').read_bytes()).hexdigest()
reports.append({'id':'merged','sha256':actual,'byte_equal':True})
(root/'qa/reproduction-report.json').write_text(json.dumps({'passed':True,'scope':'Separate output reproduction from copied recipes and helper; all 11 NBT byte hashes identical, no candidate NBT copied. Producer-run, not independent reviewer.','results':reports},ensure_ascii=False,indent=2),encoding='utf-8')
print('Reproduced 11 NBT byte-identically')
