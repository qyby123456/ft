"""Apply the small, documented rum geometry shift and version export scripts."""
from pathlib import Path
import json

root=Path(__file__).resolve().parent
assets=root/'src/main/resources/assets/farmingtales_drink_anim'
model_path=assets/'models/animation/drinks/rum_body.json'
model=json.loads(model_path.read_text(encoding='utf-8'))
moved=[]
for i,e in enumerate(model['elements']):
    if min(e['from'][1],e['to'][1])>=9.8 and max(e['from'][0],e['to'][0])<=9 and min(e['from'][0],e['to'][0])>=7:
        moved.append(i)
        for key in ['from','to']:e[key][1]=round(e[key][1]-.16,8)
        if 'rotation' in e:e['rotation']['origin'][1]=round(e['rotation']['origin'][1]-.16,8)
assert len(moved)==8,moved
model_path.write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
profiles_path=assets/'drink_profiles.json'
profiles=json.loads(profiles_path.read_text(encoding='utf-8'))
rum=next(p for p in profiles if p['id']=='farmingtales:rum')
assert rum['neckJoin']==0
rum['neckJoin']=-.01
profiles_path.write_text(json.dumps(profiles,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for old,new in [('blockbench/export_drinks_v33.py','blockbench/export_drinks_v34.py'),('preview/verify_bb_fit_v33.py','preview/verify_bb_fit_v34.py')]:
    text=(root/old).read_text(encoding='utf-8').replace('v33','v34').replace('1.8.3','1.8.4')
    (root/new).write_text(text,encoding='utf-8')
print('Shifted rum neck column 0.16 pixels and created v34 export scripts.')
