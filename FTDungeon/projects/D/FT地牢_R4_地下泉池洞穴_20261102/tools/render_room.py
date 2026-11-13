import json
import zipfile

from PIL import Image, ImageDraw

import render_engine as engine
from build_room import ROOT, digest, write_json
from nbtio import AIR, Structure


def plan(room, blueprint):
    image = Image.new('RGB', (1500, 1400), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'R4 地下泉池洞穴 / 最终 NBT 平面', font=engine.font(32), fill='#e9e5ce')
    draw.text((35, 80), '北 ↑   玩家脚部 Y=3；泉池 Y=1…2；中央干路与横桥三格净宽', font=engine.font(23), fill='#a0c2ba')
    cell, left, top = 54, 80, 160
    required = {(position[0], position[2]) for position in blueprint['required_feet']}
    for pos_z in range(19):
        for pos_x in range(17):
            foot = room.get((pos_x, 3, pos_z)).name.removeprefix('minecraft:')
            below = room.get((pos_x, 2, pos_z)).name.removeprefix('minecraft:')
            color = '#397d92' if below == 'water' else '#47504c' if foot == 'air' else '#707668'
            if foot == 'barrel':
                color = '#987548'
            if foot == 'water':
                color = '#45a4b5'
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            draw.rectangle(rectangle, fill=color)
            if (pos_x, pos_z) in required:
                draw.rectangle(rectangle, outline='#d8dc9c', width=3)
    for pos_x, pos_z, label in ((8, 0, '入'), (8, 3, '安'), (8, 17, '泉')):
        draw.text((left + pos_x * cell + 13, top + pos_z * cell + 10), label, font=engine.font(23), fill='#fff2c9')
    for coordinate in range(19):
        draw.text((left - 40, top + coordinate * cell + 13), str(coordinate), font=engine.font(17), fill='#a0b4a6')
    for coordinate in range(17):
        draw.text((left + coordinate * cell + 15, top - 32), str(coordinate), font=engine.font(17), fill='#a0b4a6')
    labels = ['北入口 (8,3,0)', '安全点 (8,3,3)', '石桥 X=1…15', '净通行 Z=7…9', '桥下连通泉池', '六根实心桥墩', '四根落地石柱', '后墙真实阶瀑', '三个空木桶', '全封闭地板/屋顶', '黄框：必达干脚位']
    for index, label in enumerate(labels):
        draw.text((1050, 210 + index * 72), label, font=engine.font(22), fill='#cbd9c4')
    draw.text((35, 1250), '图中水色由最终方块读取；桥底另有连通水层。屋顶、悬挂件见剖视及外观图。', font=engine.font(23), fill='#a0b9ab')
    draw.text((35, 1320), '实际 NBT 软件示意，非 AI 概念图或游戏客户端截图。', font=engine.font(23), fill='#94a5ac')
    return image


def main():
    room = Structure.read(ROOT / 'room.nbt')
    blueprint = json.loads((ROOT / 'blueprint.json').read_text(encoding='utf-8'))
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    views = [('north', '北侧 / 石桥与后墙泉瀑', (-0.85, -1)), ('south', '南侧 / 北入口与双池', (0.85, 1)), ('west', '西侧 / 石柱与桥下水层', (-1, 0.65)), ('east', '东侧 / 桶架与钟乳石', (1, -0.65))]
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, (name, label, camera) in enumerate(views):
        image = engine.render(room, camera, label, elevation=0.8)
        image.save(ROOT / 'preview' / (name + '.png'))
        sheet.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
    sheet.save(ROOT / 'preview/four-views.png')
    hero_camera = (-0.55, -1)
    engine.render(room, hero_camera, '主视图 / 支撑石桥、密封泉池与阶瀑', elevation=0.72).save(ROOT / 'preview.png')
    engine.render(room, (-0.85, -1), '完整屋顶与北侧入口', exterior=True).save(ROOT / 'preview/exterior.png')
    plan(room, blueprint).save(ROOT / 'preview/plan.png')
    capture = ROOT / 'qa/native-placed-none.nbt'
    if capture.exists():
        engine.render(Structure.read(capture), hero_camera, '原生世界 240 tick 后回采', native=True, elevation=0.72).save(ROOT / 'preview/native-capture.png')
    write_json(ROOT / 'qa/render-manifest.json', {
        'status': 'PASS', 'source_nbt_sha256': digest(ROOT / 'room.nbt'),
        'source': 'Final NBT, vanilla 1.20.1 block models and textures; never the AI concept image.',
        'client_jar': str(engine.CLIENT), 'client_jar_sha256': digest(engine.CLIENT),
        'outputs': ['preview.png', 'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png', 'preview/native-capture.png'],
        'limitations': ['Software orthographic renderer, not an in-game screenshot.', 'Cutaway removes roof and near external walls only, not pillars/furniture.', 'Water is a static approximate blue volume; no shader, flowing-water mesh or emitted-light simulation.', 'Amethyst is purple and warped fungi cyan in vanilla; blue concept glow is not reproduced by a custom shader.'],
    })
    print('R4_RENDER_PASS')


if __name__ == '__main__':
    main()
