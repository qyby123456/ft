import copy
import json
import zipfile

from PIL import Image, ImageDraw

import render_engine as engine
from build_room import ROOT, digest, write_json
from nbtio import AIR, Structure


def plans(room):
    image = Image.new('RGB', (1900, 1040), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((30, 20), 'B 螺旋楼梯塔 / 三层路面平面与接口', font=engine.font(32), fill='#f0e5ce')
    for index, (feet_y, title) in enumerate([(1, '下层 +1 / 南出口'), (8, '主层 +8 / 北入口'), (20, '上层 +20 / 北出口')]):
        left, top, cell = 35 + index * 625, 145, 21
        draw.text((left, 95), title, font=engine.font(26), fill='#c9d4cd')
        for pos_z in range(27):
            for pos_x in range(27):
                floor = room.get((pos_x, feet_y - 1, pos_z)).name.split(':')[1]
                block = room.get((pos_x, feet_y, pos_z)).name.split(':')[1]
                color = '#2b333d' if floor == 'air' else '#828477'
                if block not in ('air', 'wall_torch', 'red_carpet'):
                    color = '#4b5865' if block != 'iron_bars' else '#c7cdd1'
                if block == 'red_carpet':
                    color = '#893f3c'
                if floor == 'stone_brick_stairs':
                    color = '#b29458'
                rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 1, top + (pos_z + 1) * cell - 1)
                draw.rectangle(rectangle, fill=color)
        port_z = 26 if feet_y == 1 else 0
        draw.rectangle((left + 12 * cell, top + port_z * cell, left + 15 * cell, top + (port_z + 1) * cell), outline='#92e7c2', width=3)
    labels = ['模板 27×27×27；主层北口 (13,8,0)，上层北口 (13,20,0)，下层南口 (13,1,26)。', '上行绕八角中柱一周，四段各升3格，共升12格；独立直梯下降7格，梯道及平台净宽3格。', '原图斜圆弧台阶按原版方块转译为四转折环绕梯道；完整屋顶、地基和圆塔外壳始终保留在交付 NBT。', '平面图仅为对应脚高水平切片；真实 0.6×1.8 碰撞、回程和所有台阶净空由原生 Forge QA 核验。']
    for index, label in enumerate(labels):
        draw.text((35, 765 + index * 57), label, font=engine.font(23), fill='#b4c5c1')
    image.save(ROOT / 'preview/plans.png')


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    cameras = [('north-east', (0.7, -1), '东北 / 上层门与下行口'), ('north-west', (-0.8, -1), '西北 / 环绕上行'), ('south-west', (-0.8, 1), '西南 / 连续梯段'), ('south-east', (0.8, 1), '东南 / 栏杆及支撑')]
    panels = []
    for name, camera, label in cameras:
        image = engine.render(room, camera, label, size=(1600, 1500), elevation=0.62)
        image.save(ROOT / ('preview/' + name + '.png'))
        panels.append(image)
    panels[0].save(ROOT / 'out/preview.png')
    sheet = Image.new('RGB', (3200, 3000), '#121d23')
    for index, image in enumerate(panels):
        sheet.paste(image, ((index % 2) * 1600, (index // 2) * 1500))
    sheet.save(ROOT / 'preview/four-views.png')
    engine.render(room, (0.7, -1), '完整圆塔外壳与双北门', exterior=True, size=(1600, 1500), elevation=0.65).save(ROOT / 'preview/exterior.png')
    lower = copy.deepcopy(room)
    for position in list(lower.blocks):
        if position[1] >= 8 or position[1] == 7 and not (8 <= position[0] <= 10 and position[2] <= 12):
            lower.blocks[position] = AIR
    engine.render(lower, (-0.6, -1), '下层检视 / 仅预览移除主层地板', size=(1600, 1100), elevation=1).save(ROOT / 'preview/lower-floor.png')
    plans(room)
    capture = ROOT / 'qa/native-placed-none.nbt'
    if capture.exists():
        engine.render(Structure.read(capture), (0.7, -1), '原生放置200 tick后回采', size=(1600, 1500), native=True, elevation=0.62).save(ROOT / 'preview/native-world-capture.png')
    outputs = list((ROOT / 'preview').glob('*.png')) + [ROOT / 'out/preview.png']
    write_json(ROOT / 'qa/render-manifest.json', {'status': 'PASS', 'source_nbt_sha256': digest(ROOT / 'out/room.nbt'), 'client_jar': str(engine.CLIENT), 'client_jar_sha256': digest(engine.CLIENT), 'method': 'Actual NBT + Minecraft 1.20.1 native model JSON and textures, orthographic software renderer.', 'views': [{'path': str(path.relative_to(ROOT)), 'sha256': digest(path)} for path in outputs], 'limitations': ['Not client screenshots and no lighting simulation.', 'Cutaway roof/walls only removed in preview memory; lower-floor study additionally hides upper structure.', 'AI design reference is not used as an output render.']})
    print(json.dumps({'status': 'RENDERED', 'views': len(outputs)}))


if __name__ == '__main__':
    main()
