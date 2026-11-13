from pathlib import Path
import hashlib
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
PREVIEW = ROOT / 'preview'
FONT = Path('C:/Windows/Fonts/msyh.ttc')
BACKGROUND = '#111d22'
TEXT = '#e7e2cb'
MUTED = '#b2c9bd'
GOLD = '#e8c58b'


def font(size):
    return ImageFont.truetype(str(FONT), size)


def content(name):
    with Image.open(PREVIEW / f'{name}.png') as screenshot:
        return screenshot.convert('RGB').crop((0, 84, 1536, 978))


def sheet(kind, title):
    image = Image.new('RGB', (1920, 1340), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((34, 24), title, font=font(34), fill=GOLD)
    draw.text((34, 77), '实际 NBT 软件预览 · 四方向原始完整结构 · 无屋顶或墙体剖切', font=font(22), fill=MUTED)
    for index, (direction, label) in enumerate([('north', '北 / N'), ('east', '东 / E'), ('south', '南 / S'), ('west', '西 / W')]):
        column = index % 2
        row = index // 2
        card_x = 32 + column * 950
        card_y = 126 + row * 594
        draw.rounded_rectangle((card_x, card_y, card_x + 922, card_y + 570), radius=12, fill='#1b3032', outline='#456659', width=2)
        draw.text((card_x + 16, card_y + 12), label, font=font(26), fill=TEXT)
        image.paste(content(f'{kind}-{direction}').resize((896, 522), Image.Resampling.LANCZOS), (card_x + 13, card_y + 44))
    draw.text((34, 1310), 'P05 · 17 × 14 × 29 · Lodestone 0.6.0 · 非客户端实机截图', font=font(19), fill=MUTED)
    image.save(PREVIEW / f'four-{kind}.png')


def main():
    native = json.loads((ROOT / 'qa/forge-native.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS'
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    assert native['sha256'] == digest
    sheet('interior', 'P05 石柱跃厅 / 四向内部')
    sheet('exterior', 'P05 石柱跃厅 / 四向外壳')
    image = Image.new('RGB', (1920, 1440), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((36, 25), 'P05 / 石柱跃厅', font=font(46), fill=GOLD)
    draw.text((38, 94), '五根落地石柱 · 六次同高跳跃 · 浅水失败恢复 · 北黄入口 / 南绿出口', font=font(25), fill=TEXT)
    image.paste(content('cutaway').resize((1240, 722), Image.Resampling.LANCZOS), (24, 150))
    image.paste(content('interior-north').resize((598, 348), Image.Resampling.LANCZOS), (1294, 154))
    draw.text((1302, 508), '入口向出口 / 原始完整 NBT', font=font(22), fill=MUTED)
    image.paste(content('plan').resize((598, 348), Image.Resampling.LANCZOS), (1294, 554))
    draw.text((1302, 910), '北↑俯视 / 展示剖切', font=font(22), fill=MUTED)
    draw.text((40, 908), '左图仅为展示剖切；屋顶、链灯与近侧墙体在最终 NBT 中完整保留。', font=font(21), fill=MUTED)
    draw.rounded_rectangle((32, 965, 1888, 1350), radius=14, fill='#1b3032', outline='#456659', width=2)
    lines = [
        '17 × 14 × 29 / 双端 3 × 3 接口 / 5 根 2 × 2 柱顶 / 每跳空隙 2 格，无高差。',
        '柱脚至池底连续支撑；304 格一格深浅水连通，双回程梯返回北岸重新挑战。',
        '隔离 Forge 47.4.10：四向原生放置，27,608 条记录逐条核对，400 tick 稳定。',
        '四向双向路线 × 前后两阶段 = 96 次原生扫掠碰撞跳跃；恢复区、双梯通过。',
        '6 项破坏探针均被拒绝；无战利品、刷怪与预放实体；未部署、未改玩家存档。',
        '跳跃采用确定性速度与重力轨迹；未做键盘操作、完整整合包或多人游玩测试。',
    ]
    draw.text((60, 987), '验收证据 / PASS', font=font(30), fill='#a8dfb7')
    for index, line in enumerate(lines):
        draw.text((60, 1045 + index * 47), line, font=font(24), fill=TEXT)
    draw.text((36, 1382), f'ACTUAL NBT / SHA-256 {digest}', font=font(20), fill=MUTED)
    image.save(ROOT / 'preview.png')
    gallery = [('four-interior', '四向内部 / 原始完整 NBT'), ('four-exterior', '四向外壳 / 完整封顶'), ('cutaway', '轴测剖切 / 隐藏屋顶与近墙'), ('cutaway-east', '反侧剖切'), ('plan', '北向俯视 / 屋顶隐藏')]
    cards = ''.join(f'<figure><a href="{name}.png"><img src="{name}.png" alt="{label}"></a><figcaption>{label}</figcaption></figure>' for name, label in gallery)
    html = f'<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>P05 石柱跃厅 · 验收图集</title><style>body{{margin:32px;background:{BACKGROUND};color:{TEXT};font:16px/1.7 system-ui,"Microsoft YaHei",sans-serif}}h1{{color:{GOLD}}}a{{color:#a8dfb7}}main{{display:grid;grid-template-columns:repeat(auto-fit,minmax(440px,1fr));gap:22px}}figure{{margin:0;padding:12px;background:#1b3032;border-radius:12px}}img{{width:100%}}.hero{{max-width:1200px}}code{{word-break:break-all}}</style><h1>P05 / 石柱跃厅</h1><p>17 × 14 × 29 · 五根落地石柱 · 两格空隙 · 浅水双梯回程</p><p><a href="interactive.html">交互 NBT 预览（本地预览服务）</a> · <a href="../qa/forge-native.json">Forge 原生证据</a> · <a href="../README.md">交付说明</a></p><p>全部为实际 NBT 的软件预览。剖切仅改变展示；原结构完整封顶。未部署。原生路线为确定性扫掠碰撞验证，非键盘或完整整合包游玩。</p><p><code>SHA-256 {digest}</code></p><img class="hero" src="../preview.png"><main>{cards}</main></html>'
    (PREVIEW / 'index.html').write_text(html, encoding='utf-8')
    receipt = {'status': 'PASS', 'sha256': digest, 'actual_nbt': True, 'client_screenshots': False, 'files': []}
    for name in ['preview.png', 'preview/four-interior.png', 'preview/four-exterior.png']:
        destination = ROOT / name
        with Image.open(destination) as rendered:
            receipt['files'].append({'file': name, 'size': list(rendered.size), 'sha256': hashlib.sha256(destination.read_bytes()).hexdigest()})
    (ROOT / 'qa/composites.json').write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('P05 overview, four-direction sheets and offline gallery created')


if __name__ == '__main__':
    main()
