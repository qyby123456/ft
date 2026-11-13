import hashlib
import json
import shutil
from pathlib import Path

root=Path(__file__).resolve().parent
source=root/'revisions/visual-depth-v3'
output=root/'revisions/local-relief-v4'
output.mkdir(exist_ok=False)
for name in ['common.mjs','qa.py','revise.py','render.cjs','assemble.py','references.json','routecoverage.py','clearance.py','reproduce.py']:
    shutil.copyfile(source/name,output/name)
for number in range(11,21):
    shutil.copytree(source/f'S7-{number}',output/f'S7-{number}')
def relocate(value):
    if isinstance(value,str):
        return value.replace(str(source),str(output))
    if isinstance(value,list):
        return [relocate(entry) for entry in value]
    if isinstance(value,dict):
        return {key:relocate(entry) for key,entry in value.items()}
    return value
for path in [output/'references.json']+[path for number in range(11,21) for path in (output/f'S7-{number}').glob('*.json')]:
    path.write_text(json.dumps(relocate(json.loads(path.read_text(encoding='utf-8'))),ensure_ascii=False,indent=2),encoding='utf-8')
records=[{'id':f'S7-{number}','source':str(next((source/f'S7-{number}').glob('*.nbt'))),'sha256':hashlib.sha256(next((source/f'S7-{number}').glob('*.nbt')).read_bytes()).hexdigest()} for number in range(11,21)]
(output/'revision-parent.json').write_text(json.dumps({'parent':str(source),'sources':records,'independent_review':r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-submission-20261107-061916-548777\REVIEW.md','closed_keep':['19 enclosed vista depth','20 chest visibility','15 optional obstacles'],'group_accepted':False},ensure_ascii=False,indent=2),encoding='utf-8')
(output/'progress.json').write_text(json.dumps({'state':'REVISING','current':'S7-16','pending':['16 pool relief','11 entry/upper landing','12 cell frame','13 stove frame','14 rack frame','17 shelf relief','18 niche relief','affected merge QA','world and actual interactions'],'group_accepted':False},ensure_ascii=False,indent=2),encoding='utf-8')
print(output)
