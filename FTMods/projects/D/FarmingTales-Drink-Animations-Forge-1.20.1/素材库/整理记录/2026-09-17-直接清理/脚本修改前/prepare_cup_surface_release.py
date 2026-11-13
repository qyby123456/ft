from pathlib import Path
from copy import deepcopy
import json,shutil,zipfile
from prepare_cocktail_profiles import fix_handheld_surfaces

ROOT=Path(__file__).absolute().parent
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
write=lambda p,d:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
ids=['kaleidoscope_world_liquor:jerk','beachparty:coconut_cocktail']
profiles={p['id']:p for p in read(ASSETS/'drink_profiles.json')}
for item in ids:
    path=ASSETS/f"models/{profiles[item]['bodyModel']}.json"
    write(path,fix_handheld_surfaces(item,read(path)))
for file in ['build.gradle','src/main/resources/META-INF/mods.toml']:
    p=ROOT/file;p.write_text(p.read_text(encoding='utf-8').replace('1.8.27','1.8.28'),encoding='utf-8')
p=ROOT/'install_release.ps1';s=p.read_text(encoding='utf-8').replace('1.8.27','1.8.28').replace('v1827','v1828')
s=s.replace('1.8.26-before-cocktails','1.8.27-before-cup-surfaces').replace('1.8.26','1.8.27');p.write_text(s,encoding='utf-8')
p=ROOT/'README.md';s=p.read_text(encoding='utf-8').replace('动画 1.8.27','动画 1.8.28',1)
s=s.replace('Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。','Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n1.8.28：清除 jerk 杯底重复渲染面，补齐 coconut_cocktail 椰子杯内底。沿用原贴图，液体和动作参数不变；对应 Blockbench 四个项目在杯底修复-v57.zip。',1)
p.write_text(s,encoding='utf-8')

OUT=ROOT/'blockbench/杯底修复-v57';OUT.mkdir(exist_ok=True)
report=[]
for item in ids:
    name=item.split(':')[1];profile=profiles[item];scale=.7625*profile['scale']
    for arm,label in [('slim','纤细手臂'),('wide','Steve')]:
        path=ROOT/f'blockbench/鸡尾酒动画-v56/{name}-{label}-v56.bbmodel';model=read(path)
        model['name']=model['name'].replace('v56','v57')
        model['credit']='紫菜 / ARR / FarmingTales 1.8.28. Original texture; corrected cup bottom surfaces; approved v56 motion.'
        if name=='jerk':
            removed=next(e['uuid'] for e in model['elements'] if e['name']=='bottle_4')
            model['elements']=[e for e in model['elements'] if e['uuid']!=removed]
            for e in model['elements']:
                directions=['up','down'] if e['name']=='bottle_0' else ['north','east','south','west'] if e['name']=='bottle_1' else []
                for face in directions:e['faces'][face]={'uv':[0,0,0,0],'texture':None}
            def clean(nodes):
                result=[]
                for n in nodes:
                    if n==removed:continue
                    if isinstance(n,dict):n['children']=clean(n['children'])
                    result.append(n)
                return result
            model['outliner']=clean(model['outliner'])
        else:
            bottom=next(e for e in model['elements'] if e['name']=='bottle_4')
            bottom['to'][1]=16+(.95-4.8)*scale
            texture=next(i for i,t in enumerate(model['textures']) if t['name']=='coconut_cocktail_2.png' and t.get('render_mode')=='default')
            bottom['faces']['up']={'uv':[0,13,8,16],'texture':texture,'rotation':0}
        target=OUT/(model['name']+'.bbmodel')
        target.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
        report.append(dict(item=item,arm=arm,path=str(target),animations=len(model['animations']),textures=len(model['textures']),geometry_scale=scale))
shutil.copy2(ROOT/'blockbench/camera-source/cameras.js',OUT/'cameras.js')
write(OUT/'导出检查.json',report)
(OUT/'使用说明.txt').write_text('''杯底修复 v57 / MOD 1.8.28
作者：紫菜；协议：ARR。
jerk 删除杯底重叠面；coconut_cocktail 保留棕色外底并增加椰肉内底。
两种手臂共四个项目，替代鸡尾酒动画 v56 中的对应项目。其余项目继续使用原文件。
全部原贴图、十三条动作和 camera 轨迹保持不变。取出 0.5 秒、收起 0.45 秒。
''',encoding='utf-8')
with zipfile.ZipFile(OUT.with_suffix('.zip'),'w',zipfile.ZIP_DEFLATED) as z:
    for p in sorted(OUT.iterdir()):
        if p.is_file():z.write(p,p.name)
print('Prepared two model fixes and four matching Blockbench projects for 1.8.28.')
