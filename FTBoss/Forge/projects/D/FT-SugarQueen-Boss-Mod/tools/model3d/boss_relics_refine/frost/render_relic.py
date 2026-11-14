import argparse
import json
from pathlib import Path
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[4]
sys.path.insert(0, str(ROOT / 'tools/model3d/boss_materials_refine/frost'))
from render_frost import ASSETS, BG, font, render

ART = ROOT / 'art/boss_relics/refine/frost'
ITEM = 'frost_crown_charm'


def load():
    model = json.loads((ASSETS / f'models/item/boss_relics/{ITEM}.json').read_text(encoding='utf-8'))
    texture = np.array(Image.open(ASSETS / ('textures/' + model['textures']['atlas'].split(':')[1] + '.png')).convert('RGBA'))
    return model, texture


def icon(model, texture):
    item = render(model, texture, -10, 7, (32, 32), True, center=(8, 8.55, 8), world_width=15.2)
    alpha = item.getchannel('A')
    silhouette = Image.new('RGBA', (32, 32), (40, 28, 50, 0))
    silhouette.putalpha(alpha.filter(ImageFilter.MaxFilter(3)))
    silhouette.alpha_composite(item)
    silhouette.save(ASSETS / f'textures/item/{ITEM}.png')


def previews():
    model, texture = load()
    ART.mkdir(parents=True, exist_ok=True)
    sheet = Image.new('RGBA', (1120, 1080), BG)
    draw = ImageDraw.Draw(sheet)
    draw.text((32, 25), '霜糖王冠坠 · 最终运行时四视图', font=font(27), fill='#f5e9d7')
    draw.text((32, 66), '五尖晶冠 / 玫粉镶座 / 金属包边 / 背部吊环与连接件', font=font(17), fill='#a9baca')
    for index, (label, yaw) in enumerate([('正面 FRONT', 0), ('背面 BACK', 180), ('左侧 LEFT', -90), ('右侧 RIGHT', 90)]):
        image = render(model, texture, yaw, size=(540, 410), center=(8, 8.55, 8), world_width=16)
        xpos, ypos = 10 + index % 2 * 555, 112 + index // 2 * 475
        sheet.paste(image, (xpos, ypos))
        draw.text((xpos + 25, ypos + 418), label, font=font(21), fill='#d4dfe9')
        image.save(ART / (['front', 'back', 'left', 'right'][index] + '.png'))
    sheet.convert('RGB').save(ART / 'four_views.png')
    image = Image.new('RGBA', (1000, 1000), BG)
    view = render(model, texture, -30, 15, (1000, 850), center=(8, 8.55, 8), world_width=16)
    image.paste(view, (0, 140))
    draw = ImageDraw.Draw(image)
    draw.text((34, 25), '霜糖王冠坠 · 五尖霜晶金冠', font=font(29), fill='#f5e9d7')
    draw.text((34, 69), '糖霜女皇遗物 / 精制成品 / 金、玫粉珐琅、冰晶与乳白糖饰', font=font(18), fill='#a9baca')
    image.convert('RGB').save(ART / 'oblique_detail.png')
    image = render(model, texture, 148, 18, (760, 760), center=(8, 8.55, 8), world_width=16)
    image.convert('RGB').save(ART / 'rear_oblique.png')
    icon_image = Image.open(ASSETS / f'textures/item/{ITEM}.png').convert('RGBA')
    compare = Image.new('RGBA', (1080, 580), BG)
    draw = ImageDraw.Draw(compare)
    draw.text((32, 24), '霜糖王冠坠 / 原尺寸图标与重制前对照', font=font(26), fill='#f5e9d7')
    draw.text((32, 77), '32×32 原尺寸', font=font(18), fill='#a9baca')
    compare.alpha_composite(icon_image, (75, 140))
    compare.alpha_composite(icon_image.resize((384, 384), Image.Resampling.NEAREST), (240, 111))
    draw.text((240, 521), '最近邻 12×', font=font(18), fill='#a9baca')
    old_path = ROOT / f'_work/boss-relics-refine-20261021/frost/backup-before-refine/src__main__resources__assets__ftboss__textures__item__{ITEM}.png'
    if old_path.exists():
        old = Image.open(old_path).convert('RGBA')
        compare.alpha_composite(old.resize((288, 288), Image.Resampling.NEAREST), (710, 165))
        draw.text((710, 521), '重制前 32×32', font=font(18), fill='#a9baca')
    compare.convert('RGB').save(ART / 'icon_comparison.png')
    relation = Image.new('RGBA', (1240, 730), BG)
    draw = ImageDraw.Draw(relation)
    draw.text((32, 24), '同源材质 · 从霜糖晶锭到王冠坠成品', font=font(28), fill='#f5e9d7')
    material = json.loads((ASSETS / 'models/item/boss_materials/frost_material.json').read_text(encoding='utf-8'))
    tex_material = np.array(Image.open(ASSETS / 'textures/item/boss_materials/frost.png').convert('RGBA'))
    left = render(material, tex_material, -32, 25, (620, 590), center=(8, 7.8, 8), world_width=16)
    right = render(model, texture, -20, 10, (600, 590), center=(8, 8.55, 8), world_width=16)
    relation.paste(left, (0, 88))
    relation.paste(right, (630, 88))
    draw.text((150, 680), '原材料：霜糖晶冠配件', font=font(20), fill='#d7e4eb')
    draw.text((810, 680), '遗物：霜糖王冠坠', font=font(20), fill='#d7e4eb')
    relation.convert('RGB').save(ART / 'material_relic_relation.png')
    Image.fromarray(texture).resize((768, 768), Image.Resampling.NEAREST).save(ART / 'atlas_preview.png')
    print('Rendered relic from final runtime source: four cardinal views / front and rear oblique / icon / material relation')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--make-icon', action='store_true')
    args = parser.parse_args()
    if args.make_icon: icon(*load())
    previews()
