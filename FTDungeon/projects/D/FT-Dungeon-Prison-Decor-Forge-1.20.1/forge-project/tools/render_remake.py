import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

from export_kit import RESOURCES, REVISION_IDS, ROOT, resolve_native_model, write_json
from render_kit import font, render


NOTES = {
    'prison_brick_slab': '重刻石板与围边 / 保留原生台阶形态',
    'prison_fluted_pillar': '柱基、柱冠与真正凹槽 / 重塑比例',
    'prison_cornice': '分层挑檐与成对牛腿 / 保留负空间',
    'prison_supply_crate': '闭合箱体、凹板与锁扣 / 明确材质',
    'prison_ironbound_barrel': '收口鼓腹桶身 / 圆角轮廓与铁箍',
    'prison_shackled_post': '立柱刑架 / 实体开口铁镣',
    'prison_weapon_rack': '可辨识兵刃 / 架体与武器分层',
    'prison_alchemy_shelf': '多层陈设 / 药瓶瓶颈与书本层次',
    'prison_alchemical_lamp': '连通式蒸馏灯 / 冷暖双胆与管桥',
    'prison_ossuary_niche': '支拱深龛 / 颅骨与束骨的层次',
    'prison_warden_lectern': '重塑账台比例 / 开卷书脊与页层',
    'prison_rune_beacon': '束缚符文石碑 / 基座与封环负空间',
}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    arguments = parser.parse_args()
    catalog = json.loads((ROOT / 'catalog.json').read_text(encoding='utf-8'))
    selected = set().union(*REVISION_IDS.values())
    remade = [(index + 1, entry) for index, entry in enumerate(catalog) if entry['id'] in selected]
    poster = Image.new('RGB', (1880, 1602), '#131e28')
    drawing = ImageDraw.Draw(poster)
    drawing.text((28, 20), '06–13、15、16、21、23 / 十二款 · 重制前后', font=font(32), fill='#e9e5d9')
    drawing.text((30, 70), '左：1.0 原版   →   右：1.1 重制  |  同相机、同固定光照、实际导出模型；非游戏截图', font=font(18), fill='#9bb5bf')
    new_only = Image.new('RGB', (1880, 1315), '#131e28')
    new_draw = ImageDraw.Draw(new_only)
    new_draw.text((26, 17), 'THE FORGOTTEN CELLS / 十二款重制', font=font(29), fill='#e9e5d9')
    new_draw.text((28, 62), '06–13、15、16、21、23 / 其余十二款模型与贴图保持原样 / 1.1.0', font=font(18), fill='#8dbfc3')
    compared = []
    for index, (number, entry) in enumerate(remade):
        identifier = entry['id']
        before_path = arguments.baseline / 'preview' / (identifier + '.png')
        after_path = ROOT / 'preview' / (identifier + '.png')
        before, after = Image.open(before_path).convert('RGB'), Image.open(after_path).convert('RGB')
        across, height = 20 + index % 3 * 620, 116 + index // 3 * 366
        drawing.rounded_rectangle((across, height, across + 607, height + 349), radius=9, fill='#212e39', outline='#3a4d5a')
        drawing.text((across + 14, height + 10), f'{number:02d}  {entry["name_zh"]}', font=font(20), fill='#e9e5d9')
        poster.paste(before, (across + 3, height + 65))
        poster.paste(after, (across + 306, height + 65))
        drawing.text((across + 14, height + 43), 'BEFORE / 原版', font=font(13), fill='#8d9aa4')
        drawing.text((across + 318, height + 43), 'REMADE / 重制', font=font(13), fill='#83d4c9')
        drawing.line((across + 303, height + 70, across + 303, height + 292), fill='#435865', width=1)
        drawing.text((across + 14, height + 312), NOTES[identifier], font=font(15), fill='#adc1c9')
        new_x, new_y = 18 + index % 4 * 466, 104 + index // 4 * 397
        new_draw.rounded_rectangle((new_x, new_y, new_x + 451, new_y + 381), radius=10, fill='#212e39', outline='#3a4d5a')
        enlarged = after.resize((424, 327), Image.Resampling.NEAREST)
        new_only.paste(enlarged, (new_x + 13, new_y + 3))
        new_draw.text((new_x + 16, new_y + 330), f'{number:02d}  {entry["name_zh"]}', font=font(21), fill='#e9e5d9')
        new_draw.text((new_x + 16, new_y + 360), NOTES[identifier], font=font(14), fill='#9ab9c2')
        compared.append({'number': number, 'id': identifier,
                         'before_png_sha256': hashlib.sha256(before_path.read_bytes()).hexdigest(),
                         'after_png_sha256': hashlib.sha256(after_path.read_bytes()).hexdigest()})
    drawing.text((28, 1576), '06 原生台阶仅重做雕边材质，其他十一款重做结构与材质。没有自动安装，也未改动旧版交付。', font=font(15), fill='#839da9')
    poster.save(ROOT / 'preview/before-after-12.png')
    new_only.save(ROOT / 'preview/remade-12.png')
    slab_sheet = Image.new('RGB', (1050, 420), '#131e28')
    slab_draw = ImageDraw.Draw(slab_sheet)
    slab_draw.text((20, 15), '06 / 原生台阶 · 下半、上半、双层', font=font(24), fill='#e9e5d9')
    with zipfile.ZipFile(arguments.minecraft_jar) as vanilla:
        for index, (suffix, label) in enumerate((('', 'BOTTOM / 下半'), ('_top', 'TOP / 上半'), ('_double', 'DOUBLE / 双层'))):
            path = RESOURCES / f'assets/ftdungeonprops/models/block/prison_brick_slab{suffix}.json'
            model = resolve_native_model(json.loads(path.read_text(encoding='utf-8')), vanilla)
            slab_sheet.paste(render(model, (340, 300)), (5 + index * 350, 58))
            slab_draw.text((18 + index * 350, 365), label, font=font(18), fill='#9bbfc5')
    slab_sheet.save(ROOT / 'preview/slab-native-forms.png')
    write_json(ROOT / 'qa/remake-visual-inputs.json', {'status': 'pass', 'comparisons': compared,
               'method': 'Identical camera and fixed CPU renderer, original and final exported JSON; no visual-score claim.'})
    print(json.dumps({'comparisons': len(compared), 'outputs': ['before-after-12.png', 'remade-12.png']}))


if __name__ == '__main__':
    main()
