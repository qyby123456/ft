import hashlib
import json
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
import subprocess
import sys
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')

notes = {
    11: '已复看原图/提示词/剖切/archive-detail：蓝旗、后壁梁柱、L书架和三桌保留，锁柜需钥匙，讲台未放书，冰挂为原版块状近似。',
    12: '已复看原图/提示词/剖切/cells-detail：中央通路两侧六牢笼、铁栏和打开铁门，木柱雪顶；未配置门禁，未运行门交互或邻居更新。',
    13: '已复看原图/提示词/剖切/hearth-detail/pantry-detail：烟囱空釜和分层花盆架可辨，发光菌块代火，无炊煮声明。',
    14: '已复看原图/提示词/剖切/armor-detail：双雕塑展示、中央桌和武器意象可辨；雕塑不是装备实体，蓝灯和后墙木框可辨。',
    15: '已复看原图/提示词/剖切/training-detail：沙地三草人、靶与木障碍保留；靶红石和伤害统计未验证。',
    16: '已复看原图/提示词/bucket-detail：石井横梁链条吊桶明确；蓝冰冻结池不可取水，木桶不做升降动画。',
    17: '已复看原图/提示词/剖切/tools-detail：磨石和双层材料架、工作台炉体可辨；实际工作方块交互待验。',
    18: '已复看原图/提示词/剖切/ossuary-detail：真实骷髅头、骨块双层龛、四棺和祭坛可辨；骨块简化骨堆。',
    19: '已复看原图/提示词/剖切/watch-detail：高台阶梯栏杆、望远镜雕塑；封闭洞穴高墙弱化远眺暗空间感，保留独立视觉判断，不宣称可用望远镜。',
    20: '已复看原图/提示词/剖切/vault-detail：凹入藏宝龛、锁箱金块焦点和独立夹道；拉杆未驱动隐门，高夹道比例请独立复核。'
}
progress = read(ROOT/'progress.json')
for number, note in notes.items():
    identifier = f's3-{number}'
    result = subprocess.run([sys.executable, str(ROOT/'review.py'), identifier, note], cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW)
    assert result.returncode == 0, result.stderr
    folder = ROOT/'rooms'/identifier
    summary = read(ROOT/'reproduction'/identifier/'summary.json')
    summary['nbtPath'] = str(folder/(identifier+'.nbt'))
    summary['summaryPath'] = str(folder/'summary.json')
    save(folder/'summary.json', summary)
    report = read(folder/'qa/report.json')
    support = read(folder/'qa/support.json')
    report['inventory'] = support['inventory']
    report['block_entities'] = support['actual_nbt_entities']
    report['fluid_or_lava'] = {name: count for name, count in support['inventory'].items() if name in ('minecraft:water', 'minecraft:lava', 'minecraft:powder_snow', 'minecraft:ice')}
    save(folder/'qa/report.json', report)

folder = ROOT/'rooms/s3-merged'
candidate = folder/'s3-merged.nbt'
support = read(folder/'qa/support.json')
report = read(folder/'qa/report.json')
report['inventory'] = support['inventory']
report['block_entities'] = support['actual_nbt_entities']
report['fluid_or_lava'] = {name: count for name, count in support['inventory'].items() if name in ('minecraft:water', 'minecraft:lava', 'minecraft:powder_snow', 'minecraft:ice')}
save(folder/'qa/report.json', report)
save(folder/'qa/maker-visual-review.json', {'candidate_sha256': digest(candidate), 'timestamp': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'review': '已实际查看当前真实Lodestone complete/cutaway/connection-detail/raised-vault-route。高台连密库镜头改为显示副本剖切，路线连续可见；源NBT未剖切。', 'previews': [{'path': str(path), 'sha256': digest(path)} for path in sorted(folder.glob('*.png'))], 'independent_acceptance': 'PENDING'})
progress = read(ROOT/'progress.json')
progress['current'] = 's3-merged'
progress['rooms']['s3-merged']['visual_review'] = 'SELF_REVIEWED'
progress['takeover_source'] = 'D:/ft工程文件/FT地牢_功能房间第二轮_20261106/S3'
progress['output_directory'] = str(ROOT)
progress['timestamp'] = datetime.now(ZoneInfo('Asia/Shanghai')).isoformat()
progress['independent_acceptance'] = 'PENDING'
progress['review_checklist'] = str(ROOT/'TAKEOVER-REVIEW.md')
progress['assembly'] = 'MAKER_CHECKS_PASS_PENDING_INDEPENDENT_REVIEW'
save(ROOT/'progress.json', progress)
sheet = Image.new('RGB', (1440, 2000), '#14202b')
draw = ImageDraw.Draw(sheet)
for index, number in enumerate(range(11, 21)):
    image = Image.open(ROOT/'rooms'/f's3-{number}'/'complete.png').convert('RGB')
    image.thumbnail((720, 380))
    left, top = (index % 2)*720, (index // 2)*400
    sheet.paste(image, (left, top+20))
    draw.text((left+12, top+4), f'S3-{number} complete shell', fill='white')
sheet.save(ROOT/'complete-shell-contact.png')
