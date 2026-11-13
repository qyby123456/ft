import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

import render_engine as engine
from build_room import digest, write_json
from nbtio import AIR, Structure


ROOT = Path(__file__).resolve().parents[1]


def plan(room):
    image = Image.new('RGB', (1500, 1210), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 25), 'R5 石匠工坊 / 脚部动线与主题布局', font=engine.font(34), fill='#efe4cc')
    draw.text((35, 80), 'N 北入口在顶边；最终 NBT 含完整屋顶。所有坐标为相对 XYZ，Y=1 为脚位。', font=engine.font(22), fill='#a9c5ba')
    left, top, cell = 80, 175, 52
    colors = [((1, 5, 10, 14), '#7e6854', '架'), ((2, 4, 11, 13), '#a3a19c', '像'),
              ((11, 14, 5, 7), '#8f8c87', '台'), ((12, 14, 11, 13), '#929b9b', '兽'),
              ((2, 5, 3, 5), '#8c683c', '车'), ((10, 14, 15, 15), '#735746', '架')]
    for pos_z in range(17):
        for pos_x in range(17):
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            color = '#34423f' if room.get((pos_x, 1, pos_z)) == AIR else '#565d58'
            label = ''
            for (minimum_x, maximum_x, minimum_z, maximum_z), feature_color, feature_label in colors:
                if minimum_x <= pos_x <= maximum_x and minimum_z <= pos_z <= maximum_z:
                    color, label = feature_color, feature_label
            if 7 <= pos_x <= 9 and pos_z <= 15:
                color, label = '#2a7773', ''
            draw.rectangle(rectangle, fill=color)
            if label:
                draw.text((rectangle[0] + 13, rectangle[1] + 11), label, font=engine.font(24), fill='#eee3c9')
    for coordinate in range(17):
        draw.text((left + coordinate * cell + 15, top - 32), str(coordinate), font=engine.font(18), fill='#a5b5ae')
        draw.text((left - 43, top + coordinate * cell + 14), str(coordinate), font=engine.font(18), fill='#a5b5ae')
    labels = ['入口 (8,1,0)', '安全点 (8,1,4)', '主路 X=7…9', 'Z=0…15 三格高', '四腿雕刻台 / 切石机', '四柱脚手架 / 爬梯', '未完工石像 / 石翼兽', '手推车 / 墙边工具', '两只空桶，无奖励', '水缸无游离水', '地板289格 / 屋顶289格']
    for index, label in enumerate(labels):
        draw.text((1005, 185 + index * 68), label, font=engine.font(24), fill='#d2d5c4')
    draw.text((35, 1120), '读取实际交付 NBT；脚手架平台为上层设施，平面色块仅标识区域，不表示整区封死。', font=engine.font(23), fill='#a9beb1')
    return image


def main():
    room = Structure.read(ROOT / 'room.nbt')
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    views = [('north-west', '北西剖视 / 石像与工作台', (-0.7, -1)), ('north-east', '北东剖视 / 手推石料车', (0.7, -1)),
             ('south-west', '南西剖视 / 工作台侧面', (-0.7, 1)), ('south-east', '南东剖视 / 脚手架背面', (0.7, 1))]
    panels = []
    for name, label, camera in views:
        image = engine.render(room, camera, label, elevation=0.65)
        image.save(ROOT / 'preview' / (name + '.png'))
        panels.append(image)
    panels[0].save(ROOT / 'preview.png')
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, panel in enumerate(panels):
        sheet.paste(panel, ((index % 2) * 1600, (index // 2) * 1250))
    sheet.save(ROOT / 'preview/four-views.png')
    engine.render(room, (-0.7, -1), '完整屋顶与三格入口', exterior=True).save(ROOT / 'preview/exterior.png')
    plan(room).save(ROOT / 'preview/plan.png')
    captures = []
    for capture in sorted((ROOT / 'qa').glob('native-placed-*.nbt')):
        output = ROOT / 'preview' / (capture.stem + '.png')
        engine.render(Structure.read(capture), (-0.7, -1), 'Forge 100 tick 后世界回采', native=True, elevation=0.65).save(output)
        captures.append({'nbt': str(capture.relative_to(ROOT)), 'sha256': digest(capture), 'preview': str(output.relative_to(ROOT))})
    write_json(ROOT / 'qa/render-manifest.json', {
        'status': 'PASS', 'source_nbt_sha256': digest(ROOT / 'room.nbt'), 'preview_sha256': digest(ROOT / 'preview.png'),
        'source': 'Actual delivery NBT with vanilla 1.20.1 block models/textures, not an AI image or client screenshot',
        'client_jar': str(engine.CLIENT), 'client_jar_sha256': digest(engine.CLIENT),
        'views': 4, 'native_captures': captures,
        'limitations': ['Software orthographic projection; no Minecraft player client or FPS evidence.',
                        'No real emitted light or shader simulation; static first texture frames only.',
                        'Only render memory cuts roof and near exterior boundary walls; delivered NBT is not cut.'],
    })
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
