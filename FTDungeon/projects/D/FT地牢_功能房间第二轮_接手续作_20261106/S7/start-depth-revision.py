import hashlib
import json
import shutil
from pathlib import Path

root=Path(__file__).resolve().parent
output=root/'revisions/visual-depth-v3'
output.mkdir(parents=True,exist_ok=False)
for name in ['common.mjs','qa.py','revise.py','render.cjs','assemble.py','references.json','routecoverage.py']:
    shutil.copyfile(root/name,output/name)
for number in range(11,21):
    shutil.copytree(root/f'S7-{number}',output/f'S7-{number}')
def relocate(value):
    if isinstance(value,str):
        return value.replace(str(root),str(output))
    if isinstance(value,list):
        return [relocate(entry) for entry in value]
    if isinstance(value,dict):
        return {key:relocate(entry) for key,entry in value.items()}
    return value
for path in [output/'references.json']+[path for number in range(11,21) for path in (output/f'S7-{number}').glob('*.json')]:
    path.write_text(json.dumps(relocate(json.loads(path.read_text(encoding='utf-8'))),ensure_ascii=False,indent=2),encoding='utf-8')
sources=[]
for number in range(11,21):
    source=next((root/f'S7-{number}').glob('*.nbt'))
    sources.append({'id':f'S7-{number}','source':str(source),'sha256':hashlib.sha256(source.read_bytes()).hexdigest()})
(output/'revision-parent.json').write_text(json.dumps({'read_only_parent':str(root),'sources':sources,'changes_planned':['19 near-middle-far secure cave depth','20 irregular inset stone-soil vault massing','15 explicit obstacle equipment'],'old_independent_reports_remain_bound_to_old_hashes':True},ensure_ascii=False,indent=2),encoding='utf-8')
(output/'progress.json').write_text(json.dumps({'state':'REVISING','current':'S7-19','group_accepted':False,'pending':['19 depth','20 vault mass','15 obstacles','affected merge/parts and QA','remaining per-room visual and interaction issues']},ensure_ascii=False,indent=2),encoding='utf-8')
print(output)
