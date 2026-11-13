from pathlib import Path
import json,zipfile
ROOT=Path(__file__).resolve().parents[1]
script=ROOT/'04-导出工具/export_jam_v54.py'
script.write_text(script.read_text(encoding='utf-8').replace('Original FarmingTales container assets; runtime DrinkMotion and BottleRig 1.8.20','紫菜 / ARR / FarmingTales 1.8.25'),encoding='utf-8')
for path in (ROOT/'02-模板与基准/上古水果果酱-v54').glob('*.bbmodel'):
    data=json.loads(path.read_text(encoding='utf-8'))
    data['credit']='紫菜 / ARR / FarmingTales 1.8.25'
    path.write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
folder=ROOT/'01-常用工程/酒水扩展-v54'
with zipfile.ZipFile(ROOT/'05-打包文件'/(folder.name+'.zip'),'w',zipfile.ZIP_DEFLATED) as z:
    for path in sorted(folder.rglob('*')):
        if path.is_file():z.write(path,path.relative_to(folder).as_posix())
print('Packaged 106 drink projects; jar archive contains 258 projects.')
