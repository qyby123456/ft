"""Retexture the approved v49 rigs without refitting or changing their animation curves."""
from pathlib import Path
from PIL import Image
import base64, copy, io, json, shutil, zipfile

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
OUT = ROOT / 'blockbench/酱罐动画-v50'
PROTOTYPE = 'ancient_fruit_jam_1'

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, separators=(',', ':')), encoding='utf-8')

profiles = {p['id']: p for p in read(ASSETS / 'drink_profiles.json')}
entries = read(ROOT / 'jar-family-sources.json')['entries']
report = []
for shape, label in [('slim', '纤细手臂'), ('wide', 'Steve')]:
    template = read(ROOT / f'blockbench/上古水果果酱-v49/{PROTOTYPE}-{label}-v49.bbmodel')
    for entry in entries:
        item = entry['id'].split(':')[1]
        profile = profiles[entry['id']]
        model = copy.deepcopy(template)
        model['name'] = f'{item}-{label}-v50'
        model['model_identifier'] = item
        model['credit'] = '紫菜 / ARR / FarmingTales 1.8.21. Approved v49 animation, original jar texture.'
        for animation in model['animations']:
            animation['name'] = animation['name'].replace(PROTOTYPE, item)
        if item != PROTOTYPE:
            for texture in model['textures']:
                old_name = texture['name']
                if not old_name.startswith('ancient_fruit_jam') or old_name.endswith('_lip.png'):
                    continue
                name = old_name.replace('ancient_fruit_jam', item)
                image = Image.open(ASSETS / 'textures/jam' / name).convert('RGBA')
                if name.endswith('_content.png'):
                    reference = Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',')[1]))).convert('RGBA')
                    image.putalpha(reference.getchannel('A'))
                encoded = io.BytesIO()
                image.save(encoded, format='PNG')
                texture['name'] = name
                texture['source'] = 'data:image/png;base64,' + base64.b64encode(encoded.getvalue()).decode()
        folder = {'jam': '果酱', 'roe_sauce': '鱼籽酱', 'mayonnaise': '蛋黄酱'}[entry['category']]
        path = OUT / folder / (model['name'] + '.bbmodel')
        write(path, model)
        report.append({'item': entry['id'], 'arm': shape, 'path': str(path),
                       'animations': len(model['animations']), 'textures': len(model['textures']),
                       'geometry_scale': .7625 * profile['scale']})
    print('Exported', label, len(entries), 'jar projects.', flush=True)
shutil.copy2(ROOT / 'blockbench/camera-source/cameras.js', OUT / 'cameras.js')
(OUT / '使用说明.txt').write_text('''酱罐动画 v50 / MOD 1.8.21
作者：紫菜；协议：ARR。
32 种果酱、89 种鱼籽酱（含蟹籽酱）、8 种蛋黄酱，共 129 种单罐。
每种均提供 Steve 和纤细手臂项目，保留各自原版贴图和内容物纹理。
全部复用已确认的 v49 动作：抛起接住拿出、抛转接住摇晃检视、双手抓木塞上拔并向前抛出、单手食用和末尾抖出。相机、握持、速度、音效及液体阻尼保持相同。
包含 idle、draw、put_away、inspect、drink、walk、sprint、jump、place 和 preview_complete 十条动画。
第一人称：在 Blockbench 插件页面从文件安装本目录 cameras.js，选择并关联 camera_first_person；每条动画均含 camera 动态轨迹。
液体沿用黏稠延迟、35° 倾角和罐口约束；Blockbench 使用 6×6 小柱近似游戏连续裁切表面。游戏另有瓶内手部遮挡。
多罐摆放方块不作为单罐食物播放。V 食用、G 检视，可在游戏按键设置修改；普通右键保持原版。
''', encoding='utf-8')
write(OUT / '导出检查.json', report)
archive = OUT.with_suffix('.zip')
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
    for path in sorted(OUT.rglob('*')):
        if path.is_file():
            z.write(path, path.relative_to(OUT).as_posix())
print('Exported', len(report), 'projects:', archive, flush=True)
