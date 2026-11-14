import importlib.util
import json
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

from generate import ROOT, RES, ART, WORK, MODEL, ATLAS, ICON


SPEC = importlib.util.spec_from_file_location('butcher_material_renderer', ROOT / 'tools/model3d/boss_materials_refine/butcher/render.py')
LIB = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(LIB)
vertices = LIB.vertices
raster = LIB.raster


def main():
    model = json.loads(MODEL.read_text(encoding='utf-8'))
    texture = np.array(Image.open(ATLAS).convert('RGBA'))
    views = [('front', [0, 0, -1], 'FRONT / NORTH', 'Enamel fang, carved seal, rivets and crimson lashing'),
             ('back', [0, 0, 1], 'BACK / SOUTH', 'Lined rear plate, through-rivets, neck ring connections'),
             ('left', [-1, 0, 0], 'LEFT / WEST', 'Layered mount, forged metal thickness and enamel blade'),
             ('right', [1, 0, 0], 'RIGHT / EAST', 'Return lashing, edge clamps and pointed fang')]
    sheet = Image.new('RGBA', (1240, 1320), '#20272c')
    for index, (key, camera, title, subtitle) in enumerate(views):
        image = LIB.raster(model, texture, camera, size=512, extent=16.0)
        image.save(ART / f'{key}.png')
        panel = LIB.panel(image, title, subtitle)
        panel.save(ART / f'{key}_panel.png')
        sheet.alpha_composite(panel, ((index % 2) * 620, (index // 2) * 660))
    sheet.save(ART / 'four_views.png')
    oblique = LIB.raster(model, texture, [-0.5, 0.2, -1], size=900, extent=16.0)
    oblique.save(ART / 'oblique.png')
    LIB.panel(oblique, 'BUTCHER / FANG CHARM', 'Forged neck ring / crimson leather mount / carved bone seal / enamel fang', (1000, 1050)).save(ART / 'oblique_detail.png')
    LIB.raster(model, texture, [0.55, 0.25, 1], size=700, extent=16.0).save(ART / 'rear_oblique.png')
    small = LIB.raster(model, texture, [0.15, 0.07, -1], size=32, extent=15.4)
    outline = small.getchannel('A').filter(ImageFilter.MaxFilter(3))
    icon = Image.new('RGBA', small.size, '#443035')
    icon.putalpha(outline)
    icon.alpha_composite(small)
    icon.save(ICON)
    previous = Image.open(WORK / 'backup-before-refine/src/main/resources/assets/ftboss/textures/item/butcher_fang_charm.png').convert('RGBA')
    comparison = LIB.background((1120, 620)).convert('RGBA')
    draw = ImageDraw.Draw(comparison)
    draw.text((30, 22), 'BUTCHER FANG CHARM / INVENTORY ICON', font=LIB.font(25), fill='#eadfc7')
    draw.text((30, 65), 'Refined wearable relic. Gameplay and Curios effects remain unchanged.', font=LIB.font(17), fill='#9eaaad')
    draw.text((60, 122), 'Previous 32 x 32', font=LIB.font(21), fill='#acb6b9')
    draw.text((415, 122), 'Refined 32 x 32', font=LIB.font(21), fill='#eadfc7')
    comparison.alpha_composite(previous.resize((320, 320), Image.Resampling.NEAREST), (40, 175))
    comparison.alpha_composite(icon.resize((320, 320), Image.Resampling.NEAREST), (405, 175))
    comparison.alpha_composite(icon, (855, 228))
    comparison.alpha_composite(icon.resize((16, 16), Image.Resampling.NEAREST), (863, 353))
    draw.text((810, 280), 'Native 32 px', font=LIB.font(18), fill='#acb6b9')
    draw.text((815, 395), '16 px slot', font=LIB.font(18), fill='#acb6b9')
    draw.text((45, 546), 'Flat mark', font=LIB.font(17), fill='#829094')
    draw.text((415, 546), 'Connected ring / riveted cap / bone tooth', font=LIB.font(17), fill='#cbbba0')
    comparison.save(ART / 'icon_comparison.png')
    material_model = json.loads((RES / 'models/item/boss_materials/butcher_material.json').read_text(encoding='utf-8'))
    material_texture = np.array(Image.open(RES / 'textures/item/boss_materials/butcher.png').convert('RGBA'))
    relationship = LIB.background((1200, 760)).convert('RGBA')
    draw = ImageDraw.Draw(relationship)
    draw.text((26, 22), 'SAME BOSS / RAW MATERIAL TO FINISHED RELIC', font=LIB.font(25), fill='#eadfc7')
    draw.text((46, 80), 'Bloodied Fang Bone', font=LIB.font(22), fill='#cbbba0')
    draw.text((650, 80), 'Butcher Fang Charm', font=LIB.font(22), fill='#cbbba0')
    relationship.alpha_composite(LIB.raster(material_model, material_texture, [-0.6, 0.35, -1], size=530, extent=15.7), (30, 145))
    relationship.alpha_composite(LIB.raster(model, texture, [-0.45, 0.18, -1], size=530, extent=16.0), (630, 145))
    draw.text((40, 702), 'Warm cortical bone + oxblood fascia -> polished fang + crimson binding + antique brass', font=LIB.font(19), fill='#a8b4b7')
    relationship.save(ART / 'material_relic_relationship.png')
    Image.open(ATLAS).resize((768, 768), Image.Resampling.NEAREST).save(ART / 'uv_atlas_enlarged.png')
    print('Rendered final runtime relic: four views, obliques, GUI icon, material relationship')


if __name__ == '__main__':
    main()
