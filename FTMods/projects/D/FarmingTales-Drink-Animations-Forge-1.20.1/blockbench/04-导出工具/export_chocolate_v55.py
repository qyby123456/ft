"""Add the missing chocolate jar without resampling the approved v54 motion."""
from pathlib import Path
from PIL import Image
import base64, copy, io, json, shutil, zipfile

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
OUT = ROOT / 'blockbench/01-常用工程/巧克力酱补充-v55'
ITEM = 'chocolate_sauce'
PROTOTYPE = 'ancient_fruit_jam_1'
read = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
OUT.mkdir(parents=True, exist_ok=True)
profile = next(p for p in read(ASSETS/'drink_profiles.json') if p['id']=='farmingtales:'+ITEM)
report = []
for shape, label in [('slim','纤细手臂'),('wide','Steve')]:
    template = read(ROOT/f'blockbench/02-模板与基准/上古水果果酱-v54/{PROTOTYPE}-{label}-v54.bbmodel')
    model = copy.deepcopy(template)
    model['name'] = f'{ITEM}-{label}-v55'
    model['model_identifier'] = ITEM
    model['credit'] = '紫菜 / ARR / FarmingTales 1.8.26. Approved v54 motion; original chocolate sauce texture.'
    for animation in model['animations']:
        animation['name'] = animation['name'].replace(PROTOTYPE,ITEM)
    for texture in model['textures']:
        old = texture['name']
        if not old.startswith('ancient_fruit_jam') or old.endswith('_lip.png'):
            continue
        name = old.replace('ancient_fruit_jam',ITEM)
        image = Image.open(ASSETS/'textures/jam'/name).convert('RGBA')
        if name.endswith('_content.png'):
            reference = Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',')[1]))).convert('RGBA')
            image.putalpha(reference.getchannel('A'))
        encoded = io.BytesIO(); image.save(encoded,format='PNG')
        texture['name'] = name
        texture['source'] = 'data:image/png;base64,'+base64.b64encode(encoded.getvalue()).decode()
    path = OUT/(model['name']+'.bbmodel')
    path.write_text(json.dumps(model,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
    report.append(dict(item=profile['id'],arm=shape,path=str(path),animations=len(model['animations']),
                       textures=len(model['textures']),geometry_scale=.7625*profile['scale']))
shutil.copy2(ROOT/'blockbench/04-导出工具/camera-source/cameras.js',OUT/'cameras.js')
(OUT/'使用说明.txt').write_text('''巧克力酱补充 v55 / MOD 1.8.26
作者：紫菜；协议：ARR。
farmingtales:chocolate_sauce，使用原巧克力酱贴图与已确认的 v54 酱罐动作。
取出 0.5 秒，收起 0.45 秒；保留抛接、检视摇晃、双手拔木塞并前扔、单手食用、末尾抖出与四种食用变化。
Steve 和纤细手臂各一份，均含呼吸与 camera 动画。其余物品继续使用酱罐动画-v54.zip 和酒水扩展-v54.zip。
第一人称：在 Blockbench 从文件安装 cameras.js，选择并关联 camera_first_person。
V 食用、G 检视，可改键；普通右键保持原版。游戏使用黏稠流体，Blockbench 小柱近似液面。
''',encoding='utf-8')
(OUT/'导出检查.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
with zipfile.ZipFile(OUT.parents[1]/'05-打包文件'/(OUT.name+'.zip'),'w',zipfile.ZIP_DEFLATED) as z:
    for path in sorted(OUT.iterdir()):
        if path.is_file(): z.write(path,path.name)
print('Exported chocolate sauce:',len(report),'projects.')
